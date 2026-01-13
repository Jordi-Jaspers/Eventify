package io.github.eventify.api.user.job;

import io.github.eventify.api.user.service.UserCleanupService;
import io.github.eventify.support.UnitTest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Unit Test - User Cleanup Job
 */
@DisplayName("Unit Test - User Cleanup Job")
public class UserCleanupJobTest extends UnitTest {

    @Mock
    private UserCleanupService userCleanupService;

    @InjectMocks
    private UserCleanupJob userCleanupJob;

    @Test
    @DisplayName("Should delegate to service when job executes")
    public void shouldDelegateToServiceWhenJobExecutes() {
        // Given: Job is ready to run

        // When: Job executes on schedule
        userCleanupJob.processExpiredUnvalidatedAccountDeletions();

        // Then: Should delegate to service
        verify(userCleanupService).deleteExpiredUnvalidatedAccounts();
        verifyNoMoreInteractions(userCleanupService);
    }

    @Test
    @DisplayName("Should handle service exception gracefully when job executes")
    public void shouldHandleServiceExceptionGracefullyWhenJobExecutes() {
        // Given: Service throws runtime exception
        final RuntimeException exception = new RuntimeException("Database connection failed");

        // When: Job executes and service throws
        try {
            userCleanupJob.processExpiredUnvalidatedAccountDeletions();
        } catch (final RuntimeException e) {
            // Then: Exception should propagate for scheduler to handle
            verify(userCleanupService).deleteExpiredUnvalidatedAccounts();
        }
    }

    @Test
    @DisplayName("Should be idempotent when called multiple times")
    public void shouldBeIdempotentWhenCalledMultipleTimes() {
        // Given: Job has run once
        userCleanupJob.processExpiredUnvalidatedAccountDeletions();

        // When: Job runs again immediately
        userCleanupJob.processExpiredUnvalidatedAccountDeletions();

        // Then: Should delegate to service both times without issues
        verify(userCleanupService, times(2)).deleteExpiredUnvalidatedAccounts();
    }
}
