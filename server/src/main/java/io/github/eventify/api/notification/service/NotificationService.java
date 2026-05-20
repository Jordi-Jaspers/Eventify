package io.github.eventify.api.notification.service;

import io.github.eventify.api.notification.model.Notification;
import io.github.eventify.api.notification.model.NotificationMetaData;
import io.github.eventify.api.notification.repository.NotificationRepository;
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

import static io.github.eventify.common.util.TimeProvider.now;

/**
 * Service for managing user notifications.
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final NotificationRepository notificationRepository;

    private final NotificationMetaData notificationMetaData;

    /**
     * Searches paginated notifications for a user.
     *
     * @param userId the user ID
     * @param input  the search/pagination input
     * @return page of notifications
     */
    @Transactional(readOnly = true)
    public Page<Notification> searchNotifications(final Long userId, final SortablePageInput input) {
        final Sort rawSort = notificationMetaData.toSort(input.getSortOrder());
        final Sort sort = rawSort != null ? rawSort : Sort.unsorted();
        final int pageSize = input.getPageSize() > 0 ? input.getPageSize() : DEFAULT_PAGE_SIZE;
        final Pageable pageable = PageRequest.of(input.getPageNumber(), pageSize, sort);
        final Specification<Notification> rawSpec = notificationMetaData.toUserNotificationSpecification(input, userId);
        final Specification<Notification> emptySpec = (root, query, cb) -> null;
        final Specification<Notification> spec = rawSpec != null ? rawSpec : emptySpec;
        return notificationRepository.findAll(spec, pageable);
    }

    /**
     * Returns the count of unread notifications for a user.
     *
     * @param userId the user ID
     * @return unread count
     */
    @Transactional(readOnly = true)
    public long getUnreadCount(final Long userId) {
        return notificationRepository.countByUserIdAndReadAtIsNull(userId);
    }

    /**
     * Marks a notification as read. Idempotent — no-op if already read.
     * Throws {@link DataNotFoundException} if the notification doesn't belong to the user.
     *
     * @param notificationId the notification ID
     * @param userId         the user ID
     */
    @Transactional
    public void markAsRead(final Long notificationId, final Long userId) {
        final Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
            .orElseThrow(() -> new DataNotFoundException((String) null));
        if (notification.getReadAt() == null) {
            notification.setReadAt(now());
        }
        notificationRepository.save(notification);
    }

    /**
     * Marks all unread notifications as read for a user.
     *
     * @param userId the user ID
     * @return number of notifications marked as read
     */
    @Transactional
    public int markAllAsRead(final Long userId) {
        return notificationRepository.markAllAsReadForUser(userId, now());
    }
}
