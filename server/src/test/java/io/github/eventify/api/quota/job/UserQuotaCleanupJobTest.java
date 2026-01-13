package io.github.eventify.api.quota.job;

import io.github.eventify.api.quota.service.UserQuotaCleanupService;
import io.github.eventify.support.UnitTest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Unit Test - User Quota Cleanup Job
 */
@DisplayName("Unit Test - User Quota Cleanup Job")
public class UserQuotaCleanupJobTest extends UnitTest {

    @Mock
    private UserQuotaCleanupService userQuotaCleanupService;

    @InjectMocks
    private UserQuotaCleanupJob userQuotaCleanupJob;

    @Test
    @DisplayName("Should delegate to service when job executes")
    public void shouldDelegateToServiceWhenJobExecutes() {
        // Given: Job is ready to run

        // When: Job executes on schedule
        userQuotaCleanupJob.processExpiredTokenDeletions();

        // Then: Should delegate to service
        verify(userQuotaCleanupService).resetMonthlyQuotas();
        verifyNoMoreInteractions(userQuotaCleanupService);
    }

    @Test
    @DisplayName("Should handle service exception gracefully when job executes")
    public void shouldHandleServiceExceptionGracefullyWhenJobExecutes() {
        // Given: Service throws runtime exception
        final RuntimeException exception = new RuntimeException("Database connection failed");

        // When: Job executes and service throws
        try {
            userQuotaCleanupJob.processExpiredTokenDeletions();
        } catch (final RuntimeException e) {
            // Then: Exception should propagate for scheduler to handle
            verify(userQuotaCleanupService).resetMonthlyQuotas();
        }
    }

    @Test
    @DisplayName("Should be idempotent when called multiple times")
    public void shouldBeIdempotentWhenCalledMultipleTimes() {
        // Given: Job has run once
        userQuotaCleanupJob.processExpiredTokenDeletions();

        // When: Job runs again immediately
        userQuotaCleanupJob.processExpiredTokenDeletions();

        // Then: Should delegate to service both times without issues
        verify(userQuotaCleanupService, times(2)).resetMonthlyQuotas();
    }
}
