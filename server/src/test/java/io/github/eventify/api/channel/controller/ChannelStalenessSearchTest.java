package io.github.eventify.api.channel.controller;

import io.github.eventify.api.channel.model.Channel;
import io.github.eventify.api.channel.model.response.ChannelDetailsResponse;
import io.github.eventify.api.user.model.User;
import io.github.eventify.support.IntegrationTest;
import io.github.jframe.datasource.search.model.input.SearchInput;
import io.github.jframe.datasource.search.model.input.SortableColumn;
import io.github.jframe.datasource.search.model.input.SortablePageInput;
import io.github.jframe.datasource.search.model.resource.PageResource;
import tools.jackson.core.type.TypeReference;

import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static io.github.eventify.api.Paths.USER_CHANNELS_SEARCH_PATH;
import static io.github.eventify.common.constant.Constants.Security.BEARER;
import static io.github.eventify.common.util.TimeProvider.now;
import static io.github.jframe.util.mapper.ObjectMappers.fromJson;
import static io.github.jframe.util.mapper.ObjectMappers.toJson;
import static jakarta.servlet.http.HttpServletResponse.SC_OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration Test - Channel Staleness Search Filters
 */
@DisplayName("Integration Test - Channel Staleness Search")
public class ChannelStalenessSearchTest extends IntegrationTest {

    @Test
    @DisplayName("Should filter channels by isStale=true")
    public void shouldFilterChannelsByIsStaleTrue() throws Exception {
        // Given: User with stale and active channels
        final User user = aValidatedUser();

        final Channel staleChannel = aChannelForUser(user, "Stale Channel");
        staleChannel.setIsStale(true);
        staleChannel.setLastEventAt(now().minusDays(10));
        channelRepository.save(staleChannel);

        final Channel activeChannel = aChannelForUser(user, "Active Channel");
        activeChannel.setIsStale(false);
        activeChannel.setLastEventAt(now().minusDays(2));
        channelRepository.save(activeChannel);

        // And: Search input filtering for stale channels
        final SortablePageInput searchInput = new SortablePageInput();
        searchInput.setPageNumber(0);
        searchInput.setPageSize(10);

        final SearchInput isStaleFilter = new SearchInput();
        isStaleFilter.setFieldName("isStale");
        isStaleFilter.setTextValue("true");
        searchInput.getSearchInputs().add(isStaleFilter);

        // When: Searching with isStale=true filter
        final MockHttpServletRequestBuilder request = post(USER_CHANNELS_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(searchInput));

        final ResultActions response = mockMvc.perform(request);

        // Then: Only stale channel returned
        response.andExpect(status().is(SC_OK));

        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<ChannelDetailsResponse> pageResource = fromJson(
            content,
            new TypeReference<PageResource<ChannelDetailsResponse>>() {}
        );

        assertThat(pageResource.getContent(), hasSize(1));

        final List<ChannelDetailsResponse> channels = pageResource.getContent();
        assertThat(channels.get(0).getName(), is("Stale Channel"));
        assertThat(channels.get(0).getIsStale(), is(true));
    }

    @Test
    @DisplayName("Should filter channels by isStale=false")
    public void shouldFilterChannelsByIsStaleFalse() throws Exception {
        // Given: User with stale and active channels
        final User user = aValidatedUser();

        final Channel staleChannel = aChannelForUser(user, "Stale Channel");
        staleChannel.setIsStale(true);
        staleChannel.setLastEventAt(now().minusDays(10));
        channelRepository.save(staleChannel);

        final Channel activeChannel = aChannelForUser(user, "Active Channel");
        activeChannel.setIsStale(false);
        activeChannel.setLastEventAt(now().minusDays(2));
        channelRepository.save(activeChannel);

        // And: Search input filtering for active channels
        final SortablePageInput searchInput = new SortablePageInput();
        searchInput.setPageNumber(0);
        searchInput.setPageSize(10);

        final SearchInput isStaleFilter = new SearchInput();
        isStaleFilter.setFieldName("isStale");
        isStaleFilter.setTextValue("false");
        searchInput.getSearchInputs().add(isStaleFilter);

        // When: Searching with isStale=false filter
        final MockHttpServletRequestBuilder request = post(USER_CHANNELS_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(searchInput));

        final ResultActions response = mockMvc.perform(request);

        // Then: Only active channel returned
        response.andExpect(status().is(SC_OK));

        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<ChannelDetailsResponse> pageResource = fromJson(
            content,
            new TypeReference<PageResource<ChannelDetailsResponse>>() {}
        );

        assertThat(pageResource.getContent(), hasSize(1));

        final List<ChannelDetailsResponse> channels = pageResource.getContent();
        assertThat(channels.get(0).getName(), is("Active Channel"));
        assertThat(channels.get(0).getIsStale(), is(false));
    }

