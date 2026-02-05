#!/bin/bash

set -eu

npmrcFile=.npmrc
distFolder=dist
targetFolder=../assets
mode=$1

# Build using npm scripts (migrated from Grunt)

function build {
  echo "Build dist assets.."

  if [ -d "$distFolder" ]; then
    # Take action if $DIR exists. #
    rm -rf $distFolder
  fi

  if [ -f "$npmrcFile" ]; then
    echo "Delete $npmrcFile"
    rm "$npmrcFile"
  fi

  npm install
  echo $mode
  if [ $mode == "prod" ]; then
    npm run build:prod
  else
    npm run build
  fi
  echo ""
  echo "Build done."
  echo ""
}

function main {
  rm -rf "$targetFolder/css"
  rm -rf "$targetFolder/fonts"
  rm -rf "$targetFolder/img"
  rm -rf $distFolder

  build

  mv $distFolder/css/style.css $distFolder/css/3_style.css
  mv $distFolder/* $targetFolder/
}

# Only run main if this is the executed script (not if it is sourced in a
# different script).
if [[ "$0" = "$BASH_SOURCE" ]]; then
  echo "Start building styles"
  echo "-----"
  main $@

  echo ""
  echo "Done."
fi
