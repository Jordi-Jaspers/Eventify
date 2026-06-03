package io.github.eventify.api.notification.adapter.service;

import io.github.eventify.api.notification.adapter.model.AdapterConfig;
import io.github.eventify.api.notification.adapter.model.request.CreateAdapterConfigRequest;
import io.github.eventify.api.notification.adapter.model.request.UpdateAdapterConfigRequest;
import io.github.eventify.api.notification.adapter.repository.AdapterConfigRepository;
import io.github.eventify.api.user.model.User;
import io.github.eventify.common.security.SecurityUtil;
import io.github.jframe.exception.core.DataNotFoundException;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static io.github.eventify.common.exception.ApiErrorCode.ADAPTER_CONFIG_NOT_FOUND;

/** Service for managing personal (user-scoped) adapter configurations. */
@Service
@RequiredArgsConstructor
public class UserAdapterConfigService {

    private final AdapterConfigRepository adapterConfigRepository;

    /** Creates a personal adapter configuration for the current user. */
    @Transactional
    public AdapterConfig createPersonal(final CreateAdapterConfigRequest request) {
        final User currentUser = SecurityUtil.getLoggedInUser();
        final AdapterConfig config = new AdapterConfig();
        config.setUser(currentUser);
        config.setAdapterType(request.getAdapterType());
        config.setLabel(request.getLabel());
        config.setConfig(request.getConfig());
        config.setEnabled(request.isEnabled());
        return adapterConfigRepository.save(config);
    }

    /** Lists personal adapter configurations for the current user. */
    public List<AdapterConfig> listPersonal() {
        final User currentUser = SecurityUtil.getLoggedInUser();
        return adapterConfigRepository.findByUserIdAndOrganizationIdIsNull(currentUser.getId());
    }

    /** Returns a personal adapter config by ID. */
    public AdapterConfig get(final Long id) {
        return adapterConfigRepository.findById(id)
            .orElseThrow(() -> new DataNotFoundException(ADAPTER_CONFIG_NOT_FOUND));
    }

    /** Updates a personal adapter configuration. */
    @Transactional
    public AdapterConfig update(final Long id, final UpdateAdapterConfigRequest request) {
        final AdapterConfig config = adapterConfigRepository.findById(id)
            .orElseThrow(() -> new DataNotFoundException(ADAPTER_CONFIG_NOT_FOUND));
        config.setLabel(request.getLabel());
        config.setConfig(request.getConfig());
        config.setEnabled(request.isEnabled());
        return adapterConfigRepository.save(config);
    }

    /** Deletes a personal adapter configuration. */
    @Transactional
    public void delete(final Long id) {
        final AdapterConfig config = adapterConfigRepository.findById(id)
            .orElseThrow(() -> new DataNotFoundException(ADAPTER_CONFIG_NOT_FOUND));
        adapterConfigRepository.delete(config);
    }
}
