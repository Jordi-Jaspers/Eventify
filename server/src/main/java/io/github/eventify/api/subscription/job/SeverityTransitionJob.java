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
import io.github.eventify.api.watchlist.model.Watchlist;
import io.github.eventify.api.watchlist.repository.WatchlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Scheduled job that detects severity transitions on channels and dispatches notifications to watchlist subscribers whose target severities
 * match the new severity level. Runs every 60 seconds and updates {@code lastNotifiedSeverity} after processing each channel.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SeverityTransitionJob {

    private final ChannelRepository channelRepository;
    private final WatchlistRepository watchlistRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final NotificationDispatchService notificationDispatchService;
    private final OrganizationRepository organizationRepository;

    /**
     * Processes severity transitions and dispatches notifications. Runs every 60 seconds.
     */
    @Scheduled(
        fixedDelay = 60,
        timeUnit = TimeUnit.SECONDS
    )
    @Transactional
    public void processSeverityTransitions() {
        log.debug("[CRON JOB] Severity Transition job has been started at {}", OffsetDateTime.now());
        final List<Channel> channels = channelRepository.findChannelsWithSeverityChange();
        if (channels.isEmpty()) {
            return;
        }

        log.info("[CRON JOB] Processing severity transitions for '{}' channel(s)", channels.size());
        final List<Long> orgIds = channels.stream()
            .map(Channel::getOrganization)
            .filter(Objects::nonNull)
            .map(Organization::getId)
            .distinct()
            .collect(Collectors.toList());

        final Map<Long, OrganizationStatus> orgStatusMap;
        if (orgIds.isEmpty()) {
            orgStatusMap = Map.of();
        } else {
            final HashMap<Long, OrganizationStatus> statusMap = new HashMap<>();
            organizationRepository.findAllById(orgIds).forEach(org -> statusMap.put(org.getId(), org.getStatus()));
            orgStatusMap = statusMap;
        }

        final long notificationCount = channels.stream()
            .filter(channel -> isChannelAllowed(channel, orgStatusMap))
            .mapToLong(this::processChannelTransition)
            .sum();

        log.info("Severity transition job complete — channels processed: {}, notifications sent: {}", channels.size(), notificationCount);
    }

    private boolean isChannelAllowed(final Channel channel, final Map<Long, OrganizationStatus> orgStatusMap) {
        final Organization org = channel.getOrganization();
        final boolean allowed = org == null || OrganizationStatus.ACTIVE.equals(orgStatusMap.get(org.getId()));
        if (!allowed) {
            log.debug(
                "Skipping channel {} — organization {} has status {}",
                channel.getId(),
                org.getId(),
                orgStatusMap.get(org.getId())
            );
        }
        return allowed;
    }

    private long processChannelTransition(final Channel channel) {
        final Severity newSeverity = channel.getCurrentSeverity();
        final List<Watchlist> watchlists = watchlistRepository.findWatchlistsContainingChannel(channel.getId());

        final long notificationCount = watchlists.stream()
            .mapToLong(watchlist -> notifyWatchlistSubscribers(channel, watchlist, newSeverity))
            .sum();

        channel.setLastNotifiedSeverity(newSeverity);
        channelRepository.save(channel);
        return notificationCount;
    }

    private long notifyWatchlistSubscribers(final Channel channel, final Watchlist watchlist, final Severity severity) {
        final List<Subscription> subscriptions = subscriptionRepository
            .findByWatchlistIdAndTargetSeveritiesContaining(watchlist.getId(), severity.name());

        subscriptions.forEach(subscription -> dispatchNotification(channel, watchlist, severity, subscription));
        return subscriptions.size();
    }

    private void dispatchNotification(final Channel channel,
        final Watchlist watchlist,
        final Severity severity,
        final Subscription subscription) {
        final NotificationAudience audience = NotificationAudience.user(subscription.getUser().getId());
        final NotificationPayload payload = buildPayload(channel, watchlist, severity);
        notificationDispatchService.dispatch(audience, payload);
    }

    private NotificationPayload buildPayload(final Channel channel, final Watchlist watchlist, final Severity severity) {
        return new NotificationPayload(
            NotificationCategory.ALERT,
            "Severity change: " + channel.getName(),
            "Channel '" + channel.getName() + "' transitioned to " + severity.name() + " in watchlist '" + watchlist.getName() + "'",
            "/watchlists/" + watchlist.getId(),
            "View Watchlist",
            Severity.CRITICAL.equals(severity),
            null
        );
    }
}
