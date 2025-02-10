package org.jordijaspers.eventify.common.security.principal;

import org.jordijaspers.eventify.api.authentication.model.Authority;
import org.jordijaspers.eventify.api.source.model.Source;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;

/**
 * A Wrapper class to have a useful principal in the SecurityContext.
 */
public class SourceTokenPrincipal extends AbstractAuthenticationToken {

    private final String name;

    /**
     * Create a new {@link SourceTokenPrincipal} with the provided {@link Source}.
     *
     * @param source The source to wrap.
     */
    public SourceTokenPrincipal(final Source source) {
        super(AuthorityUtils.createAuthorityList(Authority.SOURCE.name()));
        this.name = source.getName();
        setDetails(source);
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return getDetails();
    }

    @Override
    public Object getPrincipal() {
        return name;
    }
}
