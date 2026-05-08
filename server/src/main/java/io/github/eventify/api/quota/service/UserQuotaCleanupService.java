package io.github.eventify.api.quota.service;

import io.github.eventify.api.quota.repository.UserEventQuotaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.OffsetDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static java.time.ZoneOffset.UTC;

/**
 * A service class to clean up user event quotas.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserQuotaCleanupService {

    private final UserEventQuotaRepository quotaRepository;

    /**
     * Resets all user event quotas for the current month.
     */
    @Transactional
    public void resetMonthlyQuotas() {
        final Instant start = Instant.now();
        final OffsetDateTime periodStart = OffsetDateTime.now(UTC).withDayOfMonth(1).with(LocalTime.MIN);
        log.info("Starting monthly quota reset for period starting at {}", periodStart);
        try {
            final int resetCount = quotaRepository.resetAllQuotas(periodStart);
            log.info("Reset '{}' user quotas in '{}' ms", resetCount, Duration.between(start, Instant.now()).toMillis());
        } catch (final Exception exception) {
            log.error("Failed to reset monthly user quotas for period {}: {}", periodStart, exception.getMessage());
        }
    }
}
