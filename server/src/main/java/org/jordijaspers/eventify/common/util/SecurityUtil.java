package org.jordijaspers.eventify.common.util;

import java.security.Principal;

import org.jordijaspers.eventify.api.source.model.Source;
import org.jordijaspers.eventify.api.user.model.User;
import org.jordijaspers.eventify.common.exception.AuthorizationException;
import org.jordijaspers.eventify.common.security.principal.JwtUserPrincipalAuthenticationToken;
import org.jordijaspers.eventify.common.security.principal.SourceTokenPrincipal;
import org.jordijaspers.eventify.common.security.principal.UserTokenPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;

import static java.util.Objects.isNull;
import static org.jordijaspers.eventify.common.exception.ApiErrorCode.NO_SECURITY_CONTEXT_ERROR;

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
        final JwtUserPrincipalAuthenticationToken principal = (JwtUserPrincipalAuthenticationToken) getPrincipal();
        if (isNull(principal) || isNull(principal.getPrincipal())) {
            throw new AuthorizationException(NO_SECURITY_CONTEXT_ERROR);
        }

        if (principal.getPrincipal() instanceof final UserTokenPrincipal userPrincipal) {
            return userPrincipal.getUser();
        }

        throw new AuthorizationException(NO_SECURITY_CONTEXT_ERROR);
    }

    /**
     * Retrieve the logged-in source from the security context.
     */
    public static Source getLoggedInSource() {
        final SourceTokenPrincipal principal = (SourceTokenPrincipal) getPrincipal();
        if (isNull(principal) || isNull(principal.getSource())) {
            throw new AuthorizationException(NO_SECURITY_CONTEXT_ERROR);
        }
        return principal.getSource();

    }

    /**
     * Retrieve the username of the logged-in user from the security context or return 'SYSTEM' if no user is logged in.
     *
     * @return the username of the logged-in user
     */
    @SuppressWarnings("ReturnCount")
    public static String getLoggedInUsername() {
        final Principal principal = getPrincipal();
        if (principal instanceof final UserTokenPrincipal userPrincipal) {
            return userPrincipal.getUser().getEmail();
        }

        if (principal instanceof final SourceTokenPrincipal sourcePrincipal) {
            return sourcePrincipal.getSource().getName();
        }
        return "SYSTEM";
    }

    private static Principal getPrincipal() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
