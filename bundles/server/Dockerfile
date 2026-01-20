# Multi-stage Dockerfile for Explorama Server Bundle
# Includes: JDK 25, Tinyauth, Caddy (reverse proxy + static file server)

# ============================================================================
# Stage 1: Build Stage
# ============================================================================
FROM eclipse-temurin:25-jdk-noble AS builder

# Install required build tools
RUN apt-get update && apt-get install -y \
    curl \
    bash \
    git \
    && rm -rf /var/lib/apt/lists/*

# Install Node.js (for frontend dependencies)
RUN curl -fsSL https://deb.nodesource.com/setup_20.x | bash - \
    && apt-get install -y nodejs \
    && rm -rf /var/lib/apt/lists/*

# Install Babashka (for build scripts)
RUN curl -sLO https://raw.githubusercontent.com/babashka/babashka/master/install \
    && chmod +x install \
    && ./install \
    && rm install

# Install Clojure CLI tools
RUN curl -L https://github.com/clojure/brew-install/releases/latest/download/linux-install.sh | bash

# Set working directory
WORKDIR /build

# Copy the entire project structure (needed for plugins)
COPY . .

# Change to server bundle directory
WORKDIR /build/bundles/server

# Run build script
RUN ./build.sh

# ============================================================================
# Stage 2: Runtime Stage
# ============================================================================
FROM eclipse-temurin:25-jre-noble

# Install Caddy and curl
RUN apt-get update && apt-get install -y \
    curl \
    debian-keyring \
    debian-archive-keyring \
    apt-transport-https \
    ca-certificates \
    && curl -1sLf 'https://dl.cloudsmith.io/public/caddy/stable/gpg.key' | gpg --dearmor -o /usr/share/keyrings/caddy-stable-archive-keyring.gpg \
    && curl -1sLf 'https://dl.cloudsmith.io/public/caddy/stable/debian.deb.txt' | tee /etc/apt/sources.list.d/caddy-stable.list \
    && apt-get update \
    && apt-get install -y caddy \
    && rm -rf /var/lib/apt/lists/*

# Install Tinyauth
RUN curl -L https://github.com/domodwyer/tinyauth/releases/latest/download/tinyauth-linux-amd64 -o /usr/local/bin/tinyauth \
    && chmod +x /usr/local/bin/tinyauth

# Create application user
RUN useradd -m -u 1000 explorama

# Create application directories
RUN mkdir -p /app/data /app/logs /app/config \
    && chown -R explorama:explorama /app

# Set working directory
WORKDIR /app

# Copy built artifacts from builder
COPY --from=builder --chown=explorama:explorama /build/bundles/server/target/explorama-standalone.jar /app/explorama.jar
COPY --from=builder --chown=explorama:explorama /build/bundles/server/resources/public /app/public

# Copy configuration files
COPY --chown=explorama:explorama bundles/server/docker/Caddyfile /etc/caddy/Caddyfile
COPY --chown=explorama:explorama bundles/server/docker/entrypoint.sh /app/entrypoint.sh
COPY --chown=explorama:explorama bundles/server/docker/tinyauth-config.yml /app/config/tinyauth.yml

# Make entrypoint executable
RUN chmod +x /app/entrypoint.sh

# Switch to application user
USER explorama

# Expose ports
# 80: Caddy (HTTP)
# 443: Caddy (HTTPS)
# 8080: Tinyauth
# 4001: Explorama backend (internal, proxied by Caddy)
EXPOSE 80 443 8080 4001

# Environment variables
ENV EXPLORAMA_HOST=0.0.0.0 \
    EXPLORAMA_PORT=4001 \
    EXPLORAMA_SCHEME=http \
    EXPLORAMA_PROXY_SCHEME=http \
    EXPLORAMA_PROXY_HOST=localhost \
    EXPLORAMA_PROXY_PORT=80 \
    JAVA_OPTS="-Xmx2g -Xms512m -XX:+UnlockExperimentalVMOptions -XX:+UseShenandoahGC -XX:ShenandoahGCHeuristics=compact" \
    TINYAUTH_PORT=8080 \
    TINYAUTH_SECRET=change-me-in-production

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost/health || exit 1

# Volume for persistent data
VOLUME ["/app/data", "/app/logs"]

# Start services
ENTRYPOINT ["/app/entrypoint.sh"]
