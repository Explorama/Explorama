#!/usr/bin/env bash
set -euo pipefail

DIST="${EXPLORAMA_BROWSER_DIST:-../bundles/browser/dist/explorama-browser}"

if [ ! -f "$DIST/index.html" ]; then
  echo "No built browser artifact at $DIST." >&2
  echo "Run ./build.sh in bundles/browser first." >&2
  exit 1
fi

exec npx http-server -p 8099 -s -c-1 "$DIST"
