# Changelog Page with Version Indicator

**Completed:** 2026-02-13
**Epic:** UI/UX
**Type:** Feature

## Summary

Added a "What's New" changelog page that displays version history with features, improvements, and bug fixes. Includes a sidebar indicator that shows when new updates are available since the user's last visit.

## Features

### Changelog Page (`/changelog`)
- Grouped version history from 0.0.0 to current version
- Each version shows: date, features, improvements, fixes
- Collapsible sections with smooth animations
- Badge counts for each category
- Glassmorphism card styling consistent with app design

### Version Tracking
- Stores last seen version in localStorage
- Compares against current app version
- Shows green pulse indicator in sidebar when new version available
- Clicking "What's New" marks current version as seen

### Sidebar Integration
- "What's New" link in sidebar footer (always visible)
- Green pulse indicator when updates available
- "Component Playbook" link moved to footer (dev environments only)
- Version number displayed in footer

## Files Created

| File | Purpose |
|------|---------|
| `client/src/routes/(authenticated)/changelog/+page.svelte` | Changelog page UI |
| `client/src/lib/data/changelog.ts` | Version history data |
| `client/src/lib/types/changelog.ts` | TypeScript types |
| `client/src/lib/stores/version.svelte.ts` | Version tracking store |
| `client/src/lib/config/version.ts` | App version from package.json |

## Files Modified

| File | Change |
|------|--------|
| `client/src/lib/components/layout/AppSidebarUser.svelte` | Added What's New link with indicator |
| `client/src/lib/components/layout/AppSidebarNav.svelte` | Removed playbook link (moved to footer) |
| `client/src/lib/config/routes.ts` | Added CHANGELOG route |
| `client/src/routes/(public)/dev-playbook/+page.svelte` | Changed back button to use history.back() |
| `client/vite.config.ts` | Inject APP_VERSION from package.json |
| `client/package.json` | Added @types/node dependency |

## Implementation Details

### Version Store
```typescript
class VersionStore {
  #storage = new Localstorage<string>('eventify_last_seen_version', '');
  #currentVersion = getLatestVersion();

  get hasNewVersion(): boolean {
    if (!this.#storage.value) return true;
    return this.#currentVersion !== this.#storage.value;
  }

  markAsSeen(): void {
    this.#storage.value = this.#currentVersion;
  }
}
```

### Changelog Data Structure
```typescript
interface ChangelogEntry {
  version: string;
  date: string;
  features?: string[];
  improvements?: string[];
  fixes?: string[];
}
```

### Vite Version Injection
```typescript
// vite.config.ts
define: {
  'import.meta.env.APP_VERSION': JSON.stringify(pkg.version)
}
```

## Technical Decisions

1. **localStorage for version tracking** - Simple, persistent, works across sessions
2. **Centralized changelog data** - Single source of truth in `changelog.ts`
3. **Version from package.json** - Build-time injection ensures consistency
4. **Pulse indicator** - Subtle but noticeable, matches other live indicators

## Notes

- Changelog entries are manually maintained in `client/src/lib/data/changelog.ts`
- Version indicator clears when user visits the changelog page
- Footer links visible to all authenticated users
- Playbook link only shows in dev environments (DEV, TST)
