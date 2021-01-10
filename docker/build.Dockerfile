FROM openjdk:11 as jsBackendBuilder
WORKDIR /usr/src/robolab-renderer
COPY . .
RUN ./gradlew --no-daemon jsBackendSync

FROM openjdk:11 as jsFrontendWebBuilder
WORKDIR /usr/src/robolab-renderer
COPY . .
RUN ./gradlew --no-daemon jsFrontendWebSync

FROM openjdk:11 as jsFrontendElectronBuilder
WORKDIR /usr/src/robolab-renderer
COPY . .
RUN ./gradlew --no-daemon jsFrontendElectronSync

FROM electronuserland/builder:wine as electronBuilder
WORKDIR /usr/src/robolab-renderer/electron
COPY deploy/electron/ .
COPY --from=jsFrontendElectronBuilder /usr/src/robolab-renderer/deploy/distElectron/ ../distElectron/
RUN npm install
RUN npx electron-builder -l --publish always

FROM node:latest
RUN mkdir /opt/robolab-renderer
COPY --from=jsBackendBuilder /usr/src/robolab-renderer/deploy/server/ /opt/robolab-renderer/
COPY --from=jsFrontendWebBuilder /usr/src/robolab-renderer/deploy/distWeb/ /opt/robolab-renderer/web/
COPY server.ini /opt/robolab-renderer/
COPY --from=electronBuilder /usr/src/robolab-renderer/electron/dist/ /opt/robolab-renderer/download/
WORKDIR /opt/robolab-renderer
ENTRYPOINT node /opt/robolab-renderer/packages/robolab-jsBackend/kotlin/robolab-jsBackend.js
