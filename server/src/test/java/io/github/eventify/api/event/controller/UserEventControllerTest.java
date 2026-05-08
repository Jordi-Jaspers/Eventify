package io.github.eventify.api.event.controller;

import io.github.eventify.api.channel.model.Channel;
import io.github.eventify.api.event.model.Event;
import io.github.eventify.api.event.model.Severity;
import io.github.eventify.api.user.model.User;
import io.github.eventify.support.IntegrationTest;
import io.github.jframe.datasource.search.model.input.SearchInput;
import io.github.jframe.datasource.search.model.input.SortablePageInput;
import io.github.jframe.datasource.search.model.resource.PageResource;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static io.github.eventify.api.Paths.USER_EVENTS_SEARCH_PATH;
import static io.github.eventify.common.constant.Constants.Security.BEARER;
import static io.github.jframe.util.mapper.ObjectMappers.fromJson;
import static io.github.jframe.util.mapper.ObjectMappers.toJson;
import static jakarta.servlet.http.HttpServletResponse.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Integration Test - User Event Controller")
public class UserEventControllerTest extends IntegrationTest {

    @Test
    @DisplayName("Should return events for user's channel")
    public void searchEventsSuccess() throws Exception {
        // Given: User with a channel
        final User user = aValidatedUser();
        final Channel channel = aChannelForUser(user, "Test Channel");

        // And: Events in the channel
        final Event event1 = anEventForChannel(channel, Severity.OK, OffsetDateTime.now().minusHours(2));
        final Event event2 = anEventForChannel(channel, Severity.WARNING, OffsetDateTime.now().minusHours(1));

        // And: Search input with channel filter
        final SortablePageInput searchInput = new SortablePageInput();
        searchInput.setPageNumber(0);
        searchInput.setPageSize(20);

        final SearchInput channelFilter = new SearchInput();
        channelFilter.setFieldName("channelId");
        channelFilter.setTextValue(channel.getId().toString());
        searchInput.getSearchInputs().add(channelFilter);

        // When: Searching events
        final MockHttpServletRequestBuilder request = post(USER_EVENTS_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(searchInput));

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should contain events
        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<?> pageResource = fromJson(content, PageResource.class);

        assertThat(pageResource.getContent(), hasSize(2));
        assertThat(pageResource.getTotalElements(), is(2L));
    }

    @Test
    @DisplayName("Should reject request without authentication")
    public void searchEventsUnauthorized() throws Exception {
        // Given: Search input
        final SortablePageInput searchInput = new SortablePageInput();
        searchInput.setPageNumber(0);
        searchInput.setPageSize(20);

        final SearchInput channelFilter = new SearchInput();
        channelFilter.setFieldName("channelId");
        channelFilter.setTextValue("123");
        searchInput.getSearchInputs().add(channelFilter);

        // When: Searching without authentication
        final MockHttpServletRequestBuilder request = post(USER_EVENTS_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(searchInput));

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be UNAUTHORIZED
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }

