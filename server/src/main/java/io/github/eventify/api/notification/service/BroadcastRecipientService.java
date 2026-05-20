package io.github.eventify.api.notification.service;

import io.github.eventify.api.notification.model.Notification;
import io.github.eventify.api.notification.model.NotificationRecipientMetaData;
import io.github.eventify.api.notification.repository.NotificationBroadcastRepository;
import io.github.eventify.api.notification.repository.NotificationRepository;
import io.github.eventify.common.exception.ApiErrorCode;
import io.github.jframe.datasource.search.model.input.SortablePageInput;
import io.github.jframe.exception.core.DataNotFoundException;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for querying broadcast recipients.
 */
@Service
@RequiredArgsConstructor
public class BroadcastRecipientService {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final NotificationBroadcastRepository notificationBroadcastRepository;

    private final NotificationRepository notificationRepository;

    private final NotificationRecipientMetaData recipientMetaData;

    /**
     * Returns paginated recipients for a broadcast with optional search.
     *
     * @param broadcastId the broadcast ID
     * @param input       the sortable page input
     * @return page of notification entities
     */
    @Transactional(readOnly = true)
    public Page<Notification> searchBroadcastRecipients(final Long broadcastId, final SortablePageInput input) {
        if (!notificationBroadcastRepository.existsById(broadcastId)) {
            throw new DataNotFoundException(ApiErrorCode.BROADCAST_NOT_FOUND);
        }
        final Sort sort = recipientMetaData.toSort(input.getSortOrder());
        final int pageSize = input.getPageSize() > 0 ? input.getPageSize() : DEFAULT_PAGE_SIZE;
        final Pageable pageable = PageRequest.of(input.getPageNumber(), pageSize, sort);
        final Specification<Notification> spec = recipientMetaData.toRecipientSpecification(input, broadcastId);
        return notificationRepository.findAll(spec, pageable);
    }
}
