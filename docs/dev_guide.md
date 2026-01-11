# Explorama Bundles Guide

This guide covers how to start, test, and compile the `bundles/browser` and `bundles/server` modules.

## Prerequisites

Before working with either bundle, ensure you have the following installed:

- **Clojure CLI tools** (version 1.12.4+)
- **Node.js and npm** (for JavaScript dependencies)
- **Babashka** (for build scripts)
- **Chromium** (for headless testing)

## bundles/browser

The browser bundle contains the frontend application code with ClojureScript.

### Directory Structure

```
bundles/browser/
├── backend/          # Backend ClojureScript code
├── frontend/         # Frontend ClojureScript code
├── shared/           # Shared code between frontend and backend
├── resources/        # Static resources (HTML, CSS, images)
├── test/             # Test files
├── deps.edn          # Clojure dependencies
├── package.json      # Node.js dependencies
├── figwheel-main.edn # Figwheel configuration
├── dev.cljs.edn      # Development build configuration
├── test.cljs.edn     # Test build configuration
└── prod-opts.edn     # Production build options
```

### Setup

1. Install dependencies:
```bash
cd bundles/browser
npm install
```

### Development

Start the development server with hot reloading:

```bash
npm run dev
# or
clj -M:dev
```

This will:
- Start Figwheel Main on port 8020
- Watch for changes in `frontend/`, `backend/`, `shared/`, and plugin directories
- Enable hot code reloading
- Open a browser REPL
- Serve the app with source maps and debugging enabled

The application entry point is `de.explorama.frontend.woco.app.core` (bundles/browser/frontend/de/explorama/frontend/woco/app/core.cljs).

Configuration:
- **Port**: 8020
- **Output**: `resources/public/js/out/main.js`
- **WebSocket**: `ws://localhost:8020/figwheel-connect`

### Testing

#### Interactive Testing

Run tests with auto-reload:
```bash
npm test
# or
clj -M:test
```

This runs the test suite defined in `de.explorama.test-runner` with:
- Headless Chromium browser
- Auto-testing enabled
- Test output in `target/public/cljs-out/test/`

#### CI Testing

For continuous integration:
```bash
npm run test-ci
# or
bb run-tests-ci.bb.clj
```

This generates a JUnit-compatible XML report at `report.xml`.

### Building for Production

Build an optimized production bundle:

```bash
npm run build
# or
./build.sh
```

The build process:
1. Cleans the `dist/` directory
2. Gathers assets using `gather-assets.bb.clj`
3. Compiles ClojureScript with advanced optimizations
4. Copies static assets (CSS, fonts, images) to `dist/`
5. Inlines CSS into HTML using `merge-build-assets.bb.clj`

Output location: `dist/` directory

---

## bundles/server

The server bundle contains both a Clojure backend server and a ClojureScript frontend client.

### Directory Structure

```
bundles/server/
├── backend/          # Clojure backend code
├── frontend/         # ClojureScript frontend code
├── shared/           # Shared code
├── dev/              # Development utilities
├── test/
│   ├── backend/      # Backend tests (Clojure)
│   └── frontend/     # Frontend tests (ClojureScript)
├── resources/        # Static resources
├── clj.deps.edn      # Backend (Clojure) dependencies
├── cljs.deps.edn     # Frontend (ClojureScript) dependencies
├── package.json      # Node.js dependencies
├── figwheel-main.edn # Figwheel configuration
└── test.cljs.edn     # Test configuration
```

### Setup

1. Install dependencies:
```bash
cd bundles/server
npm install
```

### Development

The server bundle has separate development modes for backend and frontend.

#### Backend Development

Start the backend with nREPL:

```bash
npm run dev
# or
clj -M:dev
```

This starts:
- nREPL server on port 7888
- CIDER middleware enabled
- Shenandoah GC for better performance
- Entry point: `de.explorama.backend.woco.app.core`

You can connect your editor to the nREPL server at `localhost:7888`.

#### Frontend Development

Start the frontend development server:

```bash
clj -Sdeps cljs.deps.edn -M:dev
```

This starts Figwheel Main with:
- Port 8020
- Hot reloading for frontend and shared code
- WebSocket connection at `ws://localhost:8020/figwheel-connect`

### Testing

The server bundle has separate test suites for backend (Clojure) and frontend (ClojureScript).

#### Backend Tests (Clojure)

Run backend tests:
```bash
clj -M:test
```

Uses `cognitect.test-runner` to run tests in:
- `test/backend/`
- `../../plugins/backend_test/`
- `../../plugins/shared_test/`

For CI with XML report:
```bash
clj -M:test-ci
```

This generates `server-report.xml`.

#### Frontend Tests (ClojureScript)

Run frontend tests:
```bash
clj -Sdeps cljs.deps.edn -M:test
```

Uses Figwheel with:
- Test runner: `de.explorama.frontend.test-runner`
- Headless Chromium browser
- Output: `target/public/cljs-out/test/`

For CI:
```bash
bb run-tests-ci.bb.clj
```

This generates `client-report.xml`.

#### Run All Tests (CI)

To run both backend and frontend tests for CI:

```bash
npm run test-ci
```

This runs the Babashka script which executes both test suites.

### Building for Production

Build the production server:

```bash
npm run build
```

This compiles the frontend ClojureScript with advanced optimizations using the `:prod` alias.

For the backend, you can create an uberjar using:

```bash
clj -M:prod -m de.explorama.backend.woco.app.core
```

---

## Common Issues and Tips

### Port Conflicts

Both bundles use port 8020 for Figwheel. Don't run both development servers simultaneously unless you change the port in one of the `figwheel-main.edn` files.

### Headless Testing

The `figwheel-headless.sh` script requires Chromium installed. On Ubuntu/Debian:
```bash
sudo apt install chromium-browser
```

### Asset Gathering

Both bundles use Babashka scripts (`gather-assets.bb.clj`) to collect assets from plugins and core directories. If assets are missing, run:
```bash
bb gather-assets.bb.clj dev
```

### Clean Build

If you encounter strange compilation issues, try cleaning:

```bash
# For browser bundle
rm -rf bundles/browser/target bundles/browser/.cpcache bundles/browser/resources/public/js

# For server bundle
rm -rf bundles/server/target bundles/server/.cpcache
```

### Plugin System

Both bundles include code from the `../../plugins/` directory. Changes in plugins will be picked up by the development servers automatically.

### Dependency Updates

After updating dependencies in `deps.edn` or `package.json`:

```bash
npm install  # Update Node.js dependencies
clj -P       # Preload Clojure dependencies
```

---

## Summary

### Quick Reference

| Task | Browser Bundle | Server Bundle (Backend) | Server Bundle (Frontend) |
|------|----------------|------------------------|--------------------------|
| **Install** | `npm install` | `npm install` | `npm install` |
| **Dev** | `npm run dev` | `npm run dev` (nREPL) | `clj -Sdeps cljs.deps.edn -M:dev` |
| **Test** | `npm test` | `clj -M:test` | `clj -Sdeps cljs.deps.edn -M:test` |
| **Test CI** | `npm run test-ci` | `clj -M:test-ci` | `bb run-tests-ci.bb.clj` |
| **Build** | `npm run build` | `clj -M:prod` | `clj -Sdeps cljs.deps.edn -M:prod` |
| **Port** | 8020 (Figwheel) | 7888 (nREPL) | 8020 (Figwheel) |

### Entry Points

- **Browser Frontend**: `de.explorama.frontend.woco.app.core`
- **Server Backend**: `de.explorama.backend.woco.app.core`
- **Server Frontend**: `de.explorama.frontend.woco.app.core`