    @Test
    @DisplayName("Should sort channels by lastEventAt ascending")
    public void shouldSortChannelsByLastEventAtAscending() throws Exception {
        // Given: User with multiple channels with different lastEventAt
        final User user = aValidatedUser();

        final Channel channel1 = aChannelForUser(user, "Oldest Events");
        channel1.setLastEventAt(now().minusDays(10));
        channelRepository.save(channel1);

        final Channel channel2 = aChannelForUser(user, "Medium Events");
        channel2.setLastEventAt(now().minusDays(5));
        channelRepository.save(channel2);

        final Channel channel3 = aChannelForUser(user, "Recent Events");
        channel3.setLastEventAt(now().minusDays(1));
        channelRepository.save(channel3);

        // And: Search input with lastEventAt ascending sort
        final SortablePageInput searchInput = new SortablePageInput();
        searchInput.setPageNumber(0);
        searchInput.setPageSize(10);

        final SortableColumn sortColumn = new SortableColumn();
        sortColumn.setName("lastEventAt");
        sortColumn.setDirection("asc");
        searchInput.setSortOrder(List.of(sortColumn));

        // When: Searching with sort
        final MockHttpServletRequestBuilder request = post(USER_CHANNELS_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(searchInput));

        final ResultActions response = mockMvc.perform(request);

        // Then: Channels returned in ascending order (oldest first)
        response.andExpect(status().is(SC_OK));

        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<ChannelDetailsResponse> pageResource = fromJson(
            content,
            new TypeReference<PageResource<ChannelDetailsResponse>>() {}
        );

        assertThat(pageResource.getContent(), hasSize(3));

        final List<ChannelDetailsResponse> channels = pageResource.getContent();
        assertThat(channels.get(0).getName(), is("Oldest Events"));
        assertThat(channels.get(1).getName(), is("Medium Events"));
        assertThat(channels.get(2).getName(), is("Recent Events"));
    }

    @Test
    @DisplayName("Should sort channels by lastEventAt descending")
    public void shouldSortChannelsByLastEventAtDescending() throws Exception {
        // Given: User with multiple channels with different lastEventAt
        final User user = aValidatedUser();

        final Channel channel1 = aChannelForUser(user, "Oldest Events");
        channel1.setLastEventAt(now().minusDays(10));
        channelRepository.save(channel1);

        final Channel channel2 = aChannelForUser(user, "Medium Events");
        channel2.setLastEventAt(now().minusDays(5));
        channelRepository.save(channel2);

        final Channel channel3 = aChannelForUser(user, "Recent Events");
        channel3.setLastEventAt(now().minusDays(1));
        channelRepository.save(channel3);

        // And: Search input with lastEventAt descending sort
        final SortablePageInput searchInput = new SortablePageInput();
        searchInput.setPageNumber(0);
        searchInput.setPageSize(10);

        final SortableColumn sortColumn = new SortableColumn();
        sortColumn.setName("lastEventAt");
        sortColumn.setDirection("desc");
        searchInput.setSortOrder(List.of(sortColumn));

        // When: Searching with sort
        final MockHttpServletRequestBuilder request = post(USER_CHANNELS_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(searchInput));

        final ResultActions response = mockMvc.perform(request);

        // Then: Channels returned in descending order (newest first)
        response.andExpect(status().is(SC_OK));

        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<ChannelDetailsResponse> pageResource = fromJson(
            content,
            new TypeReference<PageResource<ChannelDetailsResponse>>() {}
        );

        assertThat(pageResource.getContent(), hasSize(3));

        final List<ChannelDetailsResponse> channels = pageResource.getContent();
        assertThat(channels.get(0).getName(), is("Recent Events"));
        assertThat(channels.get(1).getName(), is("Medium Events"));
        assertThat(channels.get(2).getName(), is("Oldest Events"));
    }

