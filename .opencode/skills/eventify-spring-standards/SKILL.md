---
name: springboot-standards
description: Spring Boot code standards, patterns, and architecture guidelines. Use when implementing features, writing entities, services, controllers, DTOs, validators, exceptions, tests, or migrations. Covers Lombok, Mapstruct, JFrame validation/search/pagination, and Liquibase patterns.
metadata:
  skill-type: backend
  language: Java
  framework: Spring Boot
  build-tool: gradle
---

# Spring Boot Code Standards

Project-specific patterns for the eventify backend

## File Structure

```
Paths.java               # API endpoint paths as constants
api/{domain}/
├── controller/          # REST controllers
├── service/             # Business logic
├── repository/          # Spring Data JPA
└── model/
    ├── {Domain}.java    # Entity
    ├── mapper/          # Mapstruct mappers
    ├── request/         # Request DTOs
    ├── response/        # Response DTOs
    └── validator/       # Custom validators (@Component)
```

## Java Code Rules

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

**Non-negotiable:**
- ✅ All variables `final`
- ✅ Explicit types (NEVER `var`)
- ✅ Constructor injection (NO `@Autowired` fields)
- ✅ NO Java records (standard classes only)
- ✅ Lombok: `@Getter`, `@Setter`, `@Builder`, `@RequiredArgsConstructor`
- ✅ Lombok `@Accessors(chain = true)` for response objects
- ✅ Layered: Controller → Service → Repository → Entity
- ✅ Enums for categorical fields in entities (`@Enumerated(EnumType.STRING)`), request DTOs, and response DTOs — NEVER use `String` for values that have a fixed set (e.g. category, status, type, role)

## Entity Pattern

```java
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "email", nullable = false, unique = true)
    private String email;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    // Business constructor for domain-driven creation
    public User(final String email, final String password) {
        this.email = email;
        this.password = password;
        this.createdAt = Instant.now();
        this.enabled = false;
    }
}
```

**Entity rules:**
- ✅ **Always use explicit `@Column` annotations** even when column name matches field
- ✅ **Keep `@NoArgsConstructor`** for JPA (required by Hibernate)
- ✅ **Add business constructors** for domain-driven creation logic (set defaults, timestamps, derived values)
- ✅ **Services use constructors** instead of `new Entity()` + multiple setters

## Entity Conversion Methods

Entities can have methods to convert to related entities (e.g., for audit trails):

```java
@Entity
public class ApiKey {
    // ... fields ...
    
    /**
     * Creates an audit record from this API key when it's being revoked.
     */
    public ApiKeyAudit toAuditRecord(final User revoker) {
        return new ApiKeyAudit(
            this.name,
            this.prefix,
            this.user,
            this.organization,
            this.createdAt,
            revoker
        );
    }
}
```

**Conversion method rules:**
- ✅ Domain entities own conversion logic to related entities
- ✅ Use for audit records, history entries, or derived entities
- ✅ Service calls `entity.toAuditRecord(...)` instead of building inline
- ✅ Keeps domain logic in domain layer (DDD principle)

## Entity/Schema Alignment

**Critical:** Entity `@JoinColumn` nullable MUST match database schema constraints.

```java
// ✅ CORRECT - DB has ON DELETE SET NULL, entity allows null
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "revoked_by")  // nullable by default
private User revokedBy;

// ❌ WRONG - DB has ON DELETE SET NULL but entity says NOT NULL
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "revoked_by", nullable = false)  // Constraint violation!
private User revokedBy;
```

**Transient fields** for temporary data not persisted to DB:
```java
@Transient
private String key;  // Full API key, only populated during creation
```

## Audit Table Pattern

Audit tables preserve history even when referenced users are deleted:

```java
// Audit entity - FK columns should be NULLABLE
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "revoked_by")  // Nullable - user may be deleted later
private User revokedBy;

@Column(name = "owner_user_id")   // Store ID directly for deleted user references
private Long ownerUserId;
```

