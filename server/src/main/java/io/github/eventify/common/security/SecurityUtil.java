package io.github.eventify.common.security;

import io.github.eventify.api.user.model.User;
import io.github.eventify.common.exception.AuthorizationException;
import io.github.eventify.common.security.principal.JwtUserPrincipalAuthenticationToken;
import io.github.eventify.common.security.principal.UserTokenPrincipal;

import java.security.Principal;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static io.github.eventify.common.exception.ApiErrorCode.NO_SECURITY_CONTEXT_ERROR;
import static java.util.Objects.isNull;

/**
 * Utility class for security related operations.
 */
public final class SecurityUtil {

    private SecurityUtil() {
        // Utility class
    }

    /**
     * Retrieve the logged-in user from the security context.
     */
    public static User getLoggedInUser() {
        if (getPrincipal() instanceof final JwtUserPrincipalAuthenticationToken jwtToken) {
            if (isNull(jwtToken.getPrincipal())) {
                throw new AuthorizationException(NO_SECURITY_CONTEXT_ERROR);
            }

            if (jwtToken.getPrincipal() instanceof final UserTokenPrincipal userPrincipal) {
                return userPrincipal.getUser();
            }
        }
        throw new AuthorizationException(NO_SECURITY_CONTEXT_ERROR);
    }

    private static Principal getPrincipal() {
        final Principal principal = SecurityContextHolder.getContext().getAuthentication();
        if (isNull(principal) || principal instanceof AnonymousAuthenticationToken) {
            throw new AuthorizationException(NO_SECURITY_CONTEXT_ERROR);
        }
        return principal;
    }
}
