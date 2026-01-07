# User Event Quota Tracking

**Epic**: API Key Management
**Status**: Ready for Dev
**Estimate**: M (Medium)
**Created Date**: 2026-01-06

## 1. User Story
**As a** platform operator
**I want** to track and enforce monthly event quotas for users
**So that** users are limited to 1,000 events per month and can monitor their usage

## 2. Business Context & Value
Event quotas are essential for:
- **Fair Usage**: Preventing abuse of the platform by limiting free/personal tier usage
- **Business Model**: Creating differentiation between personal (limited) and organization (unlimited) accounts
- **Capacity Planning**: Understanding platform usage patterns
- **User Transparency**: Users can see their quota usage and plan accordingly

Organizations have unlimited events, so this only applies to personal (user-scoped) API keys.

## 3. Acceptance Criteria

### Quota Enforcement
*   [ ] **Scenario 1**: User under quota can send events
    *   Given I have used 500 events this month (under 1,000 limit)
    *   When I send an event via my personal API key
    *   Then the event is accepted (201 response)
    *   And my usage count is incremented

*   [ ] **Scenario 2**: User at quota is blocked
    *   Given I have used 1,000 events this month (at limit)
    *   When I try to send another event via my personal API key
    *   Then I receive a 429 Too Many Requests response
    *   And the response includes `Retry-After` header indicating quota reset
    *   And error code is `QUOTA_EXCEEDED`

*   [ ] **Scenario 3**: Organization keys have no quota
    *   Given I'm sending events via an organization API key
    *   When I send any number of events
    *   Then events are always accepted (no quota limit)

*   [ ] **Scenario 4**: Quota resets on first of month
    *   Given today is February 1st
    *   And I used 1,000 events in January
    *   When I send an event
    *   Then the event is accepted
    *   And my February usage count is 1

### Usage Tracking
*   [ ] **Scenario 5**: Usage is tracked per user (not per key)
    *   Given I have 3 personal API keys
    *   When I send 100 events via key A, 200 via key B, 300 via key C
    *   Then my total usage shows 600 events
    *   And quota is shared across all my keys

*   [ ] **Scenario 6**: User can view current usage
    *   Given I am on my Developer settings page
    *   When I view my quota status
    *   Then I see current usage (e.g., "342 / 1,000 events")
    *   And I see the reset date (e.g., "Resets Feb 1, 2026")

### Usage API
*   [ ] **Scenario 7**: Backend returns quota information
    *   Given I am an authenticated user
    *   When I GET `/v1/user/quota`
    *   Then I receive my current quota status:
        *   `used`: current month usage
        *   `limit`: 1000 (personal tier limit)
        *   `remaining`: limit - used
        *   `periodStart`: first of current month
        *   `periodEnd`: first of next month
        *   `percentUsed`: percentage for progress bar

## 4. Technical Requirements

### Database Schema

#### Table: `user_event_quota`
```sql
CREATE TABLE user_event_quota (
    id              SERIAL PRIMARY KEY,
    user_id         INTEGER NOT NULL UNIQUE,
    event_count     BIGINT NOT NULL DEFAULT 0,
    period_start    DATE NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE
);

CREATE INDEX idx_user_event_quota_user ON user_event_quota(user_id);
CREATE INDEX idx_user_event_quota_period ON user_event_quota(period_start);

COMMENT ON TABLE user_event_quota IS 'Tracks monthly event usage per user for quota enforcement';
COMMENT ON COLUMN user_event_quota.event_count IS 'Number of events sent in the current period';
COMMENT ON COLUMN user_event_quota.period_start IS 'First day of the current quota period (month)';
```

### JPA Entity

#### `UserEventQuota.java`
Location: `server/src/main/java/io/github/eventify/api/quota/model/UserEventQuota.java`

```java
@Entity
@Table(name = "user_event_quota")
@Getter
@Setter
@NoArgsConstructor
public class UserEventQuota {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    @Column(name = "event_count", nullable = false)
    private Long eventCount = 0L;
    
    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
    
    public UserEventQuota(User user) {
        this.user = user;
        this.eventCount = 0L;
        this.periodStart = LocalDate.now().withDayOfMonth(1);
    }
    
    public boolean isCurrentPeriod() {
        LocalDate currentPeriodStart = LocalDate.now().withDayOfMonth(1);
        return periodStart.equals(currentPeriodStart);
    }
    
    public void resetIfNewPeriod() {
        if (!isCurrentPeriod()) {
            this.eventCount = 0L;
            this.periodStart = LocalDate.now().withDayOfMonth(1);
        }
    }
}
```

