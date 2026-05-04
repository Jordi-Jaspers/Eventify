package io.github.eventify.common.security.oauth2;

import io.github.eventify.api.authentication.service.CookieService;
import io.github.eventify.api.token.service.TokenService;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import static io.github.eventify.common.constant.Constants.OAuthAttributes.EMAIL;
import static io.github.eventify.common.util.TimeProvider.now;
import static java.util.Objects.isNull;

/**
 * Success handler for OAuth2 authentication. Issues JWT tokens after successful OAuth2 login and redirects to the frontend.
 * <p>
 * Security Note: JWT tokens are stored in HTTP-only, secure cookies with SameSite=Lax attribute for maximum security. This prevents token
 * exposure through browser history, Referer headers, or server logs that would occur with URL query parameters. The frontend can access
 * these tokens through cookie headers on subsequent requests.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final TokenService tokenService;

    private final UserService userService;

    private final CookieService cookieService;

    private final OAuth2RedirectHelper redirectHelper;

    /**
     * Handle successful OAuth2 authentication by generating JWT tokens and redirecting to frontend.
     *
     * @param request        The HTTP request.
     * @param response       The HTTP response.
     * @param authentication The authentication object.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public void onAuthenticationSuccess(
        final HttpServletRequest request,
        final HttpServletResponse response,
        final Authentication authentication) throws IOException {
        if (response.isCommitted()) {
            log.info("Response has already been committed. Unable to redirect to OAuth2 success URL");
            return;
        }
        processAuthentication(request, response, authentication);
    }

    private void processAuthentication(
        final HttpServletRequest request,
        final HttpServletResponse response,
        final Authentication authentication) throws IOException {
        try {
            final OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            final String email = oAuth2User.getAttribute(EMAIL);
            if (isNull(email)) {
                final String redirectUrl = redirectHelper.buildRedirectUrl("Email not found from OAuth2 provider");
                getRedirectStrategy().sendRedirect(request, response, redirectUrl);
                return;
            }

            authorize(request, response, email);
        } catch (final Exception exception) {
            log.error("Exception occurred while processing OAuth2 authentication", exception);
            final String redirectUrl = redirectHelper.buildRedirectUrl("Authentication processing failed");
            getRedirectStrategy().sendRedirect(request, response, redirectUrl);
        }
    }

    private void authorize(final HttpServletRequest request, final HttpServletResponse response, final String email) throws IOException {
        User user = userService.loadUserByUsername(email);
        user.setLastLogin(now());
        userService.updateUserDetails(user);

        log.info("User '{}' successfully authenticated via OAuth", email);
        user = tokenService.generateAuthorizationTokens(user, request);
        cookieService.setAuthCookies(response, user.getAccessToken(), user.getRefreshToken());

        final String redirectUrl = redirectHelper.buildRedirectUrl();
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
