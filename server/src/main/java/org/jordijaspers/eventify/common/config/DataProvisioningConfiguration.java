package org.jordijaspers.eventify.common.config;

import lombok.RequiredArgsConstructor;
import org.jordijaspers.eventify.api.authentication.model.Authority;
import org.jordijaspers.eventify.api.authentication.model.Role;
import org.jordijaspers.eventify.api.authentication.repository.RoleRepository;
import org.jordijaspers.eventify.common.config.properties.ApplicationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * A configuration class that provisions data when the application context is started.
 */
@Component
@RequiredArgsConstructor
public class DataProvisioningConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataProvisioningConfiguration.class);

    private final ApplicationProperties applicationProperties;

    private final RoleRepository roleRepository;

    /**
     * Provisions the data when the application context is started.
     */
    @EventListener(ApplicationStartedEvent.class)
    public void loadData() {
        LOGGER.info("Application context for has been started, provisioning data...");
        Authority.stream().forEach(authority -> {
            if (!roleRepository.existsByAuthority(authority)) {
                LOGGER.debug("[Permissions] Provisioning role '{}' - '{}'", authority, authority.getDescription());
                roleRepository.save(new Role(authority));
            } else {
                LOGGER.debug("[Permissions] Role '{}' already exists, updating details.", authority);
                roleRepository.findByAuthority(authority).ifPresent(role -> {
                    role.setDescription(authority.getDescription());
                    roleRepository.save(role);
                });
            }
        });
        LOGGER.info("Data provisioning completed successfully. Eventify ({}) is ready to use.", applicationProperties.getVersion());
    }
}