### Quota Service

#### `UserQuotaService.java`
Location: `server/src/main/java/io/github/eventify/api/quota/service/UserQuotaService.java`

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class UserQuotaService {
    
    private static final long MONTHLY_EVENT_LIMIT = 1000L;
    
    private final UserEventQuotaRepository quotaRepository;
    
    /**
     * Check if user can send an event.
     * Returns true if under quota, false if quota exceeded.
     */
    @Transactional(readOnly = true)
    public boolean canSendEvent(Long userId) {
        UserEventQuota quota = getOrCreateQuota(userId);
        quota.resetIfNewPeriod();
        return quota.getEventCount() < MONTHLY_EVENT_LIMIT;
    }
    
    /**
     * Increment the event count for a user.
     * Should be called after successful event ingestion.
     */
    @Transactional
    public void incrementUsage(Long userId) {
        UserEventQuota quota = getOrCreateQuota(userId);
        quota.resetIfNewPeriod();
        quota.setEventCount(quota.getEventCount() + 1);
        quotaRepository.save(quota);
    }
    
    /**
     * Get current quota status for a user.
     */
    @Transactional(readOnly = true)
    public UserQuotaResponse getQuotaStatus(Long userId) {
        UserEventQuota quota = getOrCreateQuota(userId);
        quota.resetIfNewPeriod();
        
        LocalDate periodEnd = quota.getPeriodStart().plusMonths(1);
        long remaining = Math.max(0, MONTHLY_EVENT_LIMIT - quota.getEventCount());
        double percentUsed = (quota.getEventCount() * 100.0) / MONTHLY_EVENT_LIMIT;
        
        return new UserQuotaResponse(
            quota.getEventCount(),
            MONTHLY_EVENT_LIMIT,
            remaining,
            quota.getPeriodStart(),
            periodEnd,
            Math.min(100, percentUsed)
        );
    }
    
    private UserEventQuota getOrCreateQuota(Long userId) {
        return quotaRepository.findByUserId(userId)
            .orElseGet(() -> {
                User user = userRepository.findById(userId).orElseThrow();
                return quotaRepository.save(new UserEventQuota(user));
            });
    }
}
```

### API Endpoint

#### Get User Quota
```
GET /v1/user/quota
Authorization: Bearer {jwt}

Response (200 OK):
{
  "used": 342,
  "limit": 1000,
  "remaining": 658,
  "periodStart": "2026-01-01",
  "periodEnd": "2026-02-01",
  "percentUsed": 34.2
}
```

Add to `UserController.java` or create `UserQuotaController.java`:
```java
@GetMapping("/quota")
public ResponseEntity<UserQuotaResponse> getQuota() {
    User user = SecurityUtil.getLoggedInUser();
    return ResponseEntity.ok(quotaService.getQuotaStatus(user.getId()));
}
```

### Path Constants
Add to `Paths.java`:
```java
public static final String USER_QUOTA_PATH = USERS_PATH + "/quota";
```

### Integration with Event Ingestion

When processing events via API key (in the future Event Ingestion story), the flow is:

```java
// In EventIngestionService (future story)
public void ingestEvent(ApiKeyPrincipal principal, EventRequest request) {
    // For user keys, check and update quota
    if (principal.isUserKey()) {
        if (!quotaService.canSendEvent(principal.getUserId())) {
            throw new QuotaExceededException();
        }
    }
    
    // ... process event ...
    
    // Increment quota after successful ingestion
    if (principal.isUserKey()) {
        quotaService.incrementUsage(principal.getUserId());
    }
}
```

### Error Response for Quota Exceeded

Add to `ApiErrorCode.java`:
```java
QUOTA_EXCEEDED("QUOTA_001", "Monthly event quota exceeded")
```

Response format:
```json
{
  "errorCode": "QUOTA_001",
  "errorMessage": "Monthly event quota exceeded",
  "statusCode": 429,
  "statusMessage": "Too Many Requests"
}
```

Include `Retry-After` header with seconds until next month:
```java
long secondsUntilReset = Duration.between(
    OffsetDateTime.now(),
    LocalDate.now().plusMonths(1).withDayOfMonth(1).atStartOfDay().atOffset(ZoneOffset.UTC)
).getSeconds();

