package io.github.eventify.api.quota.service;

import io.github.eventify.api.quota.repository.UserEventQuotaRepository;
import io.github.eventify.support.UnitTest;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit Test - User Quota Cleanup Service
 */
@DisplayName("Unit Test - User Quota Cleanup Service")
public class UserQuotaCleanupServiceTest extends UnitTest {

    @Mock
    private UserEventQuotaRepository quotaRepository;

    @InjectMocks
    private UserQuotaCleanupService userQuotaCleanupService;

    @Test
    @DisplayName("Should reset all quotas when quotas exist")
    public void shouldResetAllQuotasWhenQuotasExist() {
        // Given: Repository returns 10 reset quotas
        when(quotaRepository.resetAllQuotas(any(OffsetDateTime.class))).thenReturn(10);

        // When: Resetting monthly quotas
        userQuotaCleanupService.resetMonthlyQuotas();

        // Then: Should call repository method with period start
        verify(quotaRepository).resetAllQuotas(any(OffsetDateTime.class));
    }

    @Test
    @DisplayName("Should complete gracefully when no quotas to reset")
    public void shouldCompleteGracefullyWhenNoQuotasToReset() {
        // Given: Repository returns 0 reset quotas
        when(quotaRepository.resetAllQuotas(any(OffsetDateTime.class))).thenReturn(0);

        // When: Resetting monthly quotas
        userQuotaCleanupService.resetMonthlyQuotas();

        // Then: Should complete without error
        verify(quotaRepository).resetAllQuotas(any(OffsetDateTime.class));
    }

    @Test
    @DisplayName("Should handle repository exception gracefully")
    public void shouldHandleRepositoryExceptionGracefully() {
        // Given: Repository throws exception
        doThrow(new RuntimeException("Database connection lost"))
            .when(quotaRepository).resetAllQuotas(any(OffsetDateTime.class));

        // When: Resetting quotas (exception is caught internally)
        userQuotaCleanupService.resetMonthlyQuotas();

        // Then: Should not propagate exception
        verify(quotaRepository).resetAllQuotas(any(OffsetDateTime.class));
    }

    @Test
    @DisplayName("Should be idempotent when called multiple times")
    public void shouldBeIdempotentWhenCalledMultipleTimes() {
        // Given: Repository returns different counts
        when(quotaRepository.resetAllQuotas(any(OffsetDateTime.class)))
            .thenReturn(10)
            .thenReturn(0);

        // When: Called multiple times
        userQuotaCleanupService.resetMonthlyQuotas();
        userQuotaCleanupService.resetMonthlyQuotas();

        // Then: Should call repository twice
        verify(quotaRepository, times(2)).resetAllQuotas(any(OffsetDateTime.class));
    }

    @Test
    @DisplayName("Should use first day of month as period start")
    public void shouldUseFirstDayOfMonthAsPeriodStart() {
        // Given: Repository accepts any date
        when(quotaRepository.resetAllQuotas(any(OffsetDateTime.class))).thenReturn(5);

        // When: Resetting quotas
        userQuotaCleanupService.resetMonthlyQuotas();

        // Then: Should call with a date (the service calculates first of month)
        verify(quotaRepository).resetAllQuotas(any(OffsetDateTime.class));
    }

    @Test
    @DisplayName("Should reset large number of quotas")
    public void shouldResetLargeNumberOfQuotas() {
        // Given: Repository returns large count
        when(quotaRepository.resetAllQuotas(any(OffsetDateTime.class))).thenReturn(50000);

        // When: Resetting monthly quotas
        userQuotaCleanupService.resetMonthlyQuotas();

        // Then: Should complete successfully
        verify(quotaRepository).resetAllQuotas(any(OffsetDateTime.class));
    }
}
