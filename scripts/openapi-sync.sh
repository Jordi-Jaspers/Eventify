#!/bin/bash
set -e

source "$(dirname "${BASH_SOURCE[0]}")/common.sh"

readonly OPENAPI_FILE="$SERVER_DIR/openapi.json"
readonly DOCS_URL="http://localhost:8080/api/v1/public/docs"

setup_backend_trap

section "Stopping Gradle daemons"
stop_gradle_daemons

section "Cleanup"
if [ -f "$OPENAPI_FILE" ]; then
    step "Removing old openapi.json"; rm "$OPENAPI_FILE"; ok
fi

section "Backend Server"
start_backend

section "Downloading OpenAPI Spec"
step "Fetching from API"
curl -sf "$DOCS_URL" -o "$OPENAPI_FILE" && ok
info "Saved to $OPENAPI_FILE"

section "Generating TypeScript Types"
cd "$CLIENT_DIR"
step "Running openapi-typescript"
bun run generate:api > /dev/null 2>&1 && ok
info "Types generated in src/lib/types/api.d.ts"

section "Complete (Cleanup)"
stop_gradle_daemons
success_banner "OpenAPI Sync Completed Successfully!"
info "Run 'bun run check' to verify types"
exit 0
