package org.jordijaspers.eventify.common.security.principal;

import java.io.Serial;
import java.util.Collection;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.Transient;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.AbstractOAuth2TokenAuthenticationToken;

import static org.jordijaspers.eventify.Application.SERIAL_VERSION_UID;

/**
 * An {@link AbstractOAuth2TokenAuthenticationToken} implementation that uses a {@link UserTokenPrincipal} as the principal.
 */
@Transient
public class JwtUserPrincipalAuthenticationToken extends AbstractOAuth2TokenAuthenticationToken<UserTokenPrincipal> {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    /**
     * Constructs a {@code JwtUserPrincipalAuthenticationToken} using the provided parameters.
     */
    public JwtUserPrincipalAuthenticationToken(final UserTokenPrincipal principal,
                                               final Collection<? extends GrantedAuthority> authorities) {
        super(principal, authorities);
        this.setAuthenticated(true);
    }

    /**
     * {@inheritDoc}
     *
     * @return the {@link Jwt} claims associated with the token.
     */
    @Override
    public Map<String, Object> getTokenAttributes() {
        return this.getToken().getJwt().getClaims();
    }

    /**
     * {@inheritDoc}
     *
     * @return the name of the user owning this token.
     */
    @Override
    public String getName() {
        return this.getToken().getUser().getUsername();
    }
}
