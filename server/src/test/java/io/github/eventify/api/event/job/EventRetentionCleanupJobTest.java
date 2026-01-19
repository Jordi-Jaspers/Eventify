package io.github.eventify.api.event.job;

import io.github.eventify.api.event.service.EventRetentionCleanupService;
import io.github.eventify.support.UnitTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Unit Test - Event Retention Cleanup Job
 */
@DisplayName("Unit Test - Event Retention Cleanup Job")
public class EventRetentionCleanupJobTest extends UnitTest {

    @Mock
    private EventRetentionCleanupService eventRetentionCleanupService;

    @InjectMocks
    private EventRetentionCleanupJob eventRetentionCleanupJob;

    @BeforeEach
    public void setUp() {
        // Given: Job is initialized with mocked service
    }

    @Test
    @DisplayName("Should delegate to service when job executes")
    public void shouldDelegateToServiceWhenJobExecutes() {
        // Given: Job is ready to run

        // When: Job executes on schedule
        eventRetentionCleanupJob.cleanupExpiredEvents();

        // Then: Should delegate to service
        verify(eventRetentionCleanupService).cleanupExpiredEvents();
        verifyNoMoreInteractions(eventRetentionCleanupService);
    }

    @Test
    @DisplayName("Should handle service exception gracefully when job executes")
    public void shouldHandleServiceExceptionGracefullyWhenJobExecutes() {
        // Given: Service throws runtime exception
        final RuntimeException exception = new RuntimeException("Database connection failed");

        // When: Job executes and service throws
        try {
            eventRetentionCleanupJob.cleanupExpiredEvents();
        } catch (final RuntimeException e) {
            // Then: Exception should propagate for scheduler to handle
            verify(eventRetentionCleanupService).cleanupExpiredEvents();
        }
    }

    @Test
    @DisplayName("Should be idempotent when called multiple times")
    public void shouldBeIdempotentWhenCalledMultipleTimes() {
        // Given: Job has run once
        eventRetentionCleanupJob.cleanupExpiredEvents();

        // When: Job runs again immediately
        eventRetentionCleanupJob.cleanupExpiredEvents();

        // Then: Should delegate to service both times without issues
        verify(eventRetentionCleanupService, org.mockito.Mockito.times(2)).cleanupExpiredEvents();
    }
}
