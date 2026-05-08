---
epic: "NOTIF"
title: "Admin Notification Broadcast Tool"
estimate: M
status: ready
created: 2026-05-08
depends_on: ["ADMIN-01-restructure-admin-routes", "NOTIF-01-dispatch-core-and-entity"]
labels: [backend, frontend, admin]
priority: P1
claimed_by:
claimed_by_date:
---

## 1. User Story
**As a** global admin\
**I want** a tool to compose and send notifications to targeted audiences (everyone, an organization, all org owners, a single user, or all global admins)\
**So that** I can announce platform changes, alert users to incidents, send security notices, and reach the right group without writing SQL or asking engineers.\

## 2. Business Context & Value
NOTIF-01 built the dispatch foundation but the only producer is the welcome notification. This story exposes the dispatch path to admins via a UI — the primary day-to-day use case for the notification system. Replaces the need for ad-hoc Slack/email announcements, gives a single auditable record (broadcast history) of what was sent, when, by whom, and to whom. Foundation for incident comms, scheduled maintenance notices, security advisories.

## 3. Acceptance Criteria
* [ ] **Scenario 1: Send broadcast to all users**
    * Given a global admin on `/admin/tools/notifications`
    * When they fill the composer (category=`ANNOUNCEMENT`, title, message, audience=`ALL_USERS`) and click Send
    * Then they see a confirmation dialog showing recipient count
    * And on confirm, a `notification_broadcast` row is created and `notification` rows are created for every user
    * And the composer resets to empty
    * And a toast shows "Sent to N users"
* [ ] **Scenario 2: Send broadcast to a specific organization**
    * Given a global admin in the composer
    * When they select audience=`ORGANIZATION` and pick an org from a searchable picker (typeahead)
    * Then before send, the recipient count preview shows the org's member count
    * And on send, only that org's members receive the notification
* [ ] **Scenario 3: Send broadcast to all organization owners**
    * Given the composer
    * When audience=`ALL_ORGANIZATION_OWNERS`
    * Then recipient preview shows count of distinct users with OWNER role across all orgs
    * And on send, those users receive the notification
* [ ] **Scenario 4: Send to a single user**
    * Given the composer
    * When audience=`USER` and a user is picked from a typeahead picker (search by email/name)
    * Then preview shows that one user
    * And on send, only that user receives it
* [ ] **Scenario 5: Send to all global admins**
    * Given the composer
    * When audience=`GLOBAL_ROLE` with role=`ADMIN`
    * Then preview shows count of global admins
    * And on send, only global admins receive it
* [ ] **Scenario 6: Broadcast history tab**
    * Given past broadcasts exist
    * When admin opens the History sub-tab
    * Then a table shows all broadcasts ordered by `createdAt DESC` with columns: Sent At, Sent By, Audience (human-readable: "All users", "Org: Acme Corp", "All org owners", "User: alice@example.com", "Role: Admin"), Category (badge), Title, Recipient Count
    * And paginated (default 20 per page)
* [ ] **Scenario 7: Validation**
    * Given the composer
    * When admin clicks Send with title empty, message empty, or no audience selected
    * Then inline validation errors appear; Send button stays disabled
    * Title max 120 chars, message max 500 chars (inline counter)
* [ ] **Scenario 8: Confirmation gate for large audiences**
    * Given audience preview count > 100
    * When admin clicks Send
    * Then confirmation dialog requires typing the recipient count to confirm (e.g., "Type 1234 to confirm sending to 1,234 users")
    * And only then is Send active
* [ ] **Scenario 9: Action URL/label optional**
    * Given the composer
    * When admin leaves Action URL and Label blank
    * Then the broadcast sends without them; recipients see no CTA button
    * When admin fills only one of the two
    * Then validation requires both or neither
* [ ] **Edge Case: Sending to org with zero members**
    * Given an org with no members
    * When broadcast targets that org
    * Then `recipient_count=0`, the broadcast row is created but no notification rows; toast shows "Sent to 0 users (audience was empty)"
* [ ] **Edge Case: Non-admin attempts access**
    * Given a regular user
    * When they call `POST /api/v1/admin/notifications/broadcasts` directly
    * Then 403 Forbidden (existing `@PreAuthorize` admin guard)

## 4. Technical Requirements
* **API Changes**:
    * `POST /api/v1/admin/notifications/broadcasts` — body: `{ category, title, message, actionUrl?, actionLabel?, audience: { type, targetId?, role? } }` → 201 with broadcast response (id, recipientCount)
    * `GET /api/v1/admin/notifications/broadcasts?limit=20&offset=0` → `Page<BroadcastResponse>`
    * `POST /api/v1/admin/notifications/broadcasts/preview` — body: `{ audience }` → `{ recipientCount: integer }` (used by composer preview, no side effects)