    @Test
    @DisplayName("Should return isStale and lastEventAt in ChannelDetailsResponse")
    public void shouldReturnIsStaleAndLastEventAtInResponse() throws Exception {
        // Given: User with a channel
        final User user = aValidatedUser();

        final OffsetDateTime lastEventTime = now().minusDays(5);
        final Channel channel = aChannelForUser(user, "Test Channel");
        channel.setIsStale(true);
        channel.setLastEventAt(lastEventTime);
        channelRepository.save(channel);

        // And: Basic search input
        final SortablePageInput searchInput = new SortablePageInput();
        searchInput.setPageNumber(0);
        searchInput.setPageSize(10);

        // When: Searching channels
        final MockHttpServletRequestBuilder request = post(USER_CHANNELS_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(searchInput));

        final ResultActions response = mockMvc.perform(request);

        // Then: Response contains isStale and lastEventAt fields
        response.andExpect(status().is(SC_OK));

        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<ChannelDetailsResponse> pageResource = fromJson(
            content,
            new TypeReference<PageResource<ChannelDetailsResponse>>() {}
        );

        assertThat(pageResource.getContent(), hasSize(1));

        final List<ChannelDetailsResponse> channels = pageResource.getContent();

        final ChannelDetailsResponse channelResponse = channels.get(0);
        assertThat(channelResponse.getIsStale(), is(notNullValue()));
        assertThat(channelResponse.getIsStale(), is(true));
        assertThat(channelResponse.getLastEventAt(), is(notNullValue()));
        // PostgreSQL TIMESTAMPTZ truncates to microsecond precision
        assertThat(
            channelResponse.getLastEventAt().toEpochSecond(),
            is(lastEventTime.toEpochSecond())
        );
    }

    @Test
    @DisplayName("Should handle null lastEventAt in response")
    public void shouldHandleNullLastEventAtInResponse() throws Exception {
        // Given: User with a channel that never received events
        final User user = aValidatedUser();

        final Channel channel = aChannelForUser(user, "No Events Channel");
        channel.setIsStale(false);
        channel.setLastEventAt(null);
        channelRepository.save(channel);

        // And: Basic search input
        final SortablePageInput searchInput = new SortablePageInput();
        searchInput.setPageNumber(0);
        searchInput.setPageSize(10);

        // When: Searching channels
        final MockHttpServletRequestBuilder request = post(USER_CHANNELS_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(searchInput));

        final ResultActions response = mockMvc.perform(request);

        // Then: Response contains null lastEventAt
        response.andExpect(status().is(SC_OK));

        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<ChannelDetailsResponse> pageResource = fromJson(
            content,
            new TypeReference<PageResource<ChannelDetailsResponse>>() {}
        );

        assertThat(pageResource.getContent(), hasSize(1));

        final List<ChannelDetailsResponse> channels = pageResource.getContent();

        final ChannelDetailsResponse channelResponse = channels.get(0);
        assertThat(channelResponse.getIsStale(), is(false));
        assertThat(channelResponse.getLastEventAt(), is(nullValue()));
    }

    @Test
    @DisplayName("Should combine isStale filter with sort by lastEventAt")
    public void shouldCombineIsStaleFilterWithSortByLastEventAt() throws Exception {
        // Given: User with multiple channels
        final User user = aValidatedUser();

        final Channel stale1 = aChannelForUser(user, "Stale Old");
        stale1.setIsStale(true);
        stale1.setLastEventAt(now().minusDays(15));
        channelRepository.save(stale1);

        final Channel stale2 = aChannelForUser(user, "Stale Recent");
        stale2.setIsStale(true);
        stale2.setLastEventAt(now().minusDays(8));
        channelRepository.save(stale2);

        final Channel active = aChannelForUser(user, "Active");
        active.setIsStale(false);
        active.setLastEventAt(now().minusDays(1));
        channelRepository.save(active);

        // And: Search input filtering stale and sorting by lastEventAt desc
        final SortablePageInput searchInput = new SortablePageInput();
        searchInput.setPageNumber(0);
        searchInput.setPageSize(10);

        final SearchInput isStaleFilter = new SearchInput();
        isStaleFilter.setFieldName("isStale");
        isStaleFilter.setTextValue("true");
        searchInput.getSearchInputs().add(isStaleFilter);

        final SortableColumn sortColumn = new SortableColumn();
        sortColumn.setName("lastEventAt");
        sortColumn.setDirection("desc");
        searchInput.setSortOrder(List.of(sortColumn));

        // When: Searching with combined filter and sort
        final MockHttpServletRequestBuilder request = post(USER_CHANNELS_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(searchInput));

        final ResultActions response = mockMvc.perform(request);

        // Then: Only stale channels returned, sorted by lastEventAt desc
        response.andExpect(status().is(SC_OK));

        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<ChannelDetailsResponse> pageResource = fromJson(
            content,
            new TypeReference<PageResource<ChannelDetailsResponse>>() {}
        );

        assertThat(pageResource.getContent(), hasSize(2));

        final List<ChannelDetailsResponse> channels = pageResource.getContent();
        assertThat(channels.get(0).getName(), is("Stale Recent"));
        assertThat(channels.get(0).getIsStale(), is(true));
        assertThat(channels.get(1).getName(), is("Stale Old"));
        assertThat(channels.get(1).getIsStale(), is(true));
    }
}
