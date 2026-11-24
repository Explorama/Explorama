.DEFAULT_GOAL := test

# bundles/electron is split into backend/ (Clojure backend + the Electron
# main/worker process source) and frontend/ (ClojureScript UI); targets
# operate on both halves unless noted.

install:
	cd backend && npm install
	cd frontend && npm install

assets:
	cd backend && bash gather-assets.sh ${build_mode}
	cd frontend && bash gather-assets.sh ${build_mode}

# ---------------------------------------------------------------------------
# Tests
# ---------------------------------------------------------------------------

# Backend suite runs on plain Node. better-sqlite3 stays pinned at 9.4.0
# for the electron runtime, but that version cannot build on modern Node --
# the test run swaps in a @12 prebuild via --no-save (node_modules only;
# manifest and lockfile keep the pin).
test-backend:
	cd backend && npm install better-sqlite3@12 --no-save --no-package-lock
	cd backend && bb run-tests-ci.bb.clj

# Frontend suite drives a headless chromium instance launched via
# frontend/figwheel-headless.sh, generated from the (gitignored,
# per-machine) frontend/figwheel-headless.sh.template.
test-frontend:
	if [ ! -f frontend/figwheel-headless.sh ]; then \
		cp frontend/figwheel-headless.sh.template frontend/figwheel-headless.sh; \
		chmod +x frontend/figwheel-headless.sh; \
	fi
	cd frontend && bb run-tests-ci.bb.clj

test: test-backend test-frontend

# ---------------------------------------------------------------------------
# Benchmarks
# ---------------------------------------------------------------------------

# Same better-sqlite3 prebuild swap as test-backend -- the bench also runs on
# plain Node.
bench-backend:
	cd backend && npm install better-sqlite3@12 --no-save --no-package-lock
	cd backend && clj -M:bench

# ---------------------------------------------------------------------------
# Dev
# ---------------------------------------------------------------------------

clean-before:
	rm -rf backend/node_modules frontend/node_modules
	rm -rf backend/target frontend/target
	rm -rf backend/resources/public/js frontend/resources/public/js

# Each half is an independent deps.edn project with its own interactive
# figwheel :dev alias -- one recipe can't own two REPLs, so this prints
# instructions.
dev: clean-before
	@echo "Electron dev builds run per-half -- use two terminals:"
	@echo ""
	@echo "  cd bundles/electron/backend  && clj -M:dev   # backend Figwheel REPL"
	@echo "  cd bundles/electron/frontend && clj -M:dev   # frontend Figwheel REPL"
	@echo ""
	@echo "Both figwheel-main.edn configs default to ring-server port 8020, so only"
	@echo "one half can bind that port at a time as currently configured."
	@echo "Run 'make assets build_mode=dev' first if resources/public/{css,fonts,img} are missing."

# ---------------------------------------------------------------------------
# Packaging
# ---------------------------------------------------------------------------
# Three cljs builds write straight into $(prepared): main.js
# (backend/prod-main-opts.edn), public/js/backend.js (backend/prod-opts.edn),
# public/js/frontend.js (frontend/prod-opts.edn via webpack). Layout assembly
# (package.json, html, preloads, css/fonts/img, app/ icons, npm install) is
# backend/gather-assets.sh's prod branch.
dist_root := ../../dist/electron
prepared := $(dist_root)/prepared

prepare-prod:
	cd backend && bash gather-assets.sh prod
	bash prebuild-node-modules.sh linux prod
	cd backend && clj -M:prod-main
	cd backend && clj -M:prod
	cd frontend && clj -M:prod
	cp backend/clean_modules.sh $(prepared)/
	cd $(prepared) && bash clean_modules.sh
	rm -f $(prepared)/clean_modules.sh
	# cljs :output-dir trees are build scratch (goog/clojure/etc. sources +
	# unoptimized intermediate output) -- only the :output-to files
	# (main.js, public/js/{backend,frontend}.js) and frontend's async
	# webpack chunks (public/js/*.frontend.js) are needed at runtime.
	rm -rf $(prepared)/public/js/main
	rm -rf $(prepared)/public/js/backend

# electron-builder is a pinned backend devDependency, provisioned into
# prepared/node_modules by gather-assets prod's npm install; npx resolves
# that local bin. prepared/'s teardown lives in build-linux (after
# verify-boot), not here.
bundle-linux:
	cd $(prepared) && npx electron-builder --linux
	cd $(prepared) && mv dist/*.AppImage ../

# Untested on this machine (no Windows/wine toolchain available) -- mirrors
# bundle-linux's shape; verify before relying on it.
bundle-win:
	cd $(prepared) && npx electron-builder --win
	cd $(prepared) && mv dist/*.exe ../ 2>/dev/null || true
	cd $(prepared) && mv dist/*.AppImage ../ 2>/dev/null || true

# Boots $(prepared) under xvfb and polls for a clean main+worker start --
# see verify-boot.sh for the pass/fail criteria.
verify-boot:
	bash verify-boot.sh

# The chain is strictly sequential (prepare -> verify -> bundle -> teardown);
# don't let -j interleave the phony prerequisites. The self-contained
# AppImage can itself be boot-tested afterwards:
#   APP=../../dist/electron/Explorama-linux.AppImage bash verify-boot.sh
.NOTPARALLEL:

build-linux: prepare-prod verify-boot bundle-linux
	rm -rf $(prepared)

# Untested (see bundle-win); same verify-boot dependency as build-linux.
build-win: prepare-prod verify-boot bundle-win
	rm -rf $(prepared)

# Main-process dev only: compiles backend/main unoptimized to
# backend/resources/main.js (:dev-main alias) and launches electron on it.
# KNOWN GAP: the UI/worker windows load js/frontend.js / js/backend.js,
# which the per-half figwheel dev builds do not emit (they write js/out/) --
# so the renderer windows stay blank; useful for main-process work only.
dev-app:
	cd backend && clj -M:dev-main
	cd backend && npx electron --no-sandbox ./resources/main.js

.PHONY: install assets test test-backend test-frontend bench-backend \
	clean-before dev \
	prepare-prod verify-boot bundle-linux bundle-win build-linux build-win \
	dev-app