* **Database**: N/A — `notification_broadcast` table created in NOTIF-01.
* **Security**: All endpoints `@PreAuthorize("hasRole('ADMIN')")`. Audience targeting validated server-side (admin can target any org/user — no cross-tenancy restrictions for global admins).
* **Performance**: Preview endpoint runs the audience resolver query; same as dispatch resolver. Broadcast send for `ALL_USERS` on N users = single batch insert into `notification` (~50ms for 10k users). For larger systems, NOTIF-future story adds background job; for now synchronous is fine.

## 5. Design & UI/UX
**Page:** `/admin/tools/notifications` — added as the first tab under `/admin/tools`. The Tools layout (from ADMIN-01) gains its first real tab.

**Sub-tabs:** Send / History — secondary tab nav inside the page.

**Composer (Send tab):**
- Category dropdown — 5 options with icon + color preview ("ALERT 🔴", "ANNOUNCEMENT 🔵", etc.)
- Title input (max 120, counter)
- Message textarea (max 500, counter)
- Audience picker — `Select` for type; conditional secondary controls based on type:
    - `ALL_USERS` / `ALL_ORGANIZATION_OWNERS` → no secondary
    - `ORGANIZATION` → `Combobox` with org search
    - `USER` → `Combobox` with user search (by email + display name)
    - `GLOBAL_ROLE` → `Select` with role options (only `ADMIN` for now)
- Action URL (optional, text)
- Action Label (optional, max 40 chars)
- Live preview card — shows what the notification will look like in the bell (using same component as the user-facing bell)
- Recipient count badge — "Will send to N users" updates as audience changes (debounced 300ms call to preview endpoint)
- Send button (disabled until valid)

**Confirmation dialog:**
- Shows recipient count + audience description + notification preview
- For >100 recipients: type-to-confirm input

**History tab:**
- Standard `DataTable` (already a project pattern per `eventify-svelte-standards`)
- Columns as in Scenario 6
- Click row → expand to show full message + action URL + sender details
- No actions (broadcasts can't be deleted/resent — append-only audit trail)

**Empty states:**
- Send tab: composer always present, no empty state needed
- History tab: empty illustration + "No broadcasts sent yet"

## 6. Implementation Notes
**Backend:**
- `api/notification/controller/AdminNotificationController.java` — new admin-scoped controller
- `api/notification/service/NotificationBroadcastService.java` — wraps dispatch service, also creates the `notification_broadcast` row, returns recipient count
- `api/notification/service/AudienceResolver.java` (from NOTIF-01) — add `count(audience)` method for preview endpoint
- `api/notification/model/request/CreateBroadcastRequest.java`
- `api/notification/model/request/AudienceRequest.java` — discriminated union (use Jackson polymorphism with `@JsonTypeInfo` or flat record with optional fields)
- `api/notification/model/response/BroadcastResponse.java`
- `api/notification/model/validator/BroadcastValidator.java` — title/message length, action URL+label both-or-neither, audience target presence

**Frontend:**
- `client/src/routes/(authenticated)/admin/tools/notifications/+page.svelte` — sub-tab nav (Send/History)
- `client/src/routes/(authenticated)/admin/tools/notifications/send/+page.svelte` — composer
- `client/src/routes/(authenticated)/admin/tools/notifications/history/+page.svelte` — history table
- `client/src/lib/components/admin/notifications/NotificationComposer.svelte`
- `client/src/lib/components/admin/notifications/AudiencePicker.svelte`
- `client/src/lib/components/admin/notifications/BroadcastHistoryTable.svelte`
- `client/src/lib/components/admin/notifications/NotificationPreviewCard.svelte` (shared with user bell — see NOTIF-03)
- `client/src/lib/api/admin/notification.ts` — typed wrappers for the 3 new endpoints
- `client/src/lib/types/notification.ts` — extend with broadcast types

**Tools tab layout:** add `/admin/tools/+page.svelte` to redirect to `/admin/tools/notifications` once this story ships (replaces the empty state from ADMIN-01).

**Pitfalls:**
- Audience picker's user/org search must use existing search endpoints (don't build new ones — see `20251224-MULTI-TENANT-user-search-with-owner-selector.md` and `20251229-MULTI-TENANT-global-admin-org-listing.md` in completed/).
- Preview endpoint must be cheap — `COUNT(*)` per audience type only, no full user list returned.
- After send, refetch the History tab data so the new broadcast appears immediately.
- `urgent` flag is derived from category in the preview — composer doesn't show a separate urgency control.

## 7. Test Impact Analysis (Greenfield)
### Test modification policy:
- [x] No existing tests should be modified

### Files to modify (MANDATORY):
| File | Change | Lines |
|------|--------|-------|
| `client/src/routes/(authenticated)/admin/tools/+page.svelte` | Replace empty state with redirect to `/admin/tools/notifications` | TBD |
| `client/src/lib/components/admin/AdminTabsNav.svelte` (from ADMIN-01) tools tabs config | Add Notifications tab | TBD |
