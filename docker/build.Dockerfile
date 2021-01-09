FROM gradle:jdk11 as jsBackendBuilder
WORKDIR /usr/src/robolab-renderer
COPY . .
RUN gradle --no-daemon jsBackendSync

FROM gradle:jdk11 as jsFrontendWebBuilder
WORKDIR /usr/src/robolab-renderer
COPY . .
RUN gradle --no-daemon jsFrontendWebSync

FROM node:latest
RUN mkdir /opt/robolab-renderer
COPY --from=jsBackendBuilder /usr/src/robolab-renderer/webServer/ /opt/robolab-renderer/
COPY --from=jsFrontendWebSync /usr/src/robolab-renderer/distWeb/ /opt/robolab-renderer/web/
COPY server.ini /opt/robolab-renderer/
WORKDIR /opt/robolab-renderer
ENTRYPOINT node /opt/robolab-renderer/packages/robolab-jsServer/kotlin/robolab-jsBackend.js
