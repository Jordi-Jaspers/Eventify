package io.github.eventify.api.bootstrap.controller;

import io.github.eventify.api.bootstrap.model.response.DevCredentialsResponse;
import io.github.eventify.common.config.properties.SecurityProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static io.github.eventify.api.Paths.DEV_CREDENTIALS_PATH;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Controller for development-only endpoints. Only active in the "dev" profile.
 */
@RestController
@Profile("dev")
@Tag(
    name = "Development",
    description = "Development-only endpoints (not available in production)"
)
@RequiredArgsConstructor
public class DevCredentialsController {

    private final SecurityProperties securityProperties;

    /**
     * Get development credentials for testing.
     *
     * @return the dev credentials response
     */
    @Operation(summary = "Get dev admin credentials (dev profile only)")
    @GetMapping(
        path = DEV_CREDENTIALS_PATH,
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<DevCredentialsResponse> getDevCredentials() {
        final DevCredentialsResponse response = new DevCredentialsResponse()
            .setEmail(securityProperties.getBootstrap().getEmail())
            .setPassword(securityProperties.getBootstrap().getPassword());
        return ResponseEntity.status(OK).body(response);
    }
}
