package org.jordijaspers.eventify.common.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jordijaspers.eventify.api.authentication.model.Authority;
import org.jordijaspers.eventify.api.authentication.model.Role;
import org.jordijaspers.eventify.api.authentication.repository.RoleRepository;
import org.jordijaspers.eventify.common.config.properties.ApplicationProperties;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * A configuration class that provisions data when the application context is started.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataProvisioningConfiguration {

    private final ApplicationProperties applicationProperties;

    private final RoleRepository roleRepository;

    /**
     * Provisions the data when the application context is started.
     */
    @EventListener(ApplicationStartedEvent.class)
    public void loadData() {
        log.info("Application context for has been started, provisioning data...");
        Authority.stream().forEach(authority -> {
            if (!roleRepository.existsByAuthority(authority)) {
                log.debug("[Permissions] Provisioning role '{}' - '{}'", authority, authority.getDescription());
                roleRepository.save(new Role(authority));
            } else {
                log.debug("[Permissions] Role '{}' already exists, updating details.", authority);
                roleRepository.findByAuthority(authority).ifPresent(role -> {
                    role.setDescription(authority.getDescription());
                    roleRepository.save(role);
                });
            }
        });
        log.info("Data provisioning completed successfully. Eventify ({}) is ready to use.", applicationProperties.getVersion());
    }
}
