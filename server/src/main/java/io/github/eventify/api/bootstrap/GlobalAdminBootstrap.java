package io.github.eventify.api.bootstrap;

import io.github.eventify.api.authentication.model.Role;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.user.repository.UserRepository;
import io.github.eventify.common.config.properties.SecurityProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Bootstrap component to create a global admin user on application startup.
 *
 * This component listens for the ApplicationStartedEvent and creates a global admin user
 * if no admin exists and all required bootstrap properties are present.
 *
 * Configuration is done via application.yml under security.bootstrap:
 * - security.bootstrap.email: Email address of the global admin (required)
 * - security.bootstrap.password: Password for the global admin (required, will be hashed)
 * - security.bootstrap.first-name: First name (optional, default: "Global")
 * - security.bootstrap.last-name: Last name (optional, default: "Admin")
 *
 * Environment variables can override these values:
 * - GLOBAL_ADMIN_EMAIL
 * - GLOBAL_ADMIN_PASSWORD
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class GlobalAdminBootstrap {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final SecurityProperties securityProperties;

    /**
     * Handles the application started event to bootstrap global admin user.
     */
    @EventListener(ApplicationStartedEvent.class)
    public void onApplicationStarted() {
        final String adminEmail = securityProperties.getBootstrap().getEmail();
        final String adminPassword = securityProperties.getBootstrap().getPassword();

        if (userRepository.existsByRole(Role.ADMIN)) {
            log.debug("Global admin bootstrap skipped: admin user already exists");
            return;
        }

        if (isValid(adminEmail) || isValid(adminPassword)) {
            final String firstName = securityProperties.getBootstrap().getFirstName();
            final String lastName = securityProperties.getBootstrap().getLastName();

            final User globalAdmin = new User();
            globalAdmin.setEmail(adminEmail);
            globalAdmin.setFirstName(firstName);
            globalAdmin.setLastName(lastName);
            globalAdmin.setPassword(passwordEncoder.encode(adminPassword));
            globalAdmin.setRole(Role.ADMIN);
            globalAdmin.setEnabled(true);
            globalAdmin.setValidated(true);

            userRepository.save(globalAdmin);
            log.info("Global admin user created successfully with email: {}", adminEmail);
        } else {
            log.debug("Global admin bootstrap skipped: required bootstrap properties not set");
        }
    }

    /**
     * Checks if a string is valid (not null and not empty).
     *
     * @param value The string to check
     * @return true if the value is not null and not empty, false otherwise
     */
    private boolean isValid(final String value) {
        return value != null && !value.isBlank();
    }
}
