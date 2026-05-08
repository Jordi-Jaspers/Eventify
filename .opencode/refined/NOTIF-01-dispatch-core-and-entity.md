---
epic: "NOTIF"
title: "Notification Dispatch Core, Entity & Welcome Notification"
estimate: L
status: ready
created: 2026-05-08
depends_on: []
labels: [backend, database, foundation]
priority: P1
claimed_by:
claimed_by_date:
---

## 1. User Story
**As a** newly registered user\
**I want** a welcome notification to appear in my notification bell\
**So that** I have an immediate signal that the platform is alive and that my notifications surface works — and so that the platform has a real, production-running notification dispatch path that future features (broadcasts, severity transitions, security alerts) can plug into.\

## 2. Business Context & Value
This story builds the notification subsystem foundation: entity, adapter pattern, dispatch service, and the read API consumed by the bell. The welcome notification is the **first real producer** — it proves the entire path (producer → dispatch service → adapter → DB → read API) works end-to-end in production, not just in tests. Without this foundation, NOTIF-02 (admin broadcasts), NOTIF-03 (bell integration), NOTIF-05 (release notifications), the future watchlist subscriptions (originally NOTIF-02 in backlog), and Telegram (originally NOTIF-03) all have nowhere to plug in. The adapter pattern (`NotificationAdapter` interface + `List<NotificationAdapter>` injection) is a new architectural pattern in this codebase — first introduction.

## 3. Acceptance Criteria
* [ ] **Scenario 1: Welcome notification on registration**
    * Given a user completes registration (email/password or OAuth2)
    * When the user is persisted
    * Then exactly one `notification` row is created for that user with `category=ANNOUNCEMENT`, `title="Welcome to Eventify"`, `message="Get started by creating your first channel and sending events. Need help? Check the docs."`, `action_url="/channels"`, `action_label="Get started"`, `read_at=NULL`, `broadcast_id=NULL`
* [ ] **Scenario 2: List notifications for current user**
    * Given an authenticated user with N notifications
    * When they call `GET /api/v1/notifications?limit=20&offset=0`
    * Then they receive their notifications ordered by `created_at DESC`, paginated, with `category`, `title`, `message`, `actionUrl`, `actionLabel`, `createdAt`, `readAt`, `urgent` (derived from category) fields
* [ ] **Scenario 3: Unread count**
    * Given an authenticated user with 3 unread notifications
    * When they call `GET /api/v1/notifications/unread-count`
    * Then the response is `{"count": 3}`
* [ ] **Scenario 4: Mark single notification as read**
    * Given an authenticated user with an unread notification owned by them
    * When they call `POST /api/v1/notifications/{id}/read`
    * Then `read_at` is set to current timestamp; subsequent calls are idempotent (no error if already read)
* [ ] **Scenario 5: Mark all as read**
    * Given an authenticated user with multiple unread notifications
    * When they call `POST /api/v1/notifications/read-all`
    * Then all their unread notifications get `read_at` set to current timestamp
