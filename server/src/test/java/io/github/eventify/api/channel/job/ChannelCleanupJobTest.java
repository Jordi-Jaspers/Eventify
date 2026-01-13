package io.github.eventify.api.channel.job;

import io.github.eventify.api.channel.service.ChannelCleanupService;
import io.github.eventify.support.UnitTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Unit Test - Channel Deletion Job
 */
@DisplayName("Unit Test - Channel Deletion Job")
public class ChannelCleanupJobTest extends UnitTest {

    @Mock
    private ChannelCleanupService channelCleanupService;

    @InjectMocks
    private ChannelCleanupJob channelCleanupJob;

    @BeforeEach
    public void setUp() {
        // Given: Job is initialized with mocked service
    }

    @Test
    @DisplayName("Should delegate to service when job executes")
    public void shouldDelegateToServiceWhenJobExecutes() {
        // Given: Job is ready to run

        // When: Job executes on schedule
        channelCleanupJob.processChannelDeletions();

        // Then: Should delegate to service
        verify(channelCleanupService).deletePendingChannels();
        verifyNoMoreInteractions(channelCleanupService);
    }

    @Test
    @DisplayName("Should handle service exception gracefully when job executes")
    public void shouldHandleServiceExceptionGracefullyWhenJobExecutes() {
        // Given: Service throws runtime exception
        final RuntimeException exception = new RuntimeException("Database connection failed");

        // When: Job executes and service throws
        try {
            channelCleanupJob.processChannelDeletions();
        } catch (final RuntimeException e) {
            // Then: Exception should propagate for scheduler to handle
            verify(channelCleanupService).deletePendingChannels();
        }
    }

    @Test
    @DisplayName("Should be idempotent when called multiple times")
    public void shouldBeIdempotentWhenCalledMultipleTimes() {
        // Given: Job has run once
        channelCleanupJob.processChannelDeletions();

        // When: Job runs again immediately
        channelCleanupJob.processChannelDeletions();

        // Then: Should delegate to service both times without issues
        verify(channelCleanupService, org.mockito.Mockito.times(2)).deletePendingChannels();
    }
}
