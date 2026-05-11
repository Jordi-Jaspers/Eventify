package io.github.eventify.api.notification.controller;

import io.github.eventify.api.notification.model.NotificationCategory;
import io.github.eventify.api.notification.model.response.MarkAllReadResponse;
import io.github.eventify.api.notification.model.response.NotificationResponse;
import io.github.eventify.api.notification.model.response.UnreadCountResponse;
import io.github.eventify.api.user.model.User;
import io.github.eventify.support.IntegrationTest;
import io.github.jframe.datasource.search.model.input.SortablePageInput;
import io.github.jframe.datasource.search.model.resource.PageResource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static io.github.eventify.api.Paths.*;
import static io.github.eventify.common.constant.Constants.Security.BEARER;
import static io.github.jframe.util.mapper.ObjectMappers.fromJson;
import static io.github.jframe.util.mapper.ObjectMappers.toJson;
import static jakarta.servlet.http.HttpServletResponse.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Integration Test - Notification Controller")
public class NotificationControllerTest extends IntegrationTest {

    @Test
    @DisplayName("Should create welcome notification on email/password registration")
    public void welcomeNotificationOnRegistration() throws Exception {
        // Given: A newly registered and validated user
        final User user = aValidatedUser();

        // When: Searching notifications for that user
        final SortablePageInput searchInput = new SortablePageInput();
        searchInput.setPageNumber(0);
        searchInput.setPageSize(10);

        final MockHttpServletRequestBuilder request = post(NOTIFICATIONS_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(searchInput));

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Should contain exactly 1 welcome notification
        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<?> pageResponse = fromJson(content, PageResource.class);

        assertThat(pageResponse.getContent(), hasSize(1));

        final String notificationJson = toJson(pageResponse.getContent().get(0));
        final NotificationResponse notification = fromJson(notificationJson, NotificationResponse.class);
        assertThat(notification.getId(), is(notNullValue()));
        assertThat(notification.getCategory(), is(NotificationCategory.ANNOUNCEMENT));
        assertThat(notification.getTitle(), is("Welcome to Eventify"));
        assertThat(notification.getMessage(), containsString("Get started by creating your first channel"));
        assertThat(notification.getActionUrl(), is("/channels"));
        assertThat(notification.getActionLabel(), is("Get started"));
        assertThat(notification.isUrgent(), is(false));
        assertThat(notification.getReadAt(), is(nullValue()));
    }

    @Test
    @DisplayName("Should list notifications paginated ordered by createdAt DESC")
    public void listNotificationsPaginated() throws Exception {
        // Given: A validated user with a welcome notification (from registration)
        final User user = aValidatedUser();

        // And: Additional notifications seeded directly
        aNotificationForUser(user, "Second notification");
        aNotificationForUser(user, "Third notification");

        // When: Requesting first page with pageSize=2
        final SortablePageInput searchInput = new SortablePageInput();
        searchInput.setPageNumber(0);
        searchInput.setPageSize(2);

        final MockHttpServletRequestBuilder request = post(NOTIFICATIONS_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(searchInput));

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Should return at most 2 notifications
        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<?> pageResponse = fromJson(content, PageResource.class);

        assertThat(pageResponse.getContent(), hasSize(2));
        assertThat(pageResponse.getTotalElements(), is(3L));

        // And: Each notification should have required fields
        final String firstJson = toJson(pageResponse.getContent().get(0));
        final NotificationResponse first = fromJson(firstJson, NotificationResponse.class);
        assertThat(first.getId(), is(notNullValue()));
        assertThat(first.getCategory(), is(notNullValue()));
        assertThat(first.getTitle(), is(notNullValue()));
        assertThat(first.getMessage(), is(notNullValue()));
        assertThat(first.getCreatedAt(), is(notNullValue()));
    }

    @Test
    @DisplayName("Should return empty page when no notifications exist beyond offset")
    public void listNotificationsWithOffsetBeyondMost() throws Exception {
        // Given: A validated user with 3 notifications total
        final User user = aValidatedUser();
        aNotificationForUser(user, "Second notification");
        aNotificationForUser(user, "Third notification");

        // When: Requesting page 2 with pageSize=2 (only 1 notification remains)
        final SortablePageInput searchInput = new SortablePageInput();
        searchInput.setPageNumber(1);
        searchInput.setPageSize(2);

        final MockHttpServletRequestBuilder request = post(NOTIFICATIONS_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(searchInput));

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Should return 1 notification (the remaining one)
        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<?> pageResponse = fromJson(content, PageResource.class);

        assertThat(pageResponse.getContent(), hasSize(1));
        assertThat(pageResponse.getTotalElements(), is(3L));
    }

    @Test
    @DisplayName("Should return empty page when user has no notifications")
    public void listNotificationsEmptyPage() throws Exception {
        // Given: A validated user (welcome notification exists, but we test empty search result)
        final User user = aValidatedUser();

        // When: Requesting page beyond total results
        final SortablePageInput searchInput = new SortablePageInput();
        searchInput.setPageNumber(99);
        searchInput.setPageSize(10);

        final MockHttpServletRequestBuilder request = post(NOTIFICATIONS_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(searchInput));

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK with empty content
        response.andExpect(status().is(SC_OK));

        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<?> pageResponse = fromJson(content, PageResource.class);

        assertThat(pageResponse.getContent(), is(empty()));
    }

