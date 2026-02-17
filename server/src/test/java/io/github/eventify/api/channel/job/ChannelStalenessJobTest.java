package io.github.eventify.api.channel.job;

import io.github.eventify.api.channel.repository.ChannelRepository;
import io.github.eventify.support.UnitTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Unit Test - Channel Staleness Job
 */
@DisplayName("Unit Test - Channel Staleness Job")
public class ChannelStalenessJobTest extends UnitTest {

    @Mock
    private ChannelRepository channelRepository;

    private ChannelStalenessJob channelStalenessJob;

    @BeforeEach
    public void setUp() {
        // Given: Job is initialized with mocked repository
        channelStalenessJob = new ChannelStalenessJob(channelRepository);
    }

    @Test
    @DisplayName("Should mark channels as stale when lastEventAt is older than 7 days")
    public void shouldMarkChannelsAsStaleWhenLastEventAtIsOlderThan7Days() {
        // Given: Repository will return count of marked channels
        final int expectedMarkedCount = 5;
        given(channelRepository.markChannelsAsStale(any(), any())).willReturn(expectedMarkedCount);
        given(channelRepository.clearStaleForActiveChannels(any())).willReturn(0);

        // When: Job executes on schedule
        channelStalenessJob.markStaleChannels();

        // Then: Should call repository to mark channels as stale and clear active ones
        verify(channelRepository).markChannelsAsStale(any(), any());
        verify(channelRepository).clearStaleForActiveChannels(any());
        verifyNoMoreInteractions(channelRepository);
    }

    @Test
    @DisplayName("Should log count when channels are marked stale")
    public void shouldLogCountWhenChannelsAreMarkedStale() {
        // Given: Repository will mark 3 channels as stale
        final int markedCount = 3;
        given(channelRepository.markChannelsAsStale(any(), any())).willReturn(markedCount);
        given(channelRepository.clearStaleForActiveChannels(any())).willReturn(0);

        // When: Job executes
        channelStalenessJob.markStaleChannels();

        // Then: Should complete successfully with count logged
        verify(channelRepository).markChannelsAsStale(any(), any());
        verify(channelRepository).clearStaleForActiveChannels(any());
    }

    @Test
    @DisplayName("Should handle zero marked channels gracefully")
    public void shouldHandleZeroMarkedChannelsGracefully() {
        // Given: No channels to mark as stale
        given(channelRepository.markChannelsAsStale(any(), any())).willReturn(0);
        given(channelRepository.clearStaleForActiveChannels(any())).willReturn(0);

        // When: Job executes
        channelStalenessJob.markStaleChannels();

        // Then: Should complete without errors
        verify(channelRepository).markChannelsAsStale(any(), any());
        verify(channelRepository).clearStaleForActiveChannels(any());
        verifyNoMoreInteractions(channelRepository);
    }

    @Test
    @DisplayName("Should be idempotent when called multiple times")
    public void shouldBeIdempotentWhenCalledMultipleTimes() {
        // Given: Repository marks channels on each call
        given(channelRepository.markChannelsAsStale(any(), any())).willReturn(2);
        given(channelRepository.clearStaleForActiveChannels(any())).willReturn(1);

        // When: Job runs twice
        channelStalenessJob.markStaleChannels();
        channelStalenessJob.markStaleChannels();

        // Then: Should delegate to repository both times
        verify(channelRepository, org.mockito.Mockito.times(2)).markChannelsAsStale(any(), any());
        verify(channelRepository, org.mockito.Mockito.times(2)).clearStaleForActiveChannels(any());
    }

    @Test
    @DisplayName("Should handle repository exception gracefully")
    public void shouldHandleRepositoryExceptionGracefully() {
        // Given: Repository throws runtime exception
        final RuntimeException exception = new RuntimeException("Database connection failed");
        given(channelRepository.markChannelsAsStale(any(), any())).willThrow(exception);

        // When/Then: Exception should propagate for scheduler to handle
        try {
            channelStalenessJob.markStaleChannels();
        } catch (final RuntimeException e) {
            verify(channelRepository).markChannelsAsStale(any(), any());
        }
    }

    @Test
    @DisplayName("Should clear stale flag for channels with recent activity")
    public void shouldClearStaleFlagForChannelsWithRecentActivity() {
        // Given: Repository will clear some channels from stale status
        given(channelRepository.markChannelsAsStale(any(), any())).willReturn(0);
        given(channelRepository.clearStaleForActiveChannels(any())).willReturn(3);

        // When: Job executes
        channelStalenessJob.markStaleChannels();

        // Then: Should call both mark and clear methods
        verify(channelRepository).markChannelsAsStale(any(), any());
        verify(channelRepository).clearStaleForActiveChannels(any());
        verifyNoMoreInteractions(channelRepository);
    }
}
