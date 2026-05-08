## [2026-01-03] - Sortable Tables + Organization Owner Display

### Feature plan approved by user
**Requirements Summary**

- Display owner name in Organizations admin table (was showing "No owner" even when owner exists)
- Add disabled OWNER role button in AddMemberSheet with tooltip explaining only 1 owner allowed
- Create reusable SortableTableHeader component for sorting support
- Organizations table columns should be sortable based on OrganizationMetaData fields (name, status, memberCount)

**Technical Approach**

**Frontend Changes:**
1. Create `SortableTableHeader` component - reusable sortable table header
2. Update `OrganizationController.ts` - add sort params to search
3. Update `OrganizationListService.svelte.ts` - add sort state and methods
4. Update `/admin/organizations/+page.svelte` - add Owner column and sorting
5. Update `AddMemberSheet.svelte` - add disabled OWNER button with tooltip

**Backend (Already Implemented):**
- `OrganizationMetaData` defines sortable fields: name, status, memberCount
- `SortablePageInput` accepts `sortOrder` array with field name and direction
- `OrganizationResponse` includes `owner` field with `UserResponse`

**Implementation Workflow**

Phase 1: Frontend Implementation
Agent: sveltekit-frontend-agent
Task: Implement all frontend changes

**Deliverable:**
- Reusable SortableTableHeader component
- Owner column in organizations table
- Sorting functionality for name, status, memberCount
- Disabled OWNER button with tooltip in AddMemberSheet
- Type checks passing

**Success Criteria**

- Users can see owner name in organizations table
- "No owner" shown in muted style when owner is null
- OWNER role button visible but disabled with tooltip
- Table headers are clickable for sortable fields
- Sort direction toggles ASC/DESC on same column click
- Sort indicator shows on active column
- Type checks pass (bun run check)

---

### Actual changelog after completion
#### Summary
Fixed organization table to display owner names correctly and added reusable sortable table functionality supporting the Jframe SortableColumn pattern.

#### Changes
**Frontend:**
- Created `SortableTableHeader.svelte` - reusable component with sortable/non-sortable column support
- Created `$lib/components/ui/table/index.ts` - exports for table components
- Added Owner column to organizations admin table with proper null handling
- Added sort functionality to organizations table (name, status, memberCount columns)
- Added disabled OWNER button with tooltip in AddMemberSheet
- Updated OrganizationController with sortKey and sortDirection params
- Updated OrganizationListService with sort state and setSort() method

**Component Features:**
- SortableTableHeader accepts column configuration with sortable flags
- Shows sort indicators (ChevronUp/ChevronDown) on active sort column
- Toggles ASC/DESC on same column, defaults to ASC on new column
- Grid layout matches existing table styling

#### Agents Used
- sveltekit-frontend-agent (all implementation)

#### Files Created
- `client/src/lib/components/ui/table/SortableTableHeader.svelte`
- `client/src/lib/components/ui/table/index.ts`

#### Files Modified
- `client/src/routes/(authenticated)/admin/organizations/+page.svelte`
- `client/src/lib/components/members/AddMemberSheet.svelte`
- `client/src/lib/api/organization/OrganizationController.ts`
- `client/src/lib/api/organization/OrganizationListService.svelte.ts`

#### Quality Metrics
- bun run check: 0 errors, 0 warnings
- All explicit TypeScript types
- Svelte 5 runes patterns used ($state, $props)
- Glassmorphism styling maintained

#### Notes
- SortableTableHeader component can be reused for other admin tables (users, etc.)
- Sorting integrates with Jframe's SortablePageInput pattern
- Owner column is not sortable (would require backend join/query changes)

---

## [2026-01-03] - Bug Fixes: User Search and OWNER Button Logic

### Issues Fixed

1. **User search not working in AddMemberSheet**
   - **Root cause:** Backend uses `@GetMapping` with `@RequestBody` for `/v1/organization/{orgId}/members/search`, which is non-standard HTTP. The openapi-fetch client doesn't support sending body with GET requests.
   - **Fix:** Changed `searchUsersToAdd()` to use `/v1/admin/user/search` (POST) instead. This works because users adding members must already be admins/owners who have access to the admin endpoint.

2. **OWNER button always grayed out**
   - **Root cause:** Button was hardcoded to `disabled={true}` unconditionally.
   - **Fix:** Made button conditionally enabled based on:
     - `hasOwner`: Whether org already has an owner (disabled if true)
     - `isGlobalAdmin`: Whether current user is a global admin (disabled if false)
   - Added dynamic tooltip messages:
     - "This organization already has an owner" (when hasOwner)
     - "Only global admins can assign owners" (when not isGlobalAdmin)

### Files Modified
- `client/src/lib/api/organization/OrganizationMembershipController.ts` - Fixed searchUsersToAdd to use POST endpoint
- `client/src/lib/components/members/AddMemberSheet.svelte` - Added hasOwner/isGlobalAdmin props, conditional OWNER button
- `client/src/routes/(authenticated)/organizations/[orgId]/members/+page.svelte` - Added hasOwner derived value, passed new props

### Quality Metrics
- bun run check: 0 errors, 0 warnings
