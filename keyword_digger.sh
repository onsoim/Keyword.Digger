#!/usr/bin/env bash

set -Eeuo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
FRONTEND_DIR="$ROOT_DIR/frontend"
BACKEND_DIR="$ROOT_DIR/backend"

if command -v npm >/dev/null 2>&1; then
    NPM_CMD="npm"
elif command -v npm.cmd >/dev/null 2>&1; then
    NPM_CMD="npm.cmd"
else
    echo "Error: npm is not installed or not available in PATH."
    exit 1
fi

run_npm() {
    (
        cd "$FRONTEND_DIR"
        "$NPM_CMD" "$@"
    )
}

run_gradle() {
    (
        cd "$BACKEND_DIR"
        bash ./gradlew "$@"
    )
}

show_help() {
    cat << 'EOF'

Keyword Digger CLI

Usage:
    ./keyword_digger.sh <command>

Development:
    frontend    Start React + Vite
    backend     Start Sprint Boot

Quality:
    lint        Run frontend Oxlint
    test        Run backend tests
    live-test   Run Google Trends live integration test
    build       Run frontend and backend
    check       Run lint, tests and build

Dependencies:
    install     Install frontend dependencies

Maintenance:
    clean       Remove build outputs
    help        Show this help

Examples:
    ./keyword_digger.sh frontend
    ./keyword_digger.sh backend
    ./keyword_digger.sh check

EOF
}

case "${1:-help}" in

    frontend)
        run_npm run dev
        ;;
    
    backend)
        run_gradle bootRun
        ;;
    
    lint)
        run_npm run lint
        ;;

    test)
        run_gradle test
        ;;
    
    build)
        run_npm run build
        run_gradle bootJar
        ;;

    check)
        run_gradle test
        run_npm run lint
        run_gradle bootJar
        run_npm run build

        echo "All checks passed."
        ;;

    install)
        run_npm ci
        ;;

    live-test)
        run_gradle liveTest
        ;;
    
    clean)
        run_gradle clean

        rm -rf "$FRONTEND_DIR/dist"
        rm -rf "$FRONTEND_DIR/node_modules/.tmp"

        echo "Build outputs cleaned."
        ;;
    
    help)
        show_help
        ;;

    *)
        echo "Unknown command: $1"
        show_help
        exit 1
        ;;

esac
