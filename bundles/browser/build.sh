#!/bin/bash
set -e

echo "Building Explorama production bundle..."

# Clean dist directory
echo "Cleaning dist directory..."
rm -rf dist
mkdir -p dist

# Gather assets (using dev mode to skip cssmin for now)
echo "Gathering assets..."
bb gather-assets.bb.clj dev

# Compile ClojureScript with advanced optimizations
echo "Compiling ClojureScript..."
clojure -M:prod

# Copy assets to dist
echo "Copying assets to dist..."
cp -r resources/public/css dist/
cp -r resources/public/fonts dist/
cp -r resources/public/img dist/

# Copy and update index.html
echo "Copying index.html..."
cp resources/public/index.html dist/index.html

# Update index.html to use the production bundle
sed -i 's|/js/out/main_bundle.js|/js/out/main_bundle.js/main.js|g' dist/index.html

echo ""
echo "Build complete! Output is in the dist/ directory."
echo "Main bundle: dist/js/out/main_bundle.js/main.js"
echo "Entry point: dist/index.html"
