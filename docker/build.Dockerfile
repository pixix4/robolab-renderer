FROM openjdk:11 as gradleBuilder
WORKDIR /usr/src/robolab-renderer
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew --no-daemon kotlinNpmInstall

FROM gradleBuilder as jsBackendBuilder
RUN ./gradlew --no-daemon jsBackendSync

FROM gradleBuilder as jsFrontendWebBuilder
RUN ./gradlew --no-daemon jsFrontendWebSync

#FROM openjdk:11 as jsFrontendElectronBuilder
#RUN ./gradlew jsFrontendElectronSync

#FROM electronuserland/builder:wine as electronBuilder
#WORKDIR /usr/src/robolab-renderer/electron
#COPY deploy/electron/ .
#COPY --from=jsFrontendElectronBuilder /usr/src/robolab-renderer/deploy/distElectron/ ../distElectron/
#RUN npm install
#RUN npx electron-builder -l --publish always

FROM node:latest
RUN mkdir /opt/robolab-renderer
COPY --from=jsBackendBuilder /usr/src/robolab-renderer/deploy/server/ /opt/robolab-renderer/
COPY server.ini /opt/robolab-renderer/

COPY --from=jsFrontendWebBuilder /usr/src/robolab-renderer/deploy/distWeb/ /opt/robolab-renderer/web/
ENV WEB_MOUNT /
ENV WEB_DIRECTORY ./web/

#COPY --from=electronBuilder /usr/src/robolab-renderer/electron/dist/ /opt/robolab-renderer/download/
#ENV ELECTRON_MOUNT /download
#ENV ELECTRON_DIRECTORY ./download/

WORKDIR /opt/robolab-renderer
EXPOSE 8080
ENTRYPOINT node /opt/robolab-renderer/packages/robolab-jsBackend/kotlin/robolab-jsBackend.js
