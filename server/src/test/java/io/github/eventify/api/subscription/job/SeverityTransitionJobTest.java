package io.github.eventify.api.subscription.job;

import io.github.eventify.api.channel.model.Channel;
import io.github.eventify.api.channel.repository.ChannelRepository;
import io.github.eventify.api.event.model.Severity;
import io.github.eventify.api.notification.model.NotificationAudience;
import io.github.eventify.api.notification.model.NotificationCategory;
import io.github.eventify.api.notification.model.NotificationPayload;
import io.github.eventify.api.notification.service.NotificationDispatchService;
import io.github.eventify.api.subscription.model.Subscription;
import io.github.eventify.api.subscription.repository.SubscriptionRepository;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.watchlist.model.Watchlist;
import io.github.eventify.api.watchlist.repository.WatchlistRepository;
import io.github.eventify.support.UnitTest;

import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@DisplayName("Unit Test - Severity Transition Job")
public class SeverityTransitionJobTest extends UnitTest {

    @Mock
    private ChannelRepository channelRepository;

    @Mock
    private WatchlistRepository watchlistRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private NotificationDispatchService notificationDispatchService;

    private SeverityTransitionJob severityTransitionJob;

    @BeforeEach
    public void setUp() {
        severityTransitionJob = new SeverityTransitionJob(
            channelRepository,
            watchlistRepository,
            subscriptionRepository,
            notificationDispatchService
        );
    }

    // ========================= No changes =========================

    @Test
    @DisplayName("Should do nothing when no severity changes detected")
    public void shouldDoNothingWhenNoSeverityChangesDetected() {
        // Given: No channels with severity changes
        given(channelRepository.findChannelsWithSeverityChange()).willReturn(List.of());

        // When: Job executes
        severityTransitionJob.processSeverityTransitions();

        // Then: No notifications dispatched
        verifyNoInteractions(notificationDispatchService);
        verifyNoInteractions(subscriptionRepository);
    }

    // ========================= Notification dispatch =========================

    @Test
    @DisplayName("Should dispatch notification for each subscriber when severity changes")
    public void shouldDispatchNotificationForEachSubscriberWhenSeverityChanges() {
        // Given: A channel with a severity change to CRITICAL
        final User channelOwner = aValidUser();
        channelOwner.setId(1L);
        final Channel channel = aChannelWithSeverityChange(10L, "Production API", channelOwner, Severity.CRITICAL, Severity.OK);

        given(channelRepository.findChannelsWithSeverityChange()).willReturn(List.of(channel));

        // And: A watchlist containing that channel
        final Watchlist watchlist = aWatchlist(20L, "My Watchlist", channelOwner);
        given(watchlistRepository.findWatchlistsContainingChannel(channel.getId()))
            .willReturn(List.of(watchlist));

        // And: Two subscribers on that watchlist who want CRITICAL notifications
        final User subscriber1 = aValidUser();
        subscriber1.setId(2L);
        final User subscriber2 = aValidUser();
        subscriber2.setId(3L);

        final Subscription sub1 = aSubscription(1L, watchlist, subscriber1, List.of("CRITICAL"), List.of("IN_APP"));
        final Subscription sub2 = aSubscription(2L, watchlist, subscriber2, List.of("CRITICAL", "WARNING"), List.of("IN_APP"));

        given(subscriptionRepository.findByWatchlistIdAndTargetSeveritiesContaining(watchlist.getId(), "CRITICAL"))
            .willReturn(List.of(sub1, sub2));

        // When: Job executes
        severityTransitionJob.processSeverityTransitions();

        // Then: Two notifications should be dispatched
        verify(notificationDispatchService, times(2)).dispatch(any(NotificationAudience.class), any(NotificationPayload.class));
    }

    @Test
    @DisplayName("Should dispatch urgent notification for CRITICAL severity")
    public void shouldDispatchUrgentNotificationForCriticalSeverity() {
        // Given: A channel transitioning to CRITICAL
        final User channelOwner = aValidUser();
        channelOwner.setId(1L);
        final Channel channel = aChannelWithSeverityChange(10L, "Production API", channelOwner, Severity.CRITICAL, Severity.OK);

        given(channelRepository.findChannelsWithSeverityChange()).willReturn(List.of(channel));

        final Watchlist watchlist = aWatchlist(20L, "My Watchlist", channelOwner);
        given(watchlistRepository.findWatchlistsContainingChannel(channel.getId()))
            .willReturn(List.of(watchlist));

        final User subscriber = aValidUser();
        subscriber.setId(2L);
        final Subscription sub = aSubscription(1L, watchlist, subscriber, List.of("CRITICAL"), List.of("IN_APP"));

        given(subscriptionRepository.findByWatchlistIdAndTargetSeveritiesContaining(watchlist.getId(), "CRITICAL"))
            .willReturn(List.of(sub));

        // When: Job executes
        severityTransitionJob.processSeverityTransitions();

        // Then: Notification should be urgent and ALERT category
        final ArgumentCaptor<NotificationPayload> payloadCaptor = ArgumentCaptor.forClass(NotificationPayload.class);
        verify(notificationDispatchService).dispatch(any(NotificationAudience.class), payloadCaptor.capture());

        final NotificationPayload payload = payloadCaptor.getValue();
        assertThat(payload.isUrgent(), is(true));
        assertThat(payload.getCategory(), is(NotificationCategory.ALERT));
    }

