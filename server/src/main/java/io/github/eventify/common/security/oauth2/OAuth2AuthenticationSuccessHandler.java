package io.github.eventify.common.security.oauth2;

import io.github.eventify.api.authentication.service.CookieService;
import io.github.eventify.api.token.service.TokenService;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.UUID;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import static io.github.eventify.common.constant.Constants.OAuthAttributes.EMAIL;
import static io.github.eventify.common.security.oauth2.OAuth2Attributes.MODE;
import static io.github.eventify.common.security.oauth2.OAuth2Attributes.MODE_LINK;
import static io.github.eventify.common.security.oauth2.OAuth2Attributes.RESOLVED_USER_ID;
import static io.github.eventify.common.util.TimeProvider.now;
import static java.util.Objects.isNull;

/**
 * Success handler for OAuth2 authentication. Issues JWT tokens after successful OAuth2 login and redirects to the frontend.
 * In link mode, skips JWT issuance and redirects to the security page.
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
     * In link mode, skips JWT issuance and redirects to the security page.
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
        if (!response.isCommitted()) {
            processAuthentication(request, response, authentication);
        } else {
            log.info("Response has already been committed. Unable to redirect to OAuth2 success URL");
        }
    }

    private void processAuthentication(
        final HttpServletRequest request,
        final HttpServletResponse response,
        final Authentication authentication) throws IOException {
        try {
            final UUID familyId = cookieService.readDeviceId(request).orElse(UUID.randomUUID());
            final OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            final String email = oAuth2User.getAttribute(EMAIL);
            final String mode = OAuth2AttributesHolder.getAttribute(MODE);
            final String provider = authentication instanceof OAuth2AuthenticationToken oAuth2Token
                ? oAuth2Token.getAuthorizedClientRegistrationId()
                : null;
            if (isNull(email)) {
                final String redirectUrl = redirectHelper.buildRedirectUrl("Email not found from OAuth2 provider");
                getRedirectStrategy().sendRedirect(request, response, redirectUrl);
            } else if (MODE_LINK.equals(mode) && provider != null) {
                final String redirectUrl = redirectHelper.buildLinkSuccessRedirectUrl(provider);
                getRedirectStrategy().sendRedirect(request, response, redirectUrl);
            } else {
                authorize(request, response, email, familyId);
            }
        } catch (final Exception exception) {
            log.error("Exception occurred while processing OAuth2 authentication", exception);
            final String redirectUrl = redirectHelper.buildRedirectUrl("Authentication processing failed");
            getRedirectStrategy().sendRedirect(request, response, redirectUrl);
        }
    }

    private void authorize(final HttpServletRequest request, final HttpServletResponse response, final String email, final UUID familyId)
        throws IOException {
        final Long resolvedUserId = OAuth2AttributesHolder.getAttribute(RESOLVED_USER_ID);
        User user;
        if (resolvedUserId != null) {
            user = userService.findById(resolvedUserId);
        } else {
            log.warn("resolvedUserId not found in OAuth2AttributesHolder; falling back to email lookup for '{}'", email);
            user = userService.loadUserByUsername(email);
        }
        user.setLastLogin(now());
        userService.updateUserDetails(user);

        log.info("User '{}' successfully authenticated via OAuth", email);
        // OAuth2 logins do not expose a remember-me toggle; always use the standard refresh-token lifetime.
        user = tokenService.generateAuthorizationTokens(user, request, false, familyId);
        cookieService.setAuthCookies(response, user.getAccessToken(), user.getRefreshToken());

        final String redirectUrl = redirectHelper.buildRedirectUrl();
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
