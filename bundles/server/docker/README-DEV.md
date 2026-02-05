# Explorama Server Bundle - Development Docker Setup

This directory contains the development Docker configuration that provides Tinyauth and Caddy while proxying to your local development services.

## Overview

The development Docker setup runs:
- **Tinyauth**: Authentication service (in container)
- **Caddy**: Reverse proxy (in container)
- **socat**: Tunnels to forward traffic to host services

Your actual backend and frontend run on your host machine with hot reloading, while the container provides authentication and routing.

## Architecture

```
┌─────────────────────────────────────┐
│ Docker Container                    │
│                                     │
│  ┌──────┐    ┌────────────────┐   │
│  │Caddy │───→│ socat tunnels  │   │
│  │:80   │    │ localhost:4001 │───┼──→ host:4001 (Backend REPL)
│  └──────┘    │ localhost:8020 │───┼──→ host:8020 (Figwheel)
│              └────────────────┘   │
│  ┌──────────┐                     │
│  │Tinyauth  │                     │
│  │:8080     │                     │
│  └──────────┘                     │
└─────────────────────────────────────┘

Access via: http://localhost
```

## Quick Start

### 1. Start the Development Container

```bash
cd bundles/server/docker
./run-dev.sh
```

This will:
- Build the development Docker image
- Start the container with socat tunnels
- Expose port 80 (Caddy) and 8080 (Tinyauth)

### 2. Start Your Backend REPL

In a separate terminal:

```bash
cd bundles/server
npm install  # If not already done
bb gather-assets.bb.clj dev  # If not already done
clj -M:dev
```

**CRITICAL**: The backend must listen on `0.0.0.0:4001` (not `127.0.0.1`), otherwise the container cannot reach it.

If you encounter connection issues, you'll need to modify `bundles/server/backend/de/explorama/backend/woco/app/server.clj` line 33:

```clojure
;; Change from:
{:ip "127.0.0.1"

;; To:
{:ip "0.0.0.0"
```

Or set the bind address via environment variable before starting:
```bash
EXPLORAMA_BIND_ADDRESS=0.0.0.0 clj -M:dev
```

### 3. Access the Application

Open your browser to: http://localhost

The requests will flow:
1. Browser → Caddy (port 80 in container)
2. Caddy → socat tunnel
3. socat → Your host machine services (ports 4001, 8020)

## Manual Setup

### Build the Development Image

```bash
# From repository root
docker build -f bundles/server/Dockerfile.dev -t explorama-dev:latest .
```

### Run the Container

**On Linux:**
```bash
docker run -d \
  --name explorama-dev \
  -p 80:80 \
  -p 8080:8080 \
  --add-host=host.docker.internal:host-gateway \
  explorama-dev:latest
```

**On macOS/Windows (Docker Desktop):**
```bash
docker run -d \
  --name explorama-dev \
  -p 80:80 \
  -p 8080:8080 \
  explorama-dev:latest
```

## Configuration

### Environment Variables

- `HOST_IP`: Host machine IP (auto-detected on Linux)
- `HOST_BACKEND_PORT`: Backend port on host (default: `4001`)
- `HOST_FIGWHEEL_PORT`: Figwheel port on host (default: `8020`)
- `TINYAUTH_PORT`: Tinyauth port in container (default: `8080`)
- `TINYAUTH_SECRET`: Secret for token signing (default: `dev-secret-change-me`)

### Custom Ports

If your backend or Figwheel run on different ports:

```bash
docker run -d \
  --name explorama-dev \
  -p 80:80 \
  -e HOST_BACKEND_PORT=5001 \
  -e HOST_FIGWHEEL_PORT=9020 \
  explorama-dev:latest
```

## Workflow

### Starting Development

1. Start the Docker container: `./run-dev.sh`
2. Start backend REPL: `cd bundles/server && clj -M:dev`
3. Open browser: http://localhost
4. Make changes to code - hot reloading works as normal

### Stopping

