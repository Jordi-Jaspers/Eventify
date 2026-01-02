## [2026-01-02] - Dev Credentials Block on Login Page

### Summary
Added a development-only block on the login page that displays admin credentials for quick testing. The block only appears when running in dev mode and includes a one-click button to auto-fill the login form.

---

### Requirements
- Show admin credentials on login page for dev testing
- Only visible in development mode (not production)
- Quick-fill functionality for convenience

---

### Actual Changelog

#### Frontend Changes

**login/+page.svelte**
- Added import for `dev` from `$app/environment`
- Added `Terminal` icon import from lucide-svelte
- Added `fillDevCredentials()` function to auto-populate form fields
- Added dev credentials block with:
  - Conditional rendering: `{#if dev}`
  - Glassmorphism styling: amber-tinted semi-transparent background
  - Displays email: `jordijaspers@gmail.com`
  - Displays password: `admin123!`
  - "Fill Credentials" button

---

### Files Modified

**Frontend:**
- `client/src/routes/(public)/login/+page.svelte`

---

### Quality Metrics

- Frontend type check: 0 errors, 0 warnings
- Build: Successful

---

### Notes

- Credentials come from `application.yml` defaults for `GLOBAL_ADMIN_EMAIL` and `GLOBAL_ADMIN_PASSWORD`
- Block uses SvelteKit's `dev` flag which is `false` in production builds
- Styling uses amber color to indicate "warning/dev" context
