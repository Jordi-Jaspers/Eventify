package org.jordijaspers.eventify.common.util;

import org.jordijaspers.eventify.api.user.model.User;
import org.jordijaspers.eventify.common.exception.AuthorizationException;
import org.jordijaspers.eventify.common.security.principal.UserTokenPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.jordijaspers.eventify.common.exception.ApiErrorCode.USER_NOT_LOGGED_IN;

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
        return getPrincipal().getUser();
    }

    /**
     * Retrieve the username of the logged-in user from the security context or return 'SYSTEM' if no user is logged in.
     *
     * @return the username of the logged-in user
     */
    public static String getLoggedInUsername() {
        final UserTokenPrincipal principal = getPrincipalOrNull();
        return principal != null && principal.getUser() != null
            ? principal.getUser().getEmail()
            : "SYSTEM";
    }

    /**
     * Retrieve the UserTokenPrincipal from the security context.
     *
     * @return the UserTokenPrincipal
     * @throws AuthorizationException if no valid authentication is present
     */
    private static UserTokenPrincipal getPrincipal() {
        final UserTokenPrincipal principal = getPrincipalOrNull();
        if (principal == null || principal.getUser() == null) {
            throw new AuthorizationException(USER_NOT_LOGGED_IN);
        }
        return principal;
    }

    /**
     * Retrieve the UserTokenPrincipal from the security context, or return null if not available.
     *
     * @return the UserTokenPrincipal or null
     */
    private static UserTokenPrincipal getPrincipalOrNull() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        return (UserTokenPrincipal) authentication.getPrincipal();
    }
}
