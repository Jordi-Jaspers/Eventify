package io.github.eventify.common.security.oauth2;

import java.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Servlet filter that populates {@link OAuth2AttributesHolder} for OAuth2 callback requests.
 * <p>
 * For requests matching {@code /login/oauth2/code/**}, loads the saved
 * {@code OAuth2AuthorizationRequest} from the HTTP session and pushes its
 * {@code attributes} map into the ThreadLocal holder so that downstream components
 * (e.g. {@link CustomOAuth2UserService}, {@link OAuth2AuthenticationSuccessHandler}) can
 * read server-side attributes such as {@code mode} and {@code linkUserId}.
 * <p>
 * The ThreadLocal is always cleared in a {@code finally} block to prevent memory leaks.
 */
@Component
public class OAuth2AttributesFilter extends OncePerRequestFilter {

    private static final String CALLBACK_PATH_PREFIX = "/login/oauth2/code/";

    private final HttpSessionOAuth2AuthorizationRequestRepository authorizationRequestRepository;

    /**
     * Default constructor used by Spring — creates a default {@link HttpSessionOAuth2AuthorizationRequestRepository}.
     */
    public OAuth2AttributesFilter() {
        this.authorizationRequestRepository = new HttpSessionOAuth2AuthorizationRequestRepository();
    }

    /**
     * Constructor for testing — allows injecting a mock repository.
     *
     * @param authorizationRequestRepository the repository to use
     */
    public OAuth2AttributesFilter(
                                  final HttpSessionOAuth2AuthorizationRequestRepository authorizationRequestRepository) {
        this.authorizationRequestRepository = authorizationRequestRepository;
    }

    @Override
    protected void doFilterInternal(
        final HttpServletRequest request,
        final HttpServletResponse response,
        final FilterChain filterChain) throws ServletException, IOException {
        try {
            if (isCallbackRequest(request)) {
                loadAttributes(request);
            }
            filterChain.doFilter(request, response);
        } finally {
            OAuth2AttributesHolder.clear();
        }
    }

    private boolean isCallbackRequest(final HttpServletRequest request) {
        final String path = request.getServletPath();
        return path != null && path.startsWith(CALLBACK_PATH_PREFIX);
    }

    private void loadAttributes(final HttpServletRequest request) {
        final OAuth2AuthorizationRequest authRequest = authorizationRequestRepository.loadAuthorizationRequest(request);
        if (authRequest != null && !authRequest.getAttributes().isEmpty()) {
            OAuth2AttributesHolder.setAttributes(authRequest.getAttributes());
        }
    }
}
