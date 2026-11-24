#!/usr/bin/env bash
set -euo pipefail

# Build the server bundle's container images with plain `docker build`,
# tagged for use outside the compose harness (e.g. pushing to a registry).
# For the compose harness itself, use full mode instead:
#   docker compose -f docker-compose.yml -f docker-compose.full.yml up --build

usage() {
    cat <<EOF
Usage: $(basename "$0") [backend|frontend|all] [--push]

Builds the server bundle images from bundles/server/Dockerfile with the
repository root as build context.

Targets:
  backend     JRE + uberjar image (Dockerfile target: backend)
  frontend    nginx image serving the static frontend (Dockerfile target: app-frontend)
  all         both images (default)

Options:
  --push      push both tags of each built image after building
  -h, --help  show this help

Environment:
  IMAGE_PREFIX  image name prefix (default: explorama/server)
  TAG           image tag (default: short git SHA, with -dirty suffix when
                the working tree has uncommitted changes); latest is always
                tagged as well
EOF
}

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
repo_root="$(cd "$script_dir/../.." && pwd)"

target="all"
push=false

while [ $# -gt 0 ]; do
    case "$1" in
        backend|frontend|all) target="$1" ;;
        --push) push=true ;;
        -h|--help) usage; exit 0 ;;
        *) echo "Unknown argument: $1" >&2; echo "" >&2; usage >&2; exit 1 ;;
    esac
    shift
done

if ! command -v docker >/dev/null 2>&1; then
    echo "docker is not installed or not on PATH" >&2
    exit 1
fi

IMAGE_PREFIX="${IMAGE_PREFIX:-explorama/server}"
if [ -z "${TAG:-}" ]; then
    TAG="$(git -C "$repo_root" rev-parse --short HEAD)"
    if [ -n "$(git -C "$repo_root" status --porcelain)" ]; then
        TAG="$TAG-dirty"
    fi
fi

build_image() {
    image_name="$IMAGE_PREFIX-$1"
    docker_target="$2"

    echo ""
    echo "Building $image_name:$TAG (Dockerfile target: $docker_target)..."
    docker build \
        -f "$repo_root/bundles/server/Dockerfile" \
        --target "$docker_target" \
        -t "$image_name:$TAG" \
        -t "$image_name:latest" \
        "$repo_root"

    if [ "$push" = true ]; then
        echo ""
        echo "Pushing $image_name..."
        docker push "$image_name:$TAG"
        docker push "$image_name:latest"
    fi
}

case "$target" in
    backend) build_image backend backend ;;
    frontend) build_image frontend app-frontend ;;
    all)
        build_image backend backend
        build_image frontend app-frontend
        ;;
esac

echo ""
echo "Done."
