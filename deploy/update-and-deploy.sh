#!/usr/bin/env bash

set -Eeuo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

RUNTIME_DIR="${KEYWORD_DIGGER_RUNTIME_DIR:-/volume1/docker/keyword-digger/runtime}"
ENV_FILE="$RUNTIME_DIR/deploy.env"
DEPLOYED_SHA_FILE="$RUNTIME_DIR/deployed.sha"
LOCK_DIR="$RUNTIME_DIR/update.lock"

mkdir -p "$RUNTIME_DIR"

if [[ ! -f "$ENV_FILE" ]]; then
    echo "Deployment configuration not found: $ENV_FILE"
    exit 1
fi

set -a
source "$ENV_FILE"
set +a

DEPLOY_BRANCH="${DEPLOY_BRANCH:-develop}"

if ! mkdir "$LOCK_DIR" 2>/dev/null; then
    echo "Another deployment is running. Skipping."
    exit 0
fi

trap 'rmdir "$LOCK_DIR" 2>/dev/null || true' EXIT

cd "$REPO_ROOT"

CURRENT_BRANCH="$(git branch --show-current)"

if [[ "$CURRENT_BRANCH" != "$DEPLOY_BRANCH" ]]; then
    echo "Unexpected branch: $CURRENT_BRANCH"
    exit 1
fi

if [[ -n "$(git status --porcelain)" ]]; then
    echo "Working tree is not clean. Deployment stopped."
    exit 1
fi

git fetch origin "$DEPLOY_BRANCH"

LOCAL_SHA="$(git rev-parse HEAD)"
REMOTE_SHA="$(git rev-parse FETCH_HEAD)"

if [[ "$LOCAL_SHA" != "$REMOTE_SHA" ]]; then

    if ! git merge-base --is-ancestor "$LOCAL_SHA" "$REMOTE_SHA"; then
        echo "Non fast-forward update detected. Deployment stopped."
        exit 1
    fi

    git merge --ff-only "$REMOTE_SHA"
fi

TARGET_SHA="$(git rev-parse HEAD)"

DEPLOYED_SHA=""

if [[ -s "$DEPLOYED_SHA_FILE" ]]; then
    IFS= read -r DEPLOYED_SHA < "$DEPLOYED_SHA_FILE" || true
fi

if [[ "$TARGET_SHA" == "$DEPLOYED_SHA" ]]; then

    FRONTEND_HEALTH_STATUS="$(docker inspect \
        --format='{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' \
        keyword-digger-frontend 2>/dev/null || true)"
    
    BACKEND_HEALTH_STATUS="$(docker inspect \
        --format='{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' \
        keyword-digger-backend 2>/dev/null || true)"
    
    if [[   "$FRONTEND_HEALTH_STATUS" == "healthy" &&
            "$BACKEND_HEALTH_STATUS" == "healthy" ]]; then
        echo "No changes detected. Application is healthy."
        exit 0
    fi

    echo "Application is not healthy. Re-deploying current revision"
    echo "Backend: $BACKEND_HEALTH_STATUS"
    echo "Frontend: $FRONTEND_HEALTH_STATUS"
fi

echo "Deploying $TARGET_SHA"

if bash "$SCRIPT_DIR/deploy.sh"; then

    TEMP_FILE="${DEPLOYED_SHA_FILE}.tmp"

    printf '%s\n' "$TARGET_SHA" > "$TEMP_FILE"
    mv "$TEMP_FILE" "$DEPLOYED_SHA_FILE"

    echo "Deployed completed: $TARGET_SHA"

else
    echo "Deployed failed. Previous deployed SHA is preserved."
    exit 1
fi
