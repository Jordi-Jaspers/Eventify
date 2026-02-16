package io.github.eventify.api.channel.service;

import io.github.eventify.api.channel.model.Channel;
import io.github.eventify.api.channel.model.ChannelStatus;
import io.github.eventify.api.channel.repository.ChannelRepository;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.watchlist.repository.WatchlistRepository;
import io.github.eventify.support.UnitTest;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit Test - Channel Deletion Service
 */
@DisplayName("Unit Test - Channel Deletion Service")
public class ChannelCleanupServiceTest extends UnitTest {

    @Mock
    private ChannelRepository channelRepository;

    @Mock
    private WatchlistRepository watchlistRepository;

    @InjectMocks
    private ChannelCleanupService channelCleanupService;

    private User user;

    @BeforeEach
    public void setUp() {
        // Given: A valid user for test channels
        user = aValidUser();
        user.setId(1L);
    }

    @Test
    @DisplayName("Should delete single channel when one pending deletion")
    public void shouldDeleteSingleChannelWhenOnePendingDeletion() {
        // Given: One channel pending deletion
        final Channel channel = createChannelPendingDeletion(1L, "Channel to Delete");
        when(channelRepository.findByStatus(ChannelStatus.PENDING_DELETION))
            .thenReturn(List.of(channel));

        // When: Processing channels pending deletion
        channelCleanupService.deletePendingChannels();

        // Then: Should remove channel from watchlists and delete the channel
        verify(channelRepository).findByStatus(ChannelStatus.PENDING_DELETION);
        verify(watchlistRepository).removeChannelFromAllConfigurations(1L);
        verify(channelRepository).delete(channel);
    }

    @Test
    @DisplayName("Should delete multiple channels when many pending deletion")
    public void shouldDeleteMultipleChannelsWhenManyPendingDeletion() {
        // Given: Three channels pending deletion
        final Channel channel1 = createChannelPendingDeletion(1L, "Channel 1");
        final Channel channel2 = createChannelPendingDeletion(2L, "Channel 2");
        final Channel channel3 = createChannelPendingDeletion(3L, "Channel 3");

        when(channelRepository.findByStatus(ChannelStatus.PENDING_DELETION))
            .thenReturn(List.of(channel1, channel2, channel3));

        // When: Processing channels pending deletion
        channelCleanupService.deletePendingChannels();

        // Then: Should remove from watchlists and delete all three channels
        verify(channelRepository).findByStatus(ChannelStatus.PENDING_DELETION);
        verify(watchlistRepository).removeChannelFromAllConfigurations(1L);
        verify(watchlistRepository).removeChannelFromAllConfigurations(2L);
        verify(watchlistRepository).removeChannelFromAllConfigurations(3L);
        verify(channelRepository).delete(channel1);
        verify(channelRepository).delete(channel2);
        verify(channelRepository).delete(channel3);
    }

    @Test
    @DisplayName("Should complete gracefully when no channels pending deletion")
    public void shouldCompleteGracefullyWhenNoChannelsPendingDeletion() {
        // Given: No channels pending deletion
        when(channelRepository.findByStatus(ChannelStatus.PENDING_DELETION))
            .thenReturn(Collections.emptyList());

        // When: Processing channels pending deletion
        channelCleanupService.deletePendingChannels();

        // Then: Should query repository but not attempt any deletions
        verify(channelRepository).findByStatus(ChannelStatus.PENDING_DELETION);
        verify(channelRepository, never()).delete(any(Channel.class));
        verifyNoInteractions(watchlistRepository);
    }

