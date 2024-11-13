package org.jordijaspers.eventify.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import lombok.RequiredArgsConstructor;
import org.jordijaspers.eventify.common.config.properties.ApplicationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

/**
 * The configuration for the OpenAPI documentation.
 */
@Configuration
@RequiredArgsConstructor
public class OpenAPIConfig {

    private final ApplicationProperties properties;

    /**
     * Creates the OpenAPI documentation for the application.
     */
    @Bean
    public OpenAPI myOpenAPI() {
        final Map<String, String> servers = Map.of(
            "local",
            "http://localhost:8080"
        );

        final Contact contact = new Contact();
        contact.setName("Jordi Jaspers");
        contact.setUrl("https://www.jordijaspers.dev");

        final Info info = new Info()
            .title("Eventify.io")
            .version(properties.getVersion())
            .contact(contact)
            .description("Eventify.io - An intuitive tool to manage and monitor your services via intelligent event creation.");

        return new OpenAPI().info(info).servers(getAvailableServers(servers));
    }

    private List<Server> getAvailableServers(final Map<String, String> serverUrls) {
        return serverUrls.entrySet().stream()
            .map(entry -> {
                final Server server = new Server();
                server.setUrl(entry.getValue());
                server.setDescription("Server URL in '" + entry.getKey() + "' environment");
                return server;
            })
            .toList();
    }
}
