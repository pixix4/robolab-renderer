FROM openjdk:11 as gradleBuilder
WORKDIR /usr/src/robolab-renderer
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew --no-daemon kotlinNpmInstall

FROM gradleBuilder as jsBackendBuilder
RUN ./gradlew --no-daemon jsBackendSync

FROM gradleBuilder as jsFrontendWebBuilder
RUN ./gradlew --no-daemon jsFrontendWebSync

FROM node:latest
RUN mkdir /opt/robolab-renderer
COPY --from=jsBackendBuilder /usr/src/robolab-renderer/deploy/server/ /opt/robolab-renderer/
COPY server.ini /opt/robolab-renderer/

COPY --from=jsFrontendWebBuilder /usr/src/robolab-renderer/deploy/distWeb/ /opt/robolab-renderer/web/
ENV WEB_MOUNT /
ENV WEB_DIRECTORY ./web/

ENV ELECTRON_MOUNT /download

WORKDIR /opt/robolab-renderer
EXPOSE 8080
ENTRYPOINT node /opt/robolab-renderer/packages/robolab-jsBackend/kotlin/robolab-jsBackend.js
