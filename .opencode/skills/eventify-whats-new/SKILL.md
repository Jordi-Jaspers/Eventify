# Eventify What's New Changelog Skill

## Overview

This skill defines when and how to update the user-facing "What's New" changelog at `server/src/main/resources/changelog.json`. This changelog powers the in-app changelog page (served via `GET /v1/public/changelog`) and the "new updates available" indicator in the sidebar.

## File Location

```
server/src/main/resources/changelog.json
```

## When to Update

### ✅ DO Update What's New For:

| Change Type | Examples |
|-------------|----------|
| **New Features** | New pages, new functionality, new API capabilities |
| **User-Visible Improvements** | UI enhancements, performance improvements users notice |
| **Bug Fixes** | Fixes that affected user experience |
| **New Components** | Reusable UI components (if noteworthy) |
| **Breaking Changes** | Anything users need to know about |

### ❌ DO NOT Update For:

| Change Type | Examples |
|-------------|----------|
| **Internal Refactoring** | Code reorganization with no user impact |
| **Test Changes** | Adding/updating tests only |
| **Documentation** | README, comments, completed stories |
| **Dev Tooling** | CI/CD, linting, build config |
| **Backend-Only Changes** | Unless they enable new user features |

## Data Structure

```json
{
  "version": "0.0.8",
  "date": "2026-02-13",
  "features": ["New capability"],
  "improvements": ["Enhancement"],
  "fixes": ["Bug fix"]
}
```

Fields: `version` (string), `date` (ISO date), `features` (string[]), `improvements` (string[]), `fixes` (string[]). All array fields are required (use empty array `[]` if none).

## How to Update

### 1. Determine the Category

| Category | Use When |
|----------|----------|
| `features` | Brand new functionality that didn't exist before |
| `improvements` | Enhancements to existing features, better UX, new components |
| `fixes` | Something was broken and is now fixed |

### 2. Add to Current Version or Create New

**Same day, same version:** Add to existing entry at top of array.

```json
{
  "version": "0.0.8",
  "date": "2026-02-13",
  "features": [
    "Existing feature",
    "Your new feature here"
  ],
  "improvements": [],
  "fixes": []
}
```

**New version:** Create new entry at TOP of array (index 0).

```json
[
  {
    "version": "0.0.9",
    "date": "2026-02-14",
    "features": ["Your new feature"],
    "improvements": [],
    "fixes": []
  },
  {
    "version": "0.0.8",
    ...
  }
]
```

### 3. Writing Good Changelog Entries

**DO:**
- Start with action verb (Add, Fix, Improve, Enable)
- Be specific about what changed
- Mention user benefit
- Keep under 80 characters

**DON'T:**
- Use technical jargon users won't understand
- Reference internal code/files
- Be vague ("Various improvements")

**Good Examples:**
```json
"features": [
  "Dashboard stats cards showing events today, active channels, error rate",
  "Organization watchlists with role-based access control",
  "Timeline monitoring page with live auto-refresh"
],
"improvements": [
  "Faster page load times on dashboard",
  "PulseIndicator component for consistent status animations"
],
"fixes": [
  "Monitor page now loads saved default filters on first visit",
  "Organization API keys no longer enforce personal quota limits"
]
```

**Bad Examples:**
```json
"features": [
  "Added DashboardStatsService",
  "New feature",
  "MONITOR-01 implementation"
],
"improvements": [
  "Refactored monitor.service.ts",
  "Various bug fixes"
]
```

## Version Numbering

This project uses semantic versioning for the changelog:

- **Major (X.0.0):** Breaking changes, major milestones
- **Minor (0.X.0):** New features, significant additions  
- **Patch (0.0.X):** Bug fixes, small improvements

**Current pattern:** Increment patch version for each release day with changes.

## Checklist

When completing a story, ask:

1. ☐ Does this change affect what users see or do?
2. ☐ Would a user want to know about this?
3. ☐ Is it a feature, improvement, or fix?
4. ☐ Is the entry clear and user-friendly?
5. ☐ Is it in the correct version (today's date)?

## Related Files

| File | Purpose |
|------|---------|
| `server/src/main/resources/changelog.json` | Changelog data (source of truth) |
| `server/src/main/java/io/github/eventify/api/changelog/` | Backend API (service + controller) |
| `client/src/routes/(authenticated)/changelog/+page.svelte` | Changelog page UI |
| `.opencode/CHANGELOG.md` | Developer changelog (different audience) |

## Example Workflow

After completing a user-facing feature:

```bash
# 1. Update What's New
# Edit: server/src/main/resources/changelog.json
# Add entry to appropriate category in current version

# 2. Verify build
cd server && ../gradlew build

# 3. Commit with feature (or separately)
git add server/src/main/resources/changelog.json
```
