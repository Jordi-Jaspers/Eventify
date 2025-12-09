package io.github.eventify.common.security.oauth2;

import io.github.jframe.autoconfigure.properties.ApplicationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.Nullable;

import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import static io.github.eventify.api.Paths.OAUTH2_FRONTEND_REDIRECT_PATH;
import static java.util.Objects.nonNull;


/**
 * Helper class for OAuth2 authentication redirects. Provides centralized error handling for OAuth2 authentication failures.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2RedirectHelper {

    private static final String ERROR_QUERY_PARAM = "error";

    private final ApplicationProperties applicationProperties;

    /**
     * Redirects to the frontend authentication callback with optional query parameters. If "error" is provided, it will be added as a query
     * parameter. the message is url encoded.
     *
     * @param errorMessage An optional error message to include as a query parameter.
     */
    public String buildRedirectUrl(@Nullable final String errorMessage) {
        final UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(applicationProperties.getUrl())
            .path(OAUTH2_FRONTEND_REDIRECT_PATH);

        if (nonNull(errorMessage)) {
            log.error("OAuth2 Authentication failed: {}", errorMessage);
            builder.queryParam(ERROR_QUERY_PARAM, errorMessage);
        }
        return builder.build().toUriString();
    }

    /**
     * Redirects to the frontend authentication callback without any query parameters. this results in a successful login redirect.
     */
    public String buildRedirectUrl() {
        return buildRedirectUrl(null);
    }
}
