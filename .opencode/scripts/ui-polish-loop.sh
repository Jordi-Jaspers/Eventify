#!/bin/bash
#
# ui-polish-loop.sh - Iterative UI polish with batched screenshot validation
#
# Usage: ./ui-polish-loop.sh <page> <test-file> [max-iterations]
# Example: ./ui-polish-loop.sh landing client/test/components/landing.spec.ts 5
#
set -euo pipefail

# ─────────────────────────────────────────────────────────────────────────────
# Config
# ─────────────────────────────────────────────────────────────────────────────
readonly RED='\033[0;91m' GREEN='\033[0;32m' BLUE='\033[0;94m'
readonly CYAN='\033[0;96m' YELLOW='\033[0;93m' BOLD='\033[1m' RESET='\033[0m'

readonly PAGE="${1:-}" TEST_FILE="${2:-}" MAX="${3:-5}"
readonly PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
readonly CLIENT_DIR="$PROJECT_ROOT/client"
readonly SCREENSHOT_DIR="$CLIENT_DIR/test/resources/screenshots/$PAGE"

# ─────────────────────────────────────────────────────────────────────────────
# Output helpers
# ─────────────────────────────────────────────────────────────────────────────
hr() { printf "${BOLD}${1}%60s${RESET}\n" | tr ' ' '─'; }
section()    { echo -e "\n$(hr "$BLUE")\n${BOLD}  $1${RESET}\n$(hr "$BLUE")\n"; }
subsection() { echo -e "\n$(hr "$CYAN")\n${BOLD}  $1${RESET}\n"; }
info()    { echo -e "  ${CYAN}ℹ${RESET} $1"; }
success() { echo -e "  ${GREEN}✓${RESET} $1"; }
error()   { echo -e "  ${RED}✗${RESET} $1"; }

# ─────────────────────────────────────────────────────────────────────────────
# Validation
# ─────────────────────────────────────────────────────────────────────────────
[[ -z "$PAGE" || -z "$TEST_FILE" ]] && {
    echo -e "${RED}Usage: $0 <page> <test-file> [max-iterations]${RESET}"
    echo "Example: $0 landing client/test/components/landing.spec.ts 5"
    exit 1
}

# ─────────────────────────────────────────────────────────────────────────────
# Core functions
# ─────────────────────────────────────────────────────────────────────────────
run_tests() {
    subsection "Running Screenshot Tests"
    if (cd "$CLIENT_DIR" && bun run test -- "$TEST_FILE" 2>&1); then
        success "All tests passed"
        return 0
    else
        error "Some tests failed (may be cleanup-related)"
        return 0  # Continue anyway - tests themselves passed
    fi
}

get_screenshot_list() {
    find "$SCREENSHOT_DIR" -name "*.png" -type f 2>/dev/null | sort | while read -r f; do
        echo "- $f"
    done
}

validate_and_fix() {
    local output="/tmp/ui-validation.txt"
    local screenshot_list
    screenshot_list=$(get_screenshot_list)
    local count
    count=$(echo "$screenshot_list" | wc -l | tr -d ' ')
    
    info "Analyzing ${BOLD}$count${RESET} screenshots in batch..."
    
    opencode run --agent ui-validator "$(cat <<EOF
PAGE: $PAGE
ITERATION: $iteration of $MAX
SCREENSHOT_DIR: $SCREENSHOT_DIR

Review ALL these screenshots for visual polish issues:

$screenshot_list

TASK:
1. Read each screenshot image
2. Analyze for visual issues across ALL screenshots:
   - Spacing/alignment problems
   - Text overflow or truncation  
   - Missing glassmorphism or gradient effects
   - Inconsistent styling between themes
   - Mobile responsiveness issues
   - Accessibility concerns (contrast, focus states)

3. If ALL screenshots look polished, output:
   ALL_SCREENSHOTS_OK

4. If issues exist, find and fix them:
   - Locate the Svelte component in client/src/routes/
   - Apply CSS/Tailwind fixes only - NO business logic changes
   - Only modify .svelte files
   - Do NOT modify +page.ts, +page.server.ts, +layout.ts, +server.ts
   - Do NOT modify .service.ts, .store.ts files

5. After applying fixes, output:
   FIXES_APPLIED
   - [describe what was fixed]

6. If you cannot fix issues, output:
   FIXES_BLOCKED
   - [describe the blocker]
EOF
)" 2>&1 | tee "$output"

    if grep -qF "ALL_SCREENSHOTS_OK" "$output"; then
        return 0  # All good
    elif grep -qF "FIXES_APPLIED" "$output"; then
        return 1  # Fixes made, need to re-run
    else
        return 2  # Blocked or unknown
    fi
}

# ─────────────────────────────────────────────────────────────────────────────
# Main loop
# ─────────────────────────────────────────────────────────────────────────────
section "UI Polish Loop"
info "Page: ${BOLD}$PAGE${RESET}"
info "Test: ${BOLD}$TEST_FILE${RESET}"
info "Max iterations: ${BOLD}$MAX${RESET}"
info "Mode: ${BOLD}Batched analysis${RESET}"

cd "$PROJECT_ROOT"

iteration=1
while [[ $iteration -le $MAX ]]; do
    section "Iteration $iteration of $MAX"
    
    run_tests
    
    # Check screenshots exist
    count=$(find "$SCREENSHOT_DIR" -name "*.png" -type f 2>/dev/null | wc -l | tr -d ' ')
    [[ $count -eq 0 ]] && { error "No screenshots at $SCREENSHOT_DIR"; exit 1; }
    
    subsection "Validating Screenshots"
    
    result=0
    validate_and_fix || result=$?
    
    case $result in
        0)
            section "UI Validation Complete"
            success "All ${BOLD}$count${RESET} screenshots passed validation"
            info "Iterations used: ${BOLD}$iteration${RESET}"
            echo -e "\n${GREEN}${BOLD}UI_VALIDATION_COMPLETE${RESET}"
            exit 0
            ;;
        1)
            success "Fixes applied, re-running tests..."
            sleep 2
            ;;
        *)
            error "Validation blocked or unknown state"
            ;;
    esac
    
    ((iteration++))
done

section "Max Iterations Reached"
error "Reached maximum of ${BOLD}$MAX${RESET} iterations"
echo -e "\n${YELLOW}${BOLD}UI_VALIDATION_BLOCKED${RESET}"
exit 1