* [ ] **Scenario 6: Authorization — cannot read others' notifications**
    * Given user A has notification X
    * When user B calls `POST /api/v1/notifications/{X}/read` or sees X in their list
    * Then user B receives 404 (not 403 — don't leak existence) and X is not modified
* [ ] **Scenario 7: Adapter pattern**
    * Given `NotificationDispatchService.dispatch(audience, payload)` is called from a service
    * When the dispatch service runs
    * Then it resolves the audience to user IDs, creates `notification` rows for each user via the `InAppNotificationAdapter`, and the adapter's writes are visible to subsequent `GET /notifications` calls
* [ ] **Edge Case: User registered before this feature deploys**
    * Given existing users in the database who never received a welcome notification
    * When this feature deploys
    * Then no welcome notifications are backfilled (welcome is for *new* registrations only — historical users are not retroactively welcomed)
* [ ] **Edge Case: Audience type with zero recipients**
    * Given `dispatch(audience=USER(nonExistentUserId), payload)` is called
    * When the audience resolves to zero users
    * Then no notification rows are created and no exception is thrown (logged at WARN level)

## 4. Technical Requirements
* **API Changes**:
    * `GET /api/v1/notifications?limit=20&offset=0` → `Page<NotificationResponse>` (current user only)
    * `GET /api/v1/notifications/unread-count` → `{ count: integer }`
    * `POST /api/v1/notifications/{id}/read` → 204 No Content (idempotent)
    * `POST /api/v1/notifications/read-all` → `{ markedCount: integer }`
    * `NotificationResponse` fields: `id`, `category` (enum), `urgent` (boolean, derived), `title`, `message`, `actionUrl` (nullable), `actionLabel` (nullable), `createdAt`, `readAt` (nullable)
* **Database**: New Liquibase changeset `YYYYMMDDHHMI-PRD-create-notification-tables.xml`. Two tables:

    `notification`:
    ```sql
    id              UUID PRIMARY KEY
    user_id         UUID NOT NULL REFERENCES "user"(id) ON DELETE CASCADE
    broadcast_id    UUID NULL REFERENCES notification_broadcast(id) ON DELETE SET NULL
    category        VARCHAR(40) NOT NULL  -- ANNOUNCEMENT | UPDATE | REMINDER | ALERT | SECURITY
    title           VARCHAR(120) NOT NULL
    message         VARCHAR(500) NOT NULL
    action_url      VARCHAR(500) NULL
    action_label    VARCHAR(40)  NULL
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
    read_at         TIMESTAMPTZ NULL
    -- Indexes
    CREATE INDEX idx_notification_user_unread ON notification (user_id, read_at, created_at DESC);
    CREATE INDEX idx_notification_user_created ON notification (user_id, created_at DESC);
    ```

    `notification_broadcast` (created in this story but only populated by NOTIF-02 admin tool; included now to keep schema cohesive):
    ```sql
    id                  UUID PRIMARY KEY
    sent_by             UUID NOT NULL REFERENCES "user"(id) ON DELETE SET NULL
    audience_type       VARCHAR(40) NOT NULL  -- ALL_USERS | ORGANIZATION | ALL_ORGANIZATION_OWNERS | USER | GLOBAL_ROLE
    audience_target_id  UUID NULL  -- orgId for ORGANIZATION, userId for USER, NULL otherwise
    audience_role       VARCHAR(40) NULL  -- "ADMIN" for GLOBAL_ROLE, NULL otherwise
    category            VARCHAR(40) NOT NULL
    title               VARCHAR(120) NOT NULL
    message             VARCHAR(500) NOT NULL
    action_url          VARCHAR(500) NULL
    action_label        VARCHAR(40)  NULL
    recipient_count     INT NOT NULL
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
    ```

* **Security**: All `/api/v1/notifications/**` endpoints require authenticated user (cookie/JWT). Notification ownership enforced in service layer (`WHERE user_id = principal.id`). Use 404 (not 403) for cross-user access attempts.
* **Performance**: List endpoint supports pagination (default 20). Unread count is single indexed query. Welcome notification dispatch is synchronous (one row insert) and runs inside the registration transaction — failure to dispatch must NOT roll back registration (catch + log, registration is the priority).

## 5. Design & UI/UX
N/A for this story — backend-only. Frontend bell still uses old changelog source (NOTIF-03 refactors it).

The welcome notification will only become visible to users when NOTIF-03 ships. Until then, users can see it via direct API call. This is acceptable: NOTIF-01 is foundation; NOTIF-03 is the user-visible payoff.

## 6. Implementation Notes
**Backend package layout** (feature-first per `.opencode/skills/eventify-architecture`):
- `api/notification/model/Notification.java` — JPA entity (Lombok)
- `api/notification/model/NotificationBroadcast.java` — JPA entity
- `api/notification/model/NotificationCategory.java` — enum with `(NotificationStyle style, boolean urgent)` constructor:
  - `ANNOUNCEMENT(INFO, false)`, `UPDATE(SUCCESS, false)`, `REMINDER(WARN, false)`, `ALERT(DANGER, true)`, `SECURITY(DANGER, true)`
- `api/notification/model/NotificationStyle.java` — enum: `INFO`, `SUCCESS`, `WARN`, `DANGER`
- `api/notification/model/audience/NotificationAudience.java` — sealed interface or class hierarchy: `AllUsers`, `Organization(orgId)`, `AllOrganizationOwners`, `User(userId)`, `GlobalRole(role)`
- `api/notification/model/request/NotificationPayload.java` — record: `category`, `title`, `message`, `actionUrl?`, `actionLabel?`
- `api/notification/model/response/NotificationResponse.java` — record (DTO returned by API)
- `api/notification/repository/NotificationRepository.java` — Spring Data JPA
- `api/notification/repository/NotificationBroadcastRepository.java`
- `api/notification/service/adapter/NotificationAdapter.java` — interface: `void deliver(Set<UUID> userIds, NotificationPayload payload, UUID broadcastId)`
- `api/notification/service/adapter/InAppNotificationAdapter.java` — implements adapter, batch-inserts via repository
- `api/notification/service/NotificationDispatchService.java` — `dispatch(audience, payload, broadcastId?)`: resolves audience → userIds, calls all registered `NotificationAdapter`s. For NOTIF-01 there's only one adapter (in-app); future stories add Telegram, Email.
- `api/notification/service/AudienceResolver.java` — resolves a `NotificationAudience` to `Set<UUID> userIds`. Queries User repo / OrganizationMembership repo / etc.
- `api/notification/service/NotificationService.java` — read API logic (list, count, mark-read, mark-all-read)
- `api/notification/controller/NotificationController.java` — REST endpoints
- `api/notification/mapper/NotificationMapper.java` — MapStruct: `Notification → NotificationResponse`

**Welcome notification hook:**
- Find user-creation point (likely `api/user/service/UserService.java` or `api/authentication/service/RegistrationService.java`).
- After user is persisted, call `notificationDispatchService.dispatch(new NotificationAudience.User(user.getId()), welcomePayload)`.
- Wrap in try/catch: log error, do NOT propagate (registration must succeed even if notification fails).
- Welcome payload defined as a static constant in `NotificationService` or a `WelcomeNotificationProducer` component.

**Adapter injection pattern:**
- `NotificationDispatchService` constructor takes `List<NotificationAdapter> adapters` (Spring auto-injects all beans implementing the interface).
- For NOTIF-01: only `InAppNotificationAdapter` exists, so list has one element. Future stories add adapters without touching the dispatch service.

**Audience resolver query patterns:**
- `AllUsers`: `SELECT id FROM "user"` (could be huge — log warning if recipient count > 10k for now; future story adds chunking).
- `Organization(orgId)`: `SELECT user_id FROM organization_membership WHERE organization_id = ?`
- `AllOrganizationOwners`: `SELECT DISTINCT user_id FROM organization_membership WHERE role = 'OWNER'`
- `User(userId)`: `SELECT id FROM "user" WHERE id = ?` (validates existence)
- `GlobalRole(role)`: `SELECT id FROM "user" WHERE global_role = ?`

**Tests to write** (per project test standards — see `eventify-spring-standards`):
- Repository test: notification CRUD, unread count query, indexed query plan check.
- Service test: `NotificationDispatchService` resolves each audience type correctly; multi-adapter dispatch (mock 2 adapters, both called).
- Controller integration test: all 4 endpoints, authorization (user A can't see user B's notifications), pagination, idempotency.
- Service test: welcome notification fires on registration; registration succeeds even if dispatch throws.
- Audience resolver test: each audience type returns expected user set.

**Pitfalls:**
- `notification_broadcast` table is created here but stays empty until NOTIF-02. Don't add a NOT NULL constraint on `notification.broadcast_id` — system notifications (welcome, future release) have NULL.
- Welcome notification must NOT roll back registration on failure. Critical: separate try/catch.
- Audience resolver for `AllUsers` could be slow on large systems — fine for now; flag in implementation note for future chunking story.
- The `urgent` flag on the response is *derived* from category, not stored separately. Mapper computes it.

## 7. Test Impact Analysis (Greenfield — minimal)
### Test modification policy:
- [x] No existing tests should be modified (greenfield feature)
- [ ] One existing test may be touched: registration test should be extended to assert welcome notification was dispatched.

### Files to modify (MANDATORY):
| File | Change | Lines |
|------|--------|-------|
| `api/user/service/UserService.java` (or `RegistrationService.java`) | Inject `NotificationDispatchService`, dispatch welcome after persist (try/catch) | TBD |
| Existing registration tests | Add assertion that welcome notification exists for new user | TBD |
| `server/src/main/resources/db/changelog/db.changelog-master.xml` (or include file) | Reference new changeset | +1 |
