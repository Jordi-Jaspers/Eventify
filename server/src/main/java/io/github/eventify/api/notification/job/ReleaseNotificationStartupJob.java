package io.github.eventify.api.notification.job;

import io.github.eventify.api.changelog.model.ChangelogEntry;
import io.github.eventify.api.changelog.service.ChangelogService;
import io.github.eventify.api.notification.model.NotificationAudienceType;
import io.github.eventify.api.notification.model.NotificationCategory;
import io.github.eventify.api.notification.model.request.AudienceRequest;
import io.github.eventify.api.notification.model.request.CreateBroadcastRequest;
import io.github.eventify.api.notification.repository.NotificationBroadcastRepository;
import io.github.eventify.api.notification.service.NotificationBroadcastService;
import io.github.jframe.autoconfigure.properties.ApplicationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Dispatches a release notification broadcast on application startup.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReleaseNotificationStartupJob {

    private final ChangelogService changelogService;
    private final NotificationBroadcastService broadcastService;
    private final NotificationBroadcastRepository broadcastRepository;
    private final ApplicationProperties applicationProperties;

    /**
     * Handles the application ready event and dispatches a release notification if needed.
     *
     * @param event the application ready event
     */
    @EventListener
    public void onApplicationReady(final ApplicationReadyEvent event) {
        final String version = applicationProperties.getVersion();
        if (version.contains("SNAPSHOT")) {
            return;
        }
        dispatchIfNeeded(version);
    }

    private void dispatchIfNeeded(final String version) {
        final String title = "Release " + version;

        changelogService.getByVersion(version).ifPresentOrElse(
            entry -> {
                if (!broadcastRepository.existsByCategoryAndTitle(NotificationCategory.UPDATE, title)) {
                    dispatch(entry, title);
                }
            },
            () -> log.warn("No changelog entry found for version {}", version)
        );
    }

    private void dispatch(final ChangelogEntry entry, final String title) {
        final String message = "%d new features, %d improvements, %d fixes".formatted(
            entry.features().size(),
            entry.improvements().size(),
            entry.fixes().size()
        );

        final CreateBroadcastRequest request = new CreateBroadcastRequest()
            .setCategory(NotificationCategory.UPDATE)
            .setTitle(title)
            .setMessage(message)
            .setActionUrl("/changelog")
            .setActionLabel("View Changelog")
            .setAudience(new AudienceRequest().setType(NotificationAudienceType.ALL_USERS));

        broadcastService.sendBroadcast(null, request);
        log.info("Release notification dispatched for version {}", applicationProperties.getVersion());
    }
}
