# Explorama Server Bundle - Docker Deployment

This directory contains the Docker configuration for deploying Explorama as a server application.

**For development setup**, see [README-DEV.md](README-DEV.md).

## Architecture

The Docker container includes:

- **JDK 25**: Runs the Clojure backend
- **Caddy**: Reverse proxy and static file server
- **Tinyauth**: Simple authentication service

## Quick Start

### Build and Run with Docker

```bash
# Build the image (from repository root)
docker build -f bundles/server/Dockerfile -t explorama-server:latest .

# Run the container
docker run -d \
  --name explorama-server \
  -p 80:80 \
  -v explorama-data:/app/data \
  -v explorama-logs:/app/logs \
  explorama-server:latest
```

## Configuration

### Environment Variables

#### Explorama Backend

- `EXPLORAMA_HOST`: Host to bind to (default: `0.0.0.0`)
- `EXPLORAMA_PORT`: Backend port (default: `4001`)
- `EXPLORAMA_SCHEME`: Protocol scheme (default: `http`)
- `EXPLORAMA_PROXY_SCHEME`: External proxy scheme (default: `http`)
- `EXPLORAMA_PROXY_HOST`: External hostname (default: `localhost`)
- `EXPLORAMA_PROXY_PORT`: External port (default: `80`)

#### Java Options

- `JAVA_OPTS`: JVM options (default: `-Xmx2g -Xms512m ...`)

#### Tinyauth

- `TINYAUTH_PORT`: Tinyauth port (default: `8080`)
- `TINYAUTH_SECRET`: Secret for token signing (required in production)

### Custom Configuration

Mount custom configuration files:

```bash
docker run -d \
  -v ./custom-tinyauth.yml:/app/config/tinyauth.yml \
  -v ./custom-Caddyfile:/etc/caddy/Caddyfile \
  explorama-server:latest
```

### Tinyauth Users

Default users (defined in `tinyauth-config.yml`):

- **admin/admin**: Administrator with full access
- **user/user**: Regular user with read access

**WARNING**: Change these credentials in production!

## Exposed Ports

- `80`: HTTP (Caddy frontend)
- `443`: HTTPS (Caddy frontend, if configured)
- `8080`: Tinyauth API (internal)
- `4001`: Explorama backend (internal)

## Volumes

- `/app/data`: Persistent data storage
- `/app/logs`: Application logs

## Health Check

The container includes a health check endpoint at `/health` that verifies all services are running.

## Production Deployment

### Security Recommendations

1. **Change default credentials**: Update `tinyauth-config.yml` with strong passwords
2. **Set secure secret**: Use a long random string for `TINYAUTH_SECRET`
3. **Enable HTTPS**: Configure Caddy with SSL certificates
4. **Use secure cookies**: Set `session.secure: true` in tinyauth config
5. **Restrict CORS**: Limit CORS origins in tinyauth config

### HTTPS with Caddy

Update the Caddyfile to enable automatic HTTPS:

```caddyfile
your-domain.com {
    encode gzip

    handle /api/* {
        reverse_proxy localhost:4001
    }

    # ... rest of configuration
}
```

Caddy will automatically obtain and renew SSL certificates from Let's Encrypt.

### Docker Production Example

```bash
docker run -d \
  --name explorama-server \
  -p 80:80 \
  -p 443:443 \
  -e EXPLORAMA_PROXY_SCHEME=https \
  -e EXPLORAMA_PROXY_HOST=your-domain.com \
  -e EXPLORAMA_PROXY_PORT=443 \
  -e TINYAUTH_SECRET=your-long-random-secret-here \
  -e JAVA_OPTS="-Xmx4g -Xms1g -XX:+UnlockExperimentalVMOptions -XX:+UseShenandoahGC" \
  -v ./tinyauth-production.yml:/app/config/tinyauth.yml \
  -v ./Caddyfile-production:/etc/caddy/Caddyfile \
  -v explorama-data:/app/data \
  -v explorama-logs:/app/logs \
  --restart always \
  explorama-server:latest
```

## Build Process

The build process is defined in `build.sh` and includes:

1. Install npm dependencies
2. Gather assets (styles, images, fonts)
3. Build ClojureScript frontend
4. Build Clojure backend uberjar

To build manually:

```bash
cd bundles/server
./build.sh
```

## Logs

View logs:

```bash
docker logs -f explorama-server
```

## Troubleshooting

### Container won't start

Check logs for errors:
```bash
docker logs explorama-server
```

### Backend not responding

1. Check if backend is listening on correct port
2. Verify `EXPLORAMA_HOST` is set to `0.0.0.0`
3. Check Java memory settings

### Tinyauth authentication failing

1. Verify `TINYAUTH_SECRET` is set
2. Check tinyauth configuration file
3. Ensure session settings are correct

## Development

For development, you can mount source code and rebuild:

```bash
docker run -it --rm \
  -v $(pwd):/build \
  -w /build/bundles/server \
  eclipse-temurin:25-jdk-noble \
  ./build.sh
```
