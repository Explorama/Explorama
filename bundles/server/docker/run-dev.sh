#!/usr/bin/env bash

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "================================================"
echo "Explorama Development Docker Environment"
echo "================================================"

# Change to repository root
cd "$(dirname "$0")/../../.."

# Check if container is already running
if docker ps --format '{{.Names}}' | grep -q '^explorama-dev$'; then
    echo -e "${YELLOW}Container 'explorama-dev' is already running${NC}"
    echo ""
    read -p "Do you want to restart it? (y/N) " -n 1 -r
    echo ""
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo "Stopping existing container..."
        docker stop explorama-dev
        docker rm explorama-dev
    else
        echo "Exiting..."
        exit 0
    fi
fi

# Build the development image
echo ""
echo "Building development Docker image..."
docker build -f bundles/server/Dockerfile.dev -t explorama-dev:latest .

# Detect host IP for Linux
HOST_IP=""
if [[ "$OSTYPE" == "linux-gnu"* ]]; then
    # On Linux, we need to pass the host IP
    HOST_IP=$(ip route | grep default | awk '{print $3}')
    echo ""
    echo -e "${GREEN}Detected host IP: ${HOST_IP}${NC}"
fi

# Run the container
echo ""
echo "Starting development container..."

DOCKER_ARGS=(
    -d
    --name explorama-dev
    -p 80:80
    -p 8080:8080
)

# Add host IP if on Linux
if [[ -n "$HOST_IP" ]]; then
    DOCKER_ARGS+=(-e HOST_IP="${HOST_IP}")
fi

# Add network mode for Linux to access host services
if [[ "$OSTYPE" == "linux-gnu"* ]]; then
    DOCKER_ARGS+=(--add-host=host.docker.internal:host-gateway)
fi

docker run "${DOCKER_ARGS[@]}" explorama-dev:latest

echo ""
echo -e "${GREEN}Development container started successfully!${NC}"
echo ""
echo "================================================"
echo "Next steps:"
echo "================================================"
echo ""
echo "1. Start your backend REPL:"
echo "   cd bundles/server"
echo "   clj -M:dev"
echo ""
echo "2. Your application will be available at:"
echo "   http://localhost"
echo ""
echo "3. View container logs:"
echo "   docker logs -f explorama-dev"
echo ""
echo "4. Stop the container:"
echo "   docker stop explorama-dev"
echo ""
echo "================================================"
echo ""
echo -e "${YELLOW}Note: Make sure your backend is listening on 0.0.0.0:4001${NC}"
echo -e "${YELLOW}      (not 127.0.0.1 or localhost)${NC}"
echo ""
