package org.jordijaspers.eventify.common.config;

import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.List;

import static org.jordijaspers.eventify.api.Paths.*;

/**
 * Configuration for the request matchers.
 */
public final class RequestMatcherConfig {

    private static final AntPathRequestMatcher ACTUATOR_MATCHER = new AntPathRequestMatcher(PUBLIC_ACTUATOR_PATH + WILDCARD_PART);
    private static final AntPathRequestMatcher PUBLIC_MATCHER = new AntPathRequestMatcher(PUBLIC_PATH + WILDCARD_PART);
    private static final AntPathRequestMatcher AUTH_MATCHER = new AntPathRequestMatcher(AUTH_PATH + WILDCARD_PART);
    private static final AntPathRequestMatcher OPENAPI_MATCHER = new AntPathRequestMatcher(OPENAPI_PATH + WILDCARD_PART);
    private static final AntPathRequestMatcher ERROR_MATCHER = new AntPathRequestMatcher(ERROR_PATH);

    private RequestMatcherConfig() {
        // Prevent instantiation
    }

    /**
     * Retrieve the list of public endpoints.
     */
    public static List<AntPathRequestMatcher> getPublicMatchers() {
        return List.of(
            ACTUATOR_MATCHER,
            PUBLIC_MATCHER,
            AUTH_MATCHER,
            OPENAPI_MATCHER,
            ERROR_MATCHER
        );
    }
}
