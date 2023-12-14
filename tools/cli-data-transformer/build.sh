#!/bin/bash
set -eu

UBER_JAR_NAME=builder

#./dl_country_mapping.sh

lein uberjar

rm -rf target/classes
rm -rf target/stale
rm -rf target/$UBER_JAR_NAME-*.jar
rm -f target/repl-port

cp builder.sh target/
#mv countries.edn target/