response.setHeader("Retry-After", String.valueOf(secondsUntilReset));
```

## 5. Design & UI/UX

### Quota Progress Bar Component
Already specified in Story 3, but here's the detailed behavior:

```svelte
<!-- QuotaProgressBar.svelte -->
<script lang="ts">
  interface Props {
    used: number;
    limit: number;
    periodEnd: string;
  }
  
  let { used, limit, periodEnd }: Props = $props();
  
  let percentage = $derived((used / limit) * 100);
  let remaining = $derived(limit - used);
  
  // Color based on usage
  let barColor = $derived(
    percentage < 60 ? 'bg-green-500' :
    percentage < 80 ? 'bg-yellow-500' :
    percentage < 95 ? 'bg-orange-500' :
    'bg-red-500'
  );
  
  function formatDate(dateStr: string): string {
    return new Date(dateStr).toLocaleDateString('en-US', { 
      month: 'short', 
      day: 'numeric', 
      year: 'numeric' 
    });
  }
</script>

<div class="rounded-lg border border-border/50 bg-card/50 p-4">
  <div class="flex justify-between items-center mb-2">
    <span class="text-sm font-medium">Monthly Event Usage</span>
    <span class="text-sm text-muted-foreground">
      {used.toLocaleString()} / {limit.toLocaleString()}
    </span>
  </div>
  
  <div class="w-full h-2 bg-muted rounded-full overflow-hidden">
    <div 
      class="h-full transition-all duration-500 {barColor}"
      style="width: {Math.min(percentage, 100)}%"
    />
  </div>
  
  <div class="flex justify-between items-center mt-2 text-xs text-muted-foreground">
    <span>{remaining.toLocaleString()} events remaining</span>
    <span>Resets {formatDate(periodEnd)}</span>
  </div>
</div>
```

### Warning States

At different thresholds, show visual warnings:

| Usage % | Color | Additional UI |
|---------|-------|---------------|
| 0-59% | Green | Normal display |
| 60-79% | Yellow | Subtle warning icon |
| 80-94% | Orange | Warning banner: "Approaching limit" |
| 95-99% | Red | Alert: "Almost at limit" |
| 100% | Red | Alert: "Limit reached. Events blocked until {date}" |

## 6. Implementation Notes / Research

### File Locations

#### Backend
```
server/src/main/java/io/github/eventify/api/quota/
├── model/
│   ├── UserEventQuota.java           # New entity
│   └── response/
│       └── UserQuotaResponse.java    # New DTO
├── repository/
│   └── UserEventQuotaRepository.java # New repository
├── service/
│   └── UserQuotaService.java         # New service
└── controller/
    └── UserQuotaController.java      # New controller (or add to UserController)
```

#### Frontend
```
client/src/lib/components/api-keys/
└── QuotaProgressBar.svelte           # New (from Story 3)

client/src/lib/api/user/
└── UserQuotaController.ts            # Generated from OpenAPI
```

### Liquibase Migration
Create: `server/src/main/resources/db/changelog/changesets/202601061100-PRD-user-event-quota-table.xml`

### Concurrency Consideration

For high-throughput scenarios, consider:
1. **Optimistic locking**: Add `@Version` to entity
2. **Atomic increment**: Use native query instead of read-modify-write

```java
@Modifying
@Query("UPDATE UserEventQuota q SET q.eventCount = q.eventCount + 1, q.updatedAt = CURRENT_TIMESTAMP " +
       "WHERE q.user.id = :userId AND q.periodStart = :periodStart")
int incrementEventCount(@Param("userId") Long userId, @Param("periodStart") LocalDate periodStart);
```

### Batch Reset Job (Optional Enhancement)

While `resetIfNewPeriod()` handles lazy reset, a scheduled job can proactively reset:

```java
@Scheduled(cron = "0 0 0 1 * *")  // First of every month at midnight
public void resetAllQuotas() {
    LocalDate newPeriod = LocalDate.now().withDayOfMonth(1);
    quotaRepository.resetAllForNewPeriod(newPeriod);
    log.info("Reset all user quotas for period starting {}", newPeriod);
}
```

This is optional since lazy reset works correctly.

### Testing Strategy

1. **Unit Tests**
   - Quota check under limit
   - Quota check at limit
   - Period reset logic
   - Increment logic

2. **Integration Tests**
   - Full flow: authenticate with API key → check quota → increment
   - Verify 429 response when quota exceeded
   - Verify organization keys bypass quota

### Configuration (Future)

Make quota limit configurable:
```yaml
eventify:
  quota:
    personal-monthly-limit: 1000
```

For MVP, hardcoding 1000 is acceptable.