    @Test
    @DisplayName("Should dispatch non-urgent notification for WARNING severity")
    public void shouldDispatchNonUrgentNotificationForWarningSeverity() {
        // Given: A channel transitioning to WARNING
        final User channelOwner = aValidUser();
        channelOwner.setId(1L);
        final Channel channel = aChannelWithSeverityChange(10L, "Production API", channelOwner, Severity.WARNING, Severity.OK);

        given(channelRepository.findChannelsWithSeverityChange()).willReturn(List.of(channel));

        final Watchlist watchlist = aWatchlist(20L, "My Watchlist", channelOwner);
        given(watchlistRepository.findWatchlistsContainingChannel(channel.getId()))
            .willReturn(List.of(watchlist));

        final User subscriber = aValidUser();
        subscriber.setId(2L);
        final Subscription sub = aSubscription(1L, watchlist, subscriber, List.of("WARNING"), List.of("IN_APP"));

        given(subscriptionRepository.findByWatchlistIdAndTargetSeveritiesContaining(watchlist.getId(), "WARNING"))
            .willReturn(List.of(sub));

        // When: Job executes
        severityTransitionJob.processSeverityTransitions();

        // Then: Notification should NOT be urgent
        final ArgumentCaptor<NotificationPayload> payloadCaptor = ArgumentCaptor.forClass(NotificationPayload.class);
        verify(notificationDispatchService).dispatch(any(NotificationAudience.class), payloadCaptor.capture());

        final NotificationPayload payload = payloadCaptor.getValue();
        assertThat(payload.isUrgent(), is(false));
        assertThat(payload.getCategory(), is(NotificationCategory.ALERT));
    }

    @Test
    @DisplayName("Should target correct user audience when dispatching notification")
    public void shouldTargetCorrectUserAudienceWhenDispatchingNotification() {
        // Given: A channel with severity change
        final User channelOwner = aValidUser();
        channelOwner.setId(1L);
        final Channel channel = aChannelWithSeverityChange(10L, "Production API", channelOwner, Severity.CRITICAL, Severity.WARNING);

        given(channelRepository.findChannelsWithSeverityChange()).willReturn(List.of(channel));

        final Watchlist watchlist = aWatchlist(20L, "My Watchlist", channelOwner);
        given(watchlistRepository.findWatchlistsContainingChannel(channel.getId()))
            .willReturn(List.of(watchlist));

        final User subscriber = aValidUser();
        subscriber.setId(99L);
        final Subscription sub = aSubscription(1L, watchlist, subscriber, List.of("CRITICAL"), List.of("IN_APP"));

        given(subscriptionRepository.findByWatchlistIdAndTargetSeveritiesContaining(watchlist.getId(), "CRITICAL"))
            .willReturn(List.of(sub));

        // When: Job executes
        severityTransitionJob.processSeverityTransitions();

        // Then: Notification should target subscriber's user ID
        final ArgumentCaptor<NotificationAudience> audienceCaptor = ArgumentCaptor.forClass(NotificationAudience.class);
        verify(notificationDispatchService).dispatch(audienceCaptor.capture(), any(NotificationPayload.class));

        final NotificationAudience audience = audienceCaptor.getValue();
        assertThat(audience.getUserId(), is(99L));
    }

    @Test
    @DisplayName("Should update lastNotifiedSeverity after notification sent")
    public void shouldUpdateLastNotifiedSeverityAfterNotificationSent() {
        // Given: A channel with severity change
        final User channelOwner = aValidUser();
        channelOwner.setId(1L);
        final Channel channel = aChannelWithSeverityChange(10L, "Production API", channelOwner, Severity.CRITICAL, Severity.OK);

        given(channelRepository.findChannelsWithSeverityChange()).willReturn(List.of(channel));

        final Watchlist watchlist = aWatchlist(20L, "My Watchlist", channelOwner);
        given(watchlistRepository.findWatchlistsContainingChannel(channel.getId()))
            .willReturn(List.of(watchlist));

        final User subscriber = aValidUser();
        subscriber.setId(2L);
        final Subscription sub = aSubscription(1L, watchlist, subscriber, List.of("CRITICAL"), List.of("IN_APP"));

        given(subscriptionRepository.findByWatchlistIdAndTargetSeveritiesContaining(watchlist.getId(), "CRITICAL"))
            .willReturn(List.of(sub));

        // When: Job executes
        severityTransitionJob.processSeverityTransitions();

        // Then: Channel's lastNotifiedSeverity should be updated to match currentSeverity
        verify(channelRepository).save(channel);
        assertThat(channel.getLastNotifiedSeverity(), is(Severity.CRITICAL));
    }

