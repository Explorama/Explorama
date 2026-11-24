# Explorama Server Docker Compose

This directory supports the compose-based server harness. It intentionally does not build or run the Explorama server inside a single container. Instead, Docker provides auth and routing while the frontend/backend services can run locally for normal development.

## Architecture

```text
Browser
  |
  v
Caddy (:80)
  |
  +-- /oauth2/* -> oauth2-proxy (:4180)
  +-- /api/*    -> socat-backend (:4001) -> host backend
  +-- /ws*      -> socat-backend (:4001) -> host backend
  +-- /*        -> socat-frontend (:8020) -> host frontend/Figwheel

oauth2-proxy -> Casdoor (:8000)
```

Services:

- `caddy`: public entry point, auth gate, frontend/backend routing.
- `casdoor-init`: one-shot service that fixes `casdoor_data` volume ownership (uid 1000) before Casdoor starts, then exits.
- `casdoor`: identity provider seeded from `docker/casdoor/init_data.json`.
- `oauth2-proxy`: validates sessions with Casdoor and exposes `/oauth2/*`.
- `socat-frontend` / `socat-backend` (in `docker-compose.dev.yml`): forward compose traffic to the host frontend/backend ports. Loaded only in dev mode.

The base `docker-compose.yml` holds the shared services (Caddy, Casdoor,
oauth2-proxy). Pick a mode with an override: `docker-compose.dev.yml` (socat
bridges to the host) or `docker-compose.full.yml` (the app in containers).

## Quick Start

Start the Docker harness in dev mode (the `dev` override adds the socat bridges
to the host):

```bash
cd bundles/server
docker compose -f docker-compose.yml -f docker-compose.dev.yml up
```

Start local Explorama services in a second terminal:

```bash
cd bundles/server
npm install
bb gather-assets.bb.clj dev
EXPLORAMA_BIND_ADDRESS=0.0.0.0 clojure -Sdeps "$(cat clj.deps.edn)" -M:dev
```

Open `http://localhost`.

## Credentials

Casdoor is seeded on first startup:

| Account | Username | Password |
|---|---|---|
| Casdoor admin | `admin` | `123` |
| Explorama dev user | `dev` | `dev123` |

Casdoor is available at `http://localhost:8000`.

## Configuration

Copy `.env.example` to `.env` when you need custom local ports or development secrets:

```bash
cp .env.example .env
```

Supported variables:

| Variable | Default | Purpose |
|---|---|---|
| `EXPLORAMA_HTTP_PORT` | `80` | Host port for Caddy |
| `CASDOOR_PORT` | `8000` | Host port for Casdoor |
| `HOST_FRONTEND_PORT` | `8020` | Host frontend/Figwheel port |
| `HOST_BACKEND_PORT` | `4001` | Host backend port |
| `CASDOOR_CLIENT_ID` | `explorama-dev-client-id` | oauth2-proxy client ID |
| `CASDOOR_CLIENT_SECRET` | `explorama-dev-client-secret` | oauth2-proxy client secret |
| `OAUTH2_PROXY_COOKIE_SECRET` | development secret | oauth2-proxy cookie encryption secret |
| `EXPLORAMA_APP_HOST` | `:80` | Caddy app site address; a hostname enables HTTPS |
| `CASDOOR_SITE_ADDRESS` | `:8000` | Caddy Casdoor site address; a hostname enables HTTPS |
| `EXPLORAMA_HTTPS_PORT` | `443` | Host port for Caddy HTTPS |
| `EXPLORAMA_PUBLIC_URL` | `http://localhost` | Browser-facing base URL (oauth2-proxy redirect) |
| `CASDOOR_LOGIN_URL` | `http://localhost:8000/login/oauth/authorize` | Browser-facing Casdoor login URL |
| `CASDOOR_ISSUER_URL` | `http://casdoor:8000` | OIDC issuer oauth2-proxy validates against |
| `CASDOOR_ORIGIN` | `http://localhost:8000` | Casdoor public origin (sets JWT `iss`) |
| `FRONTEND_UPSTREAM` | `socat-frontend:8020` | Caddy frontend upstream (full mode sets `app-frontend:80`) |
| `BACKEND_UPSTREAM` | `socat-backend:4001` | Caddy backend upstream (full mode sets `app-backend:4001`) |
| `OAUTH2_PROXY_COOKIE_SECURE` | `false` | Set `true` when serving HTTPS |
| `OAUTH2_PROXY_SKIP_OIDC_DISCOVERY` | `true` | Keep `true`: discovery advertises browser-facing endpoints the proxy container cannot reach |
| `OAUTH2_PROXY_SKIP_ISSUER_VERIFICATION` | `true` | Set `false` in prod once issuer matches |

