package io.github.eventify.common.security.oauth2;

import io.github.eventify.common.exception.LinkOAuth2Exception;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

/**
 * Failure handler for OAuth2 authentication. Redirects to the frontend login page with an error message when OAuth2 login fails.
 * For link-mode failures (LinkOAuth2Exception), redirects to /profile/connected-accounts with an error code.
 */
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final OAuth2RedirectHelper redirectHelper;

    /**
     * Handle OAuth2 authentication failure by redirecting to the frontend login page with error details.
     * If the exception is a {@link LinkOAuth2Exception}, redirects to the connected-accounts page with the error code.
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
        final LinkOAuth2Exception linkException = extractLinkException(exception);
        final String redirectUrl = linkException != null
            ? redirectHelper.buildLinkErrorRedirectUrl(linkException.getErrorCode())
            : redirectHelper.buildRedirectUrl(exception.getLocalizedMessage());
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

    private LinkOAuth2Exception extractLinkException(final AuthenticationException exception) {
        final boolean isLink = exception instanceof LinkOAuth2Exception;
        final boolean causeIsLink = !isLink && exception.getCause() instanceof LinkOAuth2Exception;
        return isLink ? (LinkOAuth2Exception) exception
            : causeIsLink ? (LinkOAuth2Exception) exception.getCause()
                : null;
    }
}
