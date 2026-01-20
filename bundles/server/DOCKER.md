# Docker Setup for Explorama Server Bundle

Complete Docker configuration for both **production** and **development** deployments.

## Quick Reference

### Production (Full Build)

```bash
# Build
docker build -f bundles/server/Dockerfile -t explorama-server:latest .

# Run
docker run -d --name explorama-server -p 80:80 \
  -v explorama-data:/app/data \
  explorama-server:latest
```

Access: http://localhost

### Development (with Host Services)

```bash
# Start (automated)
cd bundles/server/docker
./run-dev.sh

# Start backend on host
cd bundles/server
clj -M:dev
```

Access: http://localhost

See [docker/README-DEV.md](docker/README-DEV.md) for details.

## Files Overview

### Production Files

| File | Purpose |
|------|---------|
| `Dockerfile` | Production multi-stage build with JDK 25 |
| `build.sh` | Build script for frontend & backend |
| `prod-opts.edn` | ClojureScript production build config |
| `docker/Caddyfile` | Caddy reverse proxy config (production) |
| `docker/entrypoint.sh` | Production container startup script |
| `docker/tinyauth-config.yml` | Tinyauth authentication config |
| `docker/README.md` | Production documentation |
| `.dockerignore` | Files to exclude from build |

### Development Files

| File | Purpose |
|------|---------|
| `Dockerfile.dev` | Development image (no build, only proxies) |
| `docker/Caddyfile.dev` | Caddy config with socat tunnels |
| `docker/entrypoint-dev.sh` | Dev startup with socat tunnels |
| `docker/run-dev.sh` | Helper script to start dev container |
| `docker/README-DEV.md` | Development documentation |

## Architecture Comparison

### Production

```
Browser → Caddy (port 80)
            ↓
    ┌───────┴────────┐
    ↓                ↓
Backend        Tinyauth
(port 4001)    (port 8080)

All services run in the container
Frontend: Static files served by Caddy
Backend: Clojure uberjar (JDK 25)
```

### Development

```
Browser → Caddy (port 80 in container)
            ↓
    ┌───────┴────────┐
    ↓                ↓
socat tunnels   Tinyauth
    ↓           (port 8080)
    ↓
Host Machine:
  - Backend (port 4001) - Clojure REPL with hot reload
  - Figwheel (port 8020) - ClojureScript dev server
```

## Components

All configurations include:

1. **JDK 25**: Latest Java runtime
2. **Tinyauth**: Simple authentication service
3. **Caddy**: Modern reverse proxy & HTTP server
   - Automatic HTTPS support
   - HTTP/2
   - Gzip compression
   - WebSocket proxying

## Environment Variables

### Explorama Backend

- `EXPLORAMA_HOST`: Bind address (default: `0.0.0.0`)
- `EXPLORAMA_PORT`: Backend port (default: `4001`)
- `EXPLORAMA_SCHEME`: Protocol (default: `http`)
- `EXPLORAMA_PROXY_*`: External URL configuration

### Tinyauth

- `TINYAUTH_PORT`: Auth service port (default: `8080`)
- `TINYAUTH_SECRET`: Token signing secret (change in production!)

### Java

- `JAVA_OPTS`: JVM options (default: `-Xmx2g -Xms512m ...`)

### Development Only

- `HOST_IP`: Host machine IP (auto-detected)
- `HOST_BACKEND_PORT`: Backend port on host (default: `4001`)
- `HOST_FIGWHEEL_PORT`: Figwheel port on host (default: `8020`)

## Default Credentials

**WARNING**: Change these in production!

From `docker/tinyauth-config.yml`:
- Username: `admin` / Password: `admin` (full access)
- Username: `user` / Password: `user` (read-only)

## Ports

| Port | Service | Access |
|------|---------|--------|
| 80 | Caddy HTTP | External |
| 443 | Caddy HTTPS | External (if configured) |
| 4001 | Backend | Internal |
| 8080 | Tinyauth | Internal |
| 8020 | Figwheel | Dev only, internal |

## Volumes

### Production

- `/app/data`: Persistent data (SQLite databases, uploads)
- `/app/logs`: Application logs

### Development

No volumes needed (services run on host)

## Build Process

The production build (`build.sh`) performs:

1. `npm ci` - Install frontend dependencies
2. `bb gather-assets.bb.clj prod` - Build styles, gather assets
3. `clojure -M:prod` - Build ClojureScript with advanced optimization
4. `clojure -A:prod -m uberdeps.uberjar` - Build backend uberjar

Output:
- Frontend: `resources/public/` (HTML, CSS, JS, images)
- Backend: `target/explorama-standalone.jar`

## Security Recommendations

### Production Checklist

- [ ] Change default Tinyauth credentials
- [ ] Set secure `TINYAUTH_SECRET` (long random string)
- [ ] Enable HTTPS in Caddyfile
- [ ] Set `session.secure: true` in tinyauth config
- [ ] Restrict CORS origins
- [ ] Use non-root user (already configured)
- [ ] Enable Docker health checks (already configured)
- [ ] Set appropriate memory limits
- [ ] Regular security updates of base image

## Troubleshooting

### Production

**Container won't start**:
```bash
docker logs explorama-server
```

**Out of memory**:
```bash
docker run -e JAVA_OPTS="-Xmx4g" ...
```

### Development

**Cannot connect to host services**:

Check if backend is listening on `0.0.0.0`:
```bash
lsof -i :4001
```

If it shows `127.0.0.1:4001`, modify the server to bind to `0.0.0.0:4001`.

**Host IP detection fails (Linux)**:
```bash
docker run -e HOST_IP=192.168.1.100 ...
```

## Documentation

- [docker/README.md](docker/README.md) - Production deployment guide
- [docker/README-DEV.md](docker/README-DEV.md) - Development setup guide
- [CLAUDE.md](../../CLAUDE.md) - Codebase overview and development commands

## Examples

### Production with Custom Config

```bash
docker run -d \
  --name explorama-server \
  -p 80:80 \
  -p 443:443 \
  -e EXPLORAMA_PROXY_SCHEME=https \
  -e EXPLORAMA_PROXY_HOST=explorama.example.com \
  -e TINYAUTH_SECRET="$(openssl rand -base64 32)" \
  -v ./custom-tinyauth.yml:/app/config/tinyauth.yml \
  -v ./Caddyfile-prod:/etc/caddy/Caddyfile \
  -v explorama-data:/app/data \
  --restart always \
  explorama-server:latest
```

### Development with Custom Ports

```bash
docker run -d \
  --name explorama-dev \
  -p 3000:80 \
  -e HOST_BACKEND_PORT=5001 \
  -e HOST_FIGWHEEL_PORT=9020 \
  explorama-dev:latest
```

Then start your backend on port 5001 and Figwheel on port 9020.

## Support

For issues and questions:
- Production: See [docker/README.md](docker/README.md)
- Development: See [docker/README-DEV.md](docker/README-DEV.md)
- General: See [CLAUDE.md](../../CLAUDE.md)
