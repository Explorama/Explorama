#!/usr/bin/env bash

set -e

echo "================================================"
echo "Building Explorama Server Bundle"
echo "================================================"

# Change to script directory
cd "$(dirname "$0")"

# Install npm dependencies
echo ""
echo "Installing npm dependencies..."
npm ci

# Gather assets (builds styles and copies to resources)
echo ""
echo "Gathering assets..."
bb gather-assets.bb.clj prod

# Build ClojureScript frontend
echo ""
echo "Building ClojureScript frontend..."
clojure -M:prod -m cljs.main -co prod-opts.edn -c de.explorama.frontend.woco.app.core

# Build Clojure backend uberjar
echo ""
echo "Building backend uberjar..."
clojure -A:prod -m uberdeps.uberjar \
  --target target/explorama-standalone.jar \
  --main-class de.explorama.backend.woco.app.server

echo ""
echo "================================================"
echo "Build complete!"
echo "Uberjar: target/explorama-standalone.jar"
echo "Frontend: resources/public/"
echo "================================================"
