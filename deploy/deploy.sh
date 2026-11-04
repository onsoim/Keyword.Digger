#!/usr/bin/env bash

set -Eeuo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

RUNTIME_DIR="${KEYWORD_DIGGER_RUNTIME_DIR:-/volume1/docker/keyword-digger/runtime}"
ENV_FILE="$RUNTIME_DIR/deploy.env"
COMPOSE_FILE="$SCRIPT_DIR/compose.yaml"

if [[ ! -f "$ENV_FILE" ]]; then
    echo "Deployment configuration not found: $ENV_FILE"
    exit 1
fi

set -a
source "$ENV_FILE"
set +a

cd "$REPO_ROOT"
