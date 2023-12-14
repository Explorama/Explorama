#!/bin/bash

set -eu

pwd=`pwd`
mode=$1
npm_install=${2:-true}

if [ $mode == "prod" ]
then
  RES_PATH="$pwd/../../dist/server/prepared"
  echo "Gather assets for production build"
else
  RES_PATH="$pwd/resources"
  echo "Gather assets for dev build"
fi

echo "Update style assets"
echo ""

rm -rf "$RES_PATH/public/assets/css"
rm -rf "$RES_PATH/public/assets/fonts"
rm -rf "$RES_PATH/public/assets/img"
echo "remove old folders done."
if $npm_install
  then 
    echo "npm install"
    npm install
fi
  
echo ""
cd ../../styles
bash build.sh $mode
cd ../assets
if [ ! -d "$RES_PATH/public/assets/" ]; then
  mkdir -p "$RES_PATH/public/assets/"
fi
cp -r "css" $RES_PATH/public/assets/
cp -r "fonts" $RES_PATH/public/assets/
cp -r "img" $RES_PATH/public/assets/
echo "copy new styles done."

echo ""


