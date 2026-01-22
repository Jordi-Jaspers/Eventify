#!/bin/bash
set -e

readonly SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
readonly PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
readonly CLIENT_DIR="$PROJECT_ROOT/client"
readonly SERVER_DIR="$PROJECT_ROOT/server"
readonly HEALTH_URL="http://localhost:8080/api/v1/public/actuator/health"

# Colors
readonly GREEN='\033[0;32m'
readonly RED='\033[0;31m'
readonly BLUE='\033[0;94m'
readonly CYAN='\033[0;96m'
readonly BOLD='\033[1m'
readonly RESET='\033[0m'

# Backend startup timeout in seconds
readonly BACKEND_TIMEOUT=120

BACKEND_PID=""
trap '[ -n "$BACKEND_PID" ] && kill "$BACKEND_PID" 2>/dev/null' EXIT

section() { echo -e "\n${BOLD}${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${RESET}"; echo -e "${BOLD}  $1${RESET}"; echo -e "${BOLD}${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${RESET}\n"; }
step() { printf "  ${CYAN}▸${RESET} %-50s" "$1"; }
ok() { echo -e "${GREEN}✓${RESET}"; }
info() { echo -e "  ${CYAN}ℹ${RESET} $1"; }

backend_failed() {
  printf "\r  ${CYAN}▸${RESET} %-50s${RED}✗${RESET}\n" "Starting backend"
  echo -e "  ${RED}✗${RESET} $1"
  echo -e "\n  ${RED}Last 30 lines of backend log:${RESET}"
  tail -30 /tmp/backend-test.log | sed 's/^/  /'
  exit 1
}

progress_wait() {
  local chars="⣾⣽⣻⢿⡿⣟⣯⣷" i=0 elapsed=0
  while ! curl -sf "$HEALTH_URL" >/dev/null 2>&1; do
    kill -0 "$BACKEND_PID" 2>/dev/null || backend_failed "Backend process died unexpectedly"
    ((elapsed >= BACKEND_TIMEOUT)) && backend_failed "Backend failed to start within ${BACKEND_TIMEOUT}s"
    printf "\r  ${CYAN}▸${RESET} %-50s${CYAN}%s${RESET}" "Starting backend (${elapsed}s)" "${chars:i++%8:1}"
    sleep 1 && ((elapsed++))
  done
  printf "\r  ${CYAN}▸${RESET} %-50s${GREEN}✓${RESET}\n" "Backend ready (${elapsed}s)"
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

section "Tests Complete (Cleanup)"
./gradlew --stop > /dev/null 2>&1; ok
echo -e "${GREEN}${BOLD}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${RESET}"
echo -e "${GREEN}${BOLD}  ✅ Playwright Tests Completed Successfully!${RESET}"
echo -e "${GREEN}${BOLD}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${RESET}"
exit 0