    @Test
    @DisplayName("Should continue processing when one channel deletion fails")
    public void shouldContinueProcessingWhenOneChannelDeletionFails() {
        // Given: Three channels, middle one will fail
        final Channel channel1 = createChannelPendingDeletion(1L, "Channel 1");
        final Channel channel2 = createChannelPendingDeletion(2L, "Channel 2");
        final Channel channel3 = createChannelPendingDeletion(3L, "Channel 3");

        when(channelRepository.findByStatus(ChannelStatus.PENDING_DELETION))
            .thenReturn(List.of(channel1, channel2, channel3));
        doThrow(new RuntimeException("Database constraint violation"))
            .when(channelRepository).delete(channel2);

        // When: Processing channels pending deletion
        channelCleanupService.deletePendingChannels();

        // Then: Should delete channel1, fail on channel2, continue with channel3
        verify(channelRepository).findByStatus(ChannelStatus.PENDING_DELETION);
        verify(watchlistRepository).removeChannelFromAllConfigurations(1L);
        verify(watchlistRepository).removeChannelFromAllConfigurations(2L);
        verify(watchlistRepository).removeChannelFromAllConfigurations(3L);
        verify(channelRepository).delete(channel1);
        verify(channelRepository).delete(channel2);
        verify(channelRepository).delete(channel3);
    }

    @Test
    @DisplayName("Should be idempotent when called multiple times consecutively")
    public void shouldBeIdempotentWhenCalledMultipleTimesConsecutively() {
        // Given: One channel pending deletion initially
        final Channel channel = createChannelPendingDeletion(1L, "Channel to Delete");
        when(channelRepository.findByStatus(ChannelStatus.PENDING_DELETION))
            .thenReturn(List.of(channel))
            .thenReturn(Collections.emptyList());

        // When: Processing first time
        channelCleanupService.deletePendingChannels();

        // And: Processing second time immediately after
        channelCleanupService.deletePendingChannels();

        // Then: Should find channels twice, but only delete once
        verify(channelRepository, times(2)).findByStatus(ChannelStatus.PENDING_DELETION);
        verify(watchlistRepository, times(1)).removeChannelFromAllConfigurations(1L);
        verify(channelRepository, times(1)).delete(channel);
    }

    @Test
    @DisplayName("Should handle null channel gracefully when repository returns null")
    public void shouldHandleNullChannelGracefullyWhenRepositoryReturnsNull() {
        // Given: Repository returns list with null element
        when(channelRepository.findByStatus(ChannelStatus.PENDING_DELETION))
            .thenReturn(Collections.singletonList(null));

        // When: Processing channels pending deletion
        channelCleanupService.deletePendingChannels();

        // Then: Should handle null without attempting deletion
        verify(channelRepository).findByStatus(ChannelStatus.PENDING_DELETION);
        verify(channelRepository, never()).delete(any(Channel.class));
    }

    @Test
    @DisplayName("Should only process channels with PENDING_DELETION status")
    public void shouldOnlyProcessChannelsWithPendingDeletionStatus() {
        // Given: Channels are queried by PENDING_DELETION status
        final Channel channel = createChannelPendingDeletion(1L, "Channel to Delete");
        when(channelRepository.findByStatus(ChannelStatus.PENDING_DELETION))
            .thenReturn(List.of(channel));

        // When: Processing channels
        channelCleanupService.deletePendingChannels();

        // Then: Should query only PENDING_DELETION status
        verify(channelRepository).findByStatus(ChannelStatus.PENDING_DELETION);
        verify(channelRepository, never()).findByStatus(ChannelStatus.ACTIVE);
        verify(channelRepository, never()).findByStatus(ChannelStatus.PAUSED);
    }

    @Test
    @DisplayName("Should perform hard delete on channel when processing")
    public void shouldPerformHardDeleteOnChannelWhenProcessing() {
        // Given: One channel pending deletion
        final Channel channel = createChannelPendingDeletion(1L, "Channel to Delete");
        when(channelRepository.findByStatus(ChannelStatus.PENDING_DELETION))
            .thenReturn(List.of(channel));

        // When: Processing channel deletion
        channelCleanupService.deletePendingChannels();

        // Then: Should remove from watchlists and call repository delete (hard delete)
        verify(watchlistRepository).removeChannelFromAllConfigurations(1L);
        verify(channelRepository).delete(channel);
        verify(channelRepository, never()).save(any(Channel.class));
    }

