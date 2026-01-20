#!/usr/bin/env bash

set -e

echo "================================================"
echo "Starting Explorama Development Environment"
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

# Detect host machine IP
# Try multiple methods to find the host IP
if [ -n "$HOST_IP" ]; then
    echo "Using provided HOST_IP: ${HOST_IP}"
elif getent hosts host.docker.internal > /dev/null 2>&1; then
    HOST_IP="host.docker.internal"
    echo "Using host.docker.internal"
else
    # On Linux without Docker Desktop, use the gateway IP
    HOST_IP=$(ip route | grep default | awk '{print $3}')
    echo "Detected host IP: ${HOST_IP}"
fi

echo ""
echo "Tunneling configuration:"
echo "  Backend:  localhost:4001 -> ${HOST_IP}:${HOST_BACKEND_PORT}"
echo "  Figwheel: localhost:8020 -> ${HOST_IP}:${HOST_FIGWHEEL_PORT}"
echo ""

# Start socat tunnels to host machine services

# Tunnel for backend (port 4001)
echo "Starting socat tunnel for backend..."
socat TCP-LISTEN:4001,fork,reuseaddr TCP:${HOST_IP}:${HOST_BACKEND_PORT} &
SOCAT_BACKEND_PID=$!
echo "Backend tunnel started (PID: ${SOCAT_BACKEND_PID})"

# Tunnel for Figwheel (port 8020)
echo "Starting socat tunnel for Figwheel..."
socat TCP-LISTEN:8020,fork,reuseaddr TCP:${HOST_IP}:${HOST_FIGWHEEL_PORT} &
SOCAT_FIGWHEEL_PID=$!
echo "Figwheel tunnel started (PID: ${SOCAT_FIGWHEEL_PID})"

# Wait a moment for socat to be ready
sleep 2

# Start Tinyauth in the background
echo ""
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
        echo "WARNING: Tinyauth failed to start"
    fi
    sleep 1
done

# Start Caddy in the foreground
echo ""
echo "Starting Caddy reverse proxy..."
echo ""
echo "================================================"
echo "Development environment ready!"
echo "================================================"
echo "Caddy:         http://localhost"
echo "Tinyauth:      http://localhost:${TINYAUTH_PORT}"
echo ""
echo "Tunneling to host machine:"
echo "  Backend:     localhost:4001 -> ${HOST_IP}:${HOST_BACKEND_PORT}"
echo "  Figwheel:    localhost:8020 -> ${HOST_IP}:${HOST_FIGWHEEL_PORT}"
echo ""
echo "Make sure your backend and Figwheel are running:"
echo "  cd bundles/server && clj -M:dev"
echo "================================================"

# Run Caddy in foreground
exec caddy run --config /etc/caddy/Caddyfile
