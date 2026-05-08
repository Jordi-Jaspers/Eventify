package io.github.eventify.api.channel.repository;

import io.github.eventify.api.channel.model.Channel;
import io.github.eventify.api.channel.model.ChannelStatus;
import io.github.eventify.api.user.model.User;
import io.github.eventify.support.IntegrationTest;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.github.eventify.common.util.TimeProvider.now;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Integration Test - Channel Repository - Staleness Methods
 */
@DisplayName("Integration Test - Channel Repository Staleness")
public class ChannelRepositoryStalenessTest extends IntegrationTest {

    @Test
    @DisplayName("Should update isStale=true for channels with lastEventAt older than threshold")
    public void shouldUpdateIsStaleForChannelsWithOldLastEventAt() {
        // Given: User with a channel
        final User user = aValidatedUser();
        final Channel channel = aChannelForUser(user, "Old Channel");

        // And: Channel has lastEventAt 10 days ago (beyond 7-day threshold)
        final OffsetDateTime tenDaysAgo = now().minusDays(10);
        channel.setLastEventAt(tenDaysAgo);
        channel.setIsStale(false);
        channelRepository.save(channel);

        // And: Channel was created 30 days ago (past grace period) - use SQL to bypass JPA's updatable=false
        updateChannelCreatedAt(channel, now().minusDays(30));

        // And: Staleness threshold is 7 days ago
        final OffsetDateTime stalenessThreshold = now().minusDays(7);

        // When: Marking channels as stale
        final int markedCount = channelRepository.markChannelsAsStale(stalenessThreshold, stalenessThreshold);

        // Then: One channel should be marked as stale
        assertThat(markedCount, is(1));

        // And: Channel should be marked as stale in database
        final Channel updatedChannel = channelRepository.findById(channel.getId()).orElseThrow();
        assertThat(updatedChannel.getIsStale(), is(true));
    }

    @Test
    @DisplayName("Should NOT update channels with recent lastEventAt")
    public void shouldNotUpdateChannelsWithRecentLastEventAt() {
        // Given: User with a channel
        final User user = aValidatedUser();
        final Channel channel = aChannelForUser(user, "Active Channel");

        // And: Channel has lastEventAt 3 days ago (within 7-day threshold)
        final OffsetDateTime threeDaysAgo = now().minusDays(3);
        channel.setLastEventAt(threeDaysAgo);
        channel.setIsStale(false);
        channelRepository.save(channel);

        // And: Channel was created 30 days ago - use SQL to bypass JPA's updatable=false
        updateChannelCreatedAt(channel, now().minusDays(30));

        // And: Staleness threshold is 7 days ago
        final OffsetDateTime stalenessThreshold = now().minusDays(7);

        // When: Marking channels as stale
        final int markedCount = channelRepository.markChannelsAsStale(stalenessThreshold, stalenessThreshold);

        // Then: No channels should be marked
        assertThat(markedCount, is(0));

        // And: Channel should still NOT be stale
        final Channel updatedChannel = channelRepository.findById(channel.getId()).orElseThrow();
        assertThat(updatedChannel.getIsStale(), is(false));
    }

    @Test
    @DisplayName("Should NOT update channels created within grace period threshold")
    public void shouldNotUpdateChannelsCreatedWithinGracePeriod() {
        // Given: User with a new channel
        final User user = aValidatedUser();
        final Channel channel = aChannelForUser(user, "New Channel");

        // And: Channel never had events and isStale is false
        channel.setLastEventAt(null);
        channel.setIsStale(false);
        channelRepository.save(channel);

        // And: Channel was created 5 days ago (within 7-day grace period) - use SQL to bypass JPA's updatable=false
        updateChannelCreatedAt(channel, now().minusDays(5));

        // And: Grace period threshold is 7 days ago
        final OffsetDateTime gracePeriodThreshold = now().minusDays(7);

        // When: Marking channels as stale
        final int markedCount = channelRepository.markChannelsAsStale(gracePeriodThreshold, gracePeriodThreshold);

        // Then: No channels should be marked (new channel protected by grace period)
        assertThat(markedCount, is(0));

        // And: Channel should still NOT be stale
        final Channel updatedChannel = channelRepository.findById(channel.getId()).orElseThrow();
        assertThat(updatedChannel.getIsStale(), is(false));
    }

