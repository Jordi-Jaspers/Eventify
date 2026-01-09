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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.context.SecurityContextHolder;

import static io.github.eventify.api.Paths.*;
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
    private HttpServletRequest request;

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
    @DisplayName("Should not filter when no API key header present")
    public void shouldNotFilterWhenNoApiKeyHeader() throws Exception {
        // Given: Request without X-Api-Key header
        when(request.getHeader("X-Api-Key")).thenReturn(null);
        when(request.getRequestURI()).thenReturn(EVENTS_PATH);

        // When: Filter processes request
        final boolean shouldNotFilter = apiKeyAuthenticationFilter.shouldNotFilter(request);

        // Then: Filter should be skipped
        assertThat(shouldNotFilter, is(true));
    }

    @Test
    @DisplayName("Should not filter when non-event endpoint")
    public void shouldNotFilterWhenNonEventEndpoint() throws Exception {
        // Given: Request to non-event endpoint with API key
        when(request.getHeader("X-Api-Key")).thenReturn("evt_abcdefghijklmnopqrstuvwxyz123456");
        when(request.getRequestURI()).thenReturn("/v1/user");

        // When: Filter checks if it should process
        final boolean shouldNotFilter = apiKeyAuthenticationFilter.shouldNotFilter(request);

        // Then: Filter should be skipped
        assertThat(shouldNotFilter, is(true));
    }

    @Test
    @DisplayName("Should not filter when event endpoint with API key")
    public void shouldNotFilterWhenEventEndpointWithApiKey() throws Exception {
        // Given: Request to event endpoint with API key
        when(request.getHeader("X-Api-Key")).thenReturn("evt_abcdefghijklmnopqrstuvwxyz123456");
        when(request.getRequestURI()).thenReturn(EVENTS_PATH);

        // When: Filter checks if it should process
        final boolean shouldNotFilter = apiKeyAuthenticationFilter.shouldNotFilter(request);

        // Then: Filter should process the request
        assertThat(shouldNotFilter, is(false));
    }

    @Test
    @DisplayName("Should not filter when channels endpoint with API key")
    public void shouldNotFilterWhenChannelsEndpointWithApiKey() throws Exception {
        // Given: Request to channels endpoint with API key
        when(request.getHeader("X-Api-Key")).thenReturn("org_abcdefghijklmnopqrstuvwxyz123456");
        when(request.getRequestURI()).thenReturn(CHANNELS_PATH);

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
        final String apiKey = "evt_abcdefghijklmnopqrstuvwxyz123456";
        when(request.getHeader("X-Api-Key")).thenReturn(apiKey);
        when(request.getRequestURI()).thenReturn(EVENTS_PATH);

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
        final String apiKey = "evt_invalidkey123456789012345678901";
        when(request.getHeader("X-Api-Key")).thenReturn(apiKey);
        when(request.getRequestURI()).thenReturn(EVENTS_PATH);

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
        final String apiKey = "evt_expiredkey123456789012345678901";
        when(request.getHeader("X-Api-Key")).thenReturn(apiKey);
        when(request.getRequestURI()).thenReturn(EVENTS_PATH);

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
        final String apiKey = "evt_disableduser123456789012345678901";
        when(request.getHeader("X-Api-Key")).thenReturn(apiKey);
        when(request.getRequestURI()).thenReturn(EVENTS_PATH);

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
        final String apiKey = "evt_invalidkey123456789012345678901";
        when(request.getHeader("X-Api-Key")).thenReturn(apiKey);
        when(request.getRequestURI()).thenReturn(EVENTS_PATH);

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
        final String apiKey = "xxx_short";
        when(request.getHeader("X-Api-Key")).thenReturn(apiKey);
        when(request.getRequestURI()).thenReturn(EVENTS_PATH);

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
        final String apiKey = "evt_abcdefghijklmnopqrstuvwxyz123456";
        when(request.getHeader("X-Api-Key")).thenReturn(apiKey);
        when(request.getRequestURI()).thenReturn(EVENTS_PATH + "/some-event");

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
        final String apiKey = "org_abcdefghijklmnopqrstuvwxyz123456";
        when(request.getHeader("X-Api-Key")).thenReturn(apiKey);
        when(request.getRequestURI()).thenReturn(CHANNELS_PATH + "/test-channel");

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
        when(request.getHeader("X-Api-Key")).thenReturn("evt_abcdefghijklmnopqrstuvwxyz123456");
        when(request.getRequestURI()).thenReturn("/v1/auth/login");

        // When: Filter checks if it should process
        final boolean shouldNotFilter = apiKeyAuthenticationFilter.shouldNotFilter(request);

        // Then: Filter should be skipped
        assertThat(shouldNotFilter, is(true));
    }

    @Test
    @DisplayName("Should not process user endpoint with API key")
    public void shouldNotProcessUserEndpointWithApiKey() throws Exception {
        // Given: API key header on user endpoint
        when(request.getHeader("X-Api-Key")).thenReturn("evt_abcdefghijklmnopqrstuvwxyz123456");
        when(request.getRequestURI()).thenReturn("/v1/user/details");

        // When: Filter checks if it should process
        final boolean shouldNotFilter = apiKeyAuthenticationFilter.shouldNotFilter(request);

        // Then: Filter should be skipped
        assertThat(shouldNotFilter, is(true));
    }

    @Test
    @DisplayName("Should return error response with correct error code")
    public void shouldReturnErrorResponseWithCorrectErrorCode() throws Exception {
        // Given: Request with invalid API key
        final String apiKey = "evt_invalidkey123456789012345678901";
        when(request.getHeader("X-Api-Key")).thenReturn(apiKey);
        when(request.getRequestURI()).thenReturn(EVENTS_PATH);

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
}
