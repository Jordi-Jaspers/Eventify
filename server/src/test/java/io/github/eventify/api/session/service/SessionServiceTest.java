package io.github.eventify.api.session.service;

import io.github.eventify.api.session.model.mapper.SessionMapper;
import io.github.eventify.api.session.model.response.SessionResponse;
import io.github.eventify.api.token.model.Token;
import io.github.eventify.api.token.model.TokenType;
import io.github.eventify.api.token.repository.TokenRepository;
import io.github.eventify.api.user.model.User;
import io.github.eventify.support.UnitTest;
import io.github.jframe.exception.core.DataNotFoundException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static java.time.ZoneOffset.UTC;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@DisplayName("Unit Test - Session Service")
public class SessionServiceTest extends UnitTest {

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private SessionMapper sessionMapper;

    private SessionService sessionService;

    @BeforeEach
    public void setUp() {
        sessionService = new SessionService(tokenRepository, sessionMapper);
    }

    @Test
    @DisplayName("Should return only refresh tokens for the given user")
    public void listSessionsReturnsOnlyCurrentUserRefreshTokens() {
        // Given: A user with two refresh tokens and one validation token
        final User user = aValidUser();

        final Token refreshToken1 = aRefreshToken(1L, "token-1", user);
        final Token refreshToken2 = aRefreshToken(2L, "token-2", user);
        final Token validationToken = Token.builder()
            .id(3L)
            .value("validation-token")
            .type(TokenType.USER_VALIDATION_TOKEN)
            .expiresAt(OffsetDateTime.now(UTC).plusDays(1))
            .user(user)
            .build();

        when(tokenRepository.findByEmail(user.getEmail()))
            .thenReturn(List.of(refreshToken1, refreshToken2, validationToken));

        // When: Listing sessions for the user
        final List<Token> sessions = sessionService.listSessions(user);

        // Then: Only refresh tokens should be returned
        assertThat(sessions, hasSize(2));
        assertThat(sessions, everyItem(hasProperty("type", is(TokenType.REFRESH_TOKEN))));
    }

    @Test
    @DisplayName("Should delete session when it belongs to the authenticated user")
    public void revokeSessionDeletesOwnedSession() {
        // Given: A user with a session
        final User user = aValidUser();
        final Token session = aRefreshToken(10L, "my-token", user);

        when(tokenRepository.findById(10L)).thenReturn(Optional.of(session));

        // When: Revoking the session
        sessionService.revokeSession(user, 10L);

        // Then: The session should be deleted
        verify(tokenRepository, times(1)).delete(session);
    }

