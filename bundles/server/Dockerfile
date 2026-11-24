# syntax=docker/dockerfile:1

# ---- builder: JDK + Clojure CLI + Node + Babashka, runs build.sh ----
FROM clojure:temurin-21-tools-deps-bookworm AS builder
WORKDIR /build
RUN apt-get update \
 && apt-get install -y --no-install-recommends nodejs npm curl bash \
 && curl -sL https://raw.githubusercontent.com/babashka/babashka/master/install -o /tmp/install-bb \
 && bash /tmp/install-bb \
 && rm -rf /var/lib/apt/lists/* /tmp/install-bb
# Copy the inputs build.sh needs (paths relative to repo root).
COPY plugins ./plugins
COPY styles ./styles
COPY assets ./assets
COPY bundles/server ./bundles/server
WORKDIR /build/bundles/server
RUN ./build.sh

# ---- backend: JRE + uberjar, serves /ws on :4001 ----
FROM eclipse-temurin:21-jre-noble AS backend
WORKDIR /app
COPY --from=builder /build/bundles/server/target/explorama-standalone.jar ./app.jar
ENV EXPLORAMA_BIND_ADDRESS=0.0.0.0 \
    EXPLORAMA_PORT=4001
EXPOSE 4001
ENTRYPOINT ["java", \
  "-XX:+UnlockExperimentalVMOptions", "-XX:+UseShenandoahGC", \
  "-XX:ShenandoahGCHeuristics=compact", \
  "--add-opens", "java.base/jdk.internal.misc=ALL-UNNAMED", \
  "-Dio.netty.tryReflectionSetAccessible=true", \
  "-jar", "app.jar", "-m", "de.explorama.backend.woco.app.server"]

# ---- app-frontend: nginx serving the static build ----
FROM nginx:alpine AS app-frontend
COPY --from=builder /build/bundles/server/resources/public /usr/share/nginx/html
COPY bundles/server/docker/nginx/default.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
