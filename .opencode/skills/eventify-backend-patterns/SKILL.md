---
name: eventify-backend-patterns
description: Eventify backend code patterns, naming conventions, and implementation style. Use when writing Java backend code for the Eventify project (controllers, services, entities, DTOs, validators, mappers, repositories, exceptions). Extends spring-boot-standards with Eventify-specific conventions.
metadata:
  skill-type: patterns
  language: java
  framework: spring-boot
---

# Eventify Backend Patterns

## Java Code Rules (Non-Negotiable)

```java
// ✅ CORRECT
public void process(final User user) {
    final String email = user.getEmail();
    final boolean valid = validator.validate(email);
}

// ❌ WRONG
public void process(User user) {        // Missing final
    var email = user.getEmail();         // NO var
    boolean valid = validator.validate(email);  // Missing final
}
```

- ✅ All variables `final`
- ✅ Explicit types (NEVER `var`)
- ✅ Constructor injection (NO `@Autowired` fields)
- ✅ NO Java records (standard classes only)
- ✅ NO inner classes
- ✅ Services return domain objects/entities ONLY — NEVER response DTOs
- ✅ Controllers map entities → response DTOs (via MapStruct)
- ✅ Enums for categorical fields (`@Enumerated(EnumType.STRING)`), never String for fixed sets
- ✅ Lombok: `@Getter`, `@Setter`, `@Builder`, `@RequiredArgsConstructor`

---

## 1. Controller Pattern

```java
@Tag(name = "Channels", description = "Channel operations")
@RestController
@RequiredArgsConstructor
public class OrganizationChannelController {

    private final ChannelService channelService;
    private final ChannelValidator channelValidator;
    private final ChannelMapper channelMapper;

    @PostMapping(path = Paths.ORGANIZATION_CHANNELS, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(CREATED)
    @PreAuthorize("@channelSecurity.canCreate(#orgId, principal)")
    @Operation(summary = "Create channel", description = "Creates a new channel for the organization")
    public ResponseEntity<ChannelResponse> createChannel(
            @PathVariable final Long orgId,
            @RequestBody final CreateChannelRequest request,
            @AuthenticationPrincipal final UserTokenPrincipal principal) {
        channelValidator.validateAndThrow(request);
        final Channel channel = channelService.create(orgId, request);
        return ResponseEntity.status(CREATED).body(channelMapper.toResourceObject(channel));
    }
}
```

**Rules:**
- `@RestController` + `@RequiredArgsConstructor` + `@Tag` — always
- No class-level `@RequestMapping` — paths per method only
- Paths from `io.github.eventify.api.Paths` constants
- Method annotation order: `@PostMapping` → `@ResponseStatus` → `@PreAuthorize` → `@Operation` → method
- Return `ResponseEntity<T>` with explicit status
- Controller body: validate → call service → map → return. No business logic.
- Auth principals: `UserTokenPrincipal` (JWT), `ApiKeyPrincipal` (API key / ingestion)

## 2. Service Pattern

Split services by responsibility — multiple per module is the norm:

| Suffix | Purpose |
|---|---|
| `{Domain}Service` | Main CRUD |
| `{Domain}SecurityService` | SpEL access control |
| `{Domain}CreationService` | Complex creation logic |
| `{Domain}IngestionService` | External event ingestion |
| `{Domain}CleanupService` | Background / retention |

```java
@Service
@RequiredArgsConstructor
public class ChannelService {

    @Transactional
    public Channel create(final Long orgId, final CreateChannelRequest request) { ... }

    public Channel getById(final Long id) { ... }
}

// Security service — bean name for SpEL
@Service("channelSecurity")
@RequiredArgsConstructor
public class ChannelSecurityService {

    public boolean canCreate(final Long orgId, final UserTokenPrincipal principal) { ... }
}
// Used in controller: @PreAuthorize("@channelSecurity.canCreate(#orgId, principal)")
```

**Rules:**
- `@Transactional` per method, not class-level
- Only use `@Transactional` on complex operations involving multiple steps (e.g., create with related entities).
- Security services: `@Service("domainSecurity")` naming for SpEL
- Use `SecurityUtil.getLoggedInUser()` / `SecurityUtil.hasAuthority()` within services
- Returns domain entities, not DTOs

## 2a. Security Pattern (`@PreAuthorize`)

All resource-scoped endpoints MUST use `@PreAuthorize` with a security service bean:

```java
// Single resource
@PreAuthorize("@channelSecurity.canAccessChannelAsUser(#id, principal) or hasAuthority('MANAGE_USERS')")

// Batch resource — security bean checks ALL IDs (all-or-nothing)
@PreAuthorize("@channelSecurity.canAccessChannelsAsUser(#request.channelIds, principal) or hasAuthority('MANAGE_USERS')")

// Organization resource
@PreAuthorize("@orgSecurity.isOwnerOrAdmin(#orgId, principal.user.id) or hasAuthority('MANAGE_ORGANIZATIONS')")
```

