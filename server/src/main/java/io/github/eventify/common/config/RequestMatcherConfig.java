package io.github.eventify.common.config;

import lombok.experimental.UtilityClass;

import java.util.List;

import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import static io.github.eventify.api.Paths.*;
import static org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher.withDefaults;

/**
 * Configuration for the request matchers.
 */
@UtilityClass
public final class RequestMatcherConfig {

    private static final PathPatternRequestMatcher ACTUATOR_MATCHER = withDefaults().matcher(PUBLIC_ACTUATOR_PATH + WILDCARD_PART);
    private static final PathPatternRequestMatcher PUBLIC_MATCHER = withDefaults().matcher(PUBLIC_PATH + WILDCARD_PART);
    private static final PathPatternRequestMatcher AUTH_MATCHER = withDefaults().matcher(AUTH_PATH + WILDCARD_PART);
    private static final PathPatternRequestMatcher OPENAPI_MATCHER = withDefaults().matcher(OPENAPI_PATH + WILDCARD_PART);
    private static final PathPatternRequestMatcher ERROR_MATCHER = withDefaults().matcher(PUBLIC_ERROR_PATH);
    private static final PathPatternRequestMatcher EXTERNAL_MATCHER = withDefaults().matcher(EXTERNAL_PATH + WILDCARD_PART);
    private static final PathPatternRequestMatcher ADMIN_MATCHER = withDefaults().matcher(ADMIN_PATH + WILDCARD_PART);
    private static final PathPatternRequestMatcher ORG_MATCHER = withDefaults().matcher(ORGANIZATION_PATH + WILDCARD_PART);
    private static final PathPattern ORG_PATH_PATTERN = new PathPatternParser().parse(ORGANIZATION_PATH + WILDCARD_PART);

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

    public static List<PathPatternRequestMatcher> getAdminMatchers() {
        return List.of(ADMIN_MATCHER);
    }

    public static PathPatternRequestMatcher getOrgMatcher() {
        return ORG_MATCHER;
    }

    public static PathPattern getOrgPathPattern() {
        return ORG_PATH_PATTERN;
    }
}