```bash
docker stop explorama-dev
docker rm explorama-dev
```

Or use the `run-dev.sh` script which will prompt to restart if the container exists.

## Ports Reference

| Service | Port | Location | Purpose |
|---------|------|----------|---------|
| Caddy | 80 | Container | Main HTTP entry point |
| Tinyauth | 8080 | Container | Authentication service |
| Backend | 4001 | Host | Clojure REPL |
| Figwheel | 8020 | Host | ClojureScript dev server |

## Troubleshooting

### "Connection refused" errors

**Problem**: Container cannot reach host services.

**Solution**: Ensure your backend is listening on `0.0.0.0:4001`, not `127.0.0.1:4001`.

Check your backend configuration in `bundles/server/backend/de/explorama/backend/woco/server_config.clj`:

```clojure
(def explorama-host
  (defconfig
    {:env :explorama-host
     :default "0.0.0.0"  ; Should be 0.0.0.0 for Docker
     :type :string}))
```

Or set the environment variable:
```bash
EXPLORAMA_HOST=0.0.0.0 clj -M:dev
```

### Backend not starting

**Problem**: Backend fails to start or crashes.

**Solution**:
1. Check if another process is using port 4001: `lsof -i :4001`
2. Check backend logs for errors
3. Ensure all dependencies are installed: `npm install`
4. Ensure assets are gathered: `bb gather-assets.bb.clj dev`

### Figwheel not connecting

**Problem**: Frontend not loading or WebSocket errors.

**Solution**:
1. Ensure Figwheel is running on port 8020
2. Check if Figwheel is listening on `0.0.0.0:8020`
3. Check browser console for WebSocket errors
4. Verify the socat tunnel is working: `docker logs explorama-dev`

### Host IP detection issues (Linux)

**Problem**: Container cannot detect host IP.

**Solution**: Manually set the host IP:

```bash
docker run -d \
  --name explorama-dev \
  -p 80:80 \
  -e HOST_IP=192.168.1.100 \
  explorama-dev:latest
```

Find your host IP with: `ip addr show` or `hostname -I`

### View Container Logs

```bash
docker logs -f explorama-dev
```

Look for:
- Socat tunnel status
- Tinyauth startup messages
- Caddy proxy logs

## Differences from Production

| Feature | Development | Production |
|---------|-------------|------------|
| Backend | Runs on host (hot reload) | Runs in container (uberjar) |
| Frontend | Runs on host (Figwheel) | Static files in container |
| Build | No build required | Full build in Dockerfile |
| Services | Socat tunnels to host | All services in container |
| Auth | Tinyauth in container | Tinyauth in container |
| Proxy | Caddy in container | Caddy in container |

## Advanced Usage

### Custom Tinyauth Configuration

Mount a custom tinyauth config:

```bash
docker run -d \
  --name explorama-dev \
  -p 80:80 \
  -v ./my-tinyauth-config.yml:/app/config/tinyauth.yml \
  explorama-dev:latest
```

### Custom Caddyfile

Mount a custom Caddyfile:

```bash
docker run -d \
  --name explorama-dev \
  -p 80:80 \
  -v ./my-Caddyfile:/etc/caddy/Caddyfile \
  explorama-dev:latest
```

### Debugging socat Tunnels

To see tunnel traffic, run socat in verbose mode by modifying `entrypoint-dev.sh`:

```bash
socat -d -d TCP-LISTEN:4001,fork,reuseaddr TCP:${HOST_IP}:${HOST_BACKEND_PORT}
```

## Benefits

1. **Authentication**: Use Tinyauth without running it locally
2. **Consistent Routing**: Same proxy setup as production
3. **Hot Reloading**: Keep your normal development workflow
4. **Isolation**: Auth service isolated in container
5. **Easy Setup**: One command to start all supporting services
6. **Production Parity**: Use same Caddy/Tinyauth as production

## Next Steps

- See [README.md](README.md) for production Docker setup
- See [../../CLAUDE.md](../../CLAUDE.md) for development commands
