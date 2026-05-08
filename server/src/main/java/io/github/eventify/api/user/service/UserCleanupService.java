package io.github.eventify.api.user.service;

import io.github.eventify.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static java.time.ZoneOffset.UTC;

/**
 * A service class to clean up unvalidated user accounts.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserCleanupService {

    private final UserRepository userRepository;

    /**
     * Deletes all unvalidated accounts older than one month. Runs every 24 hours.
     */
    @Transactional
    public void deleteExpiredUnvalidatedAccounts() {
        final Instant start = Instant.now();
        final OffsetDateTime cutoff = OffsetDateTime.now(UTC).minusMonths(1);
        log.info("Starting cleanup of unvalidated accounts older than {}", cutoff);
        try {
            final int deleted = userRepository.deleteUnvalidatedAccounts(cutoff);
            log.info("Deleted '{}' unvalidated accounts in {} ms", deleted, Duration.between(start, Instant.now()).toMillis());
        } catch (final Exception exception) {
            log.error("Failed to delete unvalidated accounts older than {}: {}", cutoff, exception.getMessage());
        }
    }
}