    @Test
    @DisplayName("Should delete channels from different users when pending deletion")
    public void shouldDeleteChannelsFromDifferentUsersWhenPendingDeletion() {
        // Given: Channels from different users
        final User user1 = aValidUser();
        user1.setId(1L);
        final User user2 = aValidUser();
        user2.setId(2L);

        final Channel channel1 = createChannelPendingDeletion(1L, "User 1 Channel", user1);
        final Channel channel2 = createChannelPendingDeletion(2L, "User 2 Channel", user2);

        when(channelRepository.findByStatus(ChannelStatus.PENDING_DELETION))
            .thenReturn(List.of(channel1, channel2));

        // When: Processing channels pending deletion
        channelCleanupService.deletePendingChannels();

        // Then: Should remove from watchlists and delete channels from both users
        verify(watchlistRepository).removeChannelFromAllConfigurations(1L);
        verify(watchlistRepository).removeChannelFromAllConfigurations(2L);
        verify(channelRepository).delete(channel1);
        verify(channelRepository).delete(channel2);
    }

    @Test
    @DisplayName("Should handle channels with null organization when deleting")
    public void shouldHandleChannelsWithNullOrganizationWhenDeleting() {
        // Given: Personal channel (null organization)
        final Channel personalChannel = createChannelPendingDeletion(1L, "Personal Channel");
        assertThat(personalChannel.getOrganization(), is((org.hamcrest.Matchers.nullValue())));

        when(channelRepository.findByStatus(ChannelStatus.PENDING_DELETION))
            .thenReturn(List.of(personalChannel));

        // When: Processing channel deletion
        channelCleanupService.deletePendingChannels();

        // Then: Should remove from watchlists and delete personal channel successfully
        verify(watchlistRepository).removeChannelFromAllConfigurations(1L);
        verify(channelRepository).delete(personalChannel);
    }

    @Test
    @DisplayName("Should process empty list without error when no results")
    public void shouldProcessEmptyListWithoutErrorWhenNoResults() {
        // Given: Empty list from repository
        when(channelRepository.findByStatus(ChannelStatus.PENDING_DELETION))
            .thenReturn(List.of());

        // When: Processing channels
        channelCleanupService.deletePendingChannels();

        // Then: Should complete without attempting any deletions
        verify(channelRepository).findByStatus(ChannelStatus.PENDING_DELETION);
        verify(channelRepository, never()).delete(any(Channel.class));
    }

    @Test
    @DisplayName("Should handle repository exception during query gracefully")
    public void shouldHandleRepositoryExceptionDuringQueryGracefully() {
        // Given: Repository throws exception during query
        when(channelRepository.findByStatus(ChannelStatus.PENDING_DELETION))
            .thenThrow(new RuntimeException("Database connection lost"));

        // When & Then: Exception should propagate
        try {
            channelCleanupService.deletePendingChannels();
        } catch (final RuntimeException e) {
            assertThat(e.getMessage(), is("Database connection lost"));
        }

        verify(channelRepository).findByStatus(ChannelStatus.PENDING_DELETION);
        verify(channelRepository, never()).delete(any(Channel.class));
    }

    private Channel createChannelPendingDeletion(final Long id, final String name) {
        return createChannelPendingDeletion(id, name, user);
    }

    private Channel createChannelPendingDeletion(final Long id, final String name, final User channelUser) {
        final Channel channel = new Channel(name, "test.slug." + id, channelUser, null);
        channel.setId(id);
        channel.setStatus(ChannelStatus.PENDING_DELETION);
        channel.setCreatedAt(OffsetDateTime.now().minusDays(7));
        channel.setUpdatedAt(OffsetDateTime.now().minusHours(1));
        return channel;
    }
}
