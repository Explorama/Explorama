#!/bin/bash

npm install -g modclean

folders=("@cljs-oss" "@types" "@szmarczak" "accessibility-developer-tools" "acorn" "babel*" "babylon" "base64-js" "commander" "core-js" "debug" "devtron" "enhanced-resolve" "defer-to-connect" "defined" "define-properties" "detective" "detect-node" "highlight.js" "humanize-plus" "fs-extra" "loose-envify" "lodash" "errno" "es6-error" "esutils" "extract-zip" "function-bind" "get-intrinsic" "globalthis" "global-tunnel-ng" "globals" "ws" "npm-conf" "electron/dist" "global-agent" "immediate" "ini" "is-core-module" "jsonparse" "jsonfile" "JSONStream" "json-stringify-safe" "js-tokens" "konan" "lie" "lru-cache" "matcher" "memory-fs" "minimist" "ms" "pinkie" "progress" "universalify" "roarr" "exceljs/dist")

echo "------------------------------------"
echo "Reduce node modules"

modclean -n default:safe,default:caution -r

for folder in "${folders[@]}"
do	
  echo "  Remove ${folder}"
  rm -rf node_modules/$folder
done

echo "done."
echo "------------------------------------"