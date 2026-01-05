package io.github.eventify.api;

/**
 * Utility class containing all possible endpoints within the application.
 */
public final class Paths {

    /* ------------------------------- BASIC API CONFIGURATION ------------------------------- */

    public static final String BASE_PATH = "/v1";

    public static final String PUBLIC_PATH = BASE_PATH + "/public";

    public static final String AUTH_PATH = BASE_PATH + "/auth";

    public static final String ADMIN_PATH = BASE_PATH + "/admin";

    public static final String OPENAPI_PATH = "/v3/api-docs";

    /* ------------------------------- SEPARATE PATH PARTS ------------------------------- */

    public static final String WILDCARD_PART = "/**";

    public static final String WILDCARD = "*";

    public static final String ID_PART = "/{id}";

    public static final String SEARCH_PART = "/search";

    public static final String LOCK_PART = "/lock";

    public static final String UNLOCK_PART = "/unlock";

    public static final String LOGIN_PART = "/login";

    public static final String ERROR_PART = "/error";

    public static final String ORGANIZATIONS_PART = "/organization";

    public static final String USER_PART = "/user";

    /* ------------------------------- PUBLIC ENDPOINTS ------------------------------- */

    public static final String PUBLIC_SWAGGER_PATH = PUBLIC_PATH + "/docs";

    public static final String PUBLIC_ACTUATOR_PATH = PUBLIC_PATH + "/actuator";

    public static final String PUBLIC_METRICS_PATH = PUBLIC_ACTUATOR_PATH + "/metrics";

    public static final String PUBLIC_HEALTH_PATH = PUBLIC_ACTUATOR_PATH + "/health";

    public static final String PUBLIC_RESET_PASSWORD_PATH = PUBLIC_PATH + "/reset-password";

    public static final String PUBLIC_REQUEST_PASSWORD_RESET_PATH = PUBLIC_RESET_PASSWORD_PATH + "/request";

    public static final String PUBLIC_VALIDATE_EMAIL_PATH = PUBLIC_PATH + "/email/validate";

    public static final String PUBLIC_ERROR_PATH = PUBLIC_PATH + ERROR_PART;

    /* ------------------------------- AUTH ENDPOINTS ------------------------------- */

    public static final String REGISTER_PATH = AUTH_PATH + "/register";

    public static final String LOGIN_PATH = AUTH_PATH + LOGIN_PART;

    public static final String TOKEN_PATH = AUTH_PATH + "/token";

    public static final String LOGOUT_PATH = AUTH_PATH + "/logout";

    public static final String VERIFICATION_PATH = AUTH_PATH + "/verify";

    public static final String RESEND_EMAIL_VERIFICATION_PATH = VERIFICATION_PATH + "/resend";

    /* ------------------------------- OAUTH ENDPOINTS ------------------------------- */

    public static final String OAUTH2_PART = "/oauth2";

    public static final String OAUTH2_PATH = BASE_PATH + OAUTH2_PART;

    public static final String OAUTH2_AUTHORIZATION_PATH = OAUTH2_PATH + "/authorization";

    public static final String OAUTH2_AUTHORIZATION_PROVIDER_PATH = OAUTH2_AUTHORIZATION_PATH + "/{provider}";

    public static final String OAUTH2_CALLBACK_PATH = "/login/oauth2/code/{provider}";

    public static final String OAUTH2_FRONTEND_REDIRECT_PATH = OAUTH2_PART + "/redirect";

    /* ------------------------------- PASSWORD MANAGEMENT ENDPOINTS ------------------------------- */

    public static final String UPDATE_PASSWORD_PATH = BASE_PATH + "/password";

    /* ------------------------------- USER ENDPOINTS ------------------------------- */

    public static final String USERS_PATH = BASE_PATH + USER_PART;

    public static final String USER_PATH = USERS_PATH + ID_PART;

    public static final String LOCK_USER_PATH = USER_PATH + LOCK_PART;

    public static final String UNLOCK_USER_PATH = USER_PATH + UNLOCK_PART;

    public static final String USER_DETAILS = USERS_PATH + "/details";

    public static final String USER_ROLE_PATH = USER_PATH + "/role";

    public static final String INTROSPECTION_PATH = USERS_PATH + "/introspect";

    public static final String USER_INVITATION_PATH = USERS_PATH + "/invite";

    /* ------------------------------- GLOBAL ADMIN ENDPOINTS ------------------------------- */

    public static final String ADMIN_ORGANIZATIONS_PATH = ADMIN_PATH + ORGANIZATIONS_PART;

    public static final String ADMIN_ORGANIZATIONS_SEARCH_PATH = ADMIN_ORGANIZATIONS_PATH + SEARCH_PART;

    public static final String ADMIN_USERS_SEARCH_PATH = ADMIN_PATH + USER_PART + SEARCH_PART;

    public static final String ADMIN_STATS_PATH = ADMIN_PATH + "/stats";

    public static final String ADMIN_ORGANIZATION_ASSIGN_OWNER_PATH = ADMIN_PATH + ORGANIZATIONS_PART + "/{orgId}/owner";

    /* ------------------------------- ORGANIZATION MEMBERSHIP ENDPOINTS ------------------------------- */

    public static final String ORGANIZATIONS_PATH = BASE_PATH + ORGANIZATIONS_PART;

    public static final String ORGANIZATION_PATH = ORGANIZATIONS_PATH + "/{orgId}";

    public static final String ORGANIZATION_MEMBERS_PATH = ORGANIZATION_PATH + "/members";

    public static final String ORGANIZATION_MEMBER_PATH = ORGANIZATION_MEMBERS_PATH + "/{userId}";

    public static final String ORGANIZATION_NEW_MEMBERS_SEARCH_PATH = ORGANIZATION_MEMBERS_PATH + "/new" + SEARCH_PART;

    public static final String ORGANIZATION_MEMBERS_SEARCH_PATH = ORGANIZATION_MEMBERS_PATH + SEARCH_PART;

    public static final String ORGANIZATION_TRANSFER_OWNERSHIP_PATH = ORGANIZATION_PATH + "/transfer-ownership";

    public static final String USER_ORGANIZATIONS_PATH = USERS_PATH + ORGANIZATIONS_PART;

    /* ------------------------------- OPTIONS ENDPOINTS ------------------------------- */

    public static final String OPTIONS_PATH = BASE_PATH + "/options";

    /* ------------------------------- DEV ENDPOINTS ------------------------------- */

    public static final String DEV_CREDENTIALS_PATH = PUBLIC_PATH + "/dev/credentials";

    /* ------------------------------- END ------------------------------- */

    private Paths() {
        // private constructor to prevent instantiation.
    }
}
