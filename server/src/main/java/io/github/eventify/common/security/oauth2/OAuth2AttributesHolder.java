package io.github.eventify.common.security.oauth2;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * ThreadLocal holder for OAuth2 authorization request attributes.
 * <p>
 * Populated by {@link OAuth2AttributesFilter} at the start of the OAuth2 callback request
 * and cleared in a finally block after the filter chain completes.
 * <p>
 * Allows downstream components (e.g. {@link CustomOAuth2UserService},
 * {@link OAuth2AuthenticationSuccessHandler}) to read server-side attributes such as
 * {@code mode} and {@code linkUserId} without those values being exposed in the redirect URI.
 */
public final class OAuth2AttributesHolder {

    private static final ThreadLocal<Map<String, Object>> HOLDER = new ThreadLocal<>();

    private OAuth2AttributesHolder() {
        // utility class
    }

    /**
     * Stores the given attributes map in the current thread's context.
     *
     * @param attributes the attributes to store; must not be null
     */
    public static void setAttributes(final Map<String, Object> attributes) {
        HOLDER.set(Collections.unmodifiableMap(attributes));
    }

    /**
     * Returns the attributes map stored for the current thread, or an empty map if none.
     *
     * @return the attributes map; never null
     */
    public static Map<String, Object> getAttributes() {
        final Map<String, Object> attrs = HOLDER.get();
        return attrs != null ? attrs : Collections.emptyMap();
    }

    /**
     * Returns the value of the named attribute cast to {@code T}, or {@code null} if absent.
     *
     * @param name the attribute name
     * @param <T>  the expected type
     * @return the attribute value, or {@code null}
     */
    @SuppressWarnings("unchecked")
    public static <T> T getAttribute(final String name) {
        return (T) getAttributes().get(name);
    }

    /**
     * Sets a single attribute in the current thread's context.
     * If no map is currently stored, a new one is created.
     *
     * @param name  the attribute name; must not be null
     * @param value the attribute value
     */
    public static void setAttribute(final String name, final Object value) {
        final Map<String, Object> current = HOLDER.get();
        final Map<String, Object> mutable = current != null ? new HashMap<>(current) : new HashMap<>();
        mutable.put(name, value);
        HOLDER.set(Collections.unmodifiableMap(mutable));
    }

    /**
     * Clears the ThreadLocal to prevent memory leaks.
     * Must be called in a {@code finally} block after the request is processed.
     */
    public static void clear() {
        HOLDER.remove();
    }
}