**Rules:**
- ✅ `@PreAuthorize` on every endpoint accessing specific resources by ID
- ✅ Security bean returns boolean; admin bypass via `or hasAuthority(...)` in SpEL
- ✅ Rejected = 403 for user resources, 404 for org resources
- ❌ NEVER check ownership inside service methods
- ❌ NEVER skip `@PreAuthorize` and rely on scoped queries for security

**When NOT needed:** public endpoints (`/api/public/**`) and user-profile endpoints accessing only `principal` data (no resource ID in path)

## 3. Entity Pattern

```java
@Getter @Setter @Entity @NoArgsConstructor
@Table(name = "channel")
public class Channel implements PageableItem, Serializable {
    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID; // from Main.java

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    private ChannelStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    // JSONB field
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private ChannelMetadata metadata;
}
```

**Rules:**
- Implements `PageableItem` (jframe) + `Serializable`
- `serialVersionUID = SERIAL_VERSION_UID` (constant from `Main.java`)
- JSONB: `@JdbcTypeCode(SqlTypes.JSON)` + `columnDefinition = "jsonb"`
- Enums: always `EnumType.STRING`
- Relations: `FetchType.LAZY` default
- Computed/derived fields: `@Transient`

### Entity Conversion Methods

Entities can own conversion logic to related entities (audit records, history):

```java
public ApiKeyAudit toAuditRecord(final User revoker) {
    return new ApiKeyAudit(this.name, this.prefix, this.user, this.organization, this.createdAt, revoker);
}
```

### Entity/Schema Alignment

`@JoinColumn` nullable MUST match database constraints:

```java
// ✅ DB has ON DELETE SET NULL → entity allows null
@JoinColumn(name = "revoked_by")  // nullable by default

// ❌ DB has ON DELETE SET NULL but entity says NOT NULL → constraint violation!
@JoinColumn(name = "revoked_by", nullable = false)
```

Audit tables: FKs should be nullable (user may be deleted later). Store ID directly for deleted references.

## 4. Request DTO

```java
@Getter @Setter @NoArgsConstructor @Accessors(chain = true)
public class CreateChannelRequest {
    
    @Schema(description = "Channel unique identifier", example = "42", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "Channel unique identifier", example = "42", requiredMode = Schema.RequiredMode.REQUIRED)
    private String description;

    @Schema(description = "Channel unique identifier", example = "42", requiredMode = Schema.RequiredMode.REQUIRED)
    private ChannelType type;
}
```

**Rules:**
- `@Schema` on class AND every field
- `requiredMode` set explicitly per field
- **NOT USE** `@NotNull, @Size, @Valid` — validation is in Validator classes


## 5. Response DTO

```java
@Getter @Setter @NoArgsConstructor @Accessors(chain = true)
@Schema(description = "Channel resource object")
public class ChannelResponse implements PageableItemResource {

    @Schema(description = "Channel unique identifier", example = "42", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "Channel display name", example = "prod-errors", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "Channel status", example = "ACTIVE", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private ChannelStatus status;
}
```

**Rules:**
- Implements `PageableItemResource` (jframe) if used in paginated endpoints
- `@Schema` on class AND every field
- `requiredMode` set explicitly per field

## 6. Validator Pattern

```java
@Component
public class ChannelValidator implements Validator<CreateChannelRequest> {

    // Error message constants
    public static final String NAME_REQUIRED = "Channel name is required";
    public static final String NAME_TOO_LONG = "Channel name must not exceed 100 characters";
    
    // Field name constants for error reporting
    public static final String FIELD_NAME = "name";
    
    @Override
    public void validate(final CreateChannelRequest request, final ValidationResult result) {
        result.rejectField(FIELD_NAME, request.getName())
            .whenNull(NAME_REQUIRED)
            .orWhen(String::isBlank, NAME_REQUIRED)
            .orWhen(val -> val.length() > 100, NAME_TOO_LONG);
    }

    public void validateAndThrow(final CreateChannelRequest request) {
        final ValidationResult result = new ValidationResult();
        validate(request, result);
        if (result.hasErrors()) throw new ValidationException(result);
    }
}
```

**Rules:**
- No `@Valid` / `@Validated` anywhere in the codebase
- Field/message constants are `public static final String` — used in tests
- Fluent DSL: `.rejectField(FIELD, value).whenNull(MSG).orWhen(predicate, MSG)`
- `validateAndThrow()` called from controller before service call
- Sections in constants: field names, error messages, other validation params

## 7. Mapper Pattern

```java
@Mapper(config = SharedMapperConfig.class, uses = {DateTimeMapper.class})
public abstract class ChannelMapper extends PageMapper<ChannelDetailsResponse, Channel> {

    @Override
    @Named("toResourceObject")
    public abstract ChannelDetailsResponse toResourceObject(Channel channel);

    @IterableMapping(qualifiedByName = "toResourceObject")
    public abstract List<ChannelDetailsResponse> toResourceObjects(List<Channel> channels);
}
```

**Rules:**
- Abstract class extending `PageMapper<ResponseDTO, Entity>` (provides `toPageResource(Page<E>)`)
- `@Mapper(config = SharedMapperConfig.class)` — uses shared config from jframe
- `@Named("toResourceObject")` on the primary mapping method
- Method naming: `toResourceObject`, `toPageResource`, `toDetailsResponse`, `toCreatedResponse`

