package io.github.eventify.common.security.oauth2;

import io.github.eventify.support.UnitTest;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link OAuth2AttributesFilter}.
 */
@DisplayName("Unit Test - OAuth2AttributesFilter")
public class OAuth2AttributesFilterTest extends UnitTest {

    private static final String CALLBACK_PATH = "/login/oauth2/code/google";
    private static final String NON_CALLBACK_PATH = "/v1/user/details";
    private static final String MODE_LINK = "link";
    private static final Long LINK_USER_ID = 42L;

    @Mock
    private HttpSessionOAuth2AuthorizationRequestRepository authorizationRequestRepository;

    @Mock
    private FilterChain filterChain;

    private OAuth2AttributesFilter filter;

    @BeforeEach
    public void setUp() {
        filter = new OAuth2AttributesFilter(authorizationRequestRepository);
    }

    @AfterEach
    public void tearDown() {
        OAuth2AttributesHolder.clear();
    }

    @Test
    @DisplayName("Should push attributes into holder during chain and clear after for callback with saved auth request")
    public void shouldPushAttributesIntoHolderDuringChainAndClearAfterForCallbackWithSavedAuthRequest()
        throws ServletException, IOException {
        // Given: A callback request with a saved OAuth2AuthorizationRequest containing attributes
        final MockHttpServletRequest request = buildCallbackRequest();
        final HttpServletResponse response = new MockHttpServletResponse();

        final OAuth2AuthorizationRequest authRequest = buildAuthorizationRequest(
            Map.of("mode", MODE_LINK, "linkUserId", LINK_USER_ID)
        );
        when(authorizationRequestRepository.loadAuthorizationRequest(request)).thenReturn(authRequest);

        // And: A filter chain that captures the holder state during execution
        final AtomicReference<Map<String, Object>> capturedAttributes = new AtomicReference<>();
        doAnswer(invocation -> {
            capturedAttributes.set(OAuth2AttributesHolder.getAttributes());
            return null;
        }).when(filterChain).doFilter(request, response);

        // When: The filter processes the request
        filter.doFilterInternal(request, response, filterChain);

        // Then: During chain execution, attributes were populated
        assertThat(capturedAttributes.get(), hasEntry("mode", MODE_LINK));
        assertThat(capturedAttributes.get(), hasEntry("linkUserId", LINK_USER_ID));

        // And: After filter completes, holder is cleared
        assertThat(OAuth2AttributesHolder.getAttributes(), is(anEmptyMap()));
    }

    @Test
    @DisplayName("Should leave holder empty and not throw when callback has no saved auth request")
    public void shouldLeaveHolderEmptyAndNotThrowWhenCallbackHasNoSavedAuthRequest()
        throws ServletException, IOException {
        // Given: A callback request with NO saved OAuth2AuthorizationRequest
        final MockHttpServletRequest request = buildCallbackRequest();
        final HttpServletResponse response = new MockHttpServletResponse();

        when(authorizationRequestRepository.loadAuthorizationRequest(request)).thenReturn(null);

        // And: A filter chain that captures the holder state during execution
        final AtomicReference<Map<String, Object>> capturedAttributes = new AtomicReference<>();
        doAnswer(invocation -> {
            capturedAttributes.set(OAuth2AttributesHolder.getAttributes());
            return null;
        }).when(filterChain).doFilter(request, response);

        // When: The filter processes the request
        filter.doFilterInternal(request, response, filterChain);

        // Then: During chain execution, holder was empty (no attributes pushed)
        assertThat(capturedAttributes.get(), is(anEmptyMap()));

        // And: After filter completes, holder is still clear
        assertThat(OAuth2AttributesHolder.getAttributes(), is(anEmptyMap()));
    }

    @Test
    @DisplayName("Should be a no-op for non-callback requests and leave holder untouched")
    public void shouldBeNoOpForNonCallbackRequestsAndLeaveHolderUntouched()
        throws ServletException, IOException {
        // Given: A non-callback request (e.g. /v1/user/details)
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath(NON_CALLBACK_PATH);
        final HttpServletResponse response = new MockHttpServletResponse();

        // When: The filter processes the request
        filter.doFilterInternal(request, response, filterChain);

        // Then: The authorization request repository was never consulted
        verify(authorizationRequestRepository, never()).loadAuthorizationRequest(any());

        // And: The filter chain was still invoked
        verify(filterChain, times(1)).doFilter(request, response);

        // And: Holder remains empty
        assertThat(OAuth2AttributesHolder.getAttributes(), is(anEmptyMap()));
    }

    @Test
    @DisplayName("Should clear holder even when downstream filter throws an exception")
    public void shouldClearHolderEvenWhenDownstreamFilterThrows() throws ServletException, IOException {
        // Given: A callback request with a saved auth request
        final MockHttpServletRequest request = buildCallbackRequest();
        final HttpServletResponse response = new MockHttpServletResponse();

        final OAuth2AuthorizationRequest authRequest = buildAuthorizationRequest(
            Map.of("mode", MODE_LINK)
        );
        when(authorizationRequestRepository.loadAuthorizationRequest(request)).thenReturn(authRequest);

        // And: Downstream filter throws a RuntimeException
        doThrow(new RuntimeException("downstream failure"))
            .when(filterChain).doFilter(request, response);

        // When: The filter processes the request (exception propagates)
        assertThrows(RuntimeException.class, () -> filter.doFilterInternal(request, response, filterChain));

        // Then: Holder is cleared despite the exception (try/finally guarantee)
        assertThat(OAuth2AttributesHolder.getAttributes(), is(anEmptyMap()));
    }

    // ========================= Helper methods =========================

    private MockHttpServletRequest buildCallbackRequest() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath(CALLBACK_PATH);
        return request;
    }

    private OAuth2AuthorizationRequest buildAuthorizationRequest(final Map<String, Object> attributes) {
        return OAuth2AuthorizationRequest.authorizationCode()
            .clientId("google-client-id")
            .authorizationUri("https://accounts.google.com/o/oauth2/auth")
            .redirectUri("http://localhost:8080/login/oauth2/code/google")
            .attributes(attrs -> attrs.putAll(attributes))
            .build();
    }
}
