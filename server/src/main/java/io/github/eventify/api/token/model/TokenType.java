package io.github.eventify.api.token.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * The type of the token which is used to identify the purpose of the token.
 */
@Getter
@AllArgsConstructor
@Schema(description = "TokenType")
public enum TokenType {

    ACCESS_TOKEN,
    REFRESH_TOKEN,
    USER_VALIDATION_TOKEN,
    RESET_PASSWORD_TOKEN;

    public boolean isRefreshToken() {
        return this == REFRESH_TOKEN;
    }

    public boolean isAccessToken() {
        return this == ACCESS_TOKEN;
    }

}
