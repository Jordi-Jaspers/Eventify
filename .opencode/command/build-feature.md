---
description: Build a feature using test-driven orchestrator workflow. Checks backlog.md, gathers requirements, creates plan, executes with specialized agents.
argument-hint: <feature-description> / <feature-name-keyword>
model: github-copilot/claude-opus-4.5
tools:
  write: true
  read: true
  bash: true
  grep: true
  glob: true
  list: true
  webfetch: true
---

# Build Feature Command

Trigger orchestrator to build a feature using test-driven workflow with specialized agents.

## Workflow

**User provides:** Refined story name in $ARGUMENTS

**Orchestrator executes:**

1. **Check Refined backlog** - Look for existing feature story in `.opencode/jira/refined/` and notes in `.opencode/jira/backlog.md` if not found
2. **Gather requirements** - Ask clarifying questions if needed
3. **Create plan** - Write implementation plan with agent assignments
4. **Get approval** - Wait for user confirmation
5. **Execute TDD workflow** - Delegate to specialized agents in test-first order
6. **Update changelog** - Record feature addition, remove existing story if applicable

## Orchestrator Instructions

You are orchestrating a feature build. Follow this process:

### Step 1: Check Existing Context

```bash
# List all refined stories
ls .opencode/jira/refined

# Story information
cat .opencode/jira/refined/"[FEATURE-NAME-KEYWORD]"

# Check backlog.md for feature information
cat .opencode/jira/backlog.md
```

Look for:
- Feature description
- Requirements notes
- Technical decisions
- Open questions

### Step 2: Gather Requirements

If unclear, ask targeted questions:

**Functional:** User story, use cases, edge cases, error handling
**Technical:** Components, database changes, API endpoints, security
**Frontend:** Pages needed, interactions, data display

### Step 3: Create Plan + Approval Gate

```markdown
# Feature: [Name]

## Requirements Summary
- [Requirement 1]
- [Requirement 2]

## Technical Approach

### Backend
- Endpoints: [list]
- Services: [list]
- Database: [migrations]
- Security: [auth, validation]

### Frontend
- Pages: [list]
- Components: [list]
- Test file: `test/components/[page].spec.ts`

## Execution Order

| Phase | Agent | Task |
|-------|-------|------|
| 1 | java-testing-agent | Create test suite |
| 2 | java-backend-agent | Implement to pass tests |
| 3 | sveltekit-frontend-agent | Build UI + screenshot tests |
| 4 | ui-validator | Polish visuals (10 iterations) |

## Success Criteria
✅ All tests passing (>90% coverage)
✅ Build successful
✅ Type checks passing (frontend)
✅ Security requirements met
✅ Feature works as specified

## Estimated Effort
[Your time estimate]
```

Present plan clearly:

```
---
🎯 FEATURE PLAN READY

[Show plan summary here]

❓ APPROVAL REQUIRED

Review the plan above. Does it:
- ✅ Capture all requirements?
- ✅ Have proper test-first approach?
- ✅ Include necessary security?
- ✅ Sequence tasks correctly?

Reply "approved" to proceed, or suggest changes.
---
```

**STOP and WAIT for approval.** Do not proceed without explicit confirmation.

### Step 4: Execute TDD Workflow

```
Phase 1: Tests
─────────────────────────────
Calling java-testing-agent...
Result: ✅ Tests created

Phase 2: Backend
─────────────────────────────
Calling java-backend-agent...
Result: ✅ All tests passing

Phase 2.5: Backend Review
─────────────────────────────
Waiting for user review...
❓ Reply "approved" to proceed to frontend
   Or: "changes: [list changes]" to request fixes

Phase 3: Frontend
─────────────────────────────
Calling sveltekit-frontend-agent...
Result: ✅ Build passing
        Page: [page-name]
        Test: test/components/[page].spec.ts
```

### Step 5: UI Polish Approval Gate

**After frontend completes:**

```
📋 FRONTEND COMPLETE - UI POLISH READY

**Page:** [page name]
**Test file:** `test/components/[page].spec.ts`
**Iterations:** 10

The UI Agent will polish visuals only (no logic changes).

❓ Reply "approved" to start UI validation loop
   Or: "approved 15" for more iterations
   Or: "skip" to skip
```

