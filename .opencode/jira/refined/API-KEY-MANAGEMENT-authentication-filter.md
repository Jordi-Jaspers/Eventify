# API Key Authentication Filter

**Epic**: API Key Management
**Status**: Ready for Dev
**Estimate**: M (Medium)
**Created Date**: 2026-01-06

## 1. User Story
**As an** external system or integration
**I want** to authenticate using an API key via the `X-Api-Key` header
**So that** I can send events to Eventify programmatically without user credentials

## 2. Business Context & Value
This is the core authentication mechanism that enables the entire event ingestion use case. API keys provide a secure, auditable way for external systems to send events. Unlike JWTs which are tied to user sessions, API keys are long-lived credentials designed for programmatic access. This filter validates incoming API keys and creates an authentication context that event ingestion endpoints can use.

## 3. Acceptance Criteria

*   [ ] **Scenario 1**: Valid user API key authenticates successfully
    *   Given I have a valid personal API key
    *   When I send a request with header `X-Api-Key: evt_a1b2c3d4...`
    *   Then the request is authenticated
    *   And `last_used_at` is updated on the API key
    *   And `total_requests` is incremented

*   [ ] **Scenario 2**: Valid organization API key authenticates successfully
    *   Given I have a valid organization API key
    *   When I send a request with header `X-Api-Key: org_x9y8z7w6...`
    *   Then the request is authenticated
    *   And the authentication context includes the organization ID

*   [ ] **Scenario 3**: Expired API key is rejected
    *   Given I have an API key that expired yesterday
    *   When I send a request with that key
    *   Then I receive a 401 Unauthorized response
    *   And the error code is `API_KEY_EXPIRED`

*   [ ] **Scenario 4**: Invalid/revoked API key is rejected
    *   Given I have an API key that was revoked
    *   When I send a request with that key
    *   Then I receive a 401 Unauthorized response
    *   And the error code is `INVALID_API_KEY`

*   [ ] **Scenario 5**: Malformed API key is rejected
    *   Given I send a request with header `X-Api-Key: invalid_format`
    *   When the filter processes it
    *   Then I receive a 401 Unauthorized response
    *   And the error code is `INVALID_API_KEY`

*   [ ] **Scenario 6**: API key only works for event endpoints
    *   Given I have a valid API key
    *   When I try to access `/v1/user/details` with the API key
    *   Then I receive a 401 Unauthorized (API keys not accepted)
    *   But when I access `/v1/events` with the API key
    *   Then I am authenticated

*   [ ] **Scenario 7**: JWT still works for all endpoints
    *   Given I have a valid JWT token
    *   When I access any authenticated endpoint
    *   Then authentication works as before (no regression)

*   [ ] **Scenario 8**: API key takes precedence when both present
    *   Given I send a request with both `Authorization: Bearer {jwt}` and `X-Api-Key: {key}`
    *   When the filter processes it
    *   Then the API key is used for authentication (X-Api-Key takes precedence for event endpoints)

*   [ ] **Scenario 9**: Disabled user's personal key is rejected
    *   Given I have a personal API key
    *   And my user account is disabled
    *   When I send a request with that key
    *   Then I receive a 401 Unauthorized response

## 4. Technical Requirements

### New Filter: `ApiKeyAuthenticationFilter.java`
Location: `server/src/main/java/io/github/eventify/common/security/filter/ApiKeyAuthenticationFilter.java`

