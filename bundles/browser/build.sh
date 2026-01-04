#!/bin/bash
set -e

echo "Building Explorama production bundle..."

# Clean dist directory
echo "Cleaning dist directory..."
rm -rf dist/js
rm -rf dist/css
rm -rf dist/fonts
rm -rf dist/img

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

echo "Done"
