#!/bin/bash
# In-repo replacement for the never-committed dl_style_assets.sh (#9):
# builds the styles workspace and copies the gallery's stylesheet set from
# the repo's own assets/ output, so a fresh clone can provision the tool
# without any external script.
#
# index.html links (in order): blueprint-datetime.css, blueprint.css (both
# checked in under resources/public/css/), 2_woco.css, 3_style.css,
# 4_temp.css, page.css (checked in), 5_utilities.css — this script gathers
# the four numbered sheets plus the fonts/ and img/ trees that 3_style.css
# references relatively (../fonts, ../img).
set -eu
cd "$(dirname "$0")"

(cd ../../styles && bash build.sh dev)

cp -f ../../assets/css/2_woco.css \
      ../../assets/css/3_style.css \
      ../../assets/css/4_temp.css \
      ../../assets/css/5_utilities.css \
      resources/public/css/
rm -rf resources/public/fonts resources/public/img
cp -r ../../assets/fonts resources/public/
cp -r ../../assets/img resources/public/

echo "ui-base-overview assets gathered."
