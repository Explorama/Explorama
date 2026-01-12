# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

Explorama is a data analytics tool written in Clojure/ClojureScript. The codebase supports three deployment models (browser, server, electron) from a shared plugin architecture.

## Development Commands

### Browser Bundle (bundles/browser)

Development:
```bash
cd bundles/browser
npm install
bb gather-assets.bb.clj dev
clj -M:dev  # Starts Figwheel on port 8020
```

Currently requires workaround:
```bash
npx shadow-cljs compile app
vite build --mode development
npx shadow-cljs watch app
```

Build production:
```bash
cd bundles/browser
./build.sh
```

Tests:
```bash
cd bundles/browser
npm run test-ci  # Runs all tests in CI mode
clj -M:test      # Runs tests with interactive REPL
```

### Electron Bundle (bundles/electron)

Development (requires two terminals):
```bash
# Terminal 1 - Build backend/frontend
cd bundles/electron
make dev

# Terminal 2 - Run electron app
cd bundles/electron
make dev-app
```

Build for Windows:
```bash
cd bundles/electron
make build-win
```

Build for Linux:
```bash
cd bundles/electron
make build-linux
```

Tests:
```bash
cd bundles/electron
make test           # All tests
make test-backend   # Backend only
make test-frontend  # Frontend only
```

### Server Bundle (bundles/server)

Note: Server bundle is not currently functional.

Backend development (Clojure REPL on port 7888):
```bash
cd bundles/server
npm install
bash gather-assets.sh dev
clj -M:dev
```

Frontend development (ClojureScript):
```bash
cd bundles/server
clj -M:dev  # Figwheel on port 8020
```

Tests:
```bash
cd bundles/server
clj -M:test     # Backend tests
clj -M:test-ci  # Frontend tests in CI mode
```

### Linting

```bash
# Lint Clojure/ClojureScript code
clj-kondo --lint plugins/
clj-kondo --lint bundles/browser/
clj-kondo --lint bundles/electron/
clj-kondo --lint bundles/server/
```

## Architecture

### Plugin System

Plugins are the core architectural unit. Each plugin follows a three-layer structure:

- **Backend** (`plugins/backend/de/explorama/backend/{plugin-name}/`): Clojure server-side logic
- **Frontend** (`plugins/frontend/de/explorama/frontend/{plugin-name}/`): ClojureScript UI and client logic
- **Shared** (`plugins/shared/de/explorama/shared/{plugin-name}/`): Code shared between frontend and backend

Key plugins include: `table`, `charts`, `map`, `mosaic`, `indicator`, `algorithms`, `projects`, `reporting`, `search`, `configuration`, `expdb`, `data-atlas`, `woco` (workspace core).

Plugin initialization pattern:
```clojure
;; Backend (backend.cljc)
(defn init []
  (frontend-api/register-routes websocket/endpoints))

;; Frontend (core.cljs)
(re-frame/reg-event-fx ::init-event
  (fn [{db :db} _]
    {:dispatch-n [[::register-plugin]
                  [::init-client user-info]]}))
```

### Frontend Architecture (Re-frame)

The frontend uses **re-frame** for state management:

- **Single app-db**: All state in one atom
- **Events**: Trigger state changes via `(re-frame/dispatch [::event-name params])`
- **Subscriptions**: Derive data from app-db via `(re-frame/subscribe [::sub-name])`
- **Effects**: Side effects (HTTP, backend calls) as data

Event naming convention:
- Namespaced: `:plugin-name/event-name`
- Private: `::event-name` (expands to current namespace)

Frontend Interface (FI) API provides plugin registry and shared services. Located in `plugins/frontend/de/explorama/frontend/woco/api/core.cljs`.

Naming conventions for FI API:
- `*-raw`: Direct values
- `*-fn`: Callable functions
- `*-sub`: Re-frame subscription functions
- `*-sub-vec`: Subscription vectors for @(subscribe ...)
- `*-db-get`: Direct DB access functions
- `*-event-vec`: Event vectors
- `*-event-dispatch`: Dispatch functions

