## [2026-01-02] - Dev Credentials API and Login Integration

### Summary
Added a development-only API endpoint that serves bootstrap admin credentials, and updated the login page to fetch these credentials dynamically using the proper controller pattern. The block only appears when running in dev mode and includes loading states, error handling, and a one-click button to auto-fill the login form.

---

### Requirements
- Show admin credentials on login page for dev testing
- Fetch credentials from backend API (not hardcoded)
- Only visible in development mode (not production)
- API endpoint only available with `dev` Spring profile
- Frontend follows controller pattern (like other API calls)

---

### Actual Changelog

#### Backend Changes

**New Files Created (in bootstrap package):**
- `api/bootstrap/model/response/DevCredentialsResponse.java` - DTO with email and password fields
- `api/bootstrap/controller/DevCredentialsController.java` - REST controller with `@Profile("dev")` restriction
- (Test) `api/bootstrap/controller/DevCredentialsControllerTest.java` - 4 integration tests

**Path Constant Added:**
- `api/Paths.java` - Added `DEV_CREDENTIALS_PATH = PUBLIC_PATH + "/dev/credentials"`

**Endpoint Details:**
- **Path:** `GET /api/v1/public/dev/credentials`
- **Profile:** Only available with `@Profile("dev")`
- **Auth:** Public (no authentication required)
- **Response:** `{ "email": "...", "password": "..." }`
- Reads from `security.bootstrap.email` and `security.bootstrap.password` config properties

#### Frontend Changes

**New Files Created:**
- `src/lib/api/dev/DevController.ts` - Controller using openapi-fetch client pattern

**Updated Files:**
- `src/lib/api/models.ts` - Added `DevCredentialsResponse` interface
- `src/routes/(public)/login/+page.svelte`:
  - Imports `getDevCredentials` from DevController
  - Imports `DevCredentialsResponse` type from models
  - Uses typed state: `devCredentials: DevCredentialsResponse | null`
  - Calls controller in `$effect()` for dev mode

**OpenAPI Types Regenerated:**
- `src/lib/types/api.d.ts` - Now includes `DevCredentialsResponse` type

---

### Files Modified

**Backend:**
- `server/src/main/java/io/github/eventify/api/bootstrap/model/response/DevCredentialsResponse.java` (new)
- `server/src/main/java/io/github/eventify/api/bootstrap/controller/DevCredentialsController.java` (new)
- `server/src/test/java/io/github/eventify/api/bootstrap/controller/DevCredentialsControllerTest.java` (new)
- `server/src/main/java/io/github/eventify/api/Paths.java` (modified)

**Frontend:**
- `client/src/lib/api/dev/DevController.ts` (new)
- `client/src/lib/api/models.ts` (modified)
- `client/src/routes/(public)/login/+page.svelte` (modified)
- `client/src/lib/types/api.d.ts` (regenerated)

---

### Quality Metrics

- Backend tests: 4 tests, all passing
- Frontend type check: 0 errors, 0 warnings
- Build: Successful

---

### Security Considerations

- Endpoint is protected by `@Profile("dev")` - not available in production
- No sensitive data exposed in prod builds
- Frontend uses `dev` flag from SvelteKit which is `false` in production

---

### Notes

- Credentials come from `application.yml` properties: `security.bootstrap.email` and `security.bootstrap.password`
- Block uses SvelteKit's `dev` flag which is `false` in production builds
- Styling uses amber color to indicate "warning/dev" context
- API approach allows credentials to be environment-specific without code changes
- Controller placed in `bootstrap` package alongside `GlobalAdminBootstrap` component
