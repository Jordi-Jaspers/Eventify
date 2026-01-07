package io.github.eventify.common.security.principal;

import io.github.eventify.api.apikey.model.ApiKey;
import io.github.eventify.api.apikey.model.ApiKeyScope;
import io.github.eventify.api.user.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.security.Principal;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import static io.github.eventify.Main.SERIAL_VERSION_UID;

/**
 * Principal representing an authenticated API key.
 */
@Data
@AllArgsConstructor
public class ApiKeyPrincipal implements Principal, Serializable {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    private final Long apiKeyId;

    private final String keyPrefix;

    private final ApiKeyScope scope;

    private final Long userId;

    private final User user;

    private final Long organizationId;

    /**
     * Constructor to create an ApiKeyPrincipal from an ApiKey.
     *
     * @param apiKey the API key
     */
    public ApiKeyPrincipal(final ApiKey apiKey) {
        this.apiKeyId = apiKey.getId();
        this.keyPrefix = apiKey.getScope().getPrefix();
        this.scope = apiKey.getScope();
        this.userId = apiKey.getUser().getId();
        this.user = apiKey.getUser();
        this.organizationId = apiKey.getOrganization() != null ? apiKey.getOrganization().getId() : null;
    }

    @Override
    public String getName() {
        return keyPrefix;
    }

    /**
     * Get authorities for this API key.
     *
     * @return collection of granted authorities
     */
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("SEND_EVENTS"));
    }

    /**
     * Check if this is a user-scoped key.
     *
     * @return true if user scope
     */
    public boolean isUserKey() {
        return scope == ApiKeyScope.USER;
    }

    /**
     * Check if this is an organization-scoped key.
     *
     * @return true if organization scope
     */
    public boolean isOrganizationKey() {
        return scope == ApiKeyScope.ORGANIZATION;
    }
}
