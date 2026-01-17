#!/bin/bash
set -e

echo "Building Explorama production bundle..."

# Clean dist directory
echo "Cleaning dist directory..."
rm -rf dist/js
rm -rf dist/css
rm -rf dist/fonts
rm -rf dist/img
rm -rf dist/explorama-browser
rm -rf dist/explorama-browser.zip

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

# Inline CSS into HTML to avoid CORS issues
echo "Inlining CSS into HTML..."
bb merge-build-assets.bb.clj

mkdir dist/explorama-browser

mv dist/index.html dist/explorama-browser/
mkdir -p dist/explorama-browser/js/bundle/
mv dist/js/bundle/main.js dist/explorama-browser/js/bundle/main.js
mv dist/img dist/explorama-browser/

cd dist

zip -r explorama-browser.zip explorama-browser

echo "Done"