**STOP. Wait for response.**

### Step 6: Run UI Validation Loop

On approval:

```bash
./.opencode/scripts/ralph-loop.sh [page] test/components/[page].spec.ts 10
```

**Results:**

| Output | Action |
|--------|--------|
| `UI_VALIDATION_COMPLETE` | Continue to changelog |
| `UI_VALIDATION_BLOCKED` | Report blocker, may need frontend fix |
| Max iterations | Ask user: more iterations or accept |

### Step 7: Update Changelog + Report

1. Create `.opencode/jira/completed/YYYYMMDD-EPIC-feature.md`
2. Update `.opencode/jira/CHANGELOG.md`
3. Delete story from `.opencode/jira/refined/` if exists

Report:

```
✅ FEATURE COMPLETE: [Name]

Backend: [summary]
Frontend: [summary]
UI Polish: [X] iterations
Tests: [X] passing

Files: [list]
```

## Agent Task Formats

### java-testing-agent
```
COMPONENT: [Class/feature]
REQUIREMENTS: [Behavior to test]
SECURITY: [Constraints]
EDGE_CASES: [Known cases]
CONTEXT: [Dependencies]
```

### java-backend-agent
```
FEATURE: [What to build]
REQUIREMENTS: [Business logic]
TESTS: [Test file paths]
SECURITY: [Auth, validation]
DATABASE: [Schema changes]
CONTEXT: [Related components]
```

### sveltekit-frontend-agent
```
FEATURE: [What to build]
REQUIREMENTS: [Interactions, data display]
API_ENDPOINTS: [Backend endpoints]
USE_DATATABLE: [Yes/No]
AUTH: [Requirements]
CONTEXT: [Related components]

REQUIRED OUTPUT:
- Page name
- Test file: test/components/[page].spec.ts
```

### ui-validation (via script)

```bash
./.opencode/scripts/ralph-loop.sh [page] [test-file] [iterations]
```

NOT called directly as subagent.

## Example: Refined Story Found

```
User: /build-feature API-KEY-MANAGEMENT

Orchestrator:
1. Finds .opencode/jira/refined/API-KEY-MANAGEMENT.md
2. Creates plan
3. "📋 PLAN READY... Reply approved"
4. User: "approved"
5. java-testing-agent → tests
6. java-backend-agent → implementation
7. sveltekit-frontend-agent → UI (returns: Page: api-keys, Test: test/components/api-keys.spec.ts)
8. "📋 UI POLISH READY... Reply approved"
9. User: "approved"
10. ralph-loop.sh api-keys test/components/api-keys.spec.ts 10
11. UI_VALIDATION_COMPLETE after 6 iterations
12. Updates changelog, reports completion
```

## Example: No Refined Story

```
User: /build-feature password reset

Orchestrator:
1. Checks backlog.md, finds note
2. Asks: "Token expiry? Rate limiting?"
3. User answers
4. Creates plan
5. "📋 PLAN READY... Reply approved"
6. User: "approved"
7. [TDD workflow]
8. "📋 UI POLISH READY... Reply approved"
9. User: "approved"
10. [UI validation loop]
11. Reports completion
```

## Example: Skip UI Polish

```
User: /build-feature quick-fix

[... TDD workflow ...]

Orchestrator: "📋 UI POLISH READY... Reply approved or skip"
User: "skip"
Orchestrator: Proceeds to changelog, skips UI loop
```

## Critical Reminders

1. **Check Refined AND backlog.md first** - Don't ask questions already answered
2. **TDD always** - Tests before implementation
3. **Two approval gates** - Plan AND UI polish
4. **Structured context to agents** - Use task format above
5. **Clear execution log** - Show what each agent did
4. **Frontend must output** - Page name + test file path
6. **Orchestrator thinks, agents execute** - Don't delegate planning

## What NOT to Do

❌ Don't create separate specification files (orchestrator plans in-chat)
❌ Don't skip approval gate
❌ Don't implement before tests
❌ Don't delegate planning to agents (orchestrator plans, agents execute)
❌ Don't Call ui-validator directly (use ralph-loop.sh)
❌ Don't Skip frontend's page/test output requirement

In all interactions, be extremely concise and sacrifice grammar for concision.
