#!/bin/bash
set -eu

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

if [ ! -f "better-sqlite3-v$bettersqlite3_version-electron-$bettersqlite3_v-$arch.tar.gz" ]
then
    wget "https://github.com/WiseLibs/better-sqlite3/releases/download/v$bettersqlite3_version/better-sqlite3-v$bettersqlite3_version-electron-$bettersqlite3_v-$arch.tar.gz"
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
