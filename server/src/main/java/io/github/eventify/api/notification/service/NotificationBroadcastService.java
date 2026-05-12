package io.github.eventify.api.notification.service;

import io.github.eventify.api.authentication.model.Role;
import io.github.eventify.api.notification.model.NotificationAudience;
import io.github.eventify.api.notification.model.NotificationAudienceType;
import io.github.eventify.api.notification.model.NotificationBroadcast;
import io.github.eventify.api.notification.model.NotificationBroadcastMetaData;
import io.github.eventify.api.notification.model.NotificationCategory;
import io.github.eventify.api.notification.model.NotificationPayload;
import io.github.eventify.api.notification.model.request.AudienceRequest;
import io.github.eventify.api.notification.model.request.CreateBroadcastRequest;
import io.github.eventify.api.notification.repository.NotificationBroadcastRepository;
import io.github.eventify.api.user.model.User;
import io.github.jframe.datasource.search.model.input.SortablePageInput;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing notification broadcasts.
 */
@Service
@RequiredArgsConstructor
public class NotificationBroadcastService {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final AudienceResolver audienceResolver;

    private final NotificationDispatchService notificationDispatchService;

    private final NotificationBroadcastRepository notificationBroadcastRepository;

    private final NotificationBroadcastMetaData broadcastMetaData;

    /**
     * Sends a broadcast notification to the resolved audience.
     *
     * @param sender  the admin user sending the broadcast
     * @param request the broadcast request
     * @return the saved broadcast entity
     */
    @Transactional
    public NotificationBroadcast sendBroadcast(final User sender, final CreateBroadcastRequest request) {
        final NotificationAudience audience = toAudience(request.getAudience());
        final List<User> recipients = audienceResolver.resolve(audience);

        final NotificationBroadcast broadcast = buildBroadcast(sender, request, recipients.size());
        final NotificationBroadcast saved = notificationBroadcastRepository.save(broadcast);

        final NotificationPayload payload = buildPayload(request);
        for (final User recipient : recipients) {
            notificationDispatchService.dispatch(NotificationAudience.user(recipient.getId()), payload);
        }

        return saved;
    }

    /**
     * Returns the count of recipients for the given audience without dispatching.
     *
     * @param audienceRequest the audience request
     * @return the recipient count
     */
    @Transactional(readOnly = true)
    public int previewRecipientCount(final AudienceRequest audienceRequest) {
        final NotificationAudience audience = toAudience(audienceRequest);
        return (int) audienceResolver.count(audience);
    }

    /**
     * Returns a paginated list of broadcasts ordered by createdAt DESC.
     *
     * @param input the page input
     * @return page of broadcast entities
     */
    @Transactional(readOnly = true)
    public Page<NotificationBroadcast> searchBroadcasts(final SortablePageInput input) {
        final Sort sort = broadcastMetaData.toSort(input.getSortOrder());
        final int pageSize = input.getPageSize() > 0 ? input.getPageSize() : DEFAULT_PAGE_SIZE;
        final Pageable pageable = PageRequest.of(input.getPageNumber(), pageSize, sort);
        final Specification<NotificationBroadcast> spec = broadcastMetaData.toSearchSpecification(input);
        return notificationBroadcastRepository.findAll(spec, pageable);
    }

    private NotificationBroadcast buildBroadcast(final User sender, final CreateBroadcastRequest request,
        final int recipientCount) {
        final NotificationBroadcast broadcast = new NotificationBroadcast();
        broadcast.setSentBy(sender);
        broadcast.setCategory(NotificationCategory.valueOf(request.getCategory()));
        broadcast.setTitle(request.getTitle());
        broadcast.setMessage(request.getMessage());
        broadcast.setActionUrl(request.getActionUrl());
        broadcast.setActionLabel(request.getActionLabel());
        broadcast.setAudienceType(request.getAudience().getType());
        broadcast.setAudienceTargetId(request.getAudience().getTargetId());
        broadcast.setAudienceRole(request.getAudience().getRole());
        broadcast.setRecipientCount(recipientCount);
        return broadcast;
    }

    private NotificationPayload buildPayload(final CreateBroadcastRequest request) {
        return new NotificationPayload(
            NotificationCategory.valueOf(request.getCategory()),
            request.getTitle(),
            request.getMessage(),
            request.getActionUrl(),
            request.getActionLabel(),
            false
        );
    }

    private NotificationAudience toAudience(final AudienceRequest audienceRequest) {
        final NotificationAudienceType type = NotificationAudienceType.valueOf(audienceRequest.getType());
        return switch (type) {
            case USER -> NotificationAudience.user(audienceRequest.getTargetId());
            case ALL_USERS -> NotificationAudience.allUsers();
            case ORGANIZATION -> NotificationAudience.organization(audienceRequest.getTargetId());
            case ALL_ORGANIZATION_OWNERS -> NotificationAudience.allOrganizationOwners();
            case GLOBAL_ROLE -> NotificationAudience.globalRole(Role.valueOf(audienceRequest.getRole()));
        };
    }
}
