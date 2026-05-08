---
epic: "NOTIF"
title: "Automated Release Notification on Startup"
estimate: S
status: ready
created: 2026-05-08
depends_on: ["NOTIF-01-dispatch-core-and-entity", "NOTIF-04-changelog-backend-source"]
labels: [backend, automation]
priority: P2
claimed_by:
claimed_by_date:
---

## 1. User Story
**As a** developer releasing a new version of Eventify\
**I want** every existing user to automatically receive a notification announcing the new version when the new build deploys\
**So that** users know what changed without me having to remember to send a manual broadcast every release, and the announcement uses the same content as the `/changelog` page (one source of truth).\

## 2. Business Context & Value
NOTIF-02 lets admins manually broadcast — but for routine version releases, that's repetitive and forgettable (per the user: "i think it is scary because people can forget"). This story automates it: on app startup, if the current `app.version` has a matching entry in the backend changelog AND no broadcast for this version exists in this environment AND there are pre-existing users, dispatch a release notification. Idempotent — safe to start up many times. Skips empty databases (don't notify the very first user "what's new" before they've used the platform). The dev's only job is updating `changelog.json` before tagging the release.

## 3. Acceptance Criteria
* [ ] **Scenario 1: First startup with new version dispatches**
    * Given the deployed `app.version=1.1.0` and `changelog.json` contains an entry for `1.1.0`
    * And there is no `notification_broadcast` row whose title contains `1.1.0` and category=`UPDATE`
    * And there are pre-existing users in the database (created before this startup)
    * When the app starts and the release-notification job runs
    * Then a `notification_broadcast` row is created with audience=`ALL_USERS`, category=`UPDATE`, title=`What's new in v1.1.0`, message=`<the changelog description>`, action_url=`/changelog`, action_label=`See what's new`
    * And `notification` rows are fanned out to all pre-existing users
* [ ] **Scenario 2: Idempotent subsequent startups**
    * Given the v1.1.0 release notification was already dispatched in this environment
    * When the app restarts
    * Then the job runs, finds the existing broadcast for v1.1.0, and skips dispatch (no duplicate notifications)
* [ ] **Scenario 3: Empty database (fresh deploy / first user)**
    * Given there are zero users in the database (fresh environment, brand-new install)
    * When the app starts with v1.1.0
    * Then the job runs, sees no users, skips dispatch
    * And the next startup (after a user registers) STILL skips — because the version was already "released" before the user existed (we use a marker or check against a per-version timestamp; see implementation notes)
* [ ] **Scenario 4: Version not in changelog**
    * Given `app.version=1.1.5` but `changelog.json` has no entry for 1.1.5
    * When the app starts
    * Then the job logs WARN ("No changelog entry for current version 1.1.5; skipping release notification") and skips dispatch
