FROM openjdk:11 as gradleBuilder
WORKDIR /usr/src/robolab-renderer
COPY . .
RUN chmod +x ./gradlew

FROM gradleBuilder as jsBackendBuilder
RUN ./gradlew --no-daemon deployBackend

FROM gradleBuilder as jsFrontendWebBuilder
RUN ./gradlew --no-daemon deployWeb

FROM node:14.15.4
WORKDIR /opt/robolab-renderer
EXPOSE 8080
ENTRYPOINT node /opt/robolab-renderer/packages/robolab-service-renderer-js-backend/kotlin/robolab-service-renderer-js-backend.js

ENV WEB_DIRECTORY ./web/

COPY --from=jsBackendBuilder /usr/src/robolab-renderer/deploy/distServer/ /opt/robolab-renderer/
COPY --from=jsFrontendWebBuilder /usr/src/robolab-renderer/deploy/distWeb/ /opt/robolab-renderer/web/
