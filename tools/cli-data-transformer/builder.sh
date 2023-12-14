#!/bin/bash
set -eu

SCRIPT_DIR="$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
#export EXPLORAMA_COUNTRY_PATH="$SCRIPT_DIR/countries.edn"
#export EXPLORAMA_COUNTRY_MAPPING_SEARCH_ENABLED=true

java -jar "$SCRIPT_DIR/builder.jar" "$@"
