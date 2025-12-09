package io.github.eventify.common.security.oauth2;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

/**
 * Failure handler for OAuth2 authentication. Redirects to the frontend login page with an error message when OAuth2 login fails.
 */
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final OAuth2RedirectHelper redirectHelper;

    /**
     * Handle OAuth2 authentication failure by redirecting to the frontend login page with error details.
     *
     * @param request   The HTTP request.
     * @param response  The HTTP response.
     * @param exception The authentication exception.
     * @throws IOException If an I/O error occurs during redirect.
     */
    @Override
    public void onAuthenticationFailure(
        final HttpServletRequest request,
        final HttpServletResponse response,
        final AuthenticationException exception) throws IOException {
        final String redirectUrl = redirectHelper.buildRedirectUrl(exception.getLocalizedMessage());
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