The default `CASDOOR_CLIENT_ID` and `CASDOOR_CLIENT_SECRET` must match the first-run seed data in `docker/casdoor/init_data.json`. If you change them after Casdoor has initialized, either update the application in the Casdoor UI or reset the `casdoor_data` volume.

## Full mode (run the app in containers)

Dev mode (`docker-compose.dev.yml`) bridges to host-run frontend/backend via
`socat`. To run the Explorama app itself in containers, use the full override
instead:

```bash
docker compose -f docker-compose.yml -f docker-compose.full.yml up --build
```

This builds two images from `Dockerfile` and repoints Caddy at them:

- `app-backend`: the backend uberjar, serving `/ws` on `:4001`.
- `app-frontend`: nginx serving the advanced-compiled frontend.

Because the socat bridges live in `docker-compose.dev.yml`, they are not loaded
here — nothing sits idle. Caddy routes to the app containers via
`FRONTEND_UPSTREAM` / `BACKEND_UPSTREAM`. Full mode composes with the HTTPS env:
add your production `.env` to serve it over Let's Encrypt.

To build the same images outside compose (e.g. tagged for a registry), use
`../build-docker.sh [backend|frontend|all] [--push]`; images default to
`explorama/server-*` tagged with the short git SHA plus `latest`
(`IMAGE_PREFIX` / `TAG` override). See `../build-docker.sh --help`.

Full mode is verified end-to-end: the images build, the OIDC login flow
completes, and the app (including the `/ws` websocket) is served through Caddy.

## HTTPS (production)

The harness serves plain HTTP on `localhost` by default. To enable HTTPS with
automatic Let's Encrypt certificates, deploy on a host with two public DNS
records (one for the app, one for Casdoor) and inbound ports 80 and 443 open,
then:

1. Copy the production template and edit the hosts and secrets:

   ```bash
   cp .env.production.example .env
   ```

   Set `EXPLORAMA_APP_HOST`/`CASDOOR_SITE_ADDRESS` to your hostnames and change
   `CASDOOR_CLIENT_ID`, `CASDOOR_CLIENT_SECRET`, and `OAUTH2_PROXY_COOKIE_SECRET`.

2. Register the production OAuth callback in Casdoor. Because `init_data.json`
   only seeds on first run, either add
   `https://<app-host>/oauth2/callback` to the application's redirect URIs in
   `docker/casdoor/init_data.json` **before** the first start, or
   add it later in the Casdoor admin UI (Applications → Explorama Dev).

3. Start the stack with a mode override (HTTPS is orthogonal to the mode). With
   the app running on the host:

   ```bash
   docker compose -f docker-compose.yml -f docker-compose.dev.yml up -d
   ```

   Or, to run the app in containers, swap `docker-compose.dev.yml` for
   `docker-compose.full.yml` (see [Full mode](#full-mode-run-the-app-in-containers)).

Caddy serves both hostnames on port 443 and provisions/renews certificates
automatically (certs persist in the `caddy_data` volume). Caddy uses an
anonymous ACME account; to receive expiry notices, add an `email you@example.com`
line to the global block in `docker/caddy/Caddyfile`. While testing, point
Caddy at the Let's Encrypt staging endpoint to avoid rate limits by adding
`acme_ca https://acme-staging-v02.api.letsencrypt.org/directory` to the global
block.

## Local Backend Reachability

The backend must listen on an address reachable from Docker. Use:

```bash
EXPLORAMA_BIND_ADDRESS=0.0.0.0 clojure -Sdeps "$(cat clj.deps.edn)" -M:dev
```

The compose file maps `host.docker.internal` to Docker's host gateway for Linux. Docker Desktop already provides this host name on macOS and Windows.

## Reset State

Casdoor stores its SQLite database in the `casdoor_data` volume. Caddy stores runtime data in `caddy_data` and `caddy_config`.

To reset all Docker-managed state:

```bash
cd bundles/server
docker compose down -v
```

## Production Notes

This compose file is a development harness and a starting point for a real deployment. Before production use:

- HTTPS with automatic Let's Encrypt certificates is available — see
  [HTTPS (production)](#https-production).
- **Replace all development secrets and default users.** The compose fallbacks
  (`CASDOOR_CLIENT_SECRET`, `OAUTH2_PROXY_COOKIE_SECRET`, the `admin`/`dev`
  passwords) are published in this repository, and the stack starts without
  complaint if you keep them — with the committed cookie secret, anyone can
  forge an authenticated session and bypass the login entirely.
- Set `OAUTH2_PROXY_COOKIE_SECURE=true` and
  `OAUTH2_PROXY_SKIP_ISSUER_VERIFICATION=false` (the dev defaults are
  insecure); the production env template sets both.
- Decide whether Explorama services run as compose services or external
  upstreams.
- Persist and back up Casdoor data (the `casdoor_data` volume).
