package io.github.eventify.common.config.properties;

import lombok.Data;


/**
 * Bootstrap related properties for creating the initial global admin user.
 *
 * These properties are bound from application.yml under the "security.bootstrap" prefix.
 */
@Data
public class BootstrapProperties {

    /**
     * Email address for the global admin user.
     * Configured via: security.bootstrap.email
     */
    private String email;

    /**
     * Password for the global admin user (will be hashed before storage).
     * Configured via: security.bootstrap.password
     */
    private String password;

    /**
     * First name for the global admin user.
     * Configured via: security.bootstrap.first-name (optional, defaults to "Global")
     */
    private String firstName = "Global";

    /**
     * Last name for the global admin user.
     * Configured via: security.bootstrap.last-name (optional, defaults to "Admin")
     */
    private String lastName = "Admin";
}
