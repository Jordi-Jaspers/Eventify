# Admin User Management Page

**Epic**: User Management
**Status**: Ready for Dev
**Estimate**: M (Medium)
**Created Date**: 2026-01-04

## 1. User Story
**As an** administrator
**I want** to view, search, filter, and manage all users on the platform
**So that** I can monitor user accounts and perform administrative actions like locking/unlocking users and updating roles

## 2. Business Context & Value
Administrators currently have no UI to manage users - the "Manage Users" button on the admin dashboard is disabled. This story enables:
- Platform-wide user visibility for support and compliance
- Quick account status management (lock/unlock)
- Role management (promote/demote users)
- User lookup by name, email, role, or status

A future story will add "Force Password Reset" functionality (see `USER-MANAGEMENT-admin-force-password-reset.md`).

## 3. Acceptance Criteria

### Page & Navigation
*   [ ] **Scenario 1**: Access user management page
    *   Given I am logged in as an admin
    *   When I click "Users" in the ADMINISTRATION sidebar section
    *   Then I navigate to `/admin/users`
    *   And I see a paginated DataTable of all users

*   [ ] **Scenario 2**: Quick action from admin dashboard
    *   Given I am on the admin dashboard
    *   When I click the "Manage Users" button
    *   Then I navigate to `/admin/users`

### Search & Filter
*   [ ] **Scenario 3**: Search users by name or email
    *   Given I am viewing the users list
    *   When I enter text in the search field
    *   Then the list filters to show users matching email, first name, or last name

*   [ ] **Scenario 4**: Filter users by role
    *   Given I am viewing the users list
    *   When I select one or more roles from the role filter (USER, ADMIN)
    *   Then the list shows only users with those roles

*   [ ] **Scenario 5**: Filter users by status
    *   Given I am viewing the users list
    *   When I toggle the enabled/disabled or validated/unvalidated filters
    *   Then the list filters accordingly

*   [ ] **Scenario 6**: Sort users by column
    *   Given I am viewing the users list
    *   When I click on a sortable column header (Email, Role, Created, Last Login)
    *   Then the list is sorted by that column
    *   And clicking again reverses the sort direction

### User Details Modal
*   [ ] **Scenario 7**: View user details
    *   Given I am viewing the users list
    *   When I click on a user row or "View" action
    *   Then a modal opens showing user details:
        *   Email, First Name, Last Name
        *   Role with badge
        *   Account status (enabled/disabled, validated/unvalidated)
        *   Created date
        *   Last login date
        *   List of organizations the user belongs to (with role in each)

### User Actions
*   [ ] **Scenario 8**: Lock a user
    *   Given I am viewing a user who is currently enabled
    *   When I click the "Lock" action button
    *   Then the user is locked immediately (no confirmation)
    *   And the UI updates to show the user as disabled
    *   And a success toast is displayed

*   [ ] **Scenario 9**: Unlock a user
    *   Given I am viewing a user who is currently disabled
    *   When I click the "Unlock" action button
    *   Then the user is unlocked immediately (no confirmation)
    *   And the UI updates to show the user as enabled
    *   And a success toast is displayed

*   [ ] **Scenario 10**: Update user role
    *   Given I am viewing a user
    *   When I change the role dropdown (USER ↔ ADMIN)
    *   Then the user's role is updated immediately
    *   And a success toast is displayed

*   [ ] **Scenario 11**: Force password reset (disabled placeholder)
    *   Given I am viewing a user
    *   When I look at the actions menu
    *   Then I see a "Force Password Reset" option that is disabled with "(Coming Soon)" label

### Quality
*   [ ] **Scenario 12**: All quality checks pass
    *   Given the implementation is complete
    *   When I run `bun run check` and backend tests
    *   Then all checks and tests pass

## 4. Technical Requirements

