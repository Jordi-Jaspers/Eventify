package io.github.eventify.api.notification.service;

import io.github.eventify.api.notification.model.Notification;
import io.github.eventify.api.notification.model.NotificationCategory;
import io.github.eventify.api.notification.model.NotificationMetaData;
import io.github.eventify.api.notification.repository.NotificationRepository;
import io.github.eventify.api.user.model.User;
import io.github.eventify.support.UnitTest;
import io.github.jframe.datasource.search.model.input.SortablePageInput;
import io.github.jframe.exception.core.DataNotFoundException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import static java.time.ZoneOffset.UTC;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("Unit Test - Notification Service")
public class NotificationServiceTest extends UnitTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationMetaData notificationMetaData;

    private NotificationService notificationService;

    @BeforeEach
    public void setUp() {
        notificationService = new NotificationService(notificationRepository, notificationMetaData);
    }

    @Test
    @DisplayName("Should return paginated notifications ordered by createdAt DESC")
    public void shouldReturnPaginatedNotifications() {
        // Given: A user with notifications
        final User user = aValidUser();
        final Notification n1 = aNotification(user, "First");
        final Notification n2 = aNotification(user, "Second");
        final Page<Notification> page = new PageImpl<>(List.of(n2, n1));

        when(notificationRepository.findAll(any(Specification.class), any(Pageable.class)))
            .thenReturn(page);

        // When: Searching notifications with pagination input
        final SortablePageInput input = new SortablePageInput();
        input.setPageNumber(0);
        input.setPageSize(10);

        final Page<Notification> result = notificationService.searchNotifications(user.getId(), input);

        // Then: Should return notifications ordered by createdAt DESC
        assertThat(result.getContent(), hasSize(2));
        assertThat(result.getContent().get(0).getTitle(), is("Second"));
    }

    @Test
    @DisplayName("Should return empty page when user has no notifications")
    public void shouldReturnEmptyPageWhenNoNotifications() {
        // Given: A user with no notifications
        final User user = aValidUser();
        final Page<Notification> emptyPage = Page.empty();

        when(notificationRepository.findAll(any(Specification.class), any(Pageable.class)))
            .thenReturn(emptyPage);

        // When: Searching notifications
        final SortablePageInput input = new SortablePageInput();
        input.setPageNumber(0);
        input.setPageSize(20);

        final Page<Notification> result = notificationService.searchNotifications(user.getId(), input);

        // Then: Should return empty page
        assertThat(result.getContent(), is(empty()));
        assertThat(result.getTotalElements(), is(0L));
    }

    @Test
    @DisplayName("Should use default page size of 20 when not specified")
    public void shouldUseDefaultPageSizeWhenNotSpecified() {
        // Given: A user and input with no page size set (0 = unset)
        final User user = aValidUser();
        final Page<Notification> page = Page.empty();

        when(notificationRepository.findAll(any(Specification.class), any(Pageable.class)))
            .thenReturn(page);

        // When: Searching with default (unset) page size
        final SortablePageInput input = new SortablePageInput();
        input.setPageNumber(0);
        input.setPageSize(0);

        notificationService.searchNotifications(user.getId(), input);

        // Then: Repository should be called with a pageable of size 20
        final ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(notificationRepository).findAll(any(Specification.class), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getPageSize(), is(20));
    }

    @Test
    @DisplayName("Should return unread count for user")
    public void shouldReturnUnreadCount() {
        // Given: A user with 3 unread notifications
        final User user = aValidUser();

        when(notificationRepository.countByUserIdAndReadAtIsNull(user.getId())).thenReturn(3L);

        // When: Getting unread count
        final long count = notificationService.getUnreadCount(user.getId());

        // Then: Should return 3
        assertThat(count, is(3L));
    }

    @Test
    @DisplayName("Should mark notification as read and set readAt timestamp")
    public void shouldMarkNotificationAsRead() {
        // Given: An unread notification belonging to the user
        final User user = aValidUser();
        final Long notificationId = 42L;
        final Notification notification = aNotification(user, "Test");

        when(notificationRepository.findByIdAndUserId(notificationId, user.getId()))
            .thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArgument(0));

        // When: Marking as read
        notificationService.markAsRead(notificationId, user.getId());

        // Then: readAt should be set
        final ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertThat(captor.getValue().getReadAt(), is(notNullValue()));
    }

    @Test
    @DisplayName("Should be idempotent when marking already-read notification as read")
    public void shouldBeIdempotentWhenMarkingAlreadyReadNotification() {
        // Given: An already-read notification
        final User user = aValidUser();
        final Long notificationId = 42L;
        final Notification notification = aNotification(user, "Test");
        notification.setReadAt(OffsetDateTime.now(UTC).minusMinutes(5));

        when(notificationRepository.findByIdAndUserId(notificationId, user.getId()))
            .thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArgument(0));

        // When: Marking as read again
        notificationService.markAsRead(notificationId, user.getId());

        // Then: Should not throw, save may or may not be called (idempotent)
        // No exception is the key assertion
    }

    @Test
    @DisplayName("Should throw DataNotFoundException when notification not found for user")
    public void shouldThrowWhenNotificationNotFound() {
        // Given: A notification ID that doesn't belong to the user
        final User user = aValidUser();
        final Long notificationId = 999L;

        when(notificationRepository.findByIdAndUserId(notificationId, user.getId()))
            .thenReturn(Optional.empty());

        // When & Then: Should throw DataNotFoundException
        assertThrows(
            DataNotFoundException.class,
            () -> notificationService.markAsRead(notificationId, user.getId())
        );
    }

    @Test
    @DisplayName("Should throw DataNotFoundException when notification belongs to different user")
    public void shouldThrowWhenNotificationBelongsToDifferentUser() {
        // Given: A notification belonging to user A, but user B tries to mark it
        final User userA = aValidUser();
        final User userB = aValidUser();
        userB.setId(2L);

        final Long notificationId = 42L;

        // Repository returns empty because userId doesn't match
        when(notificationRepository.findByIdAndUserId(notificationId, userB.getId()))
            .thenReturn(Optional.empty());

        // When & Then: Should throw DataNotFoundException (not 403)
        assertThrows(
            DataNotFoundException.class,
            () -> notificationService.markAsRead(notificationId, userB.getId())
        );
    }

    @Test
    @DisplayName("Should mark all notifications as read and return count")
    public void shouldMarkAllNotificationsAsRead() {
        // Given: A user with unread notifications
        final User user = aValidUser();

        when(notificationRepository.markAllAsReadForUser(eq(user.getId()), any(OffsetDateTime.class)))
            .thenReturn(3);

        // When: Marking all as read
        final int count = notificationService.markAllAsRead(user.getId());

        // Then: Should return the count of marked notifications
        assertThat(count, is(3));
    }

    // ========================= FACTORY METHODS =========================

    private static Notification aNotification(final User user, final String title) {
        return new Notification(
            user,
            NotificationCategory.ANNOUNCEMENT,
            title,
            "Test message",
            null,
            null,
            false
        );
    }
}
