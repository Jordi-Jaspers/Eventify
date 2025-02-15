package org.jordijaspers.eventify.common.security.principal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.time.Instant;

import org.jordijaspers.eventify.api.user.model.User;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * A Wrapper class to have a useful principal in the SecurityContext.
 */
@Data
@ToString
@AllArgsConstructor
public class UserTokenPrincipal implements OAuth2Token {

    private User user;

    private Jwt jwt;

    @Override
    public String getTokenValue() {
        return jwt.getTokenValue();
    }

    @Override
    public Instant getIssuedAt() {
        return OAuth2Token.super.getIssuedAt();
    }

    @Override
    public Instant getExpiresAt() {
        return OAuth2Token.super.getExpiresAt();
    }
}
