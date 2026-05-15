---
epic: "ORGMGMT"
title: "Organization Status Change Notifications"
estimate: S
status: ready
created: 2026-05-14
depends_on: ["ORGMGMT-01-update-organization-status"]
labels: [backend, notifications]
priority: P2
claimed_by:
claimed_by_date:
---

## 1. User Story
**As a** member of an organization\
**I want** to be notified when my organization is suspended or reactivated\
**So that** I understand why I lost or regained access\

## 2. Business Context & Value
When an admin suspends an organization, members immediately lose access. Without notification, users are confused about why their org disappeared or stopped working. This closes the communication gap between admin action and user experience.

## 3. Acceptance Criteria
* [ ] **Notification on suspension**
    * Given an admin changes an org's status to SUSPENDED
    * When the status update succeeds
    * Then all org members receive an urgent in-app notification with title "Organization suspended" and message "[name] has been suspended"
* [ ] **Notification on reactivation**
    * Given an org's current status is SUSPENDED and admin changes it to ACTIVE or TRIAL
    * When the status update succeeds
    * Then all org members receive a non-urgent in-app notification with title "Organization reactivated" and message "[name] has been reactivated"
* [ ] **No notification for other transitions**
    * Given a status change that is not to/from SUSPENDED (e.g., TRIAL → ACTIVE)
    * When the status update succeeds
    * Then no notification is dispatched
* [ ] **Notification has action link**
    * Given a status notification is received
    * When the user views it
    * Then it includes an actionUrl pointing to the org switcher

## 4. Technical Requirements
* **API Changes**: N/A — no new endpoints. Notification is dispatched as side-effect of the ORGMGMT-01 status update endpoint.
* **Database**: N/A — uses existing `notification` table.
* **Security**: N/A — dispatch is internal, triggered by already-secured admin endpoint.
* **Performance**: N/A — audience resolution for a single org is a simple query.

## 5. Design & UI/UX
N/A — uses existing notification bell UI. Notifications appear in the bell dropdown with standard rendering.

## 6. Implementation Notes
- **Where to trigger**: In the admin org status update service method (added in ORGMGMT-01), after successful status save. Check previous status vs new status.
- **Dispatch call**:
  ```java
  notificationDispatchService.dispatch(
      NotificationAudience.organization(orgId),
      NotificationPayload.builder()
          .category(NotificationCategory.SYSTEM)
          .title("Organization suspended")
          .message(org.getName() + " has been suspended")
          .actionUrl("/organizations")
          .actionLabel("View organizations")
          .urgent(true)
          .build()
  );
  ```
- **Condition logic**: Only dispatch when `newStatus == SUSPENDED` or `oldStatus == SUSPENDED && newStatus != SUSPENDED`
- **Files to modify**:
  - `server/src/main/java/io/github/eventify/api/admin/service/AdminOrganizationService.java` (or wherever ORGMGMT-01 places the status update logic)

## 7. Out of Scope
- Email notifications (future enhancement)
- Reason field in notification message (deferred to audit infrastructure)
- Notification preferences / opt-out (deferred to subscription preferences)
