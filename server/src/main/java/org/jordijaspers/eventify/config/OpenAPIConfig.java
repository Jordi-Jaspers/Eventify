package org.jordijaspers.eventify.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

/**
 * The configuration for the OpenAPI documentation.
 */
@Configuration
public class OpenAPIConfig {

    @Value("${application.version}")
    private String version;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    /**
     * Creates the OpenAPI documentation for the application.
     */
    @Bean
    public OpenAPI myOpenAPI() {
        final Map<String, String> servers = Map.of(
            "local",
            "http://localhost:8080" + contextPath
        );

        final Contact contact = new Contact();
        contact.setName("SMC DEV Team");
        contact.setUrl("https://www.vodafoneziggo.nl");

        final Info info = new Info()
            .title("CUCC (Central Use-Case Controller)")
            .version(version)
            .contact(contact)
            .description("A convenient API which collects / executes use-cases from multiple configured domains");

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
