package io.github.eventify.common.security.principal;

import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.Collection;

import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.Transient;

import static io.github.eventify.Main.SERIAL_VERSION_UID;

/**
 * Authentication token for API key authentication.
 */
@Transient
@EqualsAndHashCode(callSuper = true)
public class ApiKeyAuthenticationToken extends AbstractAuthenticationToken {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    private final ApiKeyPrincipal principal;

    /**
     * Constructs an {@code ApiKeyAuthenticationToken} using the provided parameters.
     *
     * @param principal   the principal
     * @param authorities the authorities
     */
    public ApiKeyAuthenticationToken(final ApiKeyPrincipal principal,
                                     final Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public ApiKeyPrincipal getPrincipal() {
        return principal;
    }

    @NonNull
    @Override
    public String getName() {
        return principal.getName();
    }

}
