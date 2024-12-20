package org.jordijaspers.eventify.common.util;

import org.jordijaspers.eventify.api.user.model.User;
import org.jordijaspers.eventify.common.exception.AuthorizationException;
import org.jordijaspers.eventify.common.security.principal.UserTokenPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;

import static java.util.Objects.isNull;
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
        final UserTokenPrincipal principal = (UserTokenPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (isNull(principal) || isNull(principal.getUser())) {
            throw new AuthorizationException(USER_NOT_LOGGED_IN);
        }
        return principal.getUser();
    }
}
