#!/bin/bash

set -eu

lein build-benchmark

echo "-----------------------------------"
echo "🎉🎉🎉 Build Done 🎉🎉🎉"

echo "Your Build is saved under <root>/dist/Browser and <root>/dist/browser.zip"
echo ""
echo "Open: ../../dist/browser/index.html"
echo ""
echo "Start profiling executing in your browser console de.explorama.profiling_tool.core.start() TODO change this to something without console"
echo "You can download the results with de.explorama.profiling_tool.core.download() TODO change this to something without console"
