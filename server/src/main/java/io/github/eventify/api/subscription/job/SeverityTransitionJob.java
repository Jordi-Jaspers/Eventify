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
import io.github.eventify.api.watchlist.model.Watchlist;
import io.github.eventify.api.watchlist.repository.WatchlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Scheduled job that detects severity transitions on channels and dispatches notifications
 * to watchlist subscribers whose target severities match the new severity level.
 * Runs every 60 seconds and updates {@code lastNotifiedSeverity} after processing each channel.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SeverityTransitionJob {

    private final ChannelRepository channelRepository;
    private final WatchlistRepository watchlistRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final NotificationDispatchService notificationDispatchService;

    /**
     * Processes severity transitions and dispatches notifications.
     * Runs every 60 seconds.
     */
    @Scheduled(
        fixedDelay = 60,
        timeUnit = TimeUnit.SECONDS
    )
    @Transactional
    public void processSeverityTransitions() {
        final List<Channel> channels = channelRepository.findChannelsWithSeverityChange();

        if (channels.isEmpty()) {
            return;
        }

        log.info("Processing severity transitions for {} channel(s)", channels.size());

        final long notificationCount = channels.stream()
            .mapToLong(this::processChannelTransition)
            .sum();

        log.info(
            "Severity transition job complete — channels processed: {}, notifications sent: {}",
            channels.size(),
            notificationCount
        );
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

    private long notifyWatchlistSubscribers(
        final Channel channel,
        final Watchlist watchlist,
        final Severity severity
    ) {
        final List<Subscription> subscriptions = subscriptionRepository
            .findByWatchlistIdAndTargetSeveritiesContaining(watchlist.getId(), severity.name());

        subscriptions.forEach(subscription -> dispatchNotification(channel, watchlist, severity, subscription));

        return subscriptions.size();
    }

    private void dispatchNotification(
        final Channel channel,
        final Watchlist watchlist,
        final Severity severity,
        final Subscription subscription
    ) {
        final NotificationAudience audience = NotificationAudience.user(subscription.getUser().getId());
        final NotificationPayload payload = buildPayload(channel, watchlist, severity);
        notificationDispatchService.dispatch(audience, payload);
    }

    private NotificationPayload buildPayload(
        final Channel channel,
        final Watchlist watchlist,
        final Severity severity
    ) {
        final boolean urgent = Severity.CRITICAL.equals(severity);
        final String title = "Severity change: " + channel.getName();
        final String message = "Channel '" + channel.getName() + "' transitioned to " + severity.name()
            + " in watchlist '" + watchlist.getName() + "'";
        final String actionUrl = "/watchlists/" + watchlist.getId();
        final String actionLabel = "View Watchlist";

        return new NotificationPayload(
            NotificationCategory.ALERT,
            title,
            message,
            actionUrl,
            actionLabel,
            urgent,
            null
        );
    }
}
