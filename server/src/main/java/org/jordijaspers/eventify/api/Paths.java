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

    /* ------------------------------- PUBLIC ENDPOINTS ------------------------------- */

    public static final String PUBLIC_ACTUATOR_PATH = BASE_PATH + "/actuator";

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

    public static final String LOCK_USER_PATH = USER_PATH + "/lock";

    public static final String UNLOCK_USER_PATH = USER_PATH + "/unlock";

    public static final String USER_DETAILS = USERS_PATH + "/details";

    public static final String USER_UPDATE_EMAIL_PATH = USER_DETAILS + "/email";

    /* ------------------------------- TEAM ENDPOINTS ------------------------------- */

    public static final String TEAMS_PATH = BASE_PATH + "/team";

    public static final String TEAM_PATH = TEAMS_PATH + ID_PART;

    public static final String TEAM_MEMBERS_PATH = TEAM_PATH + "/members";

    /* ------------------------------- EVENT ENDPOINTS ------------------------------- */

    public static final String EVENTS_PATH = BASE_PATH + "/event";

    /* ------------------------------- OPTIONS ENDPOINTS ------------------------------- */

    public static final String OPTIONS_PATH = BASE_PATH + "/options";

    /* ------------------------------- END ------------------------------- */

    private Paths() {
        // private constructor to prevent instantiation.
    }
}
