package io.github.eventify.api.subscription.job;

import io.github.eventify.api.channel.model.Channel;
import io.github.eventify.api.channel.repository.ChannelRepository;
import io.github.eventify.api.event.model.Severity;
import io.github.eventify.api.notification.model.NotificationAudience;
import io.github.eventify.api.notification.model.NotificationCategory;
import io.github.eventify.api.notification.model.NotificationPayload;
import io.github.eventify.api.notification.service.NotificationDispatchService;
import io.github.eventify.api.organization.model.Organization;
import io.github.eventify.api.organization.model.OrganizationStatus;
import io.github.eventify.api.organization.repository.OrganizationRepository;
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

    @Mock
    private OrganizationRepository organizationRepository;

    private SeverityTransitionJob severityTransitionJob;

    @BeforeEach
    public void setUp() {
        severityTransitionJob = new SeverityTransitionJob(
            channelRepository,
            watchlistRepository,
            subscriptionRepository,
            notificationDispatchService,
            organizationRepository
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

    // ========================= Org status filtering =========================

    @Test
    @DisplayName("Should skip channel belonging to SUSPENDED organization")
    public void shouldSkipChannelBelongingToSuspendedOrganization() {
        // Given: A channel owned by a SUSPENDED organization
        final User channelOwner = aValidUser();
        channelOwner.setId(1L);
        final Organization suspendedOrg = anOrganizationWithStatus(100L, OrganizationStatus.SUSPENDED);
        final Channel channel = aChannelWithSeverityChange(10L, "Org Channel", channelOwner, Severity.CRITICAL, Severity.OK);
        channel.setOrganization(suspendedOrg);

        given(channelRepository.findChannelsWithSeverityChange()).willReturn(List.of(channel));
        given(organizationRepository.findAllById(List.of(100L))).willReturn(List.of(suspendedOrg));

        // When: Job executes
        severityTransitionJob.processSeverityTransitions();

        // Then: No notifications dispatched and watchlist/subscription repos not queried
        verifyNoInteractions(notificationDispatchService);
        verifyNoInteractions(watchlistRepository);
        verifyNoInteractions(subscriptionRepository);
    }

    @Test
    @DisplayName("Should process channel belonging to ACTIVE organization normally")
    public void shouldProcessChannelBelongingToActiveOrganizationNormally() {
        // Given: A channel owned by an ACTIVE organization
        final User channelOwner = aValidUser();
        channelOwner.setId(1L);
        final Organization activeOrg = anOrganizationWithStatus(100L, OrganizationStatus.ACTIVE);
        final Channel channel = aChannelWithSeverityChange(10L, "Org Channel", channelOwner, Severity.CRITICAL, Severity.OK);
        channel.setOrganization(activeOrg);

        given(channelRepository.findChannelsWithSeverityChange()).willReturn(List.of(channel));
        given(organizationRepository.findAllById(List.of(100L))).willReturn(List.of(activeOrg));

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

        // Then: Notification is dispatched
        verify(notificationDispatchService).dispatch(any(NotificationAudience.class), any(NotificationPayload.class));
    }

    @Test
    @DisplayName("Should process personal channel (null organization) normally")
    public void shouldProcessPersonalChannelWithNullOrganizationNormally() {
        // Given: A personal channel with no organization
        final User channelOwner = aValidUser();
        channelOwner.setId(1L);
        final Channel channel = aChannelWithSeverityChange(10L, "Personal Channel", channelOwner, Severity.CRITICAL, Severity.OK);
        // channel.organization is null by default

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

        // Then: Notification is dispatched (personal channels are never filtered)
        verify(notificationDispatchService).dispatch(any(NotificationAudience.class), any(NotificationPayload.class));
    }

    @Test
    @DisplayName("Should look up org statuses in batch — repository called once with all org IDs")
    public void shouldLookUpOrgStatusesInBatchWithSingleRepositoryCall() {
        // Given: Three channels — two from different orgs, one personal
        final User channelOwner = aValidUser();
        channelOwner.setId(1L);

        final Organization orgA = anOrganizationWithStatus(100L, OrganizationStatus.ACTIVE);
        final Organization orgB = anOrganizationWithStatus(200L, OrganizationStatus.ACTIVE);

        final Channel channelWithOrgA = aChannelWithSeverityChange(10L, "Channel A", channelOwner, Severity.WARNING, Severity.OK);
        channelWithOrgA.setOrganization(orgA);

        final Channel channelWithOrgB = aChannelWithSeverityChange(11L, "Channel B", channelOwner, Severity.WARNING, Severity.OK);
        channelWithOrgB.setOrganization(orgB);

        final Channel personalChannel = aChannelWithSeverityChange(12L, "Personal", channelOwner, Severity.WARNING, Severity.OK);

        given(channelRepository.findChannelsWithSeverityChange())
            .willReturn(List.of(channelWithOrgA, channelWithOrgB, personalChannel));
        given(organizationRepository.findAllById(any())).willReturn(List.of(orgA, orgB));
        given(watchlistRepository.findWatchlistsContainingChannel(any())).willReturn(List.of());

        // When: Job executes
        severityTransitionJob.processSeverityTransitions();

        // Then: Organization repository is called exactly once (batch lookup, not per-channel)
        verify(organizationRepository, times(1)).findAllById(any());
    }

    @Test
    @DisplayName("Should dispatch zero notifications when all channels belong to suspended orgs")
    public void shouldDispatchZeroNotificationsWhenAllChannelsBelongToSuspendedOrgs() {
        // Given: Two channels both belonging to suspended organizations
        final User channelOwner = aValidUser();
        channelOwner.setId(1L);

        final Organization suspendedOrg1 = anOrganizationWithStatus(100L, OrganizationStatus.SUSPENDED);
        final Organization suspendedOrg2 = anOrganizationWithStatus(200L, OrganizationStatus.SUSPENDED);

        final Channel channel1 = aChannelWithSeverityChange(10L, "Channel 1", channelOwner, Severity.CRITICAL, Severity.OK);
        channel1.setOrganization(suspendedOrg1);

        final Channel channel2 = aChannelWithSeverityChange(11L, "Channel 2", channelOwner, Severity.WARNING, Severity.OK);
        channel2.setOrganization(suspendedOrg2);

        given(channelRepository.findChannelsWithSeverityChange()).willReturn(List.of(channel1, channel2));
        given(organizationRepository.findAllById(any())).willReturn(List.of(suspendedOrg1, suspendedOrg2));

        // When: Job executes
        severityTransitionJob.processSeverityTransitions();

        // Then: Zero notifications dispatched
        verifyNoInteractions(notificationDispatchService);
        verifyNoInteractions(watchlistRepository);
        verifyNoInteractions(subscriptionRepository);
    }

    @Test
    @DisplayName("Should process only active-org channels when batch contains mix of suspended and active")
    public void shouldProcessOnlyActiveOrgChannelsInMixedBatch() {
        // Given: One channel from a suspended org and one from an active org
        final User channelOwner = aValidUser();
        channelOwner.setId(1L);

        final Organization suspendedOrg = anOrganizationWithStatus(100L, OrganizationStatus.SUSPENDED);
        final Organization activeOrg = anOrganizationWithStatus(200L, OrganizationStatus.ACTIVE);

        final Channel suspendedOrgChannel = aChannelWithSeverityChange(
            10L,
            "Suspended Org Channel",
            channelOwner,
            Severity.CRITICAL,
            Severity.OK
        );
        suspendedOrgChannel.setOrganization(suspendedOrg);

        final Channel activeOrgChannel = aChannelWithSeverityChange(
            11L,
            "Active Org Channel",
            channelOwner,
            Severity.CRITICAL,
            Severity.OK
        );
        activeOrgChannel.setOrganization(activeOrg);

        given(channelRepository.findChannelsWithSeverityChange()).willReturn(List.of(suspendedOrgChannel, activeOrgChannel));
        given(organizationRepository.findAllById(any())).willReturn(List.of(suspendedOrg, activeOrg));

        final Watchlist watchlist = aWatchlist(20L, "My Watchlist", channelOwner);
        given(watchlistRepository.findWatchlistsContainingChannel(activeOrgChannel.getId()))
            .willReturn(List.of(watchlist));

        final User subscriber = aValidUser();
        subscriber.setId(2L);
        final Subscription sub = aSubscription(1L, watchlist, subscriber, List.of("CRITICAL"), List.of("IN_APP"));
        given(subscriptionRepository.findByWatchlistIdAndTargetSeveritiesContaining(watchlist.getId(), "CRITICAL"))
            .willReturn(List.of(sub));

        // When: Job executes
        severityTransitionJob.processSeverityTransitions();

        // Then: Only one notification dispatched (for the active org channel)
        verify(notificationDispatchService, times(1)).dispatch(any(NotificationAudience.class), any(NotificationPayload.class));

        // And: Suspended org channel's watchlists are never queried
        verify(watchlistRepository, never()).findWatchlistsContainingChannel(suspendedOrgChannel.getId());
    }

    @Test
    @DisplayName("Should skip channel when org status is null (defensive — treats as suspended)")
    public void shouldSkipChannelWhenOrgStatusIsNull() {
        // Given: A channel whose organization has a null status
        final User channelOwner = aValidUser();
        channelOwner.setId(1L);

        final Organization orgWithNullStatus = anOrganizationWithStatus(100L, null);
        final Channel channel = aChannelWithSeverityChange(10L, "Org Channel", channelOwner, Severity.CRITICAL, Severity.OK);
        channel.setOrganization(orgWithNullStatus);

        given(channelRepository.findChannelsWithSeverityChange()).willReturn(List.of(channel));
        given(organizationRepository.findAllById(List.of(100L))).willReturn(List.of(orgWithNullStatus));

        // When: Job executes
        severityTransitionJob.processSeverityTransitions();

        // Then: Channel is skipped — no notifications dispatched
        verifyNoInteractions(notificationDispatchService);
        verifyNoInteractions(watchlistRepository);
        verifyNoInteractions(subscriptionRepository);
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

    private static Organization anOrganizationWithStatus(final Long id, final OrganizationStatus status) {
        final Organization org = new Organization();
        org.setId(id);
        org.setName("Test Org " + id);
        org.setSlug("test-org-" + id);
        org.setStatus(status);
        return org;
    }
}
