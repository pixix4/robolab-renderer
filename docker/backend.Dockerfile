FROM gradle:jdk11 as builder

WORKDIR /usr/src/robolab-renderer
COPY . .
RUN gradle --no-daemon jsServerSync

FROM node:latest
RUN mkdir /opt/robolab-renderer
COPY --from=builder /usr/src/robolab-renderer/webServer/ /opt/robolab-renderer/
COPY server.ini /opt/robolab-renderer/
WORKDIR /opt/robolab-renderer
ENTRYPOINT node /opt/robolab-renderer/packages/robolab-jsServer/kotlin/robolab-jsServer.js
