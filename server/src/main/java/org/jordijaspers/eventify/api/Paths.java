package org.jordijaspers.eventify.api;

/**
 * All the Paths used in the exposed REST API.
 */
@SuppressWarnings(
    {
        "PMD.ClassNamingConventions",
        "MultipleStringLiterals"
    }
)
public final class Paths {

    /* ------------------------------- BASIC API CONFIGURATION ------------------------------- */

    /**
     * The base part of all rest API paths.
     */
    public static final String BASE_PATH = "/rest/v1";

    /**
     * defines publicly accessible paths.
     */
    public static final String PUBLIC_PATH = BASE_PATH + "/public";

    /**
     * The path to the error page.
     */
    public static final String ERROR_PATH = "/error";

    /**
     * Actuator url.
     */
    public static final String ACTUATOR_PATH = "/actuator";

    /**
     * A sub path to indicate a wildcard.
     */
    public static final String WILDCARD_PART = "/**";

    /**
     * Another wildcard.
     */
    public static final String WILDCARD = "*";

    /* ------------------------------- SEPARATE PATH NAMES ------------------------------- */

    /**
     * Sub path used for search a specific entity.
     */
    private static final String ID_PART = "/{id}";

    /**
     * Partial path prefix for private calls.
     */
    private static final String PRIVATE = "/private";

    /* ------------------------------- PUBLIC ENDPOINTS ------------------------------- */

    /**
     * The path for the exposed HRX configuration.
     */
    public static final String PUBLIC_METRICS_PATH = PUBLIC_PATH + "/metrics";

    /**
     * The path for the health check of the application.
     */
    public static final String PUBLIC_HEALTH_PATH = ACTUATOR_PATH + "/health";

    /* ------------------------------ END ------------------------------- */

    private Paths() {
        // utility constructor.
    }
}
