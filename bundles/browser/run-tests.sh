#!/bin/bash
set -e

echo "Running ClojureScript tests in Chrome..."
mkdir -p target/test-results
clojure -M:test

echo ""
echo "Test report saved to: target/test-results/junit.xml"
