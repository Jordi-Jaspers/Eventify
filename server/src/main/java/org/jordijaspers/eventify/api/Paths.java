package org.jordijaspers.eventify.api;

/**
 * Utility class containing all possible endpoints within the application.
 */
public final class Paths {

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

    public static final String LOCK_PART = "/lock";

    public static final String UNLOCK_PART = "/unlock";

    /* ------------------------------- EXTERNAL ENDPOINTS (API-KEY) ------------------------------- */

    public static final String EXTERNAL_BASE_PATH = BASE_PATH + "/external";

    public static final String EVENTS_PATH = EXTERNAL_BASE_PATH + "/event";

    /* ------------------------------- PUBLIC ENDPOINTS ------------------------------- */

    public static final String PUBLIC_ACTUATOR_PATH = PUBLIC_PATH + "/actuator";

    public static final String PUBLIC_HEALTH_PATH = PUBLIC_ACTUATOR_PATH + "/health";

    public static final String PUBLIC_RESET_PASSWORD_PATH = PUBLIC_PATH + "/reset_password";

    public static final String PUBLIC_REQUEST_PASSWORD_RESET_PATH = PUBLIC_RESET_PASSWORD_PATH + "/request";

    public static final String PUBLIC_VALIDATE_EMAIL_PATH = PUBLIC_PATH + "/email/validate";

    /* ------------------------------- PASSWORD MANAGEMENT ENDPOINTS ------------------------------- */

    public static final String UPDATE_PASSWORD_PATH = BASE_PATH + "/password";

    /* ------------------------------- AUTH ENDPOINTS ------------------------------- */

    public static final String REGISTER_PATH = AUTH_PATH + "/register";

    public static final String LOGIN_PATH = AUTH_PATH + "/login";

    public static final String TOKEN_PATH = AUTH_PATH + "/token";

    public static final String LOGOUT_PATH = AUTH_PATH + "/logout";

    public static final String VERIFICATION_PATH = AUTH_PATH + "/verify";

    public static final String RESEND_EMAIL_VERIFICATION_PATH = VERIFICATION_PATH + "/resend";

    /* ------------------------------- USER ENDPOINTS ------------------------------- */

    public static final String USERS_PATH = BASE_PATH + "/user";

    public static final String USER_PATH = USERS_PATH + ID_PART;

    public static final String LOCK_USER_PATH = USER_PATH + LOCK_PART;

    public static final String UNLOCK_USER_PATH = USER_PATH + UNLOCK_PART;

    public static final String USER_DETAILS = USERS_PATH + "/details";

    public static final String USER_UPDATE_EMAIL_PATH = USER_DETAILS + "/email";

    /* ------------------------------- TEAM ENDPOINTS ------------------------------- */

    public static final String TEAMS_PATH = BASE_PATH + "/team";

    public static final String TEAM_PATH = TEAMS_PATH + ID_PART;

    public static final String TEAM_MEMBERS_PATH = TEAM_PATH + "/members";

    /* ------------------------------- DASHBOARD ENDPOINTS ------------------------------- */

    public static final String DASHBOARDS_PATH = BASE_PATH + "/dashboard";

    public static final String DASHBOARD_PATH = DASHBOARDS_PATH + ID_PART;

    public static final String DASHBOARD_CONFIGURATION_PATH = DASHBOARD_PATH + "/configuration";

    /* ------------------------------- MONITORING ENDPOINTS ------------------------------- */

    public static final String MONITORING_PATH = DASHBOARD_PATH + "/monitoring";

    public static final String MONITORING_STREAM_PATH = MONITORING_PATH + "/stream";

    /* ------------------------------- CHECKS ENDPOINTS ------------------------------- */

    public static final String CHECK_PATH = BASE_PATH + "/check";

    /* ------------------------------- SOURCE ENDPOINTS ------------------------------- */

    public static final String SOURCES_PATH = BASE_PATH + "/source";

    public static final String SOURCE_PATH = SOURCES_PATH + ID_PART;

    public static final String API_KEY_PATH = SOURCE_PATH + "/key";

    public static final String REGENERATE_API_KEY_PATH = API_KEY_PATH + "/regenerate";

    public static final String LOCK_API_KEY_PATH = API_KEY_PATH + LOCK_PART;

    public static final String UNLOCK_API_KEY_PATH = API_KEY_PATH + UNLOCK_PART;

    /* ------------------------------- OPTIONS ENDPOINTS ------------------------------- */

    public static final String OPTIONS_PATH = BASE_PATH + "/options";

    /* ------------------------------- END ------------------------------- */

    private Paths() {
        // private constructor to prevent instantiation.
    }
}
