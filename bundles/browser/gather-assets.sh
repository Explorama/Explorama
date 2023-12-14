#!/bin/bash

set -eu

pwd=`pwd`
mode=$1

echo "npm install"
npm install

if [ $mode == "prod" ]
then
  RES_PATH="$pwd/../../dist/browser"
  echo "Gather assets for production build"
fi

if [ $mode == "benchmark" ]
then
  RES_PATH="$pwd/../../dist/browser-benchmark"
  echo "Gather assets for production prifiling build"
fi

if [ $mode == "dev" ]
then
  RES_PATH="$pwd/resources/public/"
  echo "Gather assets for dev build"
fi

if [ $mode == "dev-benchmark" ]
then
  RES_PATH="$pwd/resources/public/"
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

if [ $mode == "benchmark" ]
then
  mkdir -p $RES_PATH
  cp "$pwd/resources/index_benchmark.html" "$RES_PATH/index.html"
fi

echo ""
cd ../../styles
bash build.sh $mode

cd ../assets

mkdir -p "$RES_PATH/css"
mkdir -p "$RES_PATH/fonts"
mkdir -p "$RES_PATH/img"
cp -r css/* "$RES_PATH/css/"
cp -r fonts/* "$RES_PATH/fonts/"
cp -r img/* "$RES_PATH/img/"

echo "copy new styles done."
