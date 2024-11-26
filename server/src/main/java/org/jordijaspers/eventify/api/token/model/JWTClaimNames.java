package org.jordijaspers.eventify.api.token.model;

/**
 * Contains the names of the claims that are used in the JWT. The names are based on the RFC 7519 standard. Except the custom claims, the
 * names are the same as the ones used in the Keycloak JWT.
 */
@SuppressWarnings("JavadocStyle")
public final class JWTClaimNames {

    /* ------------------------------- CUSTOM CLAIMS ------------------------------- */

    /**
     * A custom claim that contains the first name of the user.
     */
    public static final String FIRST_NAME = "first_name";

    /**
     * A custom claim that contains the last name of the user.
     */
    public static final String LAST_NAME = "last_name";

    /**
     * A custom claim that contains the authority of the user.
     */
    public static final String AUTHORITY = "authority";

    /**
     * A custom claim that contains the permissions of the user.
     */
    public static final String PERMISSIONS = "permissions";

    /**
     * A custom claim that contains the teams of the user.
     */
    public static final String TEAMS = "teams";

    /**
     * A custom claim that contains the enabled status of the user.
     */
    public static final String ENABLED = "enabled";

    /**
     * A custom claim that contains the validated status of the user.
     */
    public static final String VALIDATED = "validated";

    /**
     * A custom claim that contains the last login date of the user.
     */
    public static final String LAST_LOGIN = "last_login";

    /**
     * A custom claim that contains the creation date of the user.
     */
    public static final String CREATED = "created";

    /* ------------------------------- RFC REGISTERED CLAIMS ------------------------------- */

    /**
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc7519#section-4.1.1">RFC 7519 "iss" (Issuer) Claim</a>
     */
    public static final String ISSUER = "iss";

    /**
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc7519#section-4.1.2">RFC 7519 "sub" (Subject) Claim</a>
     */
    public static final String SUBJECT = "sub";

    /**
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc7519#section-4.1.3">RFC 7519 "aud" (Audience) Claim</a>
     */
    public static final String AUDIENCE = "aud";

    /**
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc7519#section-4.1.4">RFC 7519 "exp" (Expiration Time) Claim</a>
     */
    public static final String EXPIRATION_TIME = "exp";

    /**
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc7519#section-4.1.5">RFC 7519 "nbf" (Not Before) Claim</a>
     */
    public static final String NOT_BEFORE = "nbf";

    /**
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc7519#section-4.1.6">RFC 7519 "iat" (Issued At) Claim</a>
     */
    public static final String ISSUED_AT = "iat";

    /**
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc7519#section-4.1.7">RFC 7519 "jti" (JWT ID) Claim</a>
     */
    public static final String JWT_ID = "jti";

    /* ------------------------------- END ------------------------------- */

    private JWTClaimNames() {
        // No-args constructor to prevent instantiation.
    }
}
