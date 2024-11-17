#!/bin/bash

set -eu

current_dir=$(pwd)

mkdir -p ../gen_raw_data
mkdir -p ../gen_resources/exp-import/
mkdir -p ../gen_src

echo "Generating test data"
# bb csv-mock-data.clj -f test-data-csv-desc.clj

# echo "Transforming test data"
# bash transform-data.sh

# echo "Generate Data-Tiles"
# bb data-tiles.clj

echo "Preparing resources for browsers"
bb prepare_resources.clj
echo ""
echo "🚀🚀🚀 done 🚀🚀🚀"
