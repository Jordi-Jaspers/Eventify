package io.github.eventify.api.session.controller;

import io.github.eventify.api.session.model.response.SessionResponse;
import io.github.eventify.api.session.service.SessionService;
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

import static io.github.eventify.api.Paths.USER_SESSIONS_PATH;
import static io.github.eventify.api.Paths.USER_SESSION_PATH;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Controller for managing user sessions.
 */
@RestController
@RequiredArgsConstructor
@Tag(
    name = "Sessions",
    description = "User session management endpoints"
)
public class SessionController {

    private final SessionService sessionService;

    @GetMapping(
        path = USER_SESSIONS_PATH,
        produces = APPLICATION_JSON_VALUE
    )
    @Operation(
        summary = "List sessions",
        description = "Returns all active sessions for the authenticated user"
    )
    public ResponseEntity<List<SessionResponse>> listSessions(
        @AuthenticationPrincipal final UserTokenPrincipal principal) {
        return ResponseEntity.status(OK).body(sessionService.listSessionsForUser(principal.getUser(), principal.getRefreshTokenId()));
    }

    @DeleteMapping(path = USER_SESSION_PATH)
    @Operation(
        summary = "Revoke session",
        description = "Revokes a specific session by ID"
    )
    public ResponseEntity<Void> revokeSession(
        @AuthenticationPrincipal final UserTokenPrincipal principal,
        @PathVariable final Long id) {
        sessionService.revokeSession(principal.getUser(), id);
        return ResponseEntity.status(NO_CONTENT).build();
    }

    @DeleteMapping(path = USER_SESSIONS_PATH)
    @Operation(
        summary = "Revoke all other sessions",
        description = "Revokes all sessions except the current one"
    )
    public ResponseEntity<Void> revokeAllOtherSessions(
        @AuthenticationPrincipal final UserTokenPrincipal principal) {
        sessionService.revokeAllOtherSessions(principal.getUser(), principal.getRefreshTokenId());
        return ResponseEntity.status(NO_CONTENT).build();
    }
}
