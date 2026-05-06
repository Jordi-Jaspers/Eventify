package io.github.eventify.api.user.controller;

import io.github.eventify.api.user.model.response.ProviderResponse;
import io.github.eventify.api.user.service.UserAuthProviderService;
import io.github.eventify.common.security.principal.UserTokenPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import static io.github.eventify.api.Paths.USER_PROVIDERS_PATH;
import static io.github.eventify.api.Paths.USER_PROVIDER_PATH;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Controller for managing connected authentication providers.
 */
@RestController
@RequiredArgsConstructor
@Tag(
    name = "Connected Accounts",
    description = "Manage linked OAuth2 and local authentication providers"
)
public class UserProviderController {

    private final UserAuthProviderService userAuthProviderService;

    @GetMapping(
        path = USER_PROVIDERS_PATH,
        produces = APPLICATION_JSON_VALUE
    )
    @Operation(
        summary = "List connected providers",
        description = "Returns all authentication providers with their connection status for the authenticated user"
    )
    public ResponseEntity<List<ProviderResponse>> listProviders(
        @AuthenticationPrincipal final UserTokenPrincipal principal) {
        return ResponseEntity.status(OK).body(userAuthProviderService.listProvidersForUser(principal.getUser()));
    }

    @DeleteMapping(path = USER_PROVIDER_PATH)
    @Operation(
        summary = "Unlink provider",
        description = "Unlinks a specific authentication provider from the user account"
    )
    public ResponseEntity<Void> unlinkProvider(
        @AuthenticationPrincipal final UserTokenPrincipal principal,
        @PathVariable final Long id) {
        userAuthProviderService.unlinkProvider(principal.getUser(), id);
        return ResponseEntity.status(NO_CONTENT).build();
    }
}
