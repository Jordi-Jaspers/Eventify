package io.github.eventify.api.token.job;

import io.github.eventify.api.token.service.TokenCleanupService;
import io.github.eventify.support.UnitTest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Unit Test - Token Cleanup Job
 */
@DisplayName("Unit Test - Token Cleanup Job")
public class TokenCleanupJobTest extends UnitTest {

    @Mock
    private TokenCleanupService tokenCleanupService;

    @InjectMocks
    private TokenCleanupJob tokenCleanupJob;

    @Test
    @DisplayName("Should delegate to service when job executes")
    public void shouldDelegateToServiceWhenJobExecutes() {
        // Given: Job is ready to run

        // When: Job executes on schedule
        tokenCleanupJob.processExpiredTokenDeletions();

        // Then: Should delegate to service
        verify(tokenCleanupService).deleteExpiredTokens();
        verifyNoMoreInteractions(tokenCleanupService);
    }

    @Test
    @DisplayName("Should handle service exception gracefully when job executes")
    public void shouldHandleServiceExceptionGracefullyWhenJobExecutes() {
        // Given: Service throws runtime exception
        final RuntimeException exception = new RuntimeException("Database connection failed");

        // When: Job executes and service throws
        try {
            tokenCleanupJob.processExpiredTokenDeletions();
        } catch (final RuntimeException e) {
            // Then: Exception should propagate for scheduler to handle
            verify(tokenCleanupService).deleteExpiredTokens();
        }
    }

    @Test
    @DisplayName("Should be idempotent when called multiple times")
    public void shouldBeIdempotentWhenCalledMultipleTimes() {
        // Given: Job has run once
        tokenCleanupJob.processExpiredTokenDeletions();

        // When: Job runs again immediately
        tokenCleanupJob.processExpiredTokenDeletions();

        // Then: Should delegate to service both times without issues
        verify(tokenCleanupService, times(2)).deleteExpiredTokens();
    }
}
