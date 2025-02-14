package org.jordijaspers.eventify.common.security.principal;

import lombok.Getter;

import java.util.stream.Collectors;

import org.jordijaspers.eventify.api.authentication.model.Authority;
import org.jordijaspers.eventify.api.source.model.Source;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * A Wrapper class to have a useful principal in the SecurityContext.
 */
@Getter
public class SourceTokenPrincipal extends AbstractAuthenticationToken {

    private final Source source;

    /**
     * Create a new instance of the SourceTokenPrincipal.
     *
     * @param source The source to use as the principal
     */
    public SourceTokenPrincipal(final Source source) {
        super(
            Authority.SOURCE.getPermissions().stream()
                .map(permission -> new SimpleGrantedAuthority(permission.name()))
                .collect(Collectors.toSet())
        );
        this.source = source;
        setDetails(source);
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return source.getApiKey().getKey();
    }

    @Override
    public Object getPrincipal() {
        return source;
    }
}
