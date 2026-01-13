package io.github.eventify.api.token.service;

import io.github.eventify.api.token.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for cleaning up expired tokens.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenCleanupService {

    private final TokenRepository tokenRepository;

    /**
     * Deletes all expired tokens. Runs every 5 minutes in a single batch operation.
     */
    @Transactional
    public void deleteExpiredTokens() {
        final Instant start = Instant.now();
        log.info("Starting cleanup of expired tokens");

        try {
            final int deleted = tokenRepository.deleteExpiredTokens();
            log.info("Deleted {} expired tokens in {} ms", deleted, Duration.between(start, Instant.now()).toMillis());
        } catch (final Exception exception) {
            log.error("Failed to delete expired tokens: {}", exception.getMessage());
        }
    }
}