### Backend Communication

Communication between frontend and backend uses **pneumatic-tubes** (WebSocket-based):

Frontend sends events:
```clojure
(backend-api/dispatch [route-keyword {:client-callback [::response-event]} ...params])
```

Backend defines routes:
```clojure
;; In websocket.cljc
(def endpoints
  {route-keyword handler-fn})

;; In backend.cljc
(frontend-api/register-routes endpoints)
```

Metadata options for requests:
- `:client-callback`: Event to dispatch on success
- `:failed-callback`: Event to dispatch on failure
- `:broadcast-callback`: Event to broadcast to all clients
- `:user-info`: User information
- `:client-id`: Unique client identifier

### Deployment Models

**Browser Bundle**: Frontend and backend both run in browser as ClojureScript. No network required. Data stored in IndexedDB.

**Server Bundle**: Frontend (ClojureScript) in browser, backend (Clojure) on JVM server. WebSocket communication. Multi-user capable.

**Electron Bundle**: Desktop app with separate processes:
- Main Process: Electron app coordinator
- UI Window: ClojureScript frontend
- Worker Window: Clojure backend
- Communication via MessagePorts

### Data Flow

1. User imports data (CSV/file)
2. Data Transformer validates and maps schema (`plugins/shared/de/explorama/shared/data_transformer/`)
3. EXPDB persists data with indexing (`plugins/backend/de/explorama/backend/expdb/`)
4. Visualization plugins query data via backend routes
5. Frontend receives filtered/aggregated data
6. Plugin renders visualization

Data provider system allows plugins to register data sources:
```clojure
(data-provider/register-provider "search"
  {:data-tiles handler-fn
   :data-tile-ref handler-fn})
```

### Frame Pattern

Visualizations are "frames" (windows/cards in workspace). Frame descriptor defines UI:
```clojure
{:loading? loading-impl
 :frame-header header-impl
 :toolbar toolbar-impl
 :legend legend-impl
 :filter filter-impl}
```

## File Organization

- **bundles/**: Bundle-specific code for each deployment model (browser, electron, server)
- **plugins/**: Shared plugin code (backend, frontend, shared, tests)
- **styles/**: Stylesheets and images
- **tools/**: Build tools and utilities
- **assets/**: Static assets
- **data/**: Sample data files

## Key Dependencies

- Clojure 1.12.4
- ClojureScript 1.12.134
- re-frame 1.2.0: Frontend state management
- Reagent 1.0.0: React wrapper
- Figwheel Main 0.2.18: ClojureScript hot reloading
- pneumatic-tubes 0.3.0: WebSocket communication
- Mount 0.1.17: Component lifecycle
- Malli 0.12.0: Schema validation
- Timbre 5.1.2: Logging
- Babashka: Build automation

Frontend JavaScript dependencies include React 17, OpenLayers 7, Chart.js 3, Pixi.js 7, Quill, PapaParse.

## Common Patterns

### Path-based DB Access
```clojure
;; Define paths as constants
(def path/slides [:slides])

;; Consistent access
(get-in db path/slides)
(assoc-in db path/slides [...])
```

### Event Registration
```clojure
;; Register event handler
(re-frame/reg-event-fx ::my-event
  (fn [{db :db} [_ param]]
    {:db (assoc db :key param)
     :dispatch [::next-event]}))
```

### Subscription Registration
```clojure
;; Register subscription
(re-frame/reg-sub ::my-sub
  (fn [db _]
    (get-in db [:my :path])))
```

### Backend Route Handler
```clojure
(defn handler-fn
  [_ [params] {:keys [client-callback]}]
  (let [result (process params)]
    (when client-callback
      (tube/dispatch client-callback result))))
```

## Notes

- Browser bundle uses ClojureScript for backend (runs in browser, no server)
- Server bundle is currently non-functional
- Electron is the primary deployment target
- Three separate test suites: backend tests (Clojure), frontend tests (ClojureScript), electron tests
- Hot reloading available in development via Figwheel
- Production builds use advanced ClojureScript optimization