    @Test
    @DisplayName("Should notify subscribers of each watchlist independently when channel is in multiple watchlists")
    public void shouldNotifySubscribersOfEachWatchlistIndependentlyWhenChannelInMultipleWatchlists() {
        // Given: A channel with severity change
        final User channelOwner = aValidUser();
        channelOwner.setId(1L);
        final Channel channel = aChannelWithSeverityChange(10L, "Shared Channel", channelOwner, Severity.CRITICAL, Severity.OK);

        given(channelRepository.findChannelsWithSeverityChange()).willReturn(List.of(channel));

        // And: Channel is in two different watchlists
        final Watchlist watchlist1 = aWatchlist(20L, "Watchlist A", channelOwner);
        final Watchlist watchlist2 = aWatchlist(21L, "Watchlist B", channelOwner);
        given(watchlistRepository.findWatchlistsContainingChannel(channel.getId()))
            .willReturn(List.of(watchlist1, watchlist2));

        // And: Each watchlist has one subscriber
        final User subscriber1 = aValidUser();
        subscriber1.setId(2L);
        final User subscriber2 = aValidUser();
        subscriber2.setId(3L);

        final Subscription sub1 = aSubscription(1L, watchlist1, subscriber1, List.of("CRITICAL"), List.of("IN_APP"));
        final Subscription sub2 = aSubscription(2L, watchlist2, subscriber2, List.of("CRITICAL"), List.of("IN_APP"));

        given(subscriptionRepository.findByWatchlistIdAndTargetSeveritiesContaining(watchlist1.getId(), "CRITICAL"))
            .willReturn(List.of(sub1));
        given(subscriptionRepository.findByWatchlistIdAndTargetSeveritiesContaining(watchlist2.getId(), "CRITICAL"))
            .willReturn(List.of(sub2));

        // When: Job executes
        severityTransitionJob.processSeverityTransitions();

        // Then: Both subscribers should receive notifications
        verify(notificationDispatchService, times(2)).dispatch(any(NotificationAudience.class), any(NotificationPayload.class));
    }

    @Test
    @DisplayName("Should not notify subscribers whose targetSeverities do not match new severity")
    public void shouldNotNotifySubscribersWhoseTargetSeveritiesDoNotMatchNewSeverity() {
        // Given: A channel transitioning to WARNING
        final User channelOwner = aValidUser();
        channelOwner.setId(1L);
        final Channel channel = aChannelWithSeverityChange(10L, "Production API", channelOwner, Severity.WARNING, Severity.OK);

        given(channelRepository.findChannelsWithSeverityChange()).willReturn(List.of(channel));

        final Watchlist watchlist = aWatchlist(20L, "My Watchlist", channelOwner);
        given(watchlistRepository.findWatchlistsContainingChannel(channel.getId()))
            .willReturn(List.of(watchlist));

        // And: No subscribers want WARNING notifications
        given(subscriptionRepository.findByWatchlistIdAndTargetSeveritiesContaining(watchlist.getId(), "WARNING"))
            .willReturn(List.of());

        // When: Job executes
        severityTransitionJob.processSeverityTransitions();

        // Then: No notifications dispatched
        verifyNoInteractions(notificationDispatchService);
    }

    // ========================= FACTORY METHODS =========================

    private Channel aChannelWithSeverityChange(
        final Long id,
        final String name,
        final User user,
        final Severity currentSeverity,
        final Severity lastNotifiedSeverity
    ) {
        final Channel channel = aChannel(id, name, user);
        channel.setCurrentSeverity(currentSeverity);
        channel.setLastNotifiedSeverity(lastNotifiedSeverity);
        return channel;
    }

    private static Subscription aSubscription(
        final Long id,
        final Watchlist watchlist,
        final User user,
        final List<String> targetSeverities,
        final List<String> adapters
    ) {
        final Subscription subscription = new Subscription();
        subscription.setId(id);
        subscription.setWatchlist(watchlist);
        subscription.setUser(user);
        subscription.setTargetSeverities(targetSeverities);
        subscription.setAdapters(adapters);
        subscription.setCreatedAt(OffsetDateTime.now().minusDays(1));
        return subscription;
    }
}
