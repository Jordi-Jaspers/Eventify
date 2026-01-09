#!/bin/bash
#
# ralph-loop.sh - UI validation loop for OpenCode
#
# Usage: ./ralph-loop.sh <page> <test-file> [max-iterations]
# Example: ./ralph-loop.sh dashboard test/components/dashboard.spec.ts 10
#
set -euo pipefail

PAGE="${1:-}"
TEST_FILE="${2:-}"
MAX="${3:-10}"
TEMP_OUTPUT=$(mktemp)

cleanup() {
    rm -f "$TEMP_OUTPUT"
}
trap cleanup EXIT

if [[ -z "$PAGE" || -z "$TEST_FILE" ]]; then
    echo "Usage: ./ralph-loop.sh <page> <test-file> [max-iterations]"
    echo "Example: ./ralph-loop.sh dashboard test/components/dashboard.spec.ts 10"
    exit 1
fi

echo ""
echo "🍩 UI Validation Loop"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "   Page: $PAGE"
echo "   Test: $TEST_FILE"
echo "   Max iterations: $MAX"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

for ((i=1; i<=MAX; i++)); do
    echo ""
    echo "═══════════════════════════════════"
    echo "  ITERATION $i of $MAX"
    echo "═══════════════════════════════════"
    echo ""
    
    PROMPT="PAGE: $PAGE
TEST_FILE: $TEST_FILE
ITERATION: $i of $MAX

Run the test, read the screenshots, polish the UI. Output UI_VALIDATION_COMPLETE when done."

    echo "⏳ Running UI Agent... (streaming output)"
    echo "───────────────────────────────────"
    
    # Stream output to terminal AND capture to file for checking
    opencode run --agent ui-validator "$PROMPT" 2>&1 | tee "$TEMP_OUTPUT" || true
    
    echo ""
    echo "───────────────────────────────────"

    # Check for completion signals
    if grep -qF "UI_VALIDATION_COMPLETE" "$TEMP_OUTPUT"; then
        echo ""
        echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        echo "✅ UI Validation Complete"
        echo "   Iterations: $i"
        echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        exit 0
    fi
    
    if grep -qF "UI_VALIDATION_BLOCKED" "$TEMP_OUTPUT"; then
        echo ""
        echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        echo "⚠️  UI Validation Blocked"
        echo "   Check output above for details"
        echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        exit 1
    fi
    
    if [[ $i -lt $MAX ]]; then
        echo ""
        echo "⏳ Cooling down (3s) before next iteration..."
        sleep 3
    fi
done

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "⚠️  Max iterations ($MAX) reached"
echo "   UI may need manual review"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
exit 1
