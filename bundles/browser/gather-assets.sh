#!/bin/bash

set -eu

pwd=`pwd`
mode=$1

RES_PATH="$pwd/assets/"
TARGET_PATH="$pwd/public/"

echo "Update style assets"
echo ""

rm -rf "$RES_PATH/css"
rm -rf "$RES_PATH/fonts"
rm -rf "$RES_PATH/img"
echo "remove old folders done."

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
