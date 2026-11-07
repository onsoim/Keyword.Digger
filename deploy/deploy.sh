#!/usr/bin/env bash

set -Eeuo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

RUNTIME_DIR="${KEYWORD_DIGGER_RUNTIME_DIR:-/volume1/docker/keyword-digger/runtime}"
ENV_FILE="$RUNTIME_DIR/deploy.env"
DEPLOYED_SHA_FILE="$RUNTIME_DIR/deployed.sha"
COMPOSE_FILE="$SCRIPT_DIR/compose.yaml"

if [[ ! -f "$ENV_FILE" ]]; then
    echo "Deployment configuration not found: $ENV_FILE"
    exit 1
fi

set -a
source "$ENV_FILE"
set +a

CURRENT_SHA="$(git -C "$REPO_ROOT" rev-parse HEAD)"

PREVIOUS_SHA=""

if [[ -s "$DEPLOYED_SHA_FILE" ]]; then
    IFS= read -r PREVIOUS_SHA < "$DEPLOYED_SHA_FILE" || true
fi

export IMAGE_TAG="$CURRENT_SHA"

compose() {
    docker compose \
        --env-file "$ENV_FILE" \
        -f "$COMPOSE_FILE" \
        "$@"
}

wait_for_health() {
    for ((i=1; i<=60; i++)); do
        STATUS="$(docker inspect \
            --format='{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' \
            keyword-digger 2>/dev/null || true)"

        if [[ "$STATUS" == "healthy" ]]; then
            echo "Application is healthy."
            return 0
        fi

        if [[   "$STATUS" == "unhealthy" ||
                "$STATUS" == "exited" ||
                "$STATUS" == "dead" ]]; then
                return 1
        fi
        
        sleep 2
    done

    return 1
}

show_logs() {
    compose logs --tail=100 app || true
}

rollback() {
    if [[ -z "$PREVIOUS_SHA" ]] || 
        ! docker image inspect "keyword-digger:$PREVIOUS_SHA" >/dev/null 2>&1; then
        echo "No previous image available for rollback"
        return 1
    fi

    echo "Rolling back to $PREVIOUS_SHA"

    export IMAGE_TAG="$PREVIOUS_SHA"

    if compose up -d --no-build --remove-orphans &&
        wait_for_health; then
        echo "Rollback successful."
        return 0
    fi

    echo "Rollback failed."
    show_logs
    return 1
}

cd "$REPO_ROOT"

echo "Building image for $CURRENT_SHA"

if ! compose build frontend backend; then
    echo "Docker build failed."
    exit 1
fi

echo "Starting application..."

if ! compose up -d --no-build --remove-orphans; then
    echo "Container startup failed."
    show_logs
    rollback || true
    exit 1
fi

echo "Waiting for health check..."

if wait_for_health; then
    echo "Deployment successful."
    exit 0
fi

echo "Health check failed."
show_logs

rollback || true

exit 1