    @Test
    @DisplayName("Should mark channels with null lastEventAt if created beyond grace period")
    public void shouldMarkChannelsWithNullLastEventAtIfCreatedBeyondGracePeriod() {
        // Given: User with an old channel
        final User user = aValidatedUser();
        final Channel channel = aChannelForUser(user, "Old Unused Channel");

        // And: Channel never had events and isStale is false
        channel.setLastEventAt(null);
        channel.setIsStale(false);
        channelRepository.save(channel);

        // And: Channel was created 30 days ago (beyond grace period) - use SQL to bypass JPA's updatable=false
        updateChannelCreatedAt(channel, now().minusDays(30));

        // And: Thresholds are 7 days ago
        final OffsetDateTime threshold = now().minusDays(7);

        // When: Marking channels as stale
        final int markedCount = channelRepository.markChannelsAsStale(threshold, threshold);

        // Then: One channel should be marked (old with no events)
        assertThat(markedCount, is(1));

        // And: Channel should be marked as stale
        final Channel updatedChannel = channelRepository.findById(channel.getId()).orElseThrow();
        assertThat(updatedChannel.getIsStale(), is(true));
    }

    @Test
    @DisplayName("Should return count of updated channels")
    public void shouldReturnCountOfUpdatedChannels() {
        // Given: User with multiple channels
        final User user = aValidatedUser();

        // And: Three channels with different staleness states
        final Channel staleChannel1 = aChannelForUser(user, "Stale 1");
        staleChannel1.setLastEventAt(now().minusDays(10));
        staleChannel1.setIsStale(false);
        channelRepository.save(staleChannel1);
        updateChannelCreatedAt(staleChannel1, now().minusDays(30));

        final Channel staleChannel2 = aChannelForUser(user, "Stale 2");
        staleChannel2.setLastEventAt(now().minusDays(15));
        staleChannel2.setIsStale(false);
        channelRepository.save(staleChannel2);
        updateChannelCreatedAt(staleChannel2, now().minusDays(30));

        final Channel activeChannel = aChannelForUser(user, "Active");
        activeChannel.setLastEventAt(now().minusDays(2));
        activeChannel.setIsStale(false);
        channelRepository.save(activeChannel);
        updateChannelCreatedAt(activeChannel, now().minusDays(30));

        // And: Threshold is 7 days
        final OffsetDateTime threshold = now().minusDays(7);

        // When: Marking channels as stale
        final int markedCount = channelRepository.markChannelsAsStale(threshold, threshold);

        // Then: Exactly 2 channels should be marked
        assertThat(markedCount, is(2));
    }

    @Test
    @DisplayName("Should NOT update channels with PENDING_DELETION status")
    public void shouldNotUpdateChannelsWithPendingDeletionStatus() {
        // Given: User with a channel pending deletion
        final User user = aValidatedUser();
        final Channel channel = aChannelForUser(user, "To Delete");

        // And: Channel has old lastEventAt and is pending deletion
        channel.setLastEventAt(now().minusDays(10));
        channel.setStatus(ChannelStatus.PENDING_DELETION);
        channel.setIsStale(false);
        channelRepository.save(channel);

        // And: Channel was created 30 days ago - use SQL to bypass JPA's updatable=false
        updateChannelCreatedAt(channel, now().minusDays(30));

        // And: Threshold is 7 days
        final OffsetDateTime threshold = now().minusDays(7);

        // When: Marking channels as stale
        final int markedCount = channelRepository.markChannelsAsStale(threshold, threshold);

        // Then: No channels should be marked (pending deletion excluded)
        assertThat(markedCount, is(0));

        // And: Channel should still NOT be stale
        final Channel updatedChannel = channelRepository.findById(channel.getId()).orElseThrow();
        assertThat(updatedChannel.getIsStale(), is(false));
    }

    @Test
    @DisplayName("Should handle empty database gracefully")
    public void shouldHandleEmptyDatabaseGracefully() {
        // Given: No channels in database (test cleanup ensures this)

        // And: Threshold is 7 days
        final OffsetDateTime threshold = now().minusDays(7);

        // When: Marking channels as stale
        final int markedCount = channelRepository.markChannelsAsStale(threshold, threshold);

        // Then: Zero channels should be marked
        assertThat(markedCount, is(0));
    }

