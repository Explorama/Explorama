#!/usr/bin/env bash

set -e

echo "================================================"
echo "Starting Explorama Server Bundle"
echo "================================================"

# Function to handle shutdown
shutdown() {
    echo ""
    echo "Received shutdown signal..."

    # Kill all background jobs
    jobs -p | xargs -r kill

    echo "Shutdown complete"
    exit 0
}

# Trap signals
trap shutdown SIGTERM SIGINT

# Start Tinyauth in the background
echo "Starting Tinyauth on port ${TINYAUTH_PORT}..."
tinyauth \
    --port="${TINYAUTH_PORT}" \
    --config=/app/config/tinyauth.yml \
    --secret="${TINYAUTH_SECRET}" &
TINYAUTH_PID=$!
echo "Tinyauth started (PID: ${TINYAUTH_PID})"

# Wait for Tinyauth to be ready
echo "Waiting for Tinyauth to be ready..."
for i in {1..30}; do
    if curl -sf http://localhost:${TINYAUTH_PORT}/health > /dev/null 2>&1; then
        echo "Tinyauth is ready"
        break
    fi
    if [ $i -eq 30 ]; then
        echo "ERROR: Tinyauth failed to start"
        exit 1
    fi
    sleep 1
done

# Start Explorama backend in the background
echo ""
echo "Starting Explorama backend on port ${EXPLORAMA_PORT}..."
java ${JAVA_OPTS} \
    -jar /app/explorama.jar &
BACKEND_PID=$!
echo "Explorama backend started (PID: ${BACKEND_PID})"

# Wait for backend to be ready
echo "Waiting for Explorama backend to be ready..."
for i in {1..60}; do
    if curl -sf http://localhost:${EXPLORAMA_PORT}/ > /dev/null 2>&1; then
        echo "Explorama backend is ready"
        break
    fi
    if [ $i -eq 60 ]; then
        echo "ERROR: Explorama backend failed to start"
        exit 1
    fi
    sleep 1
done

# Start Caddy in the foreground
echo ""
echo "Starting Caddy reverse proxy..."
echo ""
echo "================================================"
echo "All services started successfully!"
echo "================================================"
echo "Caddy:    http://localhost"
echo "Backend:  http://localhost:${EXPLORAMA_PORT}"
echo "Tinyauth: http://localhost:${TINYAUTH_PORT}"
echo "================================================"

# Run Caddy in foreground
exec caddy run --config /etc/caddy/Caddyfile
