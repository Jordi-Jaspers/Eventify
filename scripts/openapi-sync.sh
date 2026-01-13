#!/bin/bash
set -e

readonly SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
readonly PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
readonly CLIENT_DIR="$PROJECT_ROOT/client"
readonly SERVER_DIR="$PROJECT_ROOT/server"
readonly OPENAPI_FILE="$SERVER_DIR/openapi.json"
readonly HEALTH_URL="http://localhost:8080/api/v1/public/actuator/health"
readonly DOCS_URL="http://localhost:8080/api/v1/public/docs"

# Colors
readonly GREEN='\033[0;32m'
readonly BLUE='\033[0;94m'
readonly CYAN='\033[0;96m'
readonly BOLD='\033[1m'
readonly RESET='\033[0m'

BACKEND_PID=""
trap '[ -n "$BACKEND_PID" ] && kill "$BACKEND_PID" 2>/dev/null' EXIT

section() { echo -e "\n${BOLD}${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${RESET}"; echo -e "${BOLD}  $1${RESET}"; echo -e "${BOLD}${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${RESET}\n"; }
step() { printf "  ${CYAN}▸${RESET} %-50s" "$1"; }
ok() { echo -e "${GREEN}✓${RESET}"; }
info() { echo -e "  ${CYAN}ℹ${RESET} $1"; }

progress_wait() {
  local chars="⣾⣽⣻⢿⡿⣟⣯⣷"
  local i=0
  until curl -sf "$HEALTH_URL" > /dev/null 2>&1; do
    printf "\r  ${CYAN}▸${RESET} %-50s${CYAN}%s${RESET}" "Starting backend" "${chars:$i:1}"
    i=$(( (i + 1) % ${#chars} ))
    sleep 0.2
  done
  printf "\r  ${CYAN}▸${RESET} %-50s${GREEN}✓${RESET}\n" "Starting backend"
}

section "Stopping Gradle daemons"
cd "$SERVER_DIR"
step "Stopping daemons"; ./gradlew --stop > /dev/null 2>&1; ok

section "Cleanup"
if [ -f "$OPENAPI_FILE" ]; then
    step "Removing old openapi.json"; rm "$OPENAPI_FILE"; ok
fi

section "Backend Server"
./gradlew bootRun --console=plain > /tmp/openapi-sync-backend.log 2>&1 &
BACKEND_PID=$!
progress_wait
info "Backend running (PID: $BACKEND_PID)"

section "Downloading OpenAPI Spec"
step "Fetching from API"
curl -sf "$DOCS_URL" -o "$OPENAPI_FILE" && ok
info "Saved to $OPENAPI_FILE"

section "Generating TypeScript Types"
cd "$CLIENT_DIR"
step "Running openapi-typescript"
bun run generate:api > /dev/null 2>&1 && ok
info "Types generated in src/lib/types/api.d.ts"

section "Complete"
info "OpenAPI spec downloaded and TypeScript types generated"
info "Run 'bun run check' to verify types"
