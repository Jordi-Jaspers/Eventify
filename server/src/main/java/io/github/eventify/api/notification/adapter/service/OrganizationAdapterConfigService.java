package io.github.eventify.api.notification.adapter.service;

import io.github.eventify.api.notification.adapter.model.AdapterConfig;
import io.github.eventify.api.notification.adapter.model.request.CreateAdapterConfigRequest;
import io.github.eventify.api.notification.adapter.repository.AdapterConfigRepository;
import io.github.eventify.api.user.model.User;
import io.github.eventify.common.security.SecurityUtil;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service for managing organization-scoped adapter configurations. */
@Service
@RequiredArgsConstructor
public class OrganizationAdapterConfigService {

    private final AdapterConfigRepository adapterConfigRepository;

    /** Creates an adapter configuration scoped to an organization. */
    @Transactional
    public AdapterConfig createForOrganization(final Long orgId, final CreateAdapterConfigRequest request) {
        final User currentUser = SecurityUtil.getLoggedInUser();
        final AdapterConfig config = new AdapterConfig();
        config.setUser(currentUser);
        config.setOrganizationId(orgId);
        config.setAdapterType(request.getAdapterType());
        config.setLabel(request.getLabel());
        config.setConfig(request.getConfig());
        config.setEnabled(request.isEnabled());
        return adapterConfigRepository.save(config);
    }

    /** Lists adapter configurations for an organization. */
    public List<AdapterConfig> listForOrganization(final Long orgId) {
        return adapterConfigRepository.findByOrganizationId(orgId);
    }
}
