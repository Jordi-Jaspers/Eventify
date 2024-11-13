package org.jordijaspers.eventify.common.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Class to configure the embedded tomcat instance for Spring boot with.
 */
@Data
@Configuration
@ConfigurationProperties(
    prefix = "application",
    ignoreUnknownFields = false
)
public class ApplicationProperties {

    /**
     * The current version of the application.
     */
    private String version;

    /**
     * The default url of the application.
     */
    private String url;

}
