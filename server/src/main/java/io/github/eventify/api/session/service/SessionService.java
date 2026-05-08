package io.github.eventify.api.session.service;

import io.github.eventify.api.session.model.mapper.SessionMapper;
import io.github.eventify.api.session.model.response.SessionResponse;
import io.github.eventify.api.token.model.Token;
import io.github.eventify.api.token.model.TokenType;
import io.github.eventify.api.token.repository.TokenRepository;
import io.github.eventify.api.user.model.User;
import io.github.jframe.exception.core.DataNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static io.github.eventify.common.exception.ApiErrorCode.SESSION_NOT_FOUND_ERROR;

/**
 * Service for managing user sessions (refresh tokens).
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SessionService {

    private static final Comparator<Token> SESSION_ORDER = Comparator
        .comparing(Token::getLastActiveAt, Comparator.nullsLast(Comparator.reverseOrder()))
        .thenComparing(Comparator.comparing(Token::getId).reversed());

    private final TokenRepository tokenRepository;

    private final SessionMapper sessionMapper;

    /**
     * Returns all active refresh tokens (sessions) for the given user.
     *
     * @param user the user whose sessions to list
     * @return list of refresh tokens sorted by lastActiveAt DESC, then id DESC
     */
    @Transactional(readOnly = true)
    public List<Token> listSessions(final User user) {
        return tokenRepository.findByEmail(user.getEmail())
            .stream()
            .filter(token -> TokenType.REFRESH_TOKEN.equals(token.getType()))
            .sorted(SESSION_ORDER)
            .toList();
    }

    /**
     * Returns all sessions for the user as response DTOs, with the {@code current} flag set on the session matching the supplied refresh
     * token id.
     *
     * @param user                  the user whose sessions to list
     * @param currentRefreshTokenId the id of the refresh token for the current session, or {@code null} if unknown
     * @return list of session responses with current flag populated
     */
    @Transactional(readOnly = true)
    public List<SessionResponse> listSessionsForUser(final User user, final Long currentRefreshTokenId) {
        return listSessions(user).stream()
            .map(token -> toResponseWithCurrentFlag(token, currentRefreshTokenId))
            .toList();
    }

    /**
     * Revokes a specific session by ID, ensuring it belongs to the given user.
     *
     * @param user      the authenticated user
     * @param sessionId the ID of the session to revoke
     */
    public void revokeSession(final User user, final Long sessionId) {
        final Token session = findOwnedSession(user, sessionId);
        tokenRepository.delete(session);
        log.info("Revoked session '{}' for user '{}'", sessionId, user.getEmail());
    }

    /**
     * Revokes all sessions for the given user except the one identified by currentTokenId.
     *
     * @param user           the authenticated user
     * @param currentTokenId the id of the refresh token to keep, or {@code null} to revoke all
     */
    public void revokeAllOtherSessions(final User user, final Long currentTokenId) {
        listSessions(user).stream()
            .filter(token -> !token.getId().equals(currentTokenId))
            .forEach(tokenRepository::delete);
        log.info("Revoked all other sessions for user '{}'", user.getEmail());
    }

    /**
     * Loads a session by id and verifies it belongs to the given user. Throws {@link DataNotFoundException} for both not-found and
     * not-owned, to avoid leaking session-id existence to other users.
     */
    private Token findOwnedSession(final User user, final Long sessionId) {
        final Token session = tokenRepository.findById(sessionId)
            .orElseThrow(() -> new DataNotFoundException(SESSION_NOT_FOUND_ERROR));
        if (!session.getUser().getId().equals(user.getId())) {
            throw new DataNotFoundException(SESSION_NOT_FOUND_ERROR);
        }
        return session;
    }

    private SessionResponse toResponseWithCurrentFlag(final Token token, final Long currentRefreshTokenId) {
        final SessionResponse response = sessionMapper.toResponse(token);
        response.setCurrent(token.getId().equals(currentRefreshTokenId));
        return response;
    }
}
