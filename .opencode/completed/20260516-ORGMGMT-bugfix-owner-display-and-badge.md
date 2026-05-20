# Bugfix: Organization Table Owner Display & Badge Styling

**Completed:** 2026-05-16
**Epic:** USER_CHANGE_REQUEST
**Source:** ad-hoc request

## Summary

Fixed two bugs in the admin organizations table: owner always showing "No owner" and badge styling inconsistency.

## Bugs Fixed

### 1. Owner Always Shows "No Owner"
**Root cause:** `OrganizationService.searchOrganizations()` enriched `memberCount` but never populated the `@Transient owner` field. The `owner` was only set during `create()`.

**Fix:** Added owner lookup via `findByOrganizationIdAndRole(orgId, OWNER)` within the `searchOrganizations` method, with `Hibernate.initialize()` to avoid `LazyInitializationException` (the mapper runs outside the transaction boundary).

### 2. Badge Styling Inconsistency
**Root cause:** Organization status badges used `min-w-[90px] justify-center` classes not used elsewhere, and displayed raw enum values (`ACTIVE`, `SUSPENDED`) instead of human-readable labels like other tables.

**Fix:** Removed extra classes, added `getOrganizationStatusLabel()` utility function to display "Active"/"Suspended" labels consistent with the users table pattern.

## Files Modified

- `server/src/main/java/io/github/eventify/api/organization/service/OrganizationService.java` — added owner enrichment in searchOrganizations
- `client/src/lib/utils/organization.ts` — added `getOrganizationStatusLabel()` function
- `client/src/routes/(authenticated)/admin/resources/organizations/+page.svelte` — use label function, remove extra badge classes