```java
@Slf4j
@Component
@RequiredArgsConstructor
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-Api-Key";
    private static final String USER_KEY_PREFIX = "evt_";
    private static final String ORG_KEY_PREFIX = "org_";
    
    private final ApiKeyAuthenticationService apiKeyAuthService;
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Only process requests with X-Api-Key header
        // AND only for event ingestion endpoints
        String apiKey = request.getHeader(API_KEY_HEADER);
        if (!hasText(apiKey)) {
            return true;
        }
        
        // Only allow API keys for specific paths
        String path = request.getRequestURI();
        return !isEventEndpoint(path);
    }
    
    private boolean isEventEndpoint(String path) {
        return path.startsWith("/v1/events") || path.startsWith("/v1/channels");
    }
    
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        
        String apiKey = request.getHeader(API_KEY_HEADER);
        
        try {
            ApiKeyPrincipal principal = apiKeyAuthService.authenticate(apiKey);
            
            ApiKeyAuthenticationToken authentication = new ApiKeyAuthenticationToken(
                principal,
                principal.getAuthorities()
            );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("API key authentication successful for key prefix '{}'", principal.getKeyPrefix());
            
            filterChain.doFilter(request, response);
            
        } catch (ApiException ex) {
            log.debug("API key authentication failed: {}", ex.getMessage());
            SecurityContextHolder.clearContext();
            respondWithError(response, ex);
        }
    }
}
```

### API Key Principal
Location: `server/src/main/java/io/github/eventify/common/security/principal/ApiKeyPrincipal.java`

```java
@Data
@AllArgsConstructor
public class ApiKeyPrincipal implements Principal {
    
    private Long apiKeyId;
    private String keyPrefix;
    private ApiKeyScope scope;
    
    // For USER scope
    private Long userId;
    private User user;  // Loaded for user status checks
    
    // For ORGANIZATION scope
    private Long organizationId;
    
    @Override
    public String getName() {
        return keyPrefix;
    }
    
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // API keys have limited permissions - only event ingestion
        return List.of(new SimpleGrantedAuthority("SEND_EVENTS"));
    }
    
    public boolean isUserKey() {
        return scope == ApiKeyScope.USER;
    }
    
    public boolean isOrganizationKey() {
        return scope == ApiKeyScope.ORGANIZATION;
    }
}
```

### API Key Authentication Token
Location: `server/src/main/java/io/github/eventify/common/security/principal/ApiKeyAuthenticationToken.java`

```java
public class ApiKeyAuthenticationToken extends AbstractAuthenticationToken {
    
    private final ApiKeyPrincipal principal;
    
    public ApiKeyAuthenticationToken(ApiKeyPrincipal principal, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        setAuthenticated(true);
    }
    
    @Override
    public Object getCredentials() {
        return null;  // Key is not exposed after authentication
    }
    
    @Override
    public ApiKeyPrincipal getPrincipal() {
        return principal;
    }
}
```

### API Key Authentication Service
Location: `server/src/main/java/io/github/eventify/api/apikey/service/ApiKeyAuthenticationService.java`

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiKeyAuthenticationService {
    
    private final ApiKeyRepository apiKeyRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    
    /**
     * Authenticates an API key and returns the principal.
     * Also updates usage statistics (last_used_at, total_requests).
     */
    @Transactional
    public ApiKeyPrincipal authenticate(String rawApiKey) {
        // Validate format
        if (!isValidKeyFormat(rawApiKey)) {
            throw new ApiException(INVALID_API_KEY);
        }
        
        // Find all non-expired keys and check hash
        // Note: We iterate because we can't query by hash directly
        List<ApiKey> candidates = apiKeyRepository.findAllActive();
        
        ApiKey matchedKey = candidates.stream()
            .filter(key -> passwordEncoder.matches(rawApiKey, key.getHashedKey()))
            .findFirst()
            .orElseThrow(() -> new ApiException(INVALID_API_KEY));
        
        // Check expiration
        if (matchedKey.getExpiresAt() != null && 
            matchedKey.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new ApiException(API_KEY_EXPIRED);
        }
        
        // For user keys, verify user is enabled
        if (matchedKey.getScope() == ApiKeyScope.USER) {
            User user = matchedKey.getUser();
            if (!user.isEnabled()) {
                throw new ApiException(USER_DISABLED);
            }
        }
        
        // Update usage statistics
        matchedKey.setLastUsedAt(OffsetDateTime.now(UTC));
        matchedKey.setTotalRequests(matchedKey.getTotalRequests() + 1);
        apiKeyRepository.save(matchedKey);
        
        // Build principal
        return buildPrincipal(matchedKey);
    }
    
    private boolean isValidKeyFormat(String key) {
        if (key == null || key.length() < 36) return false;
        return key.startsWith("evt_") || key.startsWith("org_");
    }
    
    private ApiKeyPrincipal buildPrincipal(ApiKey key) {
        return new ApiKeyPrincipal(
            key.getId(),
            key.getPrefix(),
            key.getScope(),
            key.getScope() == ApiKeyScope.USER ? key.getUser().getId() : null,
            key.getScope() == ApiKeyScope.USER ? key.getUser() : null,
            key.getScope() == ApiKeyScope.ORGANIZATION ? key.getOrganization().getId() : null
        );
    }
}
```

### Repository Extension
Add to `ApiKeyRepository.java`:

```java
/**
 * Find all non-expired, active API keys.
 * Used for authentication - we check hash against all candidates.
 */