    @Test
    @DisplayName("Should reject request for channel user doesn't own")
    public void searchEventsForbiddenForOtherUsersChannel() throws Exception {
        // Given: Two different users
        final User owner = aValidatedUser();
        final User otherUser = aValidatedUser();

        // And: Channel owned by first user
        final Channel channel = aChannelForUser(owner, "Owner's Channel");
        anEventForChannel(channel, Severity.OK, OffsetDateTime.now());

        // And: Search input for that channel
        final SortablePageInput searchInput = new SortablePageInput();
        searchInput.setPageNumber(0);
        searchInput.setPageSize(20);

        final SearchInput channelFilter = new SearchInput();
        channelFilter.setFieldName("channelId");
        channelFilter.setTextValue(channel.getId().toString());
        searchInput.getSearchInputs().add(channelFilter);

        // When: Other user tries to search
        final MockHttpServletRequestBuilder request = post(USER_EVENTS_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + otherUser.getAccessToken().getValue())
            .content(toJson(searchInput));

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be FORBIDDEN
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should handle pagination correctly")
    public void searchEventsWithPagination() throws Exception {
        // Given: User with a channel
        final User user = aValidatedUser();
        final Channel channel = aChannelForUser(user, "Test Channel");

        // And: Multiple events
        for (int i = 0; i < 25; i++) {
            anEventForChannel(channel, Severity.OK, OffsetDateTime.now().minusHours(i));
        }

        // And: Search input with pagination (page 1, size 10)
        final SortablePageInput searchInput = new SortablePageInput();
        searchInput.setPageNumber(1);
        searchInput.setPageSize(10);

        final SearchInput channelFilter = new SearchInput();
        channelFilter.setFieldName("channelId");
        channelFilter.setTextValue(channel.getId().toString());
        searchInput.getSearchInputs().add(channelFilter);

        // When: Searching with pagination
        final MockHttpServletRequestBuilder request = post(USER_EVENTS_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(searchInput));

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Pagination should be correct
        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<?> pageResource = fromJson(content, PageResource.class);

        assertThat(pageResource.getContent(), hasSize(10));
        assertThat(pageResource.getTotalElements(), is(25L));
    }

    @Test
    @DisplayName("Should filter by time range")
    public void searchEventsWithTimeRange() throws Exception {
        // Given: User with a channel
        final User user = aValidatedUser();
        final Channel channel = aChannelForUser(user, "Test Channel");

        // And: Events at different times
        final OffsetDateTime now = OffsetDateTime.now();
        anEventForChannel(channel, Severity.OK, now.minusHours(5)); // Outside range
        anEventForChannel(channel, Severity.WARNING, now.minusHours(2)); // Inside range
        anEventForChannel(channel, Severity.CRITICAL, now.minusHours(1)); // Inside range
        anEventForChannel(channel, Severity.OK, now.plusHours(1)); // Outside range

        // And: Search input with time range filter
        final SortablePageInput searchInput = new SortablePageInput();
        searchInput.setPageNumber(0);
        searchInput.setPageSize(20);

        final SearchInput channelFilter = new SearchInput();
        channelFilter.setFieldName("channelId");
        channelFilter.setTextValue(channel.getId().toString());
        searchInput.getSearchInputs().add(channelFilter);

        final SearchInput timeFilter = new SearchInput();
        timeFilter.setFieldName("timestamp");
        timeFilter.setFromDateValue(now.minusHours(3).toString());
        timeFilter.setToDateValue(now.toString());
        searchInput.getSearchInputs().add(timeFilter);

        // When: Searching with time range
        final MockHttpServletRequestBuilder request = post(USER_EVENTS_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(searchInput));

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Only events within time range should be returned
        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<?> pageResource = fromJson(content, PageResource.class);

        assertThat(pageResource.getContent(), hasSize(2));
        assertThat(pageResource.getTotalElements(), is(2L));
    }

    @Test
    @DisplayName("Should return empty page when no events match")
    public void searchEventsEmptyResult() throws Exception {
        // Given: User with a channel
        final User user = aValidatedUser();
        final Channel channel = aChannelForUser(user, "Test Channel");

        // And: No events in the channel

        // And: Search input
        final SortablePageInput searchInput = new SortablePageInput();
        searchInput.setPageNumber(0);
        searchInput.setPageSize(20);

        final SearchInput channelFilter = new SearchInput();
        channelFilter.setFieldName("channelId");
        channelFilter.setTextValue(channel.getId().toString());
        searchInput.getSearchInputs().add(channelFilter);

        // When: Searching for events
        final MockHttpServletRequestBuilder request = post(USER_EVENTS_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(searchInput));

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Empty page should be returned
        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<?> pageResource = fromJson(content, PageResource.class);

        assertThat(pageResource.getContent(), anyOf(nullValue(), is(empty())));
        assertThat(pageResource.getTotalElements(), is(0L));
    }

    @Test
    @DisplayName("Should reject request for non-existent channel")
    public void searchEventsForNonExistentChannel() throws Exception {
        // Given: User without channels
        final User user = aValidatedUser();

        // And: Search input for non-existent channel
        final SortablePageInput searchInput = new SortablePageInput();
        searchInput.setPageNumber(0);
        searchInput.setPageSize(20);

        final SearchInput channelFilter = new SearchInput();
        channelFilter.setFieldName("channelId");
        channelFilter.setTextValue("999999");
        searchInput.getSearchInputs().add(channelFilter);

        // When: Searching for events
        final MockHttpServletRequestBuilder request = post(USER_EVENTS_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(searchInput));

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be Forbidden (user doesn't have access to this channel)
        response.andExpect(status().is(SC_FORBIDDEN));
    }
}
