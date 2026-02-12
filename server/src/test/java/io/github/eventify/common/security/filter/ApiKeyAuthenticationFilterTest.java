package io.github.eventify.common.security.filter;

import io.github.eventify.api.apikey.model.ApiKey;
import io.github.eventify.api.apikey.model.ApiKeyScope;
import io.github.eventify.api.apikey.service.ApiKeyAuthenticationService;
import io.github.eventify.api.organization.model.Organization;
import io.github.eventify.api.user.model.User;
import io.github.eventify.common.exception.ApiErrorCode;
import io.github.eventify.common.exception.ApiKeyExpiredException;
import io.github.eventify.common.exception.InvalidApiKeyException;
import io.github.eventify.common.exception.UserDisabledException;
import io.github.eventify.common.security.principal.ApiKeyPrincipal;
import io.github.eventify.support.UnitTest;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;

import static io.github.eventify.api.Paths.*;
import static io.github.eventify.common.security.filter.ApiKeyAuthenticationFilter.API_KEY_HEADER;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("Unit Test - API Key Authentication Filter")
@MockitoSettings(strictness = Strictness.LENIENT)
public class ApiKeyAuthenticationFilterTest extends UnitTest {

    @Mock
    private ApiKeyAuthenticationService apiKeyAuthenticationService;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private ApiKeyAuthenticationFilter apiKeyAuthenticationFilter;

    private User user;

    @BeforeEach
    public void setUp() {
        SecurityContextHolder.clearContext();

        user = aValidUser();
        user.setId(1L);
    }

    private MockHttpServletRequest createRequest(final String path) {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath(path);
        request.setRequestURI(path);
        return request;
    }

    private void setupResponseWriter() throws Exception {
        lenient().when(response.getWriter()).thenReturn(mock(java.io.PrintWriter.class));
    }

    private ApiKeyPrincipal createUserPrincipal() {
        final ApiKey apiKey = new ApiKey();
        apiKey.setId(1L);
        apiKey.setScope(ApiKeyScope.USER);
        apiKey.setUser(user);
        apiKey.setOrganization(null);
        return new ApiKeyPrincipal(apiKey);
    }

    private ApiKeyPrincipal createOrgPrincipal(final Long orgId) {
        final Organization org = new Organization();
        org.setId(orgId);
        final ApiKey apiKey = new ApiKey();
        apiKey.setId(2L);
        apiKey.setScope(ApiKeyScope.ORGANIZATION);
        apiKey.setUser(user);
        apiKey.setOrganization(org);
        return new ApiKeyPrincipal(apiKey);
    }

    @Test
    @DisplayName("Should filter external endpoint even without API key header")
    public void shouldFilterExternalEndpointWithoutApiKeyHeader() throws Exception {
        // Given: Request to external endpoint without X-Api-Key header
        final MockHttpServletRequest request = createRequest(EXTERNAL_EVENTS_PATH);
        // No API key header set

        // When: Filter checks if it should process
        final boolean shouldNotFilter = apiKeyAuthenticationFilter.shouldNotFilter(request);

        // Then: Filter should process the request (to return 401)
        assertThat(shouldNotFilter, is(false));
    }