    @Test
    @DisplayName("Should throw DataNotFoundException when session does not exist")
    public void revokeSessionThrowsWhenSessionDoesNotExist() {
        // Given: A user
        final User user = aValidUser();

        // And: No session with the given ID
        when(tokenRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then: Should throw DataNotFoundException
        final DataNotFoundException exception = assertThrows(
            DataNotFoundException.class,
            () -> sessionService.revokeSession(user, 999L)
        );

        assertThat(exception, is(notNullValue()));
    }

    @Test
    @DisplayName("Should throw DataNotFoundException when session belongs to a different user (no existence leak)")
    public void revokeSessionThrowsWhenSessionBelongsToDifferentUser() {
        // Given: Two users
        final User owner = aValidUser();
        owner.setId(1L);

        final User attacker = aValidUser();
        attacker.setId(2L);
        attacker.setEmail("attacker@example.com");

        // And: A session belonging to owner
        final Token ownerSession = aRefreshToken(5L, "owner-token", owner);

        when(tokenRepository.findById(5L)).thenReturn(Optional.of(ownerSession));

        // When: Attacker tries to revoke owner's session
        // Then: Should throw DataNotFoundException (not 403, to avoid existence leak)
        final DataNotFoundException exception = assertThrows(
            DataNotFoundException.class,
            () -> sessionService.revokeSession(attacker, 5L)
        );

        assertThat(exception, is(notNullValue()));

        // And: The session should NOT be deleted
        verify(tokenRepository, never()).delete(ownerSession);
    }

    @Test
    @DisplayName("Should delete all sessions except the current one when revoking all other sessions by id")
    public void revokeAllOtherSessionsKeepsCurrentTokenById() {
        // Given: A user with three refresh tokens
        final User user = aValidUser();

        final Token token1 = aRefreshToken(1L, "v1", user);
        final Token token2 = aRefreshToken(2L, "v2", user);
        final Token token3 = aRefreshToken(3L, "v3", user);

        when(tokenRepository.findByEmail(user.getEmail()))
            .thenReturn(List.of(token1, token2, token3));

        // When: Revoking all sessions except token with id=2
        sessionService.revokeAllOtherSessions(user, 2L);

        // Then: token1 and token3 should be deleted
        verify(tokenRepository, times(1)).delete(token1);
        verify(tokenRepository, times(1)).delete(token3);

        // And: token2 should NOT be deleted
        verify(tokenRepository, never()).delete(token2);
    }

    @Test
    @DisplayName("Should do nothing when only the current session exists")
    public void revokeAllOtherSessionsDoesNothingWhenOnlyCurrentExists() {
        // Given: A user with only one refresh token (the current one)
        final User user = aValidUser();
        final Token currentToken = aRefreshToken(1L, "current-token", user);

        when(tokenRepository.findByEmail(user.getEmail()))
            .thenReturn(List.of(currentToken));

        // When: Revoking all other sessions by id
        sessionService.revokeAllOtherSessions(user, 1L);

        // Then: No tokens should be deleted
        verify(tokenRepository, never()).delete(org.mockito.ArgumentMatchers.<Token>any());
    }

    @Test
    @DisplayName("Should revoke all refresh tokens when currentTokenId is null (no cookie present)")
    public void revokeAllOtherSessionsRevokesAllWhenCurrentIdIsNull() {
        // Given: A user with two refresh tokens
        final User user = aValidUser();

        final Token token1 = aRefreshToken(1L, "v1", user);
        final Token token2 = aRefreshToken(2L, "v2", user);

        when(tokenRepository.findByEmail(user.getEmail()))
            .thenReturn(List.of(token1, token2));

        // When: Revoking all other sessions with null id (no refresh cookie was present)
        sessionService.revokeAllOtherSessions(user, null);

        // Then: All tokens should be deleted (nothing to keep)
        verify(tokenRepository, times(1)).delete(token1);
        verify(tokenRepository, times(1)).delete(token2);
    }

    @Test
    @DisplayName("Should mark the matching session as current when listing sessions by id")
    public void listSessionsForUserMarkesCurrentSessionById() {
        // Given: A user with two refresh tokens
        final User user = aValidUser();

        final Token token1 = aRefreshToken(1L, "v1", user);
        final Token token2 = aRefreshToken(2L, "v2", user);

        when(tokenRepository.findByEmail(user.getEmail()))
            .thenReturn(List.of(token1, token2));

        // And: The mapper returns a response with the id set
        when(sessionMapper.toResponse(token1)).thenReturn(aSessionResponse(1L));
        when(sessionMapper.toResponse(token2)).thenReturn(aSessionResponse(2L));

        // When: Listing sessions with token2 as current
        final List<SessionResponse> responses = sessionService.listSessionsForUser(user, 2L);

        // Then: token2's response should be marked as current
        assertThat(responses, hasSize(2));
        final SessionResponse currentSession = responses.stream()
            .filter(SessionResponse::isCurrent)
            .findFirst()
            .orElseThrow(() -> new AssertionError("No current session found"));
        assertThat(currentSession.getId(), is(equalTo(2L)));

        // And: token1's response should NOT be current
        final SessionResponse otherSession = responses.stream()
            .filter(r -> !r.isCurrent())
            .findFirst()
            .orElseThrow(() -> new AssertionError("No non-current session found"));
        assertThat(otherSession.getId(), is(equalTo(1L)));
    }

    @Test
    @DisplayName("Should mark no session as current when currentTokenId is null")
    public void listSessionsForUserMarksNoSessionCurrentWhenIdIsNull() {
        // Given: A user with one refresh token
        final User user = aValidUser();
        final Token token1 = aRefreshToken(1L, "v1", user);

        when(tokenRepository.findByEmail(user.getEmail()))
            .thenReturn(List.of(token1));
        when(sessionMapper.toResponse(token1)).thenReturn(aSessionResponse(1L));

        // When: Listing sessions with null current id
        final List<SessionResponse> responses = sessionService.listSessionsForUser(user, null);

        // Then: No session should be marked as current
        assertThat(responses, hasSize(1));
        assertThat(responses.get(0).isCurrent(), is(false));
    }

    // ========================= FACTORY METHODS =========================

    private static Token aRefreshToken(final Long id, final String value, final User user) {
        return Token.builder()
            .id(id)
            .value(value)
            .type(TokenType.REFRESH_TOKEN)
            .expiresAt(OffsetDateTime.now(UTC).plusDays(30))
            .user(user)
            .build();
    }

    private static SessionResponse aSessionResponse(final Long id) {
        final SessionResponse response = new SessionResponse();
        response.setId(id);
        return response;
    }
}
