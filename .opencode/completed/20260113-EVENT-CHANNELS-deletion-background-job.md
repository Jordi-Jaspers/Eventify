## [2026-01-13] - Channel Deletion Background Job

### Plan (approved)
Scheduled job to clean up channels marked for deletion asynchronously. Prevents request timeouts for channels with many events.

**Requirements:**
- Job runs every 5 minutes (configurable)
- Finds channels with status `PENDING_DELETION`
- Hard deletes channels (events table doesn't exist yet)
- Logs progress: channel ID, time taken
- Idempotent and continues processing if one fails

### Actual Changes

**Backend:**
- `ChannelCleanupJob` - Scheduled job every 5 minutes, delegates to service
- `ChannelCleanupService` - Finds PENDING_DELETION channels, hard deletes each, logs progress
- `ChannelRepository.findByStatus()` - New repository method

**Refactoring (user-requested):**
- Separated scheduled jobs from services across the codebase to match consistent pattern:
  - `TokenService` → `TokenCleanupJob` + `TokenCleanupService`
  - `UserService` → `UserCleanupJob` + `UserCleanupService`  
  - `UserQuotaService` → `UserQuotaCleanupJob` + `UserQuotaCleanupService`

### Agents Used
- java-testing-agent: Created initial test suite
- java-backend-agent: Implemented job and service
- Orchestrator: Created 6 additional test files for refactored cleanup services

### Files Created
**Implementation:**
- `server/src/main/java/io/github/eventify/api/channel/job/ChannelCleanupJob.java`
- `server/src/main/java/io/github/eventify/api/channel/service/ChannelCleanupService.java`
- `server/src/main/java/io/github/eventify/api/token/job/TokenCleanupJob.java`
- `server/src/main/java/io/github/eventify/api/token/service/TokenCleanupService.java`
- `server/src/main/java/io/github/eventify/api/user/job/UserCleanupJob.java`
- `server/src/main/java/io/github/eventify/api/user/service/UserCleanupService.java`
- `server/src/main/java/io/github/eventify/api/quota/job/UserQuotaCleanupJob.java`
- `server/src/main/java/io/github/eventify/api/quota/service/UserQuotaCleanupService.java`

**Tests:**
- `server/src/test/java/io/github/eventify/api/channel/job/ChannelCleanupJobTest.java`
- `server/src/test/java/io/github/eventify/api/channel/service/ChannelCleanupServiceTest.java`
- `server/src/test/java/io/github/eventify/api/token/job/TokenCleanupJobTest.java`
- `server/src/test/java/io/github/eventify/api/token/service/TokenCleanupServiceTest.java`
- `server/src/test/java/io/github/eventify/api/user/job/UserCleanupJobTest.java`
- `server/src/test/java/io/github/eventify/api/user/service/UserCleanupServiceTest.java`
- `server/src/test/java/io/github/eventify/api/quota/job/UserQuotaCleanupJobTest.java`
- `server/src/test/java/io/github/eventify/api/quota/service/UserQuotaCleanupServiceTest.java`

### Files Modified
- `server/src/main/java/io/github/eventify/api/channel/repository/ChannelRepository.java`
- `server/src/main/java/io/github/eventify/api/token/service/TokenService.java`
- `server/src/main/java/io/github/eventify/api/user/service/UserService.java`
- `server/src/main/java/io/github/eventify/api/quota/service/UserQuotaService.java`
- `server/src/test/java/io/github/eventify/api/quota/service/UserQuotaServiceTest.java`

### Quality Metrics
- ✅ Tests: 40 new cleanup tests, 797 total passing
- ✅ Build: Successful
- ✅ Pattern: Consistent Job + Service separation across all cleanup tasks
