package io.github.eventify.common.security.principal;

import io.github.eventify.api.user.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.time.Instant;

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

    /**
     * The ID of the refresh token associated with this principal's session.
     * Populated by {@code JwtAuthenticationFilter} when a refresh-token cookie is present.
     * {@code null} when only an access token (header or cookie) was used to authenticate.
     */
    private Long refreshTokenId;

    /**
     * Create a new {@link UserTokenPrincipal} with the provided {@link User} and token value.
     *
     * @param user       The user to wrap.
     * @param tokenValue The token value to wrap.
     */
    public UserTokenPrincipal(final User user, final String tokenValue) {
        this(user, tokenValue, null);
    }

    /**
     * Create a new {@link UserTokenPrincipal} with the provided {@link User}, token value, and refresh token id.
     *
     * @param user           The user to wrap.
     * @param tokenValue     The token value to wrap.
     * @param refreshTokenId The id of the refresh token associated with this principal's session, or {@code null}.
     */
    public UserTokenPrincipal(final User user, final String tokenValue, final Long refreshTokenId) {
        this.user = user;
        this.jwt = Jwt.withTokenValue(tokenValue)
            .header("typ", "JWT")
            .claim("sub", user.getUsername())
            .build();
        this.refreshTokenId = refreshTokenId;
    }

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
