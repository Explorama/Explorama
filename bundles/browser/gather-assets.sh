#!/bin/bash

set -eu

pwd=`pwd`
mode=$1

if [ $mode == "prod" ]
then
  RES_PATH="$pwd/../../dist/browser"
  RES_PATH="$pwd/../../dist/browser"
  echo "Gather assets for production build"
fi

if [ $mode == "dev" ]
then
  RES_PATH="$pwd/assets/"
  TARGET_PATH="$pwd/vite-target/"
  echo "Gather assets for dev build"
fi

echo "Update style assets"
echo ""

rm -rf "$RES_PATH/css"
rm -rf "$RES_PATH/fonts"
rm -rf "$RES_PATH/img"
echo "remove old folders done."
if [ $mode == "prod" ]
then
  mkdir -p $RES_PATH
  cp "$pwd/resources/index.html" "$RES_PATH/index.html"
fi

echo ""
cd ../../styles
bash build.sh $mode

cd ../assets

mkdir -p "$RES_PATH/css"
mkdir -p "$TARGET_PATH/fonts"
mkdir -p "$TARGET_PATH/img"
cp -r css/* "$RES_PATH/css/"
cp -r fonts/* "$TARGET_PATH/fonts/"
cp -r img/* "$TARGET_PATH/img/"

echo "copy new styles done."
