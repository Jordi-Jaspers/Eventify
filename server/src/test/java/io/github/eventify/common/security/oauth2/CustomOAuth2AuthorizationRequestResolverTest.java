package io.github.eventify.common.security.oauth2;

import io.github.eventify.api.user.model.User;
import io.github.eventify.common.security.principal.UserTokenPrincipal;
import io.github.eventify.support.UnitTest;

import java.util.Collections;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link CustomOAuth2AuthorizationRequestResolver}.
 * <p>
 * Verifies that the resolver captures the {@code mode} query parameter from the initial request
 * and stores it in {@link OAuth2AuthorizationRequest#getAttributes()} (server-side only).
 * When mode=link and a UserTokenPrincipal is in SecurityContext, also captures {@code linkUserId}.
 * Neither {@code mode} nor {@code linkUserId} must appear in the authorization URI sent to the provider.
 */
@DisplayName("Unit Test - CustomOAuth2AuthorizationRequestResolver")
public class CustomOAuth2AuthorizationRequestResolverTest extends UnitTest {

    @Mock
    private ClientRegistrationRepository clientRegistrationRepository;

    private CustomOAuth2AuthorizationRequestResolver resolver;

    @BeforeEach
    public void setUp() {
        final ClientRegistration googleRegistration = ClientRegistration
            .withRegistrationId("google")
            .clientId("google-client-id")
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
            .authorizationUri("https://accounts.google.com/o/oauth2/auth")
            .tokenUri("https://oauth2.googleapis.com/token")
            .scope("openid", "email", "profile")
            .build();

        final ClientRegistration githubRegistration = ClientRegistration
            .withRegistrationId("github")
            .clientId("github-client-id")
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
            .authorizationUri("https://github.com/login/oauth/authorize")
            .tokenUri("https://github.com/login/oauth/access_token")
            .scope("read:user", "user:email")
            .build();

        lenient().when(clientRegistrationRepository.findByRegistrationId("google")).thenReturn(googleRegistration);
        lenient().when(clientRegistrationRepository.findByRegistrationId("github")).thenReturn(githubRegistration);

        resolver = new CustomOAuth2AuthorizationRequestResolver(
            clientRegistrationRepository,
            "/oauth2/authorization"
        );
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private MockHttpServletRequest buildRequest(final String registrationId, final String mode) {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/oauth2/authorization/" + registrationId);
        request.setRequestURI("/oauth2/authorization/" + registrationId);
        if (mode != null) {
            request.setParameter("mode", mode);
        }
        return request;
    }

    private void authenticateUser(final Long userId) {
        final User user = aValidUser();
        user.setId(userId);
        final UserTokenPrincipal principal = new UserTokenPrincipal(user, "test-token");
        final UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
            principal,
            null,
            Collections.emptyList()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("Should capture mode=link and linkUserId from authenticated user into attributes")
    public void resolve_capturesModeLinkAndLinkUserId_whenAuthenticatedUser() {
        // Given: A request URL containing ?mode=link and an authenticated user
        final MockHttpServletRequest request = buildRequest("google", "link");
        authenticateUser(42L);

        // When: Resolving the authorization request
        final OAuth2AuthorizationRequest resolved = resolver.resolve(request);

        // Then: attributes contains mode=link and linkUserId=42
        assertThat(resolved, is(notNullValue()));
        assertThat(resolved.getAttributes(), hasEntry("mode", "link"));
        assertThat(resolved.getAttributes(), hasEntry("linkUserId", 42L));

        // And: The authorization URI does NOT contain mode= or linkUserId= query params
        assertThat(resolved.getAuthorizationRequestUri(), not(containsString("mode=")));
        assertThat(resolved.getAuthorizationRequestUri(), not(containsString("linkUserId=")));
    }

    @Test
    @DisplayName("Should capture mode=link from query param into attributes")
    public void resolve_capturesModeLinkFromQueryParam_intoAttributes() {
        // Given: A request URL containing ?mode=link and an authenticated user
        final MockHttpServletRequest request = buildRequest("google", "link");
        authenticateUser(1L);

        // When: Resolving the authorization request
        final OAuth2AuthorizationRequest resolved = resolver.resolve(request);

        // Then: attributes contains mode=link
        assertThat(resolved, is(notNullValue()));
        assertThat(resolved.getAttributes(), hasEntry("mode", "link"));

        // And: The authorization URI does NOT contain mode= query param
        assertThat(resolved.getAuthorizationRequestUri(), not(containsString("mode=")));
    }

    @Test
    @DisplayName("Should default to mode=login when no mode param is present")
    public void resolve_defaultsToModeLogin_whenNoModeParam() {
        // Given: A request URL with no mode parameter
        final MockHttpServletRequest request = buildRequest("google", null);

        // When: Resolving the authorization request
        final OAuth2AuthorizationRequest resolved = resolver.resolve(request);

        // Then: attributes contains mode=login (default) and no linkUserId
        assertThat(resolved, is(notNullValue()));
        assertThat(resolved.getAttributes(), hasEntry("mode", "login"));
        assertThat(resolved.getAttributes(), not(hasKey("linkUserId")));
    }

    @Test
    @DisplayName("Should ignore invalid mode values and default to login")
    public void resolve_ignoresInvalidModeValues() {
        // Given: A request URL with an invalid mode value (security: don't trust arbitrary client input)
        final MockHttpServletRequest request = buildRequest("google", "foo");

        // When: Resolving the authorization request
        final OAuth2AuthorizationRequest resolved = resolver.resolve(request);

        // Then: attributes defaults to mode=login (invalid values are rejected) and no linkUserId
        assertThat(resolved, is(notNullValue()));
        assertThat(resolved.getAttributes(), hasEntry("mode", "login"));
        assertThat(resolved.getAttributes(), not(hasKey("linkUserId")));
    }

    @Test
    @DisplayName("Should not include linkUserId when mode=link but no authenticated user")
    public void resolve_doesNotIncludeLinkUserId_whenModeLinkButNoAuthenticatedUser() {
        // Given: A request URL containing ?mode=link but no authenticated user in SecurityContext
        final MockHttpServletRequest request = buildRequest("google", "link");

        // When: Resolving the authorization request
        final OAuth2AuthorizationRequest resolved = resolver.resolve(request);

        // Then: attributes contains mode=link but no linkUserId
        assertThat(resolved, is(notNullValue()));
        assertThat(resolved.getAttributes(), hasEntry("mode", "link"));
        assertThat(resolved.getAttributes(), not(hasKey("linkUserId")));
    }

    @Test
    @DisplayName("Should capture mode=link when resolving with registrationId override")
    public void resolve_withRegistrationId_capturesModeLinkFromQueryParam() {
        // Given: A request URL containing ?mode=link and an authenticated user
        final MockHttpServletRequest request = buildRequest("github", "link");
        authenticateUser(7L);

        // When: Resolving the authorization request with explicit registrationId
        final OAuth2AuthorizationRequest resolved = resolver.resolve(request, "github");

        // Then: attributes contains mode=link and linkUserId
        assertThat(resolved, is(notNullValue()));
        assertThat(resolved.getAttributes(), hasEntry("mode", "link"));
        assertThat(resolved.getAttributes(), hasEntry("linkUserId", 7L));

        // And: The authorization URI does NOT contain mode= or linkUserId= query params
        assertThat(resolved.getAuthorizationRequestUri(), not(containsString("mode=")));
        assertThat(resolved.getAuthorizationRequestUri(), not(containsString("linkUserId=")));
    }

    @Test
    @DisplayName("Should default to mode=login when resolving with registrationId and no mode param")
    public void resolve_withRegistrationId_defaultsToModeLogin_whenNoModeParam() {
        // Given: A request URL with no mode parameter
        final MockHttpServletRequest request = buildRequest("github", null);

        // When: Resolving the authorization request with explicit registrationId
        final OAuth2AuthorizationRequest resolved = resolver.resolve(request, "github");

        // Then: attributes contains mode=login (default) and no linkUserId
        assertThat(resolved, is(notNullValue()));
        assertThat(resolved.getAttributes(), hasEntry("mode", "login"));
        assertThat(resolved.getAttributes(), not(hasKey("linkUserId")));
    }
}
