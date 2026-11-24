#!/bin/bash

set -eu

pwd=`pwd`
mode=$1
npm_install=${2:-true}

if [ $mode == "prod" ]
then
  # the repo root -- and therefore dist/electron/prepared -- is three
  # levels up from backend/.
  RES_PATH="$pwd/../../../dist/electron/prepared"
  echo "Gather assets for production build"
else
  RES_PATH="$pwd/resources"
  echo "Gather assets for dev build"
fi

echo "Update style assets"
echo ""

rm -rf "$RES_PATH/public/css"
rm -rf "$RES_PATH/public/fonts"
rm -rf "$RES_PATH/public/img"
echo "remove old folders done."

if [ $mode == "prod" ]
then
  mkdir -p "$RES_PATH/public"
  cp "package.json" "$RES_PATH/package.json"
  cp "$pwd/resources/public/index.html" "$RES_PATH/public/"
  cp "$pwd/resources/public/loading.html" "$RES_PATH/public/"
  cp "$pwd/resources/public/worker.html" "$RES_PATH/public/"
  cp "$pwd/resources/public/_preloadUI.js" "$RES_PATH/public/"
  cp "$pwd/resources/public/_preloadWorker.js" "$RES_PATH/public/"
  # electron-builder's icons/installer art (referenced by backend/
  # package.json's "build" block as ../public/app/... relative to prepared/).
  cp -rf "$pwd/resources/public/app" "$RES_PATH/public/"
  cd $RES_PATH
  if $npm_install
  then
    echo "npm install"
    # --ignore-scripts: better-sqlite3@9.4.0's source build fails on modern
    # Node; ../prebuild-node-modules.sh supplies the electron-ABI binding
    # afterwards. Also skips electron's postinstall binary download --
    # verify-boot.sh provisions that.
    npm install --ignore-scripts
  fi
  cd $pwd
else
  if $npm_install
  then
    echo "npm install"
    npm install
  fi
fi
  
echo ""
# backend/ and frontend/ sit one level deeper than this script's original
# location (bundles/electron/), so the repo root is three levels up.
cd ../../../styles
bash build.sh $mode
cd ../assets
mkdir -p "$RES_PATH/public"
cp -r "css" $RES_PATH/public/
cp -r "fonts" $RES_PATH/public/
cp -r "img" $RES_PATH/public/
echo "copy new styles done."

echo ""


