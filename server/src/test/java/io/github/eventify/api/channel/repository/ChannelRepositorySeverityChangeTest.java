package io.github.eventify.api.channel.repository;

import io.github.eventify.api.channel.model.Channel;
import io.github.eventify.api.event.model.Severity;
import io.github.eventify.api.user.model.User;
import io.github.eventify.support.IntegrationTest;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Integration Test - Channel Repository - findChannelsWithSeverityChange
 *
 * <p>Tests the JPQL query that detects channels whose severity has changed
 * since the last notification. Covers the NULL lastNotifiedSeverity case
 * (never notified) which is a known bug in the current query.</p>
 */
@DisplayName("Integration Test - Channel Repository Severity Change")
public class ChannelRepositorySeverityChangeTest extends IntegrationTest {

    @Test
    @DisplayName("Should return channel when currentSeverity is set and lastNotifiedSeverity is NULL (never notified)")
    public void shouldReturnChannelWhenCurrentSeveritySetAndLastNotifiedSeverityIsNull() {
        // Given: A user with a channel that has a severity but was never notified
        final User user = aValidatedUser();
        final Channel channel = aChannelForUser(user, "Never Notified Channel");

        // And: Channel has a currentSeverity but lastNotifiedSeverity is NULL
        channel.setCurrentSeverity(Severity.CRITICAL);
        channel.setLastNotifiedSeverity(null);
        channelRepository.save(channel);

        // When: Querying for channels with severity change
        final List<Channel> result = channelRepository.findChannelsWithSeverityChange();

        // Then: The channel should be returned (NULL lastNotifiedSeverity means never notified)
        final List<Long> resultIds = result.stream().map(Channel::getId).toList();
        assertThat(resultIds, hasItem(channel.getId()));
    }

    @Test
    @DisplayName("Should return channel when currentSeverity differs from lastNotifiedSeverity")
    public void shouldReturnChannelWhenCurrentSeverityDiffersFromLastNotifiedSeverity() {
        // Given: A user with a channel whose severity has changed since last notification
        final User user = aValidatedUser();
        final Channel channel = aChannelForUser(user, "Changed Severity Channel");

        // And: Channel has currentSeverity=CRITICAL but was last notified at WARNING
        channel.setCurrentSeverity(Severity.CRITICAL);
        channel.setLastNotifiedSeverity(Severity.WARNING);
        channelRepository.save(channel);

        // When: Querying for channels with severity change
        final List<Channel> result = channelRepository.findChannelsWithSeverityChange();

        // Then: The channel should be returned (severity changed)
        final List<Long> resultIds = result.stream().map(Channel::getId).toList();
        assertThat(resultIds, hasItem(channel.getId()));
    }

    @Test
    @DisplayName("Should NOT return channel when currentSeverity equals lastNotifiedSeverity")
    public void shouldNotReturnChannelWhenCurrentSeverityEqualsLastNotifiedSeverity() {
        // Given: A user with a channel whose severity has NOT changed since last notification
        final User user = aValidatedUser();
        final Channel channel = aChannelForUser(user, "Unchanged Severity Channel");

        // And: Channel has currentSeverity=CRITICAL and was last notified at CRITICAL (no change)
        channel.setCurrentSeverity(Severity.CRITICAL);
        channel.setLastNotifiedSeverity(Severity.CRITICAL);
        channelRepository.save(channel);

        // When: Querying for channels with severity change
        final List<Channel> result = channelRepository.findChannelsWithSeverityChange();

        // Then: The channel should NOT be returned (no change to notify)
        final List<Long> resultIds = result.stream().map(Channel::getId).toList();
        assertThat(resultIds, not(hasItem(channel.getId())));
    }

    @Test
    @DisplayName("Should NOT return channel when currentSeverity is NULL")
    public void shouldNotReturnChannelWhenCurrentSeverityIsNull() {
        // Given: A user with a channel that has no current severity
        final User user = aValidatedUser();
        final Channel channel = aChannelForUser(user, "No Severity Channel");

        // And: Channel has no currentSeverity (no active alert)
        channel.setCurrentSeverity(null);
        channel.setLastNotifiedSeverity(null);
        channelRepository.save(channel);

        // When: Querying for channels with severity change
        final List<Channel> result = channelRepository.findChannelsWithSeverityChange();

        // Then: The channel should NOT be returned (no active severity)
        final List<Long> resultIds = result.stream().map(Channel::getId).toList();
        assertThat(resultIds, not(hasItem(channel.getId())));
    }

    @Test
    @DisplayName("Should return only channels with severity changes among mixed set")
    public void shouldReturnOnlyChannelsWithSeverityChangesAmongMixedSet() {
        // Given: A user with multiple channels in different severity states
        final User user = aValidatedUser();

        // And: Channel 1 - never notified (NULL lastNotifiedSeverity), should be returned
        final Channel neverNotified = aChannelForUser(user, "Never Notified");
        neverNotified.setCurrentSeverity(Severity.CRITICAL);
        neverNotified.setLastNotifiedSeverity(null);
        channelRepository.save(neverNotified);

        // And: Channel 2 - severity changed from WARNING to CRITICAL, should be returned
        final Channel severityChanged = aChannelForUser(user, "Severity Changed");
        severityChanged.setCurrentSeverity(Severity.CRITICAL);
        severityChanged.setLastNotifiedSeverity(Severity.WARNING);
        channelRepository.save(severityChanged);

        // And: Channel 3 - severity unchanged (CRITICAL == CRITICAL), should NOT be returned
        final Channel severityUnchanged = aChannelForUser(user, "Severity Unchanged");
        severityUnchanged.setCurrentSeverity(Severity.CRITICAL);
        severityUnchanged.setLastNotifiedSeverity(Severity.CRITICAL);
        channelRepository.save(severityUnchanged);

        // And: Channel 4 - no current severity, should NOT be returned
        final Channel noSeverity = aChannelForUser(user, "No Severity");
        noSeverity.setCurrentSeverity(null);
        noSeverity.setLastNotifiedSeverity(null);
        channelRepository.save(noSeverity);

        // When: Querying for channels with severity change
        final List<Channel> result = channelRepository.findChannelsWithSeverityChange();

        // Then: Only the two channels with changes should be returned
        final List<Long> resultIds = result.stream().map(Channel::getId).toList();
        assertThat(resultIds, hasItem(neverNotified.getId()));
        assertThat(resultIds, hasItem(severityChanged.getId()));
        assertThat(resultIds, not(hasItem(severityUnchanged.getId())));
        assertThat(resultIds, not(hasItem(noSeverity.getId())));
    }
}
