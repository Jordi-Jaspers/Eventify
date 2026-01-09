#!/bin/bash
set -e

readonly SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
readonly PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
readonly CLIENT_DIR="$PROJECT_ROOT/client"
readonly SERVER_DIR="$PROJECT_ROOT/server"
readonly HEALTH_URL="http://localhost:8080/api/v1/public/actuator/health"

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

section "Database Reset"
"$SCRIPT_DIR/database-reset.sh"

section "Backend Server"
./gradlew bootRun --console=plain > /tmp/backend-test.log 2>&1 &
BACKEND_PID=$!
progress_wait
info "Backend running (PID: $BACKEND_PID)"

section "Playwright Tests"
cd "$CLIENT_DIR"
echo ""
bun run playwright test "$@"
