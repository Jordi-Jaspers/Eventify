package org.jordijaspers.eventify.common.constants;

/**
 * Utility class containing all possible endpoints within the application.
 */
public final class PathConstants {

    /* ------------------------------- BASIC API CONFIGURATION ------------------------------- */

    public static final String BASE_PATH = "/api";

    public static final String PUBLIC_PATH = BASE_PATH + "/public";

    public static final String AUTH_PATH = BASE_PATH + "/auth";

    public static final String OPENAPI_PATH = "/v3/api-docs";

    public static final String ERROR_PATH = "/error";

    public static final String WILDCARD_PART = "/**";

    public static final String WILDCARD = "*";

    /* ------------------------------- SEPARATE PATH PARTS ------------------------------- */

    public static final String ID_PART = "/{id}";

    /* ------------------------------- END ------------------------------- */

    private PathConstants() {
        // private constructor to prevent instantiation.
    }
}
