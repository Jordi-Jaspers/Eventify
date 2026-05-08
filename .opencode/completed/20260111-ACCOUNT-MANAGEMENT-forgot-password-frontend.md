## [2026-01-11] - Forgot Password Frontend

### Plan (approved)
Frontend-only implementation for forgot/reset password flow. Backend already exists with tested endpoints.

**Requirements:**
- `/forgot-password` page: email input, request reset, success message (security-conscious)
- `/reset-password?token=X` page: new password + confirm, validation, redirect to login
- Security: never reveal if email exists
- Validation: 8+ chars, passwords match
- UI: glassmorphism, password toggles, loading states, toasts

### Actual Changes

**Frontend:**
- Replaced stub `/forgot-password` page with working form
- Created new `/reset-password` page with token handling
- Password visibility toggles on both password fields
- **Password strength meter** - reused `PasswordStrengthMeter` component from registration
- Proper password validation using `validatePassword()` (8+ chars, uppercase, lowercase, digit, special char, no whitespace)
- "Passwords match" indicator with green dot
- Toast notifications for success/error states
- Redirect to login on successful reset
- Error state with link to request new reset when token invalid/expired
- AppLogo and glassmorphism card styling matching other auth pages

**API Client:**
- Created `PasswordController.ts` with `requestPasswordReset()` and `resetPassword()` methods
- Added `ForgotPasswordRequest` type export to models

**UI Polish:**
- forgot-password: Improved card spacing, focus states on links
- reset-password: Enhanced card opacity for dark mode visibility, improved focus states on password toggles

**Testing:**
- `test/components/forgot-password.spec.ts` (6 tests - 3 states x 2 themes)
- `test/components/reset-password.spec.ts` (12 tests - 6 states x 2 themes, including password strength meter)

### Agents Used
- sveltekit-frontend-agent: Built both pages + screenshot tests
- ui-validator (ralph-loop.sh): 1 iteration for forgot-password, 1 iteration for reset-password

### Files Modified
- `client/src/routes/(public)/forgot-password/+page.svelte` (replaced stub)
- `client/src/routes/(public)/reset-password/+page.svelte` (new)
- `client/src/lib/api/password/PasswordController.ts` (new)
- `client/src/lib/api/models.ts` (added ForgotPasswordRequest export)
- `client/test/components/forgot-password.spec.ts` (new)
- `client/test/components/reset-password.spec.ts` (new)

### Quality Metrics
- ✅ Tests: 18 screenshot tests passing (6 + 12)
- ✅ Build: Successful (0 errors, 1 pre-existing warning)
- ✅ Type check: Passing
- ✅ UI Polish: Complete (2 iterations total)
- ✅ Password strength meter: Reused from registration page
