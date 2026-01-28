#!/bin/bash
set -e

source "$(dirname "${BASH_SOURCE[0]}")/common.sh"

setup_backend_trap

section "Stopping Gradle daemons"
stop_gradle_daemons

section "Database Reset"
"$SCRIPT_DIR/database-reset.sh"

section "Backend Server"
start_backend

section "Playwright Tests"
cd "$CLIENT_DIR"
echo ""
bun run playwright test "$@"

section "Tests Complete (Cleanup)"
stop_gradle_daemons
success_banner "Playwright Tests Completed Successfully!"
exit 0
