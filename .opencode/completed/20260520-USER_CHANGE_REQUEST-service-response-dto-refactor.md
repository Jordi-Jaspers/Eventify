# Move Response DTO Building from Services to Controllers

**Completed:** 2026-05-20
**Epic:** USER_CHANGE_REQUEST
**Source:** ad-hoc request

## Summary

Refactored 7 services that incorrectly built response DTOs. Services now return domain objects (Lombok classes), controllers map to response DTOs via MapStruct mappers.

## Implementation

### Services Refactored
- `DurationService` → returns `DurationDetails`
- `AdminApiKeyService` → returns `ApiKeyStats`
- `AdminStatsService` → returns `AdminStats`
- `UserAuthProviderService` → returns `List<ProviderInfo>`
- `DashboardStatsService` → returns `DashboardStats`
- `UserQuotaService` → returns `QuotaStatus`
- `SessionService` → returns `List<SessionInfo>`

### New Domain Models (Lombok @Getter @Builder)
- `DurationDetails`, `ApiKeyStats`, `AdminStats`, `ProviderInfo`, `DashboardStats`, `QuotaStatus`, `SessionInfo`

### New/Updated MapStruct Mappers
- `DurationMapper`, `AdminApiKeyStatsMapper`, `AdminStatsMapper`, `ProviderMapper`, `DashboardStatsMapper`, `QuotaMapper`, `SessionMapper`

### Controllers Updated
- `UserChannelController`, `OrganizationChannelController`, `AdminApiKeyController`, `AdminDashboardController`, `UserProviderController`, `DashboardController`, `OrganizationDashboardController`, `UserQuotaController`, `SessionController`

### Tests
- ~68 tests updated to assert on domain objects with getter syntax

### Agent Instructions
- Updated `.opencode/agents/spring-backend-agent.md` with rules enforcing domain-only returns from services

## Files Modified
- 7 service classes, 9 controller classes, 7 domain models (new), 7 mappers (new/updated), 7 test files, 1 agent config
