package io.github.eventify.common.exception;

import lombok.Getter;

import java.io.Serial;

import org.springframework.security.core.AuthenticationException;

import static io.github.eventify.Main.SERIAL_VERSION_UID;

/**
 * Exception thrown when an OAuth2 account linking operation fails due to a business rule violation.
 * Extends AuthenticationException so it can be propagated through Spring Security's OAuth2 flow.
 */
@Getter
public class LinkOAuth2Exception extends AuthenticationException {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    private final ApiErrorCode errorCode;

    /**
     * Creates a new LinkOAuth2Exception with the given error code.
     *
     * @param errorCode the API error code describing the failure reason
     */
    public LinkOAuth2Exception(final ApiErrorCode errorCode) {
        super(errorCode.getReason());
        this.errorCode = errorCode;
    }
}
