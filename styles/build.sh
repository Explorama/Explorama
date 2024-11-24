#!/bin/bash

set -eu

npmrcFile=/home/build/.npmrc
distFolder=dist
targetFolder=../assets
mode=$1

# Refer to the readme on how to develop and build using Grunt.


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

    npm install -g grunt-cli
    npm install
    echo $mode
    if [ $mode == "prod" ]
    then 
      ./node_modules/grunt-cli/bin/grunt prod-build
    else 
      ./node_modules/grunt-cli/bin/grunt build
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

    mv $distFolder/* $targetFolder/
}

# Only run main if this is the executed script (not if it is sourced in a
# different script).
if [[ "$0" = "$BASH_SOURCE" ]]
then
    echo "Start building styles"
    echo "-----"
    main $@

    echo ""
    echo "Done."
fi
