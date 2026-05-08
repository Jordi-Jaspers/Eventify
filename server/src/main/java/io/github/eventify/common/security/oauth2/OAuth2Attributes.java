package io.github.eventify.common.security.oauth2;

import lombok.experimental.UtilityClass;

/**
 * Centralized constants for OAuth2 authorization request attributes.
 * <p>
 * These keys are used to store and retrieve server-side state across the OAuth2 flow:
 * <ul>
 * <li>{@link #MODE} — captured from the initial request (login or link)</li>
 * <li>{@link #LINK_USER_ID} — current user's ID when initiating link mode</li>
 * <li>{@link #RESOLVED_USER_ID} — the user resolved during the OAuth2 callback,
 * passed from {@link CustomOAuth2UserService} to {@link OAuth2AuthenticationSuccessHandler}</li>
 * </ul>
 * <p>
 * These values flow through {@link OAuth2AttributesHolder} (ThreadLocal) which is
 * populated by {@link OAuth2AttributesFilter} from the saved authorization request.
 */
@UtilityClass
public final class OAuth2Attributes {

    /** Attribute key for the OAuth2 flow mode (either {@link #MODE_LOGIN} or {@link #MODE_LINK}). */
    public static final String MODE = "mode";

    /** Mode value for standard login / signup flow. */
    public static final String MODE_LOGIN = "login";

    /** Mode value for linking a provider to the currently authenticated user. */
    public static final String MODE_LINK = "link";

    /** Attribute key for the ID of the user initiating a link operation. */
    public static final String LINK_USER_ID = "linkUserId";

    /** Attribute key for the ID of the user resolved by {@link CustomOAuth2UserService}. */
    public static final String RESOLVED_USER_ID = "resolvedUserId";
}