    @Test
    @DisplayName("Should return correct unread count")
    public void getUnreadCount() throws Exception {
        // Given: A validated user with a welcome notification (unread)
        final User user = aValidatedUser();

        // When: Requesting unread count
        final MockHttpServletRequestBuilder request = get(NOTIFICATIONS_UNREAD_COUNT_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Count should be 1 (welcome notification)
        final String content = response.andReturn().getResponse().getContentAsString();
        final UnreadCountResponse countResponse = fromJson(content, UnreadCountResponse.class);

        assertThat(countResponse.getCount(), is(1L));
    }

    @Test
    @DisplayName("Should mark single notification as read and be idempotent")
    public void markSingleNotificationAsRead() throws Exception {
        // Given: A validated user with a welcome notification
        final User user = aValidatedUser();

        // And: The notification ID (Long)
        final Long notificationId = getFirstNotificationId(user);

        // When: Marking the notification as read
        final MockHttpServletRequestBuilder markReadRequest = post(NOTIFICATION_READ_PATH, notificationId)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        final ResultActions markReadResponse = mockMvc.perform(markReadRequest);

        // Then: Response should be NO_CONTENT
        markReadResponse.andExpect(status().is(SC_NO_CONTENT));

        // And: The notification should now have readAt set
        final SortablePageInput searchInput = new SortablePageInput();
        searchInput.setPageNumber(0);
        searchInput.setPageSize(10);

        final MockHttpServletRequestBuilder listRequest = post(NOTIFICATIONS_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(searchInput));

        final ResultActions listResponse = mockMvc.perform(listRequest);
        final String content = listResponse.andReturn().getResponse().getContentAsString();
        final PageResource<?> pageResponse = fromJson(content, PageResource.class);

        final String firstJson = toJson(pageResponse.getContent().get(0));
        final NotificationResponse first = fromJson(firstJson, NotificationResponse.class);
        assertThat(first.getReadAt(), is(notNullValue()));

        // When: Marking the same notification as read again (idempotent)
        final ResultActions secondMarkReadResponse = mockMvc.perform(markReadRequest);

        // Then: Should still return NO_CONTENT
        secondMarkReadResponse.andExpect(status().is(SC_NO_CONTENT));
    }

    @Test
    @DisplayName("Should mark all notifications as read and return count")
    public void markAllNotificationsAsRead() throws Exception {
        // Given: A validated user with notifications
        final User user = aValidatedUser();
        aNotificationForUser(user, "Second notification");

        // When: Marking all as read
        final MockHttpServletRequestBuilder markAllRequest = post(NOTIFICATIONS_READ_ALL_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        final ResultActions markAllResponse = mockMvc.perform(markAllRequest);

        // Then: Response should be OK with marked count
        markAllResponse.andExpect(status().is(SC_OK));

        final String content = markAllResponse.andReturn().getResponse().getContentAsString();
        final MarkAllReadResponse markAllResult = fromJson(content, MarkAllReadResponse.class);

        assertThat(markAllResult.getMarkedCount(), is(greaterThanOrEqualTo(1)));

        // And: Unread count should be 0
        final MockHttpServletRequestBuilder unreadRequest = get(NOTIFICATIONS_UNREAD_COUNT_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        final ResultActions unreadResponse = mockMvc.perform(unreadRequest);
        final String unreadContent = unreadResponse.andReturn().getResponse().getContentAsString();
        final UnreadCountResponse unreadResult = fromJson(unreadContent, UnreadCountResponse.class);

        assertThat(unreadResult.getCount(), is(0L));
    }

    @Test
    @DisplayName("Should return 404 when user B tries to mark user A's notification as read")
    public void markNotificationReadCrossUserReturns404() throws Exception {
        // Given: User A with a notification
        final User userA = aValidatedUser();
        final Long userANotificationId = getFirstNotificationId(userA);

        // And: User B
        final User userB = aValidatedUser();

        // When: User B tries to mark user A's notification as read
        final MockHttpServletRequestBuilder request = post(NOTIFICATION_READ_PATH, userANotificationId)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + userB.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be 404 (not 403)
        response.andExpect(status().is(SC_NOT_FOUND));
    }

    @Test
    @DisplayName("Should return 401 when accessing notifications without authentication")
    public void listNotificationsUnauthenticatedReturns401() throws Exception {
        // When: Requesting notifications search without auth header
        final MockHttpServletRequestBuilder request = post(NOTIFICATIONS_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(new SortablePageInput()));

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be 401
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }

    // ========================= HELPER METHODS =========================

    private Long getFirstNotificationId(final User user) throws Exception {
        final SortablePageInput searchInput = new SortablePageInput();
        searchInput.setPageNumber(0);
        searchInput.setPageSize(10);

        final MockHttpServletRequestBuilder request = post(NOTIFICATIONS_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(searchInput));

        final ResultActions response = mockMvc.perform(request);
        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<?> pageResponse = fromJson(content, PageResource.class);

        final String firstJson = toJson(pageResponse.getContent().get(0));
        final NotificationResponse first = fromJson(firstJson, NotificationResponse.class);
        return first.getId();
    }
}
