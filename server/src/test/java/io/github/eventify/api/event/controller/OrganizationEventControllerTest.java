package io.github.eventify.api.event.controller;

import io.github.eventify.api.channel.model.Channel;
import io.github.eventify.api.event.model.Event;
import io.github.eventify.api.event.model.Severity;
import io.github.eventify.api.organization.model.Organization;
import io.github.eventify.api.organization.model.OrganizationalRole;
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

import static io.github.eventify.api.Paths.ORGANIZATION_EVENTS_SEARCH_PATH;
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

@DisplayName("Integration Test - Organization Event Controller")
public class OrganizationEventControllerTest extends IntegrationTest {

    @Test
    @DisplayName("Should return events for organization's channel")
    public void searchEventsSuccess() throws Exception {
        // Given: Organization with owner
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // And: Channel in the organization
        final Channel channel = aChannelForOrganisation(owner, org, "Org Channel");

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
        final String path = ORGANIZATION_EVENTS_SEARCH_PATH.replace("{orgId}", org.getId().toString());
        final MockHttpServletRequestBuilder request = post(path)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
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
    @DisplayName("Should reject request for non-member")
    public void searchEventsForbiddenForNonMember() throws Exception {
        // Given: Organization with owner
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // And: Channel in the organization
        final Channel channel = aChannelForOrganisation(owner, org, "Org Channel");
        anEventForChannel(channel, Severity.OK, OffsetDateTime.now());

        // And: Non-member user
        final User nonMember = aValidatedUser();

        // And: Search input
        final SortablePageInput searchInput = new SortablePageInput();
        searchInput.setPageNumber(0);
        searchInput.setPageSize(20);

        final SearchInput channelFilter = new SearchInput();
        channelFilter.setFieldName("channelId");
        channelFilter.setTextValue(channel.getId().toString());
        searchInput.getSearchInputs().add(channelFilter);

        // When: Non-member tries to search
        final String path = ORGANIZATION_EVENTS_SEARCH_PATH.replace("{orgId}", org.getId().toString());
        final MockHttpServletRequestBuilder request = post(path)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + nonMember.getAccessToken().getValue())
            .content(toJson(searchInput));

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be FORBIDDEN
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should reject request for channel not in organization")
    public void searchEventsForbiddenForChannelNotInOrg() throws Exception {
        // Given: Organization with owner
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // And: Personal channel (not in organization)
        final Channel personalChannel = aChannelForUser(owner, "Personal Channel");
        anEventForChannel(personalChannel, Severity.OK, OffsetDateTime.now());

        // And: Search input for personal channel
        final SortablePageInput searchInput = new SortablePageInput();
        searchInput.setPageNumber(0);
        searchInput.setPageSize(20);

        final SearchInput channelFilter = new SearchInput();
        channelFilter.setFieldName("channelId");
        channelFilter.setTextValue(personalChannel.getId().toString());
        searchInput.getSearchInputs().add(channelFilter);

        // When: Searching via organization endpoint
        final String path = ORGANIZATION_EVENTS_SEARCH_PATH.replace("{orgId}", org.getId().toString());
        final MockHttpServletRequestBuilder request = post(path)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .content(toJson(searchInput));

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be FORBIDDEN
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should allow organization member to search events")
    public void searchEventsSuccessForMember() throws Exception {
        // Given: Organization with owner
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // And: Member user
        final User member = aValidatedUser();
        addMemberToOrganization(org, member, OrganizationalRole.MEMBER);

        // And: Channel in the organization
        final Channel channel = aChannelForOrganisation(owner, org, "Org Channel");
        anEventForChannel(channel, Severity.OK, OffsetDateTime.now());

        // And: Search input
        final SortablePageInput searchInput = new SortablePageInput();
        searchInput.setPageNumber(0);
        searchInput.setPageSize(20);

        final SearchInput channelFilter = new SearchInput();
        channelFilter.setFieldName("channelId");
        channelFilter.setTextValue(channel.getId().toString());
        searchInput.getSearchInputs().add(channelFilter);

        // When: Member searches events
        final String path = ORGANIZATION_EVENTS_SEARCH_PATH.replace("{orgId}", org.getId().toString());
        final MockHttpServletRequestBuilder request = post(path)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + member.getAccessToken().getValue())
            .content(toJson(searchInput));

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should contain events
        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<?> pageResource = fromJson(content, PageResource.class);

        assertThat(pageResource.getContent(), hasSize(1));
    }

    @Test
    @DisplayName("Should handle pagination for organization events")
    public void searchEventsWithPagination() throws Exception {
        // Given: Organization with owner
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // And: Channel with multiple events
        final Channel channel = aChannelForOrganisation(owner, org, "Org Channel");
        for (int i = 0; i < 15; i++) {
            anEventForChannel(channel, Severity.OK, OffsetDateTime.now().minusHours(i));
        }

        // And: Search input with pagination
        final SortablePageInput searchInput = new SortablePageInput();
        searchInput.setPageNumber(0);
        searchInput.setPageSize(10);

        final SearchInput channelFilter = new SearchInput();
        channelFilter.setFieldName("channelId");
        channelFilter.setTextValue(channel.getId().toString());
        searchInput.getSearchInputs().add(channelFilter);

        // When: Searching with pagination
        final String path = ORGANIZATION_EVENTS_SEARCH_PATH.replace("{orgId}", org.getId().toString());
        final MockHttpServletRequestBuilder request = post(path)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
            .content(toJson(searchInput));

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Pagination should be correct
        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<?> pageResource = fromJson(content, PageResource.class);

        assertThat(pageResource.getContent(), hasSize(10));
        assertThat(pageResource.getTotalElements(), is(15L));
    }

    @Test
    @DisplayName("Should return empty page when no events match")
    public void searchEventsEmptyResult() throws Exception {
        // Given: Organization with owner
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // And: Channel with no events
        final Channel channel = aChannelForOrganisation(owner, org, "Empty Channel");

        // And: Search input
        final SortablePageInput searchInput = new SortablePageInput();
        searchInput.setPageNumber(0);
        searchInput.setPageSize(20);

        final SearchInput channelFilter = new SearchInput();
        channelFilter.setFieldName("channelId");
        channelFilter.setTextValue(channel.getId().toString());
        searchInput.getSearchInputs().add(channelFilter);

        // When: Searching for events
        final String path = ORGANIZATION_EVENTS_SEARCH_PATH.replace("{orgId}", org.getId().toString());
        final MockHttpServletRequestBuilder request = post(path)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
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
    @DisplayName("Should filter by time range for organization events")
    public void searchEventsWithTimeRange() throws Exception {
        // Given: Organization with owner
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        // And: Channel with events at different times
        final Channel channel = aChannelForOrganisation(owner, org, "Org Channel");
        final OffsetDateTime now = OffsetDateTime.now();
        anEventForChannel(channel, Severity.OK, now.minusHours(5)); // Outside range
        anEventForChannel(channel, Severity.WARNING, now.minusHours(2)); // Inside range
        anEventForChannel(channel, Severity.CRITICAL, now.minusHours(1)); // Inside range

        // And: Search input with time range
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
        final String path = ORGANIZATION_EVENTS_SEARCH_PATH.replace("{orgId}", org.getId().toString());
        final MockHttpServletRequestBuilder request = post(path)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + owner.getAccessToken().getValue())
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
}