    @Test
    @DisplayName("Should be idempotent when called multiple times")
    public void shouldBeIdempotentWhenCalledMultipleTimes() {
        // Given: User with a stale channel
        final User user = aValidatedUser();
        final Channel channel = aChannelForUser(user, "Stale Channel");
        channel.setLastEventAt(now().minusDays(10));
        channel.setIsStale(false);
        channelRepository.save(channel);

        // And: Channel was created 30 days ago - use SQL to bypass JPA's updatable=false
        updateChannelCreatedAt(channel, now().minusDays(30));

        // And: Threshold is 7 days
        final OffsetDateTime threshold = now().minusDays(7);

        // When: Marking channels as stale twice
        final int firstRunCount = channelRepository.markChannelsAsStale(threshold, threshold);
        final int secondRunCount = channelRepository.markChannelsAsStale(threshold, threshold);

        // Then: First run should mark 1 channel
        assertThat(firstRunCount, is(1));

        // And: Second run should mark 0 (already stale)
        assertThat(secondRunCount, is(0));

        // And: Channel should remain stale
        final Channel updatedChannel = channelRepository.findById(channel.getId()).orElseThrow();
        assertThat(updatedChannel.getIsStale(), is(true));
    }

    @Test
    @DisplayName("Should clear stale flag for channels with recent activity")
    public void shouldClearStaleFlagForChannelsWithRecentActivity() {
        // Given: User with a stale channel
        final User user = aValidatedUser();
        final Channel channel = aChannelForUser(user, "Was Stale Channel");

        // And: Channel is marked stale but has recent activity (simulating trigger bypass scenario)
        channel.setLastEventAt(now().minusDays(3)); // Recent activity (within 7 days)
        channel.setIsStale(true); // But marked stale
        channelRepository.save(channel);

        // And: Threshold is 7 days ago
        final OffsetDateTime threshold = now().minusDays(7);

        // When: Clearing stale for active channels
        final int clearedCount = channelRepository.clearStaleForActiveChannels(threshold);

        // Then: One channel should be cleared
        assertThat(clearedCount, is(1));

        // And: Channel should no longer be stale
        final Channel updatedChannel = channelRepository.findById(channel.getId()).orElseThrow();
        assertThat(updatedChannel.getIsStale(), is(false));
    }

    @Test
    @DisplayName("Should NOT clear stale flag for channels with old activity")
    public void shouldNotClearStaleFlagForChannelsWithOldActivity() {
        // Given: User with a correctly stale channel
        final User user = aValidatedUser();
        final Channel channel = aChannelForUser(user, "Still Stale Channel");

        // And: Channel is stale and has old activity
        channel.setLastEventAt(now().minusDays(10)); // Old activity (beyond 7 days)
        channel.setIsStale(true);
        channelRepository.save(channel);

        // And: Threshold is 7 days ago
        final OffsetDateTime threshold = now().minusDays(7);

        // When: Clearing stale for active channels
        final int clearedCount = channelRepository.clearStaleForActiveChannels(threshold);

        // Then: No channels should be cleared
        assertThat(clearedCount, is(0));

        // And: Channel should remain stale
        final Channel updatedChannel = channelRepository.findById(channel.getId()).orElseThrow();
        assertThat(updatedChannel.getIsStale(), is(true));
    }

    @Test
    @DisplayName("Should NOT clear stale flag for channels that are not stale")
    public void shouldNotClearStaleFlagForChannelsThatAreNotStale() {
        // Given: User with an active (non-stale) channel
        final User user = aValidatedUser();
        final Channel channel = aChannelForUser(user, "Active Channel");

        // And: Channel is not stale and has recent activity
        channel.setLastEventAt(now().minusDays(3));
        channel.setIsStale(false);
        channelRepository.save(channel);

        // And: Threshold is 7 days ago
        final OffsetDateTime threshold = now().minusDays(7);

        // When: Clearing stale for active channels
        final int clearedCount = channelRepository.clearStaleForActiveChannels(threshold);

        // Then: No channels should be cleared (already not stale)
        assertThat(clearedCount, is(0));
    }
}
