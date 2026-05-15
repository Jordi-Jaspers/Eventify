---
epic: "ORGMGMT"
title: "Update Organization Status"
estimate: M
status: ready
created: 2026-05-14
depends_on: []
labels: [backend, frontend, admin]
priority: P1
claimed_by:
claimed_by_date:
---

## 1. User Story
**As an** admin\
**I want** to change an organization's status (TRIAL, ACTIVE, SUSPENDED)\
**So that** I can manage organization lifecycles and restrict access to suspended organizations\

## 2. Business Context & Value
Admins need to control organization access as part of lifecycle management. Suspending an org immediately blocks member access, protecting the platform from abuse or non-payment. Activating moves orgs from trial to full access. This is foundational for the TRIAL limitations and quota enforcement stories.

## 3. Acceptance Criteria
* [ ] **Status update endpoint**
    * Given an admin with `MANAGE_ORGANIZATIONS` permission
    * When they call `PATCH /api/v1/admin/organizations/{orgId}/status` with `{ "status": "ACTIVE" }`
    * Then the organization status is updated and the response returns the updated organization
* [ ] **All transitions allowed**
    * Given any current organization status
    * When admin changes to any other status (TRIAL, ACTIVE, SUSPENDED)
    * Then the transition succeeds without restriction
* [ ] **Suspended org access blocked**
    * Given an organization with status SUSPENDED
    * When a non-admin member makes any org-scoped API call
    * Then a 403 is returned with message "Organization has been suspended"
* [ ] **Org switcher shows suspended orgs grayed out**
    * Given a user is a member of a suspended organization
    * When they view the org switcher
    * Then the org appears grayed out with a "Suspended" badge, and clicking shows info message without switching context
* [ ] **Auto-context switch on suspension**
    * Given a user's active org context is a suspended organization
    * When any org-scoped API call returns 403 "suspended"
    * Then the frontend auto-switches to personal context and shows toast "Organization [name] has been suspended"
* [ ] **Edit Organization Sheet**
    * Given an admin viewing the organizations table
    * When they open the Edit action from the row menu
    * Then an EditOrganizationSheet opens with a status dropdown, and saving updates the org
* [ ] **Consolidated row actions**
    * Given the admin organizations table
    * When viewing any row
    * Then actions (Edit, API Keys, Members) are grouped in a single kebab menu (⋮)

## 4. Technical Requirements
* **API Changes**: `PATCH /api/v1/admin/organizations/{orgId}/status` — request: `UpdateOrganizationStatusRequest(OrganizationStatus status)`, response: `OrganizationResponse`. Secured with `@PreAuthorize("hasAuthority('MANAGE_ORGANIZATIONS')")`.
* **Database**: N/A — `status` column already exists on `organization` table with enum values.
* **Security**: Add a filter/interceptor that checks org status on org-scoped requests. Non-admin members of SUSPENDED orgs get 403. Admins bypass this check.
* **Performance**: N/A — single row update, no batch concerns.

## 5. Design & UI/UX
- **Edit Organization Sheet**: Follows `CreateOrganizationSheet` pattern. Contains status dropdown (TRIAL/ACTIVE/SUSPENDED) with colored badges matching existing status badge utility. Extensible for future fields.
- **Row actions**: Replace separate "API Keys" and "Members" buttons with a kebab menu dropdown containing: Edit, API Keys, Members.
- **Org switcher**: Suspended orgs rendered with `opacity-50`, "Suspended" badge (destructive variant), click triggers info toast instead of context switch.
- **Auto-switch toast**: Destructive variant toast with org name.

## 6. Implementation Notes
- **Backend endpoint**: Add method to `AdminOrganizationController.java` at `server/src/main/java/io/github/eventify/api/admin/controller/`
- **Request DTO**: `UpdateOrganizationStatusRequest.java` in admin model/request package
- **Suspended org filter**: Consider a Spring Security filter or `HandlerInterceptor` that resolves org context from the request path/header and checks status. Must not block admin access.
- **Frontend sheet**: `client/src/lib/components/admin/EditOrganizationSheet.svelte`
- **Frontend actions refactor**: Update org table in `client/src/routes/(authenticated)/admin/resources/organizations/+page.svelte` — use shadcn DropdownMenu for row actions
- **Org switcher**: Modify component in `client/src/lib/components/` (org switcher component)
- **Global error handler**: Extend API client error handling to detect suspended-org 403 and trigger context switch
- **Status badge utility**: Already exists at `client/src/lib/utils/organization.ts`

## 7. Out of Scope
- Reason field / audit log (deferred to Audit Infrastructure)
- Notifications to org owner on status change (separate backlog item)
- TRIAL limitations enforcement (separate story)