### Backend (Already Exists - No Changes Needed)
The following endpoints already exist in `AdminUserController.java`:
*   `POST /v1/admin/user/search` - Search users with `SortablePageInput` → `PageResource<UserResponse>`
*   `POST /v1/user/{id}/lock` - Lock user → `UserDetailsResponse`
*   `POST /v1/user/{id}/unlock` - Unlock user → `UserDetailsResponse`
*   `POST /v1/user/{id}` - Update role → `UserDetailsResponse`

`UserMetaData.java` already supports:
*   `search` - MULTI_COLUMN_FUZZY (email, firstName, lastName)
*   `email` - TEXT, sortable
*   `role` - MULTI_ENUM (USER, ADMIN), sortable
*   `enabled` - BOOLEAN
*   `validated` - BOOLEAN
*   `createdAt` - DATE, sortable
*   `lastLogin` - DATE, sortable

### Frontend - New Route
*   Create `/admin/users/+page.svelte`
*   Follow the pattern established in `/admin/organizations/+page.svelte`

### Frontend - API Controller Updates
Add to `AdminUserController.ts`:
```typescript
export async function lockUser(userId: number): Promise<UserDetailsResponse>
export async function unlockUser(userId: number): Promise<UserDetailsResponse>
export async function updateUserRole(userId: number, role: Role): Promise<UserDetailsResponse>
export async function getUserDetails(userId: number): Promise<UserDetailsResponse>
```

Note: `getUserDetails` may require a new backend endpoint `GET /v1/admin/user/{id}` if not already available. Check and add if needed.

### Frontend - DataTable Configuration
```typescript
const columns: DataTableColumn<UserResponse>[] = [
  {
    key: 'search',
    label: 'User',
    filterable: true,
    filterType: 'FUZZY_TEXT',
    filterPlaceholder: 'Search by name or email...',
    colSpan: 3
  },
  {
    key: 'email',
    label: 'Email',
    sortable: true,
    colSpan: 2
  },
  {
    key: 'role',
    label: 'Role',
    sortable: true,
    filterable: true,
    filterType: 'MULTI_ENUM',
    filterOptions: [
      { value: 'USER', label: 'User' },
      { value: 'ADMIN', label: 'Admin' }
    ],
    colSpan: 1
  },
  {
    key: 'enabled',
    label: 'Status',
    filterable: true,
    filterType: 'BOOLEAN',
    colSpan: 1
  },
  {
    key: 'createdAt',
    label: 'Created',
    sortable: true,
    colSpan: 2
  },
  {
    key: 'lastLogin',
    label: 'Last Login',
    sortable: true,
    colSpan: 2
  },
  {
    key: 'actions',
    label: 'Actions',
    colSpan: 1
  }
];
```

### Frontend - User Details Modal Component
*   Create `UserDetailsSheet.svelte` in `$lib/components/admin/` or inline in page
*   Use Sheet or Dialog component from shadcn-svelte
*   Fetch full user details including organizations when modal opens
*   Display:
    *   Avatar placeholder with initials
    *   Name and email
    *   Role badge (colored)
    *   Status badges (Enabled/Disabled, Validated/Unvalidated)
    *   Timestamps (Created, Last Login)
    *   Organizations list with role in each org

### Frontend - Sidebar Update
In `AppSidebarNav.svelte`, add "Users" menu item in ADMINISTRATION section:
```svelte
<Sidebar.MenuItem>
  <Sidebar.MenuButton
    onclick={() => goto(CLIENT_ROUTES.ADMIN_USERS_PAGE.path)}
    isActive={currentPath.startsWith('/admin/users')}
  >
    <Users class="size-4" />
    <span>Users</span>
  </Sidebar.MenuButton>
</Sidebar.MenuItem>
```

### Frontend - Routes Config
Add to `routes.ts`:
```typescript
ADMIN_USERS_PAGE: {
  path: '/admin/users',
  type: RouteType.PRIVATE
}
```

