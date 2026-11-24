#!/usr/bin/env bash
set -euo pipefail

# Build release artifacts for the server bundle and the CLI data transformer,
# and collect them in one output directory.
#
# The browser bundle has bundles/browser/build.sh and the electron app has
# bundles/electron's build-linux / build-win targets; those are not driven from
# here (electron packaging is tracked in issue #28).

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" &>/dev/null && pwd)"
ROOT_DIR="$(cd -- "$SCRIPT_DIR/../.." &>/dev/null && pwd)"
OUT_DIR="$ROOT_DIR/dist/release"

usage() {
    cat <<EOF
Usage: $(basename "$0") [server|cli|all] [--out DIR]

Targets:
  server      frontend assets + backend uberjar for bundles/server
  cli         uberjar + launcher for tools/cli-data-transformer
  all         both (default)

Options:
  --out DIR   collect artifacts here (default: dist/release)
  -h, --help  show this help

Artifacts:
  server/explorama-standalone.jar   run with
                                    java -jar explorama-standalone.jar \\
                                      -m de.explorama.backend.woco.app.server
  server/public/                    static frontend, serve with any web server
  cli/builder.jar, cli/builder.sh   run with ./builder.sh <command> <args>
EOF
}

TARGET="all"
while [ $# -gt 0 ]; do
    case "$1" in
        server | cli | all)
            TARGET="$1"
            shift
            ;;
        --out)
            [ $# -ge 2 ] || {
                echo "--out needs a directory" >&2
                exit 1
            }
            OUT_DIR="$2"
            shift 2
            ;;
        -h | --help)
            usage
            exit 0
            ;;
        *)
            echo "Unknown argument: $1" >&2
            usage >&2
            exit 1
            ;;
    esac
done

# build_cli changes directory, so resolve --out before any build runs.
mkdir -p "$OUT_DIR"
OUT_DIR="$(cd -- "$OUT_DIR" &>/dev/null && pwd)"

require() {
    command -v "$1" >/dev/null 2>&1 || {
        echo "Missing required tool: $1" >&2
        exit 1
    }
}

banner() {
    echo ""
    echo "================================================"
    echo "$1"
    echo "================================================"
}

build_server() {
    banner "Building server bundle"
    require npm
    require bb
    require clojure
    "$ROOT_DIR/bundles/server/build.sh"

    mkdir -p "$OUT_DIR/server"
    cp "$ROOT_DIR/bundles/server/target/explorama-standalone.jar" "$OUT_DIR/server/"
    rm -rf "$OUT_DIR/server/public"
    cp -r "$ROOT_DIR/bundles/server/resources/public" "$OUT_DIR/server/public"
}

build_cli() {
    banner "Building cli-data-transformer"
    require clojure
    cd "$ROOT_DIR/tools/cli-data-transformer"
    rm -rf target
    clojure -M:uberjar
    # builder.sh resolves the jar next to itself, so ship them as a pair.
    cp builder.sh target/builder.sh
    chmod +x target/builder.sh

    mkdir -p "$OUT_DIR/cli"
    cp target/builder.jar target/builder.sh "$OUT_DIR/cli/"
}

require java

case "$TARGET" in
    server) build_server ;;
    cli) build_cli ;;
    all)
        build_server
        build_cli
        ;;
esac

banner "Release artifacts in $OUT_DIR"
find "$OUT_DIR" -maxdepth 2 -mindepth 1 -printf "  %P\n" | sort
