package io.github.eventify.common.security.oauth2;

import io.github.eventify.common.exception.ApiErrorCode;
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
    private static final String LINKED_QUERY_PARAM = "linked";
    private static final String SECURITY_PATH = "/profile/security";
    private static final String ERROR_SUFFIX = "_error";

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

    /**
     * Builds a redirect URL for a successful OAuth2 provider link.
     * Redirects to /profile/security?linked={provider}.
     *
     * @param provider the provider name (e.g. "google", "github")
     * @return the redirect URL
     */
    public String buildLinkSuccessRedirectUrl(final String provider) {
        return UriComponentsBuilder.fromUriString(applicationProperties.getUrl())
            .path(SECURITY_PATH)
            .queryParam(LINKED_QUERY_PARAM, provider)
            .build()
            .toUriString();
    }

    /**
     * Builds a redirect URL for a failed OAuth2 provider link.
     * Redirects to /profile/security?error={urlSafeCode}.
     * <p>
     * Mapping: enum name lowercased with "_error" suffix stripped.
     * Examples:
     * <ul>
     * <li>EMAIL_IN_USE_ERROR → email_in_use</li>
     * <li>PROVIDER_LINKED_ELSEWHERE_ERROR → provider_linked_elsewhere</li>
     * <li>PROVIDER_ALREADY_LINKED_ERROR → already_linked</li>
     * </ul>
     *
     * @param errorCode the API error code
     * @return the redirect URL
     */
    public String buildLinkErrorRedirectUrl(final ApiErrorCode errorCode) {
        final String urlSafeCode = toUrlSafeCode(errorCode);
        return UriComponentsBuilder.fromUriString(applicationProperties.getUrl())
            .path(SECURITY_PATH)
            .queryParam(ERROR_QUERY_PARAM, urlSafeCode)
            .build()
            .toUriString();
    }

    private String toUrlSafeCode(final ApiErrorCode errorCode) {
        return switch (errorCode) {
            case EMAIL_IN_USE_ERROR -> "email_in_use";
            case PROVIDER_LINKED_ELSEWHERE_ERROR -> "provider_linked_elsewhere";
            case PROVIDER_ALREADY_LINKED_ERROR -> "already_linked";
            default -> {
                final String lower = errorCode.name().toLowerCase();
                yield lower.endsWith(ERROR_SUFFIX) ? lower.substring(0, lower.length() - ERROR_SUFFIX.length()) : lower;
            }
        };
    }
}
