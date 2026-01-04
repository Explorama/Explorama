#!/bin/bash
set -e

echo "Compiling ClojureScript tests..."
clojure -M:test

echo ""
echo "Running tests in browser..."
npx web-test-runner --files "target/test/out/main_bundle.js/main.js" --config web-test-runner.config.js
