---
name: eventify-backend-testing-patterns
description: Eventify backend testing patterns. Use when writing Java unit or integration tests. Extends spring-boot-standards with project-specific conventions for JUnit5, Mockito, Testcontainers, Hamcrest. Trigger when asked to write tests, test a service, test a controller, or fix failing tests.
metadata:
  skill-type: patterns
  language: java
  framework: spring-boot, junit5, mockito, testcontainers
---

# Eventify Backend Testing Patterns

Extends: `spring-boot-standards`

## What to Test

- ✅ Services, Controllers, Validators (business logic)
- ✅ Mappers (if complex transformation logic)
- ❌ Entities, DTOs, Migrations, Repositories (no business logic — tested implicitly)

## TDD Philosophy

Tests are written FIRST, before implementation exists. They WILL FAIL initially — this is correct. Tests define the contract; backend-agent makes them pass.

## Test Hierarchy

```
TestContextInitializer   (@SpringBootTest, Testcontainers, all @Autowired repos/services)
  └── WebMvcConfigurator (MockMvc + security filters)
        └── IntegrationTest (factory methods, @BeforeEach cleanup)

UnitTest                 (@ExtendWith(MockitoExtension.class), shared constants + builders)
```

- **Unit test** → extend `UnitTest`. No Spring context. Mockito only.
- **Integration test** → extend `IntegrationTest`. Real TimescaleDB. Real Spring beans. MockMvc.

---

## 1. Unit Test Pattern

```java
@DisplayName("Unit Test - Channel Service")
public class ChannelServiceTest extends UnitTest {

    @Mock
    private ChannelRepository channelRepository;

    @Spy
    private ChannelMetaData channelMetaData = new ChannelMetaData();

    @InjectMocks
    private ChannelService channelService;

    private MockedStatic<SecurityUtil> securityUtilMock;
    private User user;

    @BeforeEach
    public void setUp() {
        user = aValidUser();
        securityUtilMock = mockStatic(SecurityUtil.class);
        securityUtilMock.when(SecurityUtil::getLoggedInUser).thenReturn(user);
    }

    @AfterEach
    public void tearDown() {
        if (securityUtilMock != null) {
            securityUtilMock.close();
        }
    }

    @Test
    @DisplayName("Should create personal channel successfully")
    public void shouldCreatePersonalChannelSuccessfully() {
        // Given: a valid create request
        final CreateChannelRequest request = new CreateChannelRequest()
            .setName("My App Errors")
            .setSlug("test.channel.1");

        when(channelRepository.save(any(Channel.class))).thenReturn(aChannel(1L, "My App Errors", user));

        // When: creating the channel
        final Channel result = channelService.createPersonalChannel(request);

        // Then: channel is returned with correct name
        assertThat(result, is(notNullValue()));
        assertThat(result.getName(), is("My App Errors"));
        verify(channelRepository, times(1)).save(any(Channel.class));
    }

    @Test
    @DisplayName("Should throw exception when channel name is duplicate")
    public void shouldThrowWhenDuplicateChannelName() {
        // Given: repository signals duplicate
        when(channelRepository.existsByNameAndUser(anyString(), any())).thenReturn(true);

        // When / Then: exception is thrown
        assertThrows(DuplicateChannelNameException.class,
            () -> channelService.createPersonalChannel(new CreateChannelRequest().setName("dup")));
    }
}
```

**Key rules:**
- `MockedStatic<SecurityUtil>` always opened in `@BeforeEach`, closed in `@AfterEach` with null-guard.
- Use `aValidUser()`, `aChannel()`, `anApiKey()` etc. from `UnitTest` / `TestBuilders`.
- Never use `@SpringBootTest` in unit tests.

---

## 2. Integration Test Pattern

