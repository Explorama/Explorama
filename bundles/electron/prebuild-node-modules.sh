#!/bin/bash
set -eu

# Downloads the electron-ABI prebuilt binding for better-sqlite3 and drops
# it into node_modules. npm install can't supply it: the 9.4.0 from-source
# build fails on modern Node/V8, and the electron runtime needs the electron
# ABI, not the host Node ABI, anyway. Run from bundles/electron/.
#
# Usage: bash prebuild-node-modules.sh <os> <mode>
#   <os>   linux | win
#   <mode> prod | dev

pwd=`pwd`
os=$1
mode=$2
arch=""
bettersqlite3_version="9.4.0"
bettersqlite3_v="v119"

if [ $mode == "prod" ]
then
  TARGET_PATH="$pwd/../../dist/electron/prepared/node_modules/better-sqlite3/"
else
  TARGET_PATH="../node_modules/better-sqlite3/"
fi

if [ $os == "win" ]
then
    arch="win32-x64"
else
    arch="linux-x64"
fi

prebuild="prebuild"

if [ ! -d "$prebuild" ]
then
    mkdir $prebuild
fi
cd $prebuild

tarball="better-sqlite3-v$bettersqlite3_version-electron-$bettersqlite3_v-$arch.tar.gz"
if [ ! -f "$tarball" ]
then
    # -f: fail on HTTP errors instead of caching an HTML error body as the
    # tarball (which would poison every later run at the tar step); drop the
    # partial file on any failure so a retry re-downloads.
    curl -fL -o "$tarball" \
        "https://github.com/WiseLibs/better-sqlite3/releases/download/v$bettersqlite3_version/$tarball" \
        || { rm -f "$tarball"; echo "download failed: $tarball" >&2; exit 1; }
fi
tar -xf better-sqlite3-v$bettersqlite3_version-electron-$bettersqlite3_v-$arch.tar.gz

if [ $mode == "prod" ]
then
  if [ ! -d "$TARGET_PATH" ]
  then
      mkdir -p $TARGET_PATH
  fi
  cp -rf build $TARGET_PATH
fi
if [ ! -d "../node_modules/better-sqlite3/" ]
then
    mkdir -p "../node_modules/better-sqlite3/"
fi
cp -rf build "../node_modules/better-sqlite3/"
echo "Replaced binary for better-sqlite3"
rm -rf build
