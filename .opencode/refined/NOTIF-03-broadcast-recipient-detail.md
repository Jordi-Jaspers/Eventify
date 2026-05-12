# NOTIF-03: Broadcast Recipient Detail View

## 1. Overview

When an admin clicks a broadcast entry in the history table, the expanded detail should show a searchable list of users who received that notification. This enables admins to verify delivery and look up specific recipients.

## 2. User Story

**As a** global admin  
**I want to** see which users received a specific broadcast notification  
**So that** I can verify delivery and troubleshoot issues for specific users

## 3. Acceptance Criteria

- [ ] Clicking a broadcast row in history expands to show recipient list
- [ ] Recipient list shows user email and name
- [ ] Fuzzy search input filters recipients in real-time (debounce 300ms, min 2 chars)
- [ ] List is paginated (20 per page) with "load more" or scroll pagination
- [ ] Shows total recipient count vs filtered count
- [ ] Empty state when no recipients match search

## 4. API Design

### GET /v1/admin/notifications/broadcasts/{id}/recipients

**Query params:**
- `search` (optional) — fuzzy search on user email/name
- `limit` (default: 20)
- `offset` (default: 0)

**Response:**
```json
{
  "content": [
    { "userId": 1, "email": "user@example.com", "name": "John Doe" }
  ],
  "totalElements": 42,
  "limit": 20,
  "offset": 0
}
```

**Security:** `hasAuthority('MANAGE_USERS')`

## 5. Technical Approach

### Backend
- New endpoint in `AdminNotificationController`
- Query `notification` table by `broadcast_id`, join `user` for email/name
- Apply fuzzy search on `user.email` and `user.first_name`/`user.last_name` via ILIKE
- Return paginated results

### Frontend
- In the expanded broadcast detail (history page), add a search input + scrollable recipient list
- Debounce search input (300ms)
- Fetch recipients on expand + on search change
- Show loading skeleton while fetching

## 6. Dependencies

- NOTIF-02 (admin broadcast tool) must be complete
