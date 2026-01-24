#!/bin/bash
set -euo pipefail

source "$(dirname "${BASH_SOURCE[0]}")/common.sh"

section "Running Code Quality Checks"
cd "$SERVER_DIR"
info "Running Gradle spotless, checkstyle, PMD and spotbugs tasks..."

if ! ./gradlew spotlessCheck checkQualityMain -xspotbugsMain; then
  echo ""
  fail
  echo -e "  ${RED}✗${RESET} Gradle code quality checks failed. Please fix the issues and try again."
  exit 1
fi

success_banner "Code Quality Checks Passed!"
exit 0
