package io.github.eventify.api.notification.adapter.service;

import io.github.eventify.api.notification.adapter.repository.AdapterConfigRepository;
import io.github.eventify.api.organization.service.OrganizationSecurityService;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

/**
 * Security service for adapter configuration access control.
 * Bean name "adapterConfigSecurity" for use in SpEL expressions.
 */
@Service("adapterConfigSecurity")
@RequiredArgsConstructor
public class AdapterConfigSecurityService {

    private final AdapterConfigRepository adapterConfigRepository;

    private final OrganizationSecurityService orgSecurity;

    /**
     * Checks if the user owns the given personal adapter config.
     *
     * @param configId the config ID
     * @param userId   the user ID
     * @return true if the config belongs to the user
     */
    public boolean canAccessPersonalConfig(final Long configId, final Long userId) {
        return adapterConfigRepository.findByIdAndUserId(configId, userId).isPresent();
    }

    /**
     * Checks if the user can manage org adapter configs (OWNER or ADMIN).
     *
     * @param orgId  the organization ID
     * @param userId the user ID
     * @return true if user is owner or admin
     */
    public boolean canManageOrgConfigs(final Long orgId, final Long userId) {
        return orgSecurity.isOwnerOrAdmin(orgId, userId);
    }
}
