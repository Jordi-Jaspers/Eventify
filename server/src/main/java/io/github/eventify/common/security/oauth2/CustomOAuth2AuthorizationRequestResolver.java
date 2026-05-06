package io.github.eventify.common.security.oauth2;

import io.github.eventify.common.security.principal.UserTokenPrincipal;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import static io.github.eventify.common.security.oauth2.OAuth2Attributes.LINK_USER_ID;
import static io.github.eventify.common.security.oauth2.OAuth2Attributes.MODE;
import static io.github.eventify.common.security.oauth2.OAuth2Attributes.MODE_LINK;
import static io.github.eventify.common.security.oauth2.OAuth2Attributes.MODE_LOGIN;

/**
 * Custom OAuth2 authorization request resolver that captures the {@code mode} query parameter
 * from the initial request and stores it in {@link OAuth2AuthorizationRequest#getAdditionalParameters()}.
 * <p>
 * When mode=link, also captures the current authenticated user's ID as {@code linkUserId} so it
 * can be read back in {@link CustomOAuth2UserService} during the OAuth2 callback.
 * <p>
 * Security: only "login" and "link" are accepted; any other value defaults to "login".
 * <p>
 * Delegates all request building to {@link DefaultOAuth2AuthorizationRequestResolver} to ensure
 * state, PKCE, nonce, and other security parameters are correctly populated.
 */
public class CustomOAuth2AuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private final DefaultOAuth2AuthorizationRequestResolver delegate;
    private final ThreadLocal<String> currentMode = new ThreadLocal<>();
    private final ThreadLocal<Long> currentLinkUserId = new ThreadLocal<>();

    /**
     * Creates a new resolver.
     *
     * @param clientRegistrationRepository the client registration repository
     * @param authorizationRequestBaseUri  the base URI for authorization requests
     */
    public CustomOAuth2AuthorizationRequestResolver(
                                                    final ClientRegistrationRepository clientRegistrationRepository,
                                                    final String authorizationRequestBaseUri) {
        this.delegate = new DefaultOAuth2AuthorizationRequestResolver(
            clientRegistrationRepository,
            authorizationRequestBaseUri
        );
        this.delegate.setAuthorizationRequestCustomizer(
            builder -> builder.attributes(attrs -> {
                attrs.put(MODE, currentMode.get() != null ? currentMode.get() : MODE_LOGIN);
                final Long userId = currentLinkUserId.get();
                if (userId != null) {
                    attrs.put(LINK_USER_ID, userId);
                }
            })
        );
    }

    /**
     * Resolves the OAuth2 authorization request, capturing {@code mode} (and {@code linkUserId} for link mode)
     * into the request's server-side attributes.
     *
     * @param request the HTTP request
     * @return the authorization request, or {@code null} if not applicable
     */
    @Override
    public OAuth2AuthorizationRequest resolve(final HttpServletRequest request) {
        try {
            populateThreadLocals(request);
            return delegate.resolve(request);
        } finally {
            clearThreadLocals();
        }
    }

    /**
     * Resolves the OAuth2 authorization request for the given client registration, capturing
     * {@code mode} (and {@code linkUserId} for link mode) into the request's server-side attributes.
     *
     * @param request              the HTTP request
     * @param clientRegistrationId the client registration ID
     * @return the authorization request, or {@code null} if not applicable
     */
    @Override
    public OAuth2AuthorizationRequest resolve(final HttpServletRequest request, final String clientRegistrationId) {
        try {
            populateThreadLocals(request);
            return delegate.resolve(request, clientRegistrationId);
        } finally {
            clearThreadLocals();
        }
    }

    private void populateThreadLocals(final HttpServletRequest request) {
        final String mode = resolveMode(request);
        currentMode.set(mode);
        if (MODE_LINK.equals(mode)) {
            currentLinkUserId.set(resolveCurrentUserId());
        }
    }

    private void clearThreadLocals() {
        currentMode.remove();
        currentLinkUserId.remove();
    }

    private String resolveMode(final HttpServletRequest request) {
        final String rawMode = request.getParameter(MODE);
        return MODE_LINK.equals(rawMode) ? MODE_LINK : MODE_LOGIN;
    }

    private Long resolveCurrentUserId() {
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        final Object principal = auth.getPrincipal();
        return principal instanceof final UserTokenPrincipal p ? p.getUser().getId() : null;
    }
}
