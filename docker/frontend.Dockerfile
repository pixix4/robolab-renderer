FROM gradle:jdk11 as builder

WORKDIR /usr/src/robolab-renderer
COPY . .
RUN gradle --no-daemon jsClientSync jvmClientSync

FROM nginx:latest
COPY --from=builder /usr/src/robolab-renderer/web/website/ /usr/share/nginx/html/
COPY --from=builder /usr/src/robolab-renderer/build/processedResources/build.ini /usr/share/nginx/html/
COPY --from=builder /usr/src/robolab-renderer/build/processedResources/build.ini /usr/share/nginx/html/jvm/
COPY --from=builder /usr/src/robolab-renderer/build/libs/robolab-jvm.jar /usr/share/nginx/html/jvm/