* [ ] **Scenario 5: SNAPSHOT versions skipped**
    * Given `app.version=1.1.0-SNAPSHOT`
    * When the app starts
    * Then the job detects the SNAPSHOT suffix and skips dispatch (dev/CI builds don't broadcast)
* [ ] **Scenario 6: New users registered after release don't get the notification**
    * Given v1.1.0 was released and broadcast at time T
    * When a new user registers at T+5 minutes
    * Then they receive a welcome notification (NOTIF-01) but NOT the v1.1.0 release notification (it predates them)
    * (Mechanism: dispatch only resolves users where `created_at < broadcast_time`)
* [ ] **Edge Case: Dispatch failure mid-fanout**
    * Given dispatch to ALL_USERS starts and fails partway through
    * When the failure occurs
    * Then partial results are committed (some users got notified, some didn't), the broadcast row IS created with `recipient_count` matching actual fanout, the error is logged
    * And on next startup, the broadcast already exists → skips (does NOT retry the failed users — acceptable for this story; a manual admin re-broadcast covers the rare case)

## 4. Technical Requirements
* **API Changes**: N/A — internal startup job.
* **Database**: N/A — uses existing `notification` and `notification_broadcast` tables.
* **Security**: N/A — runs as a system process at startup, not user-triggered. Broadcast `sent_by` will be NULL or a designated system user.
* **Performance**: One-time job per app instance startup. Detection query: indexed lookup on `notification_broadcast` for current version. Dispatch is the same as ALL_USERS broadcast in NOTIF-02 (single batched insert).

## 5. Design & UI/UX
N/A — backend startup job. Visible result is users seeing the release notification in their bell (NOTIF-03).

## 6. Implementation Notes
**Backend:**
- `api/notification/job/ReleaseNotificationStartupJob.java`
  - `@Component` with `ApplicationReadyEvent` listener (runs after app fully started, after DB ready)
  - Reads `${spring.application.version}` (or maven/gradle build property) — wire via `@Value("${app.version}")`
  - Skip if version ends with `-SNAPSHOT`
  - Lookup changelog entry via `ChangelogService.getByVersion(version)` (from NOTIF-04). Skip with WARN if not found.
  - Detection query: `SELECT 1 FROM notification_broadcast WHERE category='UPDATE' AND title = ? LIMIT 1` (title includes version)
  - If exists → skip (log INFO once)
  - If not exists AND user count > 0 → dispatch via `NotificationDispatchService` with audience=`ALL_USERS`, payload built from changelog entry
  - If user count == 0 → record a "skipped, no users" marker so we don't fire on later startups even when users do register. **Recommended marker:** still create the `notification_broadcast` row with `recipient_count=0` (broadcast happened, just to nobody). Future users created after this never get this version's notification — by design.

- `NotificationDispatchService` (from NOTIF-01) needs a small extension: when audience is `ALL_USERS` for system-initiated broadcasts, only resolve users where `created_at < NOW()` (effectively: snapshot at dispatch time). Already true for synchronous dispatch — no change needed.

- Configure `app.version` exposure: in `build.gradle.kts`, ensure the project version is available as a Spring property (likely already wired via Spring Boot's `spring.application.version` or via `BuildProperties`).

**Build configuration:**
- Use `spring-boot-starter-actuator`'s `BuildProperties` if not already enabled — gives `getVersion()` cleanly. Or use `@Value("${spring.application.version}")` if explicitly set in `application.yml`.

**Tests:**
- Job test (Spring integration):
  - Setup: insert changelog entry for v1.1.0 + 5 users; `app.version=1.1.0`
  - Run job → assert broadcast row created + 5 notification rows
  - Run job again → assert no new rows (idempotent)
- Skip-on-snapshot test
- Skip-on-no-changelog-entry test
- Skip-on-empty-db test (still creates broadcast marker with recipient_count=0)

**Pitfalls:**
- `ApplicationReadyEvent` runs in the main thread before serving requests. Keep the job fast (< few seconds for typical user counts). For 100k+ users, dispatch might be slow — acceptable for now; future story can move to background.
- Be careful about test environments: test profiles often have `app.version=test` or `0.0.0` — make sure tests don't accidentally fire real broadcasts. Gate the job on a property like `eventify.notifications.release-broadcast.enabled` (default true, override false in test profile unless explicitly testing the job).
- Use the version string directly in title matching — fragile but acceptable. Better long-term: dedicated `version VARCHAR` column on `notification_broadcast` for system-generated release broadcasts. **For this story:** YAGNI, stick with title match.

## 7. Test Impact Analysis (Greenfield)
### Test modification policy:
- [x] No existing tests should be modified
- [x] New tests added per "Tests" section above
- [x] Test profile configured to disable the job by default to avoid surprise broadcasts in test runs

### Files to modify (MANDATORY):
| File | Change | Lines |
|------|--------|-------|
| `api/notification/job/ReleaseNotificationStartupJob.java` | NEW | all |
| `server/src/main/resources/application.yml` | Add `eventify.notifications.release-broadcast.enabled` property | +1 |
| `server/src/test/resources/application-test.yml` (or test profile config) | Set the property to `false` | +1 |
| `build.gradle.kts` | Verify build properties exposure for `app.version` | small |