@Query("SELECT k FROM ApiKey k LEFT JOIN FETCH k.user LEFT JOIN FETCH k.organization " +
       "WHERE k.expiresAt IS NULL OR k.expiresAt > CURRENT_TIMESTAMP")
List<ApiKey> findAllActive();
```

### Filter Registration
Update `WebSecurityConfig.java` to add the new filter:

```java
@Bean
public SecurityFilterChain filterChain(..., ApiKeyAuthenticationFilter apiKeyFilter, ...) {
    // Add API key filter BEFORE JWT filter
    http.addFilterBefore(apiKeyFilter, JwtAuthenticationFilter.class);
    
    // ... rest of config
}
```

### Error Codes
Add to `ApiErrorCode.java`:

```java
INVALID_API_KEY("API_KEY_010", "Invalid or revoked API key"),
API_KEY_EXPIRED("API_KEY_011", "API key has expired"),
USER_DISABLED("API_KEY_012", "User account is disabled")
```

### Path Constants
Add to `Paths.java`:

```java
public static final String EVENTS_PATH = BASE_PATH + "/events";
public static final String EVENTS_BATCH_PATH = EVENTS_PATH + "/batch";
public static final String CHANNELS_PATH = BASE_PATH + "/channels";
```

## 5. Design & UI/UX
N/A - This is a backend infrastructure story.

## 6. Implementation Notes / Research

### File Locations
```
server/src/main/java/io/github/eventify/
├── common/security/
│   ├── filter/
│   │   ├── JwtAuthenticationFilter.java      # Existing - no changes
│   │   └── ApiKeyAuthenticationFilter.java   # New
│   └── principal/
│       ├── UserTokenPrincipal.java           # Existing
│       ├── ApiKeyPrincipal.java              # New
│       └── ApiKeyAuthenticationToken.java    # New
├── api/apikey/
│   └── service/
│       ├── ApiKeyService.java                # Existing - CRUD
│       └── ApiKeyAuthenticationService.java  # New - auth logic
```

### Performance Consideration: Key Lookup
The current approach loads all active keys and checks each hash. This works for a reasonable number of keys but could become slow with thousands of keys.

**Optimization for later**: Store a non-secret identifier (like the first N characters of the hash or a separate lookup token) that can be indexed. For MVP, the simple approach is fine.

```java
// Future optimization: add a lookup_hash column
// that stores SHA256(key) for O(1) candidate lookup,
// then verify with BCrypt
```

### Testing Strategy
1. Unit tests for `ApiKeyAuthenticationService`
   - Valid key authentication
   - Expired key rejection
   - Invalid format rejection
   - Disabled user rejection

2. Integration tests for `ApiKeyAuthenticationFilter`
   - Verify filter only processes X-Api-Key requests
   - Verify filter only applies to event endpoints
   - Verify JWT still works for other endpoints

3. Security tests
   - Verify API keys cannot access non-event endpoints
   - Verify timing-safe comparison (BCrypt handles this)

### Existing Patterns to Follow
- See `JwtAuthenticationFilter.java` for filter structure and error handling
- See `UserTokenPrincipal.java` for principal pattern
- See `JwtUserPrincipalAuthenticationToken.java` for authentication token pattern

### Security Notes
- Never log the full API key value
- Use BCrypt's `matches()` which is timing-safe
- Clear security context on any authentication failure
- The principal should not expose the key value after authentication