**Database schema for audit FKs:**
```sql
revoked_by INTEGER NULL,  -- Must be NULL to support SET NULL
FOREIGN KEY (revoked_by) REFERENCES "user"(id) ON DELETE SET NULL
```

## Repository Pattern

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    
    @Query("SELECT u FROM User u WHERE u.enabled = true")
    List<User> findAllEnabled();
}
```

**Repository rules:**
- ⚠️ **Query method field names MUST exist on the entity** - Spring Data derives queries from method names. `findByPrefix()` fails if entity has no `prefix` field.
- Validate field names before adding query methods
- Use `@Query` for complex queries that can't be derived from method names

## Service Pattern

```java
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RegisterValidator registerValidator;
    
    @Transactional
    public User register(final RegisterRequest request) {
        registerValidator.validateAndThrow(request);
        
        final User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        return userRepository.save(user);
    }
    
    @Transactional(readOnly = true)
    public User findById(final Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
```

**Service rules:**
- **Services return domain entities, NOT DTOs** - Controllers use MapStruct to convert
- Call validators, don't embed validation logic
- `@Transactional` for writes, `@Transactional(readOnly = true)` for reads
- `Optional.orElseThrow()` with custom exception, never `.get()`

## Controller Pattern

```java
@RestController
@RequiredArgsConstructor
@Tag(name = "User", description = "User management endpoints")
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;
    private final CreateUserValidator createUserValidator;
    
    @ResponseStatus(OK)
    @Operation(summary = "Get user details", description = "Returns the authenticated user's profile")
    @GetMapping(path = USER_DETAILS, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponse> getUser(
            @AuthenticationPrincipal final UserTokenPrincipal principal) {
        final UserResponse response = userMapper.toResponse(principal.getUser());
        return ResponseEntity.status(OK).body(response);
    }
    
    @ResponseStatus(CREATED)
    @Operation(summary = "Create user", description = "Creates a new user account")
    @PostMapping(path = USERS, consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponse> createUser(
            @RequestBody final CreateUserRequest request) {
        createUserValidator.validateAndThrow(request);
        final User user = userService.register(request);
        return ResponseEntity.status(CREATED).body(userMapper.toResponse(user));
    }
}
```

**Controller rules:**
- ✅ **ALWAYS return `ResponseEntity<T>`** - Never return raw objects
- ✅ **NEVER use `@Valid`** - Use custom validators (`validator.validateAndThrow()`)
- ✅ **NO business logic** - business logic should live in the service.
- ✅ **NO Javadoc on methods with `@Operation`** - Use `@Operation(description = "...")` instead
- ✅ Endpoints from `Paths.java` constants only (never hardcode)
- ✅ MapStruct for entity-to-DTO conversion (service returns entities)
- ✅ Inject validator as dependency, call in controller method


## DTO Pattern

```java
// Request DTO
@Data
@NoArgsConstructor
public class CreateUserRequest {
    private String email;
    private String password;
}

// Response DTO - ALL fields MUST have @Schema with requiredMode
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Schema(description = "User account information")
public class UserResponse {

    @Schema(
        description = "Unique user identifier",
        example = "123",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long id;

    @Schema(
        description = "User email address",
        example = "user@example.com",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String email;

    @Schema(
        description = "Account creation timestamp",
        example = "2026-01-08T10:30:00Z",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Instant createdAt;
}
```

**Response DTO rules:**
- ✅ **ALL fields MUST have `@Schema`** with `description`, `example`, and `requiredMode`
- ✅ Use `Schema.RequiredMode.REQUIRED` for always-present fields
- ✅ Use `Schema.RequiredMode.NOT_REQUIRED` for nullable/optional fields
- ✅ Class-level `@Schema(description = "...")` for the DTO itself

## JFrame Validation

**Package:** `io.github.jframe.validation`

Validators are `@Component` classes in `model/validator/`:

```java
@Component
@RequiredArgsConstructor
public class ChangePasswordValidator implements io.github.jframe.validation.Validator<PasswordRequest> {

    public static final String PASSWORD_REQUIRED = "Password is required";
    public static final String PASSWORDS_MUST_MATCH = "Passwords must match";
    
    private final CustomPasswordValidator passwordValidator;

    @Override
    public void validate(final PasswordRequest request, final ValidationResult result) {
        if (isNull(request)) {
            result.reject("Request body is missing");
            throw new ValidationException(result);
        }

        result.rejectField("newPassword", request.getNewPassword())
            .whenNull(PASSWORD_REQUIRED)
            .orWhen(String::isEmpty, PASSWORD_REQUIRED);

        result.rejectField("confirmPassword", request.getConfirmPassword())
            .whenNull(PASSWORDS_MUST_MATCH)
            .orWhen(p -> !p.equals(request.getNewPassword()), PASSWORDS_MUST_MATCH);

        if (result.hasErrors()) {
            throw new ValidationException(result);
        }
    }
}
```

**Validator rules:**
- Separate `@Component` classes in `model/validator/`
- Implement `Validator<RequestType>` from JFrame
- Constructor injection for dependencies
- **Each validator MUST have unit tests** in `test/.../model/validator/`
- Services call validators, don't embed validation logic
- Validators for the same controllers can be combined into one class
  - e.g. createUserRequest + updateUserRequest → UserValidator

## JFrame Exception Pattern

```java
public class ResourceNotFoundException extends ApiException {
    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    public ResourceNotFoundException(final String message) {
        super(RESOURCE_NOT_FOUND, message);
    }
}

@Getter
@RequiredArgsConstructor
public enum ApiErrorCode implements ApiError {
    INTERNAL_SERVER_ERROR("ERR-0001", "Internal server error"),
    RESOURCE_NOT_FOUND("ERR-0002", "Resource not found"),
    VALIDATION_ERROR("ERR-0003", "Validation failed");
    
    private final String code;
    private final String message;
}
```

## Mapstruct Pattern

```java
@Mapper(config = SharedMapperConfig.class, uses = {DateTimeMapper.class})
public abstract class UserMapper {
    public abstract UserResponse toResponse(User user);
    public abstract User toEntity(CreateUserRequest request);
}
```

## Liquibase Migrations

See `liquibase-migrations` skill for complete patterns.

**Quick rules:**
- Use `<sql>` tags with raw SQL only (NOT `<createTable>`, etc.)
- Use `<comment>` tag for descriptions
- File naming: `YYYYMMDDHHMI-{PRD|TST}-description.xml`
- Location: `resources/db/changelog/changesets/`

---

# Testing Standards

## General Rules

**What to test:**
- ✅ Services, Controllers, Validators (business logic)
- ❌ Entities, Migrations, DTOs, Mappers (no business logic)

**How to test:**
- ✅ Given-When-Then with inline comments
- ✅ `@DisplayName` on all tests
- ✅ `shouldXWhenY()` naming
- ✅ Hamcrest assertions (NOT JUnit)
- ✅ Factory methods for test data (prefix `a`/`an`)
- ✅ Extend `UnitTest` or `IntegrationTest`
- ✅ >90% line, >85% branch coverage

## Unit Test Pattern

```java
@DisplayName("UserService Tests")
class UserServiceTest extends UnitTest {
    
    private UserService userService;
    private UserRepository userRepository;
    private RegisterValidator registerValidator;
    
    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        registerValidator = mock(RegisterValidator.class);
        userService = new UserService(userRepository, registerValidator);
    }
    
    @Test
    @DisplayName("Should register user when email is unique")
    void shouldRegisterUserWhenEmailIsUnique() {
        // Given: Valid registration request
        final RegisterRequest request = aValidRegisterRequest();
        given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.empty());
        given(userRepository.save(any())).willAnswer(i -> i.getArgument(0));
        
        // When: Registration is attempted
        final User result = userService.register(request);
        
        // Then: User is created
        assertThat(result.getEmail(), is(request.getEmail()));
        verify(userRepository).save(any(User.class));
    }
    
    private static RegisterRequest aValidRegisterRequest() {
        final RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("SecurePass123!");
        return request;
    }
}
```

## Controller Test Pattern

**Reference:** `AuthenticationControllerTest.java`

```java
@DisplayName("UserController Tests")
class UserControllerTest extends IntegrationTest {
    
    @Test
    @DisplayName("Should return user details when authenticated")
    void getUserDetailsSuccess() throws Exception {
        // Given: Authenticated user
        final User user = createUser("test@example.com", "password");
        
        // When: Request user details
        final MockHttpServletRequestBuilder request = get(USER_DETAILS)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .contentType(APPLICATION_JSON);
        
        final ResultActions result = mockMvc.perform(request);
        
        // Then: Returns user data
        result.andExpect(status().isOk());
        
        final UserResponse response = fromJson(
            result.andReturn().getResponse().getContentAsString(),
            UserResponse.class
        );
        assertThat(response.getEmail(), is(user.getEmail()));
    }
    
    @Test
    @DisplayName("Should fail with invalid email")
    void createUserWithInvalidEmailFails() throws Exception {
        // Given: Invalid email
        final CreateUserRequest createRequest = new CreateUserRequest();
        createRequest.setEmail("not-an-email");
        createRequest.setPassword("SecurePass123!");
        
        // When: Create user
        final MockHttpServletRequestBuilder request = post(USERS)
            .contentType(APPLICATION_JSON)
            .content(toJson(createRequest));
        
        final ResultActions result = mockMvc.perform(request);
        
        // Then: Validation error
        result.andExpect(status().isBadRequest());
    }
}
```

**Controller test rules:**
- ✅ Extend `IntegrationTest` only (NO `@SpringBootTest`/`@AutoConfigureMockMvc`)
- ✅ Use `mockMvc` from parent (NO `@Autowired MockMvc`)
- ✅ Create test data inline (NO `@BeforeEach` setup)
- ✅ Use `MockHttpServletRequestBuilder` and `ResultActions` variables
- ✅ Use `toJson()`/`fromJson()` from `ObjectMappers`
- ✅ Static imports: `APPLICATION_JSON`, `BEARER`, `AUTHORIZATION`, `SC_*`
- ✅ Deserialize to typed objects (NOT string checking)
- ✅ For non-auth: Use `user.getAccessToken().getValue()` (no full login)
- ✅ Concise naming: `{action}{Condition}{Result}` (e.g., `loginSuccess`)
- ✅ NO section comment headers

## Validator Test Pattern

**Every validator MUST have tests:**

```java
@DisplayName("ChangePasswordValidator Tests")
class ChangePasswordValidatorTest extends UnitTest {
    
    private ChangePasswordValidator validator;
    private CustomPasswordValidator passwordValidator;
    
    @BeforeEach
    void setUp() {
        passwordValidator = mock(CustomPasswordValidator.class);
        validator = new ChangePasswordValidator(passwordValidator);
        given(passwordValidator.validatePassword(anyString()))
            .willReturn(new RuleResult(true));
    }
    
    @Test
    @DisplayName("Should pass when passwords match")
    void shouldPassWhenPasswordsMatch() {
        // Given: Matching passwords
        final PasswordRequest request = aValidPasswordRequest();
        
        // When/Then: No exception
        assertDoesNotThrow(() -> validator.validateAndThrow(request));
    }
    
    @Test
    @DisplayName("Should fail when passwords don't match")
    void shouldFailWhenPasswordsDontMatch() {
        // Given: Mismatched passwords
        final PasswordRequest request = new PasswordRequest();
        request.setNewPassword("Password123!");
        request.setConfirmPassword("Different123!");
        
        // When/Then: Validation exception
        final ValidationException ex = assertThrows(
            ValidationException.class,
            () -> validator.validateAndThrow(request)
        );
        assertThat(ex.getResult().hasFieldError("confirmPassword"), is(true));
    }
    
    private static PasswordRequest aValidPasswordRequest() {
        final PasswordRequest request = new PasswordRequest();
        request.setNewPassword("SecurePass123!");
        request.setConfirmPassword("SecurePass123!");
        return request;
    }
}
```

---

## JFrame Search & Pagination

**Dependency:** `io.github.jframeoss:starter-jpa:x.x.x`

For searchable, paginated endpoints with filtering and sorting.

### Architecture Overview

```
Controller                    Service                      MetaData
    │                            │                            │
    ▼                            ▼                            ▼
SortablePageInput ──────► toSpecification() ──────► JpaSearchSpecification
    │                            │                            │
    ▼                            ▼                            ▼
PageResource<T> ◄────── Page<Entity> ◄────── repository.findAll(spec, pageable)
```

### 1. Entity (implements PageableItem)

```java
import io.github.jframe.datasource.search.model.PageableItem;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "channel")
public class Channel implements PageableItem, Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ChannelStatus status;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
```

### 2. Response DTO (implements PageableItemResource)

```java
import io.github.jframe.datasource.search.model.resource.PageableItemResource;

@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Schema(description = "Channel details")
public class ChannelDetailsResponse implements PageableItemResource {
    
    @Schema(description = "Unique identifier", requiredMode = REQUIRED)
    private Long id;
    
    @Schema(description = "Channel name", requiredMode = REQUIRED)
    private String name;
    
    @Schema(description = "Channel status", requiredMode = REQUIRED)
    private String status;
}
```

### 3. Mapper (extends PageMapper)

```java
import io.github.jframe.datasource.search.model.mapper.PageMapper;

@Mapper(config = SharedMapperConfig.class, uses = DateTimeMapper.class)
public abstract class ChannelMapper extends PageMapper<ChannelDetailsResponse, Channel> {
    
    @Override
    @Named("toResourceObject")
    public abstract ChannelDetailsResponse toResourceObject(Channel channel);
    
    @IterableMapping(qualifiedByName = "toResourceObject")
    public abstract List<ChannelDetailsResponse> toResourceObjects(List<Channel> channels);
}
```

**Key:** `PageMapper` provides `toPageResource(Page<Entity>)` method automatically.

### 4. MetaData (extends AbstractSortSearchMetaData)

Defines searchable/sortable fields and their types:

```java
import io.github.jframe.datasource.search.SearchType;
import io.github.jframe.datasource.search.model.AbstractSortSearchMetaData;
import io.github.jframe.datasource.search.model.JpaSearchSpecification;
import io.github.jframe.datasource.search.model.SearchCriterium;
import io.github.jframe.datasource.search.model.input.SortablePageInput;

@Component
public class ChannelMetaData extends AbstractSortSearchMetaData {

    // Field name constants (match frontend filter keys)
    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";
    public static final String STATUS = "status";
    public static final String CREATED_AT = "createdAt";
    public static final String USER_TERM = "user";
    public static final String USER_FIELD = "user.id";  // Nested field path

    public ChannelMetaData() {
        super();
        // addField(fieldName, entityPath, searchType, sortable)
        addField(NAME, NAME, SearchType.FUZZY_TEXT, true);
        addField(DESCRIPTION, DESCRIPTION, SearchType.FUZZY_TEXT, true);
        addField(STATUS, STATUS, SearchType.ENUM, true);
        addField(USER_TERM, USER_FIELD, SearchType.NUMERIC, true);
        addField(CREATED_AT, CREATED_AT, SearchType.DATE, true);
        
        // Multi-column search (searches across multiple fields)
        addField(
            "search",
            List.of(NAME, DESCRIPTION),
            SearchType.MULTI_COLUMN_FUZZY,
            false  // Not sortable
        );
    }

    /**
     * Builds specification with additional business constraints.
     */
    public Specification<Channel> toUserChannelSpecification(final SortablePageInput input) {
        final List<SearchCriterium> criteria = toSearchCriteria(input.getSearchInputs());
        final Specification<Channel> searchSpec = new JpaSearchSpecification<>(criteria);

        return Specification
            .where(searchSpec)
            .and(buildNotDeletedSpecification());  // Add business constraints
    }

    private Specification<Channel> buildNotDeletedSpecification() {
        return (root, query, cb) -> cb.notEqual(root.get(STATUS), ChannelStatus.PENDING_DELETION);
    }
}
```

**SearchType options:**
| Type | Use Case | Input Field |
|------|----------|-------------|
| `TEXT` | Exact match | `textValue` |
| `FUZZY_TEXT` | Case-insensitive contains | `textValue` |
| `ENUM` | Single enum value | `textValue` |
| `MULTI_ENUM` | Multiple enum values (OR) | `textValueList` |
| `NUMERIC` | Number equality | `textValue` or `textValueAsInteger` |
| `DATE` | Date range | `fromDateValue`, `toDateValue` |
| `BOOLEAN` | True/false | `textValue` |
| `MULTI_COLUMN_FUZZY` | Search across multiple fields | `textValue` |

### 5. Repository (extends JpaSpecificationExecutor)

```java
@Repository
public interface ChannelRepository extends JpaRepository<Channel, Long>, 
                                           JpaSpecificationExecutor<Channel> {
    // Standard query methods...
}
```

### 6. Service

```java
@Service
@RequiredArgsConstructor
public class ChannelService {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final ChannelRepository channelRepository;
    private final ChannelMetaData channelMetaData;

    @Transactional(readOnly = true)
    public Page<Channel> searchUserChannels(final SortablePageInput input) {
        final User user = getLoggedInUser();
        
        // Add user filter programmatically
        final SearchInput userInput = new SearchInput();
        userInput.setFieldName(USER_TERM);
        userInput.setTextValue(user.getId().toString());
        input.addSearchInput(userInput);

        // Build sort from input
        final Sort sort = channelMetaData.toSort(input.getSortOrder());
        final int pageSize = input.getPageSize() > 0 ? input.getPageSize() : DEFAULT_PAGE_SIZE;
        final Pageable pageable = PageRequest.of(input.getPageNumber(), pageSize, sort);

        // Build specification and execute
        final Specification<Channel> specification = channelMetaData.toUserChannelSpecification(input);
        return channelRepository.findAll(specification, pageable);
    }
}
```

### 7. Controller

```java
@RestController
@RequiredArgsConstructor
public class UserChannelController {

    private final ChannelService channelService;
    private final ChannelMapper channelMapper;

    @PostMapping(path = USER_CHANNELS_SEARCH_PATH, 
                 consumes = APPLICATION_JSON_VALUE, 
                 produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(OK)
    @Transactional(readOnly = true)
    @Operation(summary = "Search channels", 
               description = "Searches channels with pagination, filtering, and sorting")
    public ResponseEntity<PageResource<ChannelDetailsResponse>> searchChannels(
            @RequestBody final SortablePageInput input) {
        final Page<Channel> page = channelService.searchUserChannels(input);
        return ResponseEntity.status(OK).body(channelMapper.toPageResource(page));
    }
}
```

### Request/Response Examples

### Testing Search Endpoints

```java
@Test
@DisplayName("Should filter channels by status")
void filterChannelsByStatus() throws Exception {
    // Given: Channels with different statuses
    createChannel("Active Channel", ChannelStatus.ACTIVE);
    createChannel("Paused Channel", ChannelStatus.PAUSED);

    // And: Search input with status filter
    final SortablePageInput searchInput = new SortablePageInput();
    searchInput.setPageNumber(0);
    searchInput.setPageSize(10);

    final SearchInput statusFilter = new SearchInput();
    statusFilter.setFieldName("status");
    statusFilter.setTextValueList(List.of("ACTIVE"));
    searchInput.getSearchInputs().add(statusFilter);

    // When: Searching with filter
    final MockHttpServletRequestBuilder request = post(USER_CHANNELS_SEARCH_PATH)
        .contentType(APPLICATION_JSON)
        .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
        .content(toJson(searchInput));

    final ResultActions response = mockMvc.perform(request);

    // Then: Only ACTIVE channels returned
    response.andExpect(status().is(SC_OK));
    
    final PageResource<?> pageResource = fromJson(
        response.andReturn().getResponse().getContentAsString(), 
        PageResource.class
    );
    assertThat(pageResource.getContent(), hasSize(1));
}
```

### Checklist for New Search Endpoint

- [ ] Entity implements `PageableItem`
- [ ] Response DTO implements `PageableItemResource`
- [ ] Mapper extends `PageMapper<ResponseDTO, Entity>`
- [ ] MetaData class extends `AbstractSortSearchMetaData`
- [ ] Repository extends `JpaSpecificationExecutor<Entity>`
- [ ] Service uses `metaData.toSort()` and `metaData.toSpecification()`
- [ ] Controller returns `PageResource<ResponseDTO>`
- [ ] Tests cover: pagination, sorting, each filter type, combined filters

## Commands

```bash
cd server/
./gradlew bootRun                 # Run
./gradlew test                    # All tests
./gradlew test --tests ClassName  # Specific test
./gradlew spotlessApply           # Fix formatting
./gradlew checkQualityMain        # Quality checks
```

## TimeProvider for Timestamps

**CRITICAL:** Always use `TimeProvider` for timestamps to ensure PostgreSQL compatibility.

PostgreSQL `TIMESTAMPTZ` has microsecond precision (6 decimal places), while Java's `OffsetDateTime.now()` has nanosecond precision (9 decimal places). This causes precision mismatches when comparing timestamps before and after database operations.

### In Production Code

```java
import static io.github.eventify.common.util.TimeProvider.now;

// ✅ CORRECT - Use TimeProvider.now()
final OffsetDateTime threshold = now().minusDays(7);
channel.setLastEventAt(now());

// ❌ WRONG - Direct OffsetDateTime.now()
final OffsetDateTime threshold = OffsetDateTime.now().minusDays(7);
```

### In Tests

```java
import static io.github.eventify.common.util.TimeProvider.now;
import static io.github.eventify.common.util.TimeProvider.truncateToMicros;

// ✅ CORRECT - Use now() for test data
channel.setLastEventAt(now().minusDays(10));
assertThat(result.getCreatedAt(), is(now().minusDays(30)));

// ✅ CORRECT - Use truncateToMicros() for external timestamps
final OffsetDateTime externalTimestamp = someExternalSource();
jdbcTemplate.update("UPDATE channel SET created_at = ?", 
    java.sql.Timestamp.from(truncateToMicros(externalTimestamp).toInstant()));
```

### TimeProvider API

```java
// Get current time with microsecond precision
TimeProvider.now()  // → OffsetDateTime truncated to micros

// Truncate existing timestamp to microsecond precision
TimeProvider.truncateToMicros(timestamp)  // → OffsetDateTime or null if input null
```

### When to Use

| Scenario | Method |
|----------|--------|
| Current timestamp in code | `TimeProvider.now()` |
| Calculating thresholds | `now().minusDays(7)` |
| Setting entity timestamps | `entity.setTimestamp(now())` |
| Test assertions with time | `assertThat(result.getTime(), is(now().minusDays(1)))` |
| JDBC/SQL with external timestamps | `truncateToMicros(externalTimestamp)` |