## 8. Repository Pattern

```java
@Repository
public interface ChannelRepository extends JpaRepository<Channel, Long>, JpaSpecificationExecutor<Channel> {

    Optional<Channel> findByIdAndOrganizationId(Long id, Long orgId);

    long countByOrganizationId(Long orgId);

    @Query("SELECT c FROM Channel c WHERE c.organization.id = :orgId AND c.status != :status")
    List<Channel> findActiveByOrganization(@Param("orgId") Long orgId, @Param("status") ChannelStatus status);

    @Modifying
    @Query(value = """
            DELETE FROM channel WHERE id IN (:ids)
            """, nativeQuery = true)
    int deleteByIds(@Param("ids") List<Long> ids);
}
```

**Rules:**
- Extends `JpaRepository` + `JpaSpecificationExecutor` (for dynamic/search queries)
- Complex queries: native SQL with Java text blocks
- `@Modifying` for all write `@Query` operations
- `{Domain}MetaData` class handles JPA Specification building + sort logic
- ⚠️ Query method field names MUST exist on the entity — Spring Data derives queries from method names

## 9. Exception Pattern

```java
public class ChannelPausedException extends ApiException {
    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    public ChannelPausedException() {
        super(ApiErrorCode.ERR_0015);
    }
}
```

**Hierarchy:**
- `ApiException` (jframe) — domain errors (4xx/5xx)
- `DataNotFoundException` (jframe) — 404s
- `ValidationException` (jframe) — 400 validation

**Throwing in services:**
```java
channel = channelRepo.findById(id).orElseThrow(() -> new DataNotFoundException(CHANNEL_NOT_FOUND));
if (channel.isPaused()) throw new ChannelPausedException();
```

**Rules:**
- Error codes: `ApiErrorCode.ERR_0001` through `ERR_0062` (central enum)
- `ApiErrorCode implements ApiError` — each entry has code + reason string
- Global exception handling provided by jframe `@ControllerAdvice` base

## 10. Naming Conventions

| Type | Convention | Example |
|---|---|---|
| Entity | `{Domain}` | `Channel`, `Event` |
| Request DTO | `Create{Domain}Request`, `Update{Domain}Request` | `CreateChannelRequest` |
| Response DTO | `{Domain}Response`, `{Domain}DetailsResponse` | `ChannelDetailsResponse` |
| Mapper | `{Domain}Mapper` | `ChannelMapper` |
| Validator | `{Domain}Validator` | `ChannelValidator` |
| Main service | `{Domain}Service` | `ChannelService` |
| Security service | `{Domain}SecurityService` | `ChannelSecurityService` |
| Repository | `{Domain}Repository` | `ChannelRepository` |
| Controller | `{Context}{Domain}Controller` | `OrganizationChannelController` |
| Exception | `{Description}Exception` | `ChannelPausedException` |
| MetaData | `{Domain}MetaData` | `ChannelMetaData` |
| Job | `{Domain}{Action}Job` | `EventRetentionCleanupJob` |

**Method naming:**
- CRUD: `create*`, `update*`, `get*`, `search*`, `delete*`, `batch*`
- Security: `canAccess*`, `canCreate*`, `isOwner*`, `isMember*`
- Validator: `validate(request, result)`, `validateAndThrow(request)`
- Mapper: `toResourceObject`, `toPageResource`, `toCreatedResponse`

## 11. Module Internal Structure

```
api/{domain}/
├── controller/
├── service/
├── repository/
├── model/
│   ├── {Entity}.java
│   ├── {Domain}Enum.java
│   ├── {Domain}MetaData.java       # Specification factory + sort builder
│   ├── mapper/
│   ├── request/
│   ├── response/
│   └── validator/
└── job/                            # optional — scheduled tasks
```

Optional additions:
- `cache/` — request-scoped caches (e.g., `ChannelCache`)
- `util/` — domain-specific utilities
- `adapter/` — notification adapters
- `projection/` — JPA projections for admin/stats

---

## 12. TimeProvider for Timestamps

**CRITICAL:** Always use `TimeProvider` for timestamps — PostgreSQL `TIMESTAMPTZ` has microsecond precision (6 digits), Java has nanosecond (9 digits).

```java
import static io.github.eventify.common.util.TimeProvider.now;
import static io.github.eventify.common.util.TimeProvider.truncateToMicros;

// ✅ CORRECT
final OffsetDateTime threshold = now().minusDays(7);
channel.setLastEventAt(now());

// ❌ WRONG — precision mismatch after DB roundtrip
final OffsetDateTime threshold = OffsetDateTime.now().minusDays(7);
```

| Scenario | Method |
|----------|--------|
| Current timestamp in code | `TimeProvider.now()` |
| Calculating thresholds | `now().minusDays(7)` |
| Setting entity timestamps | `entity.setTimestamp(now())` |
| JDBC with external timestamps | `truncateToMicros(externalTimestamp)` |