### Frontend - Admin Dashboard Update
In `/admin/dashboard/+page.svelte`, enable the "Manage Users" button:
```svelte
<Button
  class="bg-gradient-to-r from-primary to-accent..."
  onclick={() => goto(CLIENT_ROUTES.ADMIN_USERS_PAGE.path)}
>
  <UserCog class="h-5 w-5"/>
  <span>Manage Users</span>
</Button>
```

## 5. Design & UI/UX

### Users Table
*   Follow the Organizations page pattern (glassmorphism cards, DataTable)
*   Row layout:
    *   User column: Avatar (initials) + Full Name + Email (smaller, muted)
    *   Email column: Full email (desktop only, hidden on mobile since shown in User column)
    *   Role: Badge (USER = default, ADMIN = primary/accent color)
    *   Status: Badge (Enabled = green/success, Disabled = red/destructive)
    *   Created: Formatted date
    *   Last Login: Formatted date or "Never" if null
    *   Actions: Dropdown menu with View, Lock/Unlock, Role dropdown

### User Details Modal
*   Sheet sliding in from right (consistent with other sheets in app)
*   Header: User avatar (large) + name + email
*   Sections:
    *   **Account Info**: Role dropdown (editable), Status badges
    *   **Activity**: Created date, Last login date
    *   **Organizations**: List of org cards with org name + user's role in that org
*   Footer: Lock/Unlock button, Close button

### Actions Dropdown (in table row)
*   View Details
*   Lock User / Unlock User (toggle based on current state)
*   Change Role → submenu with USER, ADMIN options
*   Force Password Reset (disabled, grayed out, "(Coming Soon)")

### Status Indicators
*   Enabled: Green badge "Active"
*   Disabled: Red badge "Locked"
*   Validated: No special indicator (default state)
*   Unvalidated: Yellow/warning badge "Pending Verification"

## 6. Implementation Notes / Research

### Reference Files
*   **Page Pattern**: `client/src/routes/(authenticated)/admin/organizations/+page.svelte`
*   **DataTable**: `client/src/lib/components/data-table/DataTable.svelte`
*   **API Client**: `client/src/lib/api/admin/AdminUserController.ts` (already has `searchUsers`)
*   **Backend Controller**: `server/src/main/java/io/github/eventify/api/admin/controller/AdminUserController.java`
*   **User Metadata**: `server/src/main/java/io/github/eventify/api/user/model/UserMetaData.java`
*   **UserDetailsResponse**: Includes `organizations` list - use this for modal

### Backend Endpoint Check
Verify if `GET /v1/admin/user/{id}` exists for fetching single user details. If not, either:
1. Add the endpoint (simple: reuse `UserService.getUserWithOrganizations`)
2. Or use the data from `UserDetailsResponse` returned by lock/unlock/updateRole

### Existing Frontend Functions
`AdminUserController.ts` already has:
*   `searchUsers(input: SortablePageInput)` - for DataTable
*   `searchUsersByEmailAndName(query: string)` - utility for quick search

### Potential Pitfalls
*   `UserResponse` (from search) has fewer fields than `UserDetailsResponse` (from lock/unlock). May need to fetch full details when opening modal.
*   Role update returns `UserDetailsResponse` - update the table row after successful update
*   Ensure admin cannot lock themselves or demote themselves from ADMIN

### Future Story Reference
*   **Force Password Reset**: See `USER-MANAGEMENT-admin-force-password-reset.md`

### Files to Create/Modify
| Action | File |
|--------|------|
| Create | `client/src/routes/(authenticated)/admin/users/+page.svelte` |
| Create | `client/src/lib/components/admin/UserDetailsSheet.svelte` (optional) |
| Modify | `client/src/lib/api/admin/AdminUserController.ts` (add lock/unlock/updateRole) |
| Modify | `client/src/lib/config/routes.ts` (add ADMIN_USERS_PAGE) |
| Modify | `client/src/lib/components/layout/AppSidebarNav.svelte` (add Users menu item) |
| Modify | `client/src/routes/(authenticated)/admin/dashboard/+page.svelte` (enable button) |
