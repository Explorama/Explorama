#!/bin/bash

npm install -g modclean

# The prune list must NOT contain anything required after this script runs:
# - electron's install.js require graph (extract-zip, @electron/get + their
#   deps, and the electron/dist output dir): the postinstall binary download
#   is deliberately deferred until verify-boot.sh, which runs AFTER this.
# - electron-builder 26.x's own load-time closure (got/@electron/get chain,
#   roarr, lodash, global-agent, ...): required to exist for bundling even
#   though none of it ships in the asar. Derived from prepared/
#   package-lock.json's dependency graph, not guesswork.
folders=("@cljs-oss" "@types" "accessibility-developer-tools" "acorn" "babel*" "babylon" "core-js" "devtron" "enhanced-resolve" "defined" "detective" "highlight.js" "humanize-plus" "loose-envify" "errno" "esutils" "global-tunnel-ng" "globals" "ws" "npm-conf" "immediate" "ini" "is-core-module" "jsonparse" "JSONStream" "js-tokens" "konan" "lie" "memory-fs" "pinkie" "exceljs/dist")

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