```java
@SpringBootTest
@DisplayName("Integration Test - Create Organization Channel")
public class CreateOrgChannelControllerTest extends IntegrationTest {

    @Test
    @DisplayName("Should create organization channel when user is owner")
    public void createOrgChannelSuccessWhenOwner() throws Exception {
        // Given: an organization owner
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // And: a valid request
        final CreateChannelRequest request = new CreateChannelRequest()
            .setName("Production Errors")
            .setSlug("test.channel.1");

        // When: posting to the endpoint
        final ResultActions response = mockMvc.perform(
            post(ORGANIZATION_CHANNELS_PATH.replace("{orgId}", org.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
                .content(toJson(request))
        );

        // Then: response is CREATED
        response.andExpect(status().is(SC_CREATED));

        // And: body contains expected data
        final ChannelDetailsResponse body = fromJson(
            response.andReturn().getResponse().getContentAsString(),
            ChannelDetailsResponse.class
        );
        assertThat(body.getId(), is(notNullValue()));
        assertThat(body.getName(), is("Production Errors"));
        assertThat(body.getStatus(), is("ACTIVE"));
    }
}
```

**Key rules:**
- Always `@SpringBootTest` at class level (not on the base class directly).
- Auth via `AUTHORIZATION` header + `BEARER + user.getAccessToken().getValue()`.
- Deserialize response with `fromJson(content, ResponseClass.class)` then assert with Hamcrest.
- No `@Transactional` — cleanup is done by `@BeforeEach` in `IntegrationTest`.

### Controller Test Checklist

- ✅ Extends `IntegrationTest` (no other annotations needed)
- ✅ NO `@Autowired` fields (use `mockMvc` from parent)
- ✅ NO `@BeforeEach` setup (create data inline per test)
- ✅ Use `MockHttpServletRequestBuilder` + `ResultActions` variables
- ✅ Use `toJson()` / `fromJson()` helpers
- ✅ Static imports: `APPLICATION_JSON`, `BEARER`, `AUTHORIZATION`, `SC_*`
- ✅ Deserialize to typed response objects (NOT string checking)
- ✅ Concise method names: `{action}{Condition}{Result}` (e.g., `createOrgChannelSuccessWhenOwner`)
- ❌ NO `@SpringBootTest` / `@AutoConfigureMockMvc` (redundant — inherited)
- ❌ NO `@Autowired MockMvc` / `ObjectMapper` (use from parent)
- ❌ NO section comment headers (`// ===== Tests =====`)

---

## 3. Naming Convention

| What | Pattern | Example |
|------|---------|---------|
| Class | `{ClassName}Test` | `ChannelServiceTest` |
| Class `@DisplayName` | `"Unit Test - <Feature>"` or `"Integration Test - <Feature>"` | `"Unit Test - Channel Service"` |
| Method | camelCase descriptive | `shouldCreatePersonalChannelSuccessfully` |
| Method `@DisplayName` | `"Should X when Y"` | `"Should throw exception when channel name is duplicate"` |

Split by concern when a class gets large: `AdminApiKeyServiceTest` + `AdminApiKeyServiceStatisticsTest`.

---

## 4. Assertions

**Always use Hamcrest** — never AssertJ (dependency present but unused).

```java
// Hamcrest primary
assertThat(result, is(notNullValue()));
assertThat(result.getName(), is("expected"));
assertThat(list, hasSize(2));
assertThat(list, hasItem(expectedItem));
assertThat(str, containsString("partial"));
assertThat(str, containsStringIgnoringCase("partial"));
assertThat(value, anyOf(is("a"), is("b")));

// Exceptions
assertThrows(DataNotFoundException.class, () -> service.get(999L));

// MockMvc status
response.andExpect(status().is(SC_OK));
response.andExpect(status().is(SC_CREATED));
response.andExpect(status().is(SC_FORBIDDEN));
```

Static imports:
```java
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
```

---

## 5. Test Data

### Unit Tests — `TestBuilders` (static, no DB)

```java
// Available in UnitTest base and TestBuilders
aChannel(id, name, user)
anApiKey(id, suffix, name, user)
aWatchlist()
anOrganization()
aValidUser()          // from UnitTest base
aValidUserWithTokens()
```

### Integration Tests — Factory Methods (writes real DB)

```java
// Users
User owner = aValidatedUser();
User unvalidated = anUnvalidatedUser();
User locked = aLockedUser();

// Orgs
Organization org = anOrganisationWithOwner(owner);
addMemberToOrganization(org, member, OrganizationalRole.ADMIN);

// Channels
Channel channel = aChannelForUser(owner);
Channel orgChannel = aChannelForOrganisation(org);
pauseChannel(channel);
updateChannelCreatedAt(channel, LocalDateTime.now().minusDays(7));  // uses JDBC

// API Keys
anApiKeyForUser(owner);
anExpiredApiKeyForUser(owner);
anApiKeyForOrganisation(org);

// Events / Watchlists
anEventForChannel(channel);
aWatchlistForUser(owner);
addChannelToWatchlist(watchlist, channel);
```

