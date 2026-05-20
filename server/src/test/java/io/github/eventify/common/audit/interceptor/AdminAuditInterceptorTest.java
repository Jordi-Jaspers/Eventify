package io.github.eventify.common.audit.interceptor;

import io.github.eventify.api.user.model.User;
import io.github.eventify.common.audit.event.AuditEvent;
import io.github.eventify.common.security.SecurityUtil;
import io.github.eventify.support.UnitTest;

import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.util.ContentCachingRequestWrapper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Unit Test - AdminAuditInterceptor")
public class AdminAuditInterceptorTest extends UnitTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private AdminAuditInterceptor interceptor;

    @BeforeEach
    public void setUp() {
        interceptor = new AdminAuditInterceptor(eventPublisher);
    }

    @Test
    @DisplayName("Should publish AuditEvent with null body for GET request")
    public void shouldPublishAuditEventWithNullBodyForGetRequest() throws Exception {
        // Given: An authenticated admin user
        final User actor = aValidUser();
        final MockHttpServletRequest raw = new MockHttpServletRequest("GET", "/v1/admin/users/search");
        raw.setRemoteAddr("192.168.1.1");
        final ContentCachingRequestWrapper request = new ContentCachingRequestWrapper(raw, 4096);
        final MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(HttpServletResponse.SC_OK);

        try (final MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getLoggedInUser).thenReturn(actor);

            // When: After request completes
            interceptor.afterCompletion(request, response, null, null);
        }

        // Then: AuditEvent should be published with null body
        final ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        final AuditEvent published = captor.getValue();
        assertThat(published.getActor().getId(), is(equalTo(actor.getId())));
        assertThat(published.getMethod(), is(equalTo("GET")));
        assertThat(published.getPath(), is(equalTo("/v1/admin/users/search")));
        assertThat(published.getStatusCode(), is(equalTo(200)));
        assertThat(published.getRequestBody(), is(nullValue()));
        assertThat(published.getIpAddress(), is(equalTo("192.168.1.1")));
    }

    @Test
    @DisplayName("Should publish AuditEvent with null body for HEAD request")
    public void shouldPublishAuditEventWithNullBodyForHeadRequest() throws Exception {
        // Given: An authenticated admin user
        final User actor = aValidUser();
        final MockHttpServletRequest raw = new MockHttpServletRequest("HEAD", "/v1/admin/users");
        final ContentCachingRequestWrapper request = new ContentCachingRequestWrapper(raw, 4096);
        final MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(HttpServletResponse.SC_OK);

        try (final MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getLoggedInUser).thenReturn(actor);

            // When: After request completes
            interceptor.afterCompletion(request, response, null, null);
        }

        // Then: AuditEvent should be published with null body
        final ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        final AuditEvent published = captor.getValue();
        assertThat(published.getRequestBody(), is(nullValue()));
    }

    @Test
    @DisplayName("Should publish AuditEvent with null body for OPTIONS request")
    public void shouldPublishAuditEventWithNullBodyForOptionsRequest() throws Exception {
        // Given: An authenticated admin user
        final User actor = aValidUser();
        final MockHttpServletRequest raw = new MockHttpServletRequest("OPTIONS", "/v1/admin/users");
        final ContentCachingRequestWrapper request = new ContentCachingRequestWrapper(raw, 4096);
        final MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(HttpServletResponse.SC_OK);

        try (final MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getLoggedInUser).thenReturn(actor);

            // When: After request completes
            interceptor.afterCompletion(request, response, null, null);
        }

        // Then: AuditEvent should be published with null body
        final ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        final AuditEvent published = captor.getValue();
        assertThat(published.getRequestBody(), is(nullValue()));
    }

    @Test
    @DisplayName("Should capture request body as JSON string for POST request")
    public void shouldCaptureRequestBodyForPostRequest() throws Exception {
        // Given: An authenticated admin user with a POST request containing a body
        final User actor = aValidUser();
        final String jsonBody = "{\"email\":\"user@example.com\",\"role\":\"ADMIN\"}";
        final MockHttpServletRequest raw = new MockHttpServletRequest("POST", "/v1/admin/users/1/role");
        raw.setContent(jsonBody.getBytes());
        raw.setContentType("application/json");
        final ContentCachingRequestWrapper request = new ContentCachingRequestWrapper(raw, 4096);
        // Simulate body being read (ContentCachingRequestWrapper caches after read)
        request.getInputStream().readAllBytes();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(HttpServletResponse.SC_OK);

        try (final MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getLoggedInUser).thenReturn(actor);

            // When: After request completes
            interceptor.afterCompletion(request, response, null, null);
        }

        // Then: AuditEvent should be published with request body
        final ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        final AuditEvent published = captor.getValue();
        assertThat(published.getMethod(), is(equalTo("POST")));
        assertThat(published.getRequestBody(), is(notNullValue()));
        assertThat(published.getRequestBody(), containsString("email"));
    }

    @Test
    @DisplayName("Should capture request body for PUT request")
    public void shouldCaptureRequestBodyForPutRequest() throws Exception {
        // Given: An authenticated admin user with a PUT request
        final User actor = aValidUser();
        final String jsonBody = "{\"name\":\"Updated Name\"}";
        final MockHttpServletRequest raw = new MockHttpServletRequest("PUT", "/v1/admin/organizations/1");
        raw.setContent(jsonBody.getBytes());
        raw.setContentType("application/json");
        final ContentCachingRequestWrapper request = new ContentCachingRequestWrapper(raw, 4096);
        request.getInputStream().readAllBytes();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(HttpServletResponse.SC_OK);

        try (final MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getLoggedInUser).thenReturn(actor);

            // When: After request completes
            interceptor.afterCompletion(request, response, null, null);
        }

        // Then: AuditEvent should be published with request body
        final ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        final AuditEvent published = captor.getValue();
        assertThat(published.getMethod(), is(equalTo("PUT")));
        assertThat(published.getRequestBody(), is(notNullValue()));
    }

    @Test
    @DisplayName("Should capture request body for PATCH request")
    public void shouldCaptureRequestBodyForPatchRequest() throws Exception {
        // Given: An authenticated admin user with a PATCH request
        final User actor = aValidUser();
        final String jsonBody = "{\"enabled\":false}";
        final MockHttpServletRequest raw = new MockHttpServletRequest("PATCH", "/v1/admin/users/1");
        raw.setContent(jsonBody.getBytes());
        raw.setContentType("application/json");
        final ContentCachingRequestWrapper request = new ContentCachingRequestWrapper(raw, 4096);
        request.getInputStream().readAllBytes();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(HttpServletResponse.SC_OK);

        try (final MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getLoggedInUser).thenReturn(actor);

            // When: After request completes
            interceptor.afterCompletion(request, response, null, null);
        }

        // Then: AuditEvent should be published with request body
        final ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        final AuditEvent published = captor.getValue();
        assertThat(published.getMethod(), is(equalTo("PATCH")));
        assertThat(published.getRequestBody(), is(notNullValue()));
    }

    @Test
    @DisplayName("Should capture request body for DELETE request")
    public void shouldCaptureRequestBodyForDeleteRequest() throws Exception {
        // Given: An authenticated admin user with a DELETE request
        final User actor = aValidUser();
        final MockHttpServletRequest raw = new MockHttpServletRequest("DELETE", "/v1/admin/users/1");
        final ContentCachingRequestWrapper request = new ContentCachingRequestWrapper(raw, 4096);
        final MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);

        try (final MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getLoggedInUser).thenReturn(actor);

            // When: After request completes
            interceptor.afterCompletion(request, response, null, null);
        }

        // Then: AuditEvent should be published
        final ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        final AuditEvent published = captor.getValue();
        assertThat(published.getMethod(), is(equalTo("DELETE")));
        assertThat(published.getStatusCode(), is(equalTo(204)));
    }

    @Test
    @DisplayName("Should capture 4xx status code for failed requests")
    public void shouldCapture4xxStatusCodeForFailedRequests() throws Exception {
        // Given: An authenticated admin user whose request returns 403
        final User actor = aValidUser();
        final MockHttpServletRequest raw = new MockHttpServletRequest("GET", "/v1/admin/users");
        final ContentCachingRequestWrapper request = new ContentCachingRequestWrapper(raw, 4096);
        final MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        try (final MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getLoggedInUser).thenReturn(actor);

            // When: After request completes with 403
            interceptor.afterCompletion(request, response, null, null);
        }

        // Then: AuditEvent should be published with 403 status
        final ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        final AuditEvent published = captor.getValue();
        assertThat(published.getStatusCode(), is(equalTo(403)));
    }

    @Test
    @DisplayName("Should capture 5xx status code for server error requests")
    public void shouldCapture5xxStatusCodeForServerErrorRequests() throws Exception {
        // Given: An authenticated admin user whose request returns 500
        final User actor = aValidUser();
        final MockHttpServletRequest raw = new MockHttpServletRequest("POST", "/v1/admin/users/search");
        final ContentCachingRequestWrapper request = new ContentCachingRequestWrapper(raw, 4096);
        final MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        try (final MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getLoggedInUser).thenReturn(actor);

            // When: After request completes with 500
            interceptor.afterCompletion(request, response, null, null);
        }

        // Then: AuditEvent should be published with 500 status
        final ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        final AuditEvent published = captor.getValue();
        assertThat(published.getStatusCode(), is(equalTo(500)));
    }

    @Test
    @DisplayName("Should extract IPv6 address from request")
    public void shouldExtractIpv6AddressFromRequest() throws Exception {
        // Given: An authenticated admin user with an IPv6 address
        final User actor = aValidUser();
        final MockHttpServletRequest raw = new MockHttpServletRequest("GET", "/v1/admin/users");
        raw.setRemoteAddr("2001:db8::1");
        final ContentCachingRequestWrapper request = new ContentCachingRequestWrapper(raw, 4096);
        final MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(HttpServletResponse.SC_OK);

        try (final MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getLoggedInUser).thenReturn(actor);

            // When: After request completes
            interceptor.afterCompletion(request, response, null, null);
        }

        // Then: AuditEvent should contain the IPv6 address
        final ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        final AuditEvent published = captor.getValue();
        assertThat(published.getIpAddress(), is(equalTo("2001:db8::1")));
    }

    @Test
    @DisplayName("Should handle path up to 512 characters")
    public void shouldHandleVeryLongPath() throws Exception {
        // Given: An authenticated admin user with a very long path (512 chars)
        final User actor = aValidUser();
        final String longPath = "/v1/admin/users/" + "a".repeat(495);
        final MockHttpServletRequest raw = new MockHttpServletRequest("GET", longPath);
        final ContentCachingRequestWrapper request = new ContentCachingRequestWrapper(raw, 4096);
        final MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(HttpServletResponse.SC_OK);

        try (final MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getLoggedInUser).thenReturn(actor);

            // When: After request completes
            interceptor.afterCompletion(request, response, null, null);
        }

        // Then: AuditEvent should be published with the path (possibly truncated to 512)
        final ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        final AuditEvent published = captor.getValue();
        assertThat(published.getPath(), is(notNullValue()));
        assertThat(published.getPath().length(), is(lessThanOrEqualTo(512)));
    }

    @Test
    @DisplayName("Should not publish event when no authenticated user")
    public void shouldNotPublishEventWhenNoAuthenticatedUser() throws Exception {
        // Given: No authenticated user (SecurityUtil returns null)
        final MockHttpServletRequest raw = new MockHttpServletRequest("GET", "/v1/admin/users");
        final ContentCachingRequestWrapper request = new ContentCachingRequestWrapper(raw, 4096);
        final MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        try (final MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getLoggedInUser).thenReturn(null);

            // When: After request completes
            interceptor.afterCompletion(request, response, null, null);
        }

        // Then: No AuditEvent should be published
        verify(eventPublisher, never()).publishEvent(any(AuditEvent.class));
    }

    @Test
    @DisplayName("Should use X-Forwarded-For header for IP when present")
    public void shouldUseXForwardedForHeaderForIp() throws Exception {
        // Given: An authenticated admin user with X-Forwarded-For header
        final User actor = aValidUser();
        final MockHttpServletRequest raw = new MockHttpServletRequest("GET", "/v1/admin/users");
        raw.setRemoteAddr("10.0.0.1");
        raw.addHeader("X-Forwarded-For", "203.0.113.42, 10.0.0.1");
        final ContentCachingRequestWrapper request = new ContentCachingRequestWrapper(raw, 4096);
        final MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(HttpServletResponse.SC_OK);

        try (final MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getLoggedInUser).thenReturn(actor);

            // When: After request completes
            interceptor.afterCompletion(request, response, null, null);
        }

        // Then: AuditEvent should use the forwarded IP
        final ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        final AuditEvent published = captor.getValue();
        assertThat(published.getIpAddress(), is(equalTo("203.0.113.42")));
    }
}
