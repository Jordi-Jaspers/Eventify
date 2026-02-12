package io.github.eventify.common.config;

import java.util.List;

import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

import static io.github.eventify.api.Paths.*;
import static org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher.withDefaults;

/**
 * Configuration for the request matchers.
 */
public final class RequestMatcherConfig {

    private static final PathPatternRequestMatcher ACTUATOR_MATCHER = withDefaults().matcher(PUBLIC_ACTUATOR_PATH + WILDCARD_PART);
    private static final PathPatternRequestMatcher PUBLIC_MATCHER = withDefaults().matcher(PUBLIC_PATH + WILDCARD_PART);
    private static final PathPatternRequestMatcher AUTH_MATCHER = withDefaults().matcher(AUTH_PATH + WILDCARD_PART);
    private static final PathPatternRequestMatcher OPENAPI_MATCHER = withDefaults().matcher(OPENAPI_PATH + WILDCARD_PART);
    private static final PathPatternRequestMatcher ERROR_MATCHER = withDefaults().matcher(PUBLIC_ERROR_PATH);
    private static final PathPatternRequestMatcher EXTERNAL_MATCHER = withDefaults().matcher(EXTERNAL_PATH + WILDCARD_PART);

    private RequestMatcherConfig() {
        // Prevent instantiation
    }

    public static List<PathPatternRequestMatcher> getExternalMatchers() {
        return List.of(EXTERNAL_MATCHER);
    }

    public static List<PathPatternRequestMatcher> getPublicMatchers() {
        return List.of(
            ACTUATOR_MATCHER,
            PUBLIC_MATCHER,
            AUTH_MATCHER,
            OPENAPI_MATCHER,
            ERROR_MATCHER
        );
    }
}