**Uniqueness:** factory methods use `UUID.randomUUID().toString().substring(0, 5)` prefix.  
**Identification:** test users end in `@integration.test`; test orgs start with `[Integration Test] - `.

**Why this matters:** Each test creates unique data that won't collide. Cleanup automatically removes all integration test data `@BeforeEach`. Tests can run in any order without interference. Never hardcode names/emails — use factory methods.

---

## 6. Database Handling

- **Real TimescaleDB** (`timescale/timescaledb-ha:pg17`) via Testcontainers singleton.
- **No `@Transactional` rollback.** Cleanup runs explicitly in `@BeforeEach`:
  ```
  TestDataCleanupService.cleanUpTestData()
  ```
  Deletes in FK order: events → audit → quotas → api_keys → channels → watchlists → memberships → orgs → tokens → notifications → users.
- **Liquibase `drop-first: true`** in `application-test.yml` — full schema rebuilt each test run.
- **Direct JDBC** for writes that JPA cannot do (timestamp overrides, broadcast FK):
  ```java
  jdbcTemplate.update("UPDATE channel SET created_at = ? WHERE id = ?", timestamp, id);
  ```

---

## 7. Parallel Execution

- Unit tests: `CONCURRENT` at method and class level (via `junit-platform.properties`).
- Integration tests: `@Execution(SAME_THREAD)` — sequential to avoid DB state conflicts.

---

## 8. Security in Tests

### Unit Tests — `MockedStatic<SecurityUtil>`

```java
private MockedStatic<SecurityUtil> securityUtilMock;

@BeforeEach void setUp() {
    securityUtilMock = mockStatic(SecurityUtil.class);
    securityUtilMock.when(SecurityUtil::getLoggedInUser).thenReturn(aValidUser());
}

@AfterEach void tearDown() {
    if (securityUtilMock != null) securityUtilMock.close();
}
```

### Integration Tests — `SecurityContextHolder`

Set automatically in `IntegrationTest.@BeforeEach` using `JwtUserPrincipalAuthenticationToken`.  
For per-test user auth, pass the token in the request header:

```java
.header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
```

---

## Common Mistakes

| Mistake | Fix |
|---------|-----|
| Using AssertJ (`assertThat(...).isEqualTo(...)`) | Use Hamcrest `assertThat(x, is(y))` |
| `@Transactional` on integration test | Remove it; rely on `@BeforeEach` cleanup |
| Forgetting `securityUtilMock.close()` | Always close in `@AfterEach` with null-guard |
| Hardcoded names in integration tests | Use factory methods which auto-prefix with UUID |
| `@SpringBootTest` on unit test | Unit tests extend `UnitTest` only — no Spring context |
| Missing `@DisplayName` | Required on both class and each `@Test` method |

---

## 9. Validator Test Pattern

Every custom validator MUST have tests:

```java
@DisplayName("Unit Test - Channel Validator")
public class ChannelValidatorTest extends UnitTest {

    private ChannelValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ChannelValidator();
    }

    @Test
    @DisplayName("Should pass when request is valid")
    void shouldPassWhenRequestIsValid() {
        // Given: valid request
        final CreateChannelRequest request = new CreateChannelRequest().setName("Valid Name");

        // When/Then: no exception
        assertDoesNotThrow(() -> validator.validateAndThrow(request));
    }

    @Test
    @DisplayName("Should fail when name is null")
    void shouldFailWhenNameIsNull() {
        // Given: null name
        final CreateChannelRequest request = new CreateChannelRequest();

        // When/Then: validation exception with field error
        final ValidationException ex = assertThrows(
            ValidationException.class, () -> validator.validateAndThrow(request));
        assertThat(ex.getResult().hasFieldError(FIELD_NAME), is(true));
    }
}
```

---

## 10. Edge Cases to Always Cover

- `null` inputs and empty strings/collections
- Boundary values (0, negative, max length)
- Duplicate data (unique constraint violations)
- Unauthorized access (wrong user, wrong role)
- Time-based scenarios (expiration, staleness)
- External service failures (if applicable)