    @Test
    @DisplayName("Should return 401 when external endpoint called without API key")
    public void shouldReturn401WhenExternalEndpointWithoutApiKey() throws Exception {
        // Given: Request to external endpoint without X-Api-Key header
        setupResponseWriter();
        final MockHttpServletRequest request = createRequest(EXTERNAL_EVENTS_PATH);
        // No API key header set

        // When: Filter processes request
        apiKeyAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then: Should return 401 Unauthorized
        verify(response).setStatus(401);
        verify(response).setContentType("application/json");

        // And: Security context should be cleared
        assertThat(SecurityContextHolder.getContext().getAuthentication(), is(nullValue()));

        // And: Filter chain should not continue
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("Should not filter when non-external endpoint")
    public void shouldNotFilterWhenNonExternalEndpoint() throws Exception {
        // Given: Request to non-external endpoint with API key
        final MockHttpServletRequest request = createRequest("/v1/user");
        request.addHeader(API_KEY_HEADER, "evt_abcdefghijklmnopqrstuvwxyz123456");

        // When: Filter checks if it should process
        final boolean shouldNotFilter = apiKeyAuthenticationFilter.shouldNotFilter(request);

        // Then: Filter should be skipped
        assertThat(shouldNotFilter, is(true));
    }

    @Test
    @DisplayName("Should filter when external event endpoint with API key")
    public void shouldFilterWhenExternalEventEndpointWithApiKey() throws Exception {
        // Given: Request to external event endpoint with API key
        final MockHttpServletRequest request = createRequest(EXTERNAL_EVENTS_PATH);
        request.addHeader(API_KEY_HEADER, "evt_abcdefghijklmnopqrstuvwxyz123456");

        // When: Filter checks if it should process
        final boolean shouldNotFilter = apiKeyAuthenticationFilter.shouldNotFilter(request);

        // Then: Filter should process the request
        assertThat(shouldNotFilter, is(false));
    }

    @Test
    @DisplayName("Should filter when external channels endpoint with API key")
    public void shouldFilterWhenExternalChannelsEndpointWithApiKey() throws Exception {
        // Given: Request to external channels endpoint with API key
        final MockHttpServletRequest request = createRequest(EXTERNAL_CHANNELS_PATH);
        request.addHeader(API_KEY_HEADER, "org_abcdefghijklmnopqrstuvwxyz123456");

        // When: Filter checks if it should process
        final boolean shouldNotFilter = apiKeyAuthenticationFilter.shouldNotFilter(request);

        // Then: Filter should process the request
        assertThat(shouldNotFilter, is(false));
    }

    @Test
    @DisplayName("Should set security context with valid API key")
    public void shouldSetSecurityContextWithValidApiKey() throws Exception {
        // Given: Request with valid API key
        setupResponseWriter();
        final MockHttpServletRequest request = createRequest(EXTERNAL_EVENTS_PATH);
        final String apiKey = "evt_abcdefghijklmnopqrstuvwxyz123456";
        request.addHeader(API_KEY_HEADER, apiKey);

        final ApiKeyPrincipal principal = createUserPrincipal();
        when(apiKeyAuthenticationService.authenticate(apiKey)).thenReturn(principal);

        // When: Filter processes request
        apiKeyAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then: Security context should be set
        assertThat(SecurityContextHolder.getContext().getAuthentication(), is(notNullValue()));
        assertThat(
            SecurityContextHolder.getContext().getAuthentication().getPrincipal(),
            is(instanceOf(ApiKeyPrincipal.class))
        );

        // And: Filter chain should continue
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should return 401 with invalid API key")
    public void shouldReturn401WithInvalidApiKey() throws Exception {
        // Given: Request with invalid API key
        setupResponseWriter();
        final MockHttpServletRequest request = createRequest(EXTERNAL_EVENTS_PATH);
        final String apiKey = "evt_invalidkey123456789012345678901";
        request.addHeader(API_KEY_HEADER, apiKey);

        when(apiKeyAuthenticationService.authenticate(apiKey))
            .thenThrow(new InvalidApiKeyException(ApiErrorCode.INVALID_API_KEY));

        // When: Filter processes request
        apiKeyAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then: Should return 401 Unauthorized
        verify(response).setStatus(401);
        verify(response).setContentType("application/json");

        // And: Security context should be cleared
        assertThat(SecurityContextHolder.getContext().getAuthentication(), is(nullValue()));

        // And: Filter chain should not continue
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("Should return 401 with expired API key")
    public void shouldReturn401WithExpiredApiKey() throws Exception {
        // Given: Request with expired API key
        setupResponseWriter();
        final MockHttpServletRequest request = createRequest(EXTERNAL_EVENTS_PATH);
        final String apiKey = "evt_expiredkey123456789012345678901";
        request.addHeader(API_KEY_HEADER, apiKey);

        when(apiKeyAuthenticationService.authenticate(apiKey))
            .thenThrow(new ApiKeyExpiredException(ApiErrorCode.API_KEY_EXPIRED));

        // When: Filter processes request
        apiKeyAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then: Should return 401 Unauthorized
        verify(response).setStatus(401);
        verify(response).setContentType("application/json");

        // And: Security context should be cleared
        assertThat(SecurityContextHolder.getContext().getAuthentication(), is(nullValue()));

        // And: Filter chain should not continue
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("Should return 401 when user disabled")
    public void shouldReturn401WhenUserDisabled() throws Exception {
        // Given: Request with API key for disabled user
        setupResponseWriter();
        final MockHttpServletRequest request = createRequest(EXTERNAL_EVENTS_PATH);
        final String apiKey = "evt_disableduser123456789012345678901";
        request.addHeader(API_KEY_HEADER, apiKey);

        when(apiKeyAuthenticationService.authenticate(apiKey))
            .thenThrow(new UserDisabledException(ApiErrorCode.API_KEY_USER_DISABLED));

        // When: Filter processes request
        apiKeyAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then: Should return 401 Unauthorized
        verify(response).setStatus(401);
        verify(response).setContentType("application/json");

        // And: Security context should be cleared
        assertThat(SecurityContextHolder.getContext().getAuthentication(), is(nullValue()));

        // And: Filter chain should not continue
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("Should clear security context on authentication failure")
    public void shouldClearSecurityContextOnAuthenticationFailure() throws Exception {
        // Given: Request with invalid API key and existing security context
        setupResponseWriter();
        final MockHttpServletRequest request = createRequest(EXTERNAL_EVENTS_PATH);
        final String apiKey = "evt_invalidkey123456789012345678901";
        request.addHeader(API_KEY_HEADER, apiKey);

        // Set existing authentication
        SecurityContextHolder.getContext().setAuthentication(mock(org.springframework.security.core.Authentication.class));

        when(apiKeyAuthenticationService.authenticate(apiKey))
            .thenThrow(new InvalidApiKeyException(ApiErrorCode.INVALID_API_KEY));

        // When: Filter processes request
        apiKeyAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then: Security context should be cleared
        assertThat(SecurityContextHolder.getContext().getAuthentication(), is(nullValue()));
    }

    @Test
    @DisplayName("Should handle malformed API key gracefully")
    public void shouldHandleMalformedApiKeyGracefully() throws Exception {
        // Given: Request with malformed API key
        setupResponseWriter();
        final MockHttpServletRequest request = createRequest(EXTERNAL_EVENTS_PATH);
        final String apiKey = "xxx_short";
        request.addHeader(API_KEY_HEADER, apiKey);

        when(apiKeyAuthenticationService.authenticate(apiKey))
            .thenThrow(new InvalidApiKeyException(ApiErrorCode.INVALID_API_KEY));

        // When: Filter processes request
        apiKeyAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then: Should return 401 Unauthorized
        verify(response).setStatus(401);
        verify(response).setContentType("application/json");

        // And: Filter chain should not continue
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("Should process event endpoint with valid key")
    public void shouldProcessEventEndpointWithValidKey() throws Exception {
        // Given: Valid API key request to events endpoint
        setupResponseWriter();
        final MockHttpServletRequest request = createRequest(EXTERNAL_EVENTS_PATH + "/some-event");
        final String apiKey = "evt_abcdefghijklmnopqrstuvwxyz123456";
        request.addHeader(API_KEY_HEADER, apiKey);

        final ApiKeyPrincipal principal = createUserPrincipal();
        when(apiKeyAuthenticationService.authenticate(apiKey)).thenReturn(principal);

        // When: Filter processes request
        apiKeyAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then: Should authenticate and continue
        assertThat(SecurityContextHolder.getContext().getAuthentication(), is(notNullValue()));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should process channels endpoint with valid key")
    public void shouldProcessChannelsEndpointWithValidKey() throws Exception {
        // Given: Valid API key request to channels endpoint
        setupResponseWriter();
        final MockHttpServletRequest request = createRequest(EXTERNAL_CHANNELS_PATH + "/test-channel");
        final String apiKey = "org_abcdefghijklmnopqrstuvwxyz123456";
        request.addHeader(API_KEY_HEADER, apiKey);

        final ApiKeyPrincipal principal = createOrgPrincipal(100L);
        when(apiKeyAuthenticationService.authenticate(apiKey)).thenReturn(principal);

        // When: Filter processes request
        apiKeyAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then: Should authenticate and continue
        assertThat(SecurityContextHolder.getContext().getAuthentication(), is(notNullValue()));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should not process authentication endpoint with API key")
    public void shouldNotProcessAuthEndpointWithApiKey() throws Exception {
        // Given: API key header on authentication endpoint
        final MockHttpServletRequest request = createRequest("/v1/auth/login");
        request.addHeader(API_KEY_HEADER, "evt_abcdefghijklmnopqrstuvwxyz123456");

        // When: Filter checks if it should process
        final boolean shouldNotFilter = apiKeyAuthenticationFilter.shouldNotFilter(request);

        // Then: Filter should be skipped
        assertThat(shouldNotFilter, is(true));
    }

    @Test
    @DisplayName("Should not process user endpoint with API key")
    public void shouldNotProcessUserEndpointWithApiKey() throws Exception {
        // Given: API key header on user endpoint
        final MockHttpServletRequest request = createRequest("/v1/user/details");
        request.addHeader(API_KEY_HEADER, "evt_abcdefghijklmnopqrstuvwxyz123456");

        // When: Filter checks if it should process
        final boolean shouldNotFilter = apiKeyAuthenticationFilter.shouldNotFilter(request);

        // Then: Filter should be skipped
        assertThat(shouldNotFilter, is(true));
    }

    @Test
    @DisplayName("Should return error response with correct error code")
    public void shouldReturnErrorResponseWithCorrectErrorCode() throws Exception {
        // Given: Request with invalid API key
        final MockHttpServletRequest request = createRequest(EXTERNAL_EVENTS_PATH);
        final String apiKey = "evt_invalidkey123456789012345678901";
        request.addHeader(API_KEY_HEADER, apiKey);

        when(apiKeyAuthenticationService.authenticate(apiKey))
            .thenThrow(new InvalidApiKeyException(ApiErrorCode.INVALID_API_KEY));

        final java.io.PrintWriter writer = mock(java.io.PrintWriter.class);
        when(response.getWriter()).thenReturn(writer);

        // When: Filter processes request
        apiKeyAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then: Should write error response with error code
        verify(response).setStatus(401);
        verify(writer).write(anyString());
    }

    @Test
    @DisplayName("Should filter external batch events endpoint with API key")
    public void shouldFilterExternalBatchEventsEndpointWithApiKey() throws Exception {
        // Given: Request to external batch events endpoint with API key
        final MockHttpServletRequest request = createRequest(EXTERNAL_EVENTS_BATCH_PATH);
        request.addHeader(API_KEY_HEADER, "evt_abcdefghijklmnopqrstuvwxyz123456");

        // When: Filter checks if it should process
        final boolean shouldNotFilter = apiKeyAuthenticationFilter.shouldNotFilter(request);

        // Then: Filter should process the request
        assertThat(shouldNotFilter, is(false));
    }
}
