package io.github.eventify.api.watchlist.controller;

import io.github.eventify.api.channel.model.Channel;
import io.github.eventify.api.organization.model.Organization;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.watchlist.model.request.CreateWatchlistRequest;
import io.github.eventify.api.watchlist.model.request.UpdateWatchlistRequest;
import io.github.eventify.api.watchlist.model.response.WatchlistDetailsResponse;
import io.github.eventify.support.IntegrationTest;
import io.github.jframe.datasource.search.model.resource.PageResource;
import io.github.jframe.exception.resource.ApiErrorResponseResource;
import io.github.jframe.exception.resource.ValidationErrorResponseResource;

import java.util.List;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Integration Test - User Watchlist Controller")
public class UserWatchlistControllerTest extends IntegrationTest {

    // Path constants
    public static final String USER_WATCHLISTS_PATH = "/v1/user/watchlists";
    public static final String USER_WATCHLISTS_SEARCH_PATH = "/v1/user/watchlists/search";
    public static final String USER_WATCHLIST_PATH = "/v1/user/watchlists/{id}";

    @Test
    @DisplayName("Should create watchlist successfully")
    public void createWatchlistSuccess() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // And: A valid create watchlist request
        final CreateWatchlistRequest request = new CreateWatchlistRequest()
            .setName("My Watchlist")
            .setDescription("Production errors");

        // When: Creating watchlist
        final MockHttpServletRequestBuilder createRequest = post(USER_WATCHLISTS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: Response should be CREATED
        response.andExpect(status().is(SC_CREATED));

        // And: Response should contain watchlist details
        final String content = response.andReturn().getResponse().getContentAsString();
        final WatchlistDetailsResponse watchlistResponse = fromJson(content, WatchlistDetailsResponse.class);

        assertThat(watchlistResponse.getId(), is(notNullValue()));
        assertThat(watchlistResponse.getName(), is("My Watchlist"));
        assertThat(watchlistResponse.getDescription(), is("Production errors"));
        assertThat(watchlistResponse.getDefaultTimeRange(), is("24h"));
        assertThat(watchlistResponse.getDefaultOnlyCritical(), is(false));
        assertThat(watchlistResponse.getDefaultSortBySeverity(), is(true));
        assertThat(watchlistResponse.getCreatedAt(), is(notNullValue()));
    }

    @Test
    @DisplayName("Should create watchlist with channels successfully")
    public void createWatchlistWithChannelsSuccess() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // And: User has channels
        final Channel channel1 = aChannelForUser(user, "Channel 1");
        final Channel channel2 = aChannelForUser(user, "Channel 2");

        // And: Request with channel IDs
        final CreateWatchlistRequest request = new CreateWatchlistRequest()
            .setName("My Watchlist")
            .setChannelIds(List.of(channel1.getId(), channel2.getId()));

        // When: Creating watchlist
        final MockHttpServletRequestBuilder createRequest = post(USER_WATCHLISTS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: Response should be CREATED
        response.andExpect(status().is(SC_CREATED));

        // And: Response should contain channels
        final String content = response.andReturn().getResponse().getContentAsString();
        final WatchlistDetailsResponse watchlistResponse = fromJson(content, WatchlistDetailsResponse.class);

        assertThat(watchlistResponse.getChannels(), hasSize(2));
        assertThat(watchlistResponse.getChannels().get(0).getId(), is(channel1.getId()));
        assertThat(watchlistResponse.getChannels().get(1).getId(), is(channel2.getId()));
    }

    @Test
    @DisplayName("Should fail to create watchlist with duplicate name")
    public void createWatchlistDuplicateNameFails() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // And: User already has watchlist with same name
        final CreateWatchlistRequest firstRequest = new CreateWatchlistRequest()
            .setName("My Watchlist");

        mockMvc.perform(
            post(USER_WATCHLISTS_PATH)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
                .content(toJson(firstRequest))
        );

        // When: Creating another watchlist with same name
        final CreateWatchlistRequest duplicateRequest = new CreateWatchlistRequest()
            .setName("My Watchlist");

        final MockHttpServletRequestBuilder createRequest = post(USER_WATCHLISTS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(duplicateRequest));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: Response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));

        // And: Error should mention duplicate name
        final String content = response.andReturn().getResponse().getContentAsString();
        final ApiErrorResponseResource error = fromJson(content, ApiErrorResponseResource.class);
        assertThat(error.getApiErrorReason(), containsStringIgnoringCase("duplicate"));
    }

    @Test
    @DisplayName("Should fail to create watchlist with organization channel")
    public void createWatchlistWithOrgChannelFails() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // And: User is member of an organization
        final Organization org = anOrganisationWithOwner(user);

        // And: Organization has a channel
        final Channel orgChannel = aChannelForOrganisation(user, org, "Org Channel");

        // When: Creating watchlist with organization channel
        final CreateWatchlistRequest request = new CreateWatchlistRequest()
            .setName("My Watchlist")
            .setChannelIds(List.of(orgChannel.getId()));

        final MockHttpServletRequestBuilder createRequest = post(USER_WATCHLISTS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: Response should be NOT_FOUND or BAD_REQUEST
        assertThat(response.andReturn().getResponse().getStatus(), anyOf(is(SC_NOT_FOUND), is(SC_BAD_REQUEST)));
    }

    @Test
    @DisplayName("Should search watchlists successfully")
    public void searchWatchlistsSuccess() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // And: User has 3 watchlists
        for (int i = 1; i <= 3; i++) {
            final CreateWatchlistRequest createRequest = new CreateWatchlistRequest()
                .setName("Watchlist " + i);

            mockMvc.perform(
                post(USER_WATCHLISTS_PATH)
                    .contentType(APPLICATION_JSON)
                    .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
                    .content(toJson(createRequest))
            );
        }

        // When: Searching watchlists
        final MockHttpServletRequestBuilder searchRequest = post(USER_WATCHLISTS_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content("{}");

        final ResultActions response = mockMvc.perform(searchRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should contain 3 watchlists
        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<?> searchResponse = fromJson(content, PageResource.class);

        assertThat(searchResponse.getContent(), hasSize(3));
        assertThat(searchResponse.getTotalElements(), is(3L));
    }

    @Test
    @DisplayName("Should search watchlists with filter successfully")
    public void searchWatchlistsWithFilterSuccess() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // And: User has multiple watchlists
        final CreateWatchlistRequest request1 = new CreateWatchlistRequest()
            .setName("Production Errors");
        mockMvc.perform(
            post(USER_WATCHLISTS_PATH)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
                .content(toJson(request1))
        );

        final CreateWatchlistRequest request2 = new CreateWatchlistRequest()
            .setName("Staging Issues");
        mockMvc.perform(
            post(USER_WATCHLISTS_PATH)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
                .content(toJson(request2))
        );

        // When: Searching with filter
        final MockHttpServletRequestBuilder searchRequest = post(USER_WATCHLISTS_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content("{\"query\":\"Production\"}");

        final ResultActions response = mockMvc.perform(searchRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should contain filtered results
        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<?> searchResponse = fromJson(content, PageResource.class);

        assertThat(searchResponse.getTotalElements(), is(greaterThanOrEqualTo(1L)));
    }

    @Test
    @DisplayName("Should get watchlist successfully")
    public void getWatchlistSuccess() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // And: User has a watchlist
        final CreateWatchlistRequest createRequest = new CreateWatchlistRequest()
            .setName("My Watchlist");

        final ResultActions createResponse = mockMvc.perform(
            post(USER_WATCHLISTS_PATH)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
                .content(toJson(createRequest))
        );

        final String createContent = createResponse.andReturn().getResponse().getContentAsString();
        final WatchlistDetailsResponse createdWatchlist = fromJson(createContent, WatchlistDetailsResponse.class);

        // When: Getting watchlist by ID
        final MockHttpServletRequestBuilder getRequest = get(USER_WATCHLIST_PATH.replace("{id}", createdWatchlist.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(getRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should contain watchlist details
        final String content = response.andReturn().getResponse().getContentAsString();
        final WatchlistDetailsResponse watchlistResponse = fromJson(content, WatchlistDetailsResponse.class);

        assertThat(watchlistResponse.getId(), is(createdWatchlist.getId()));
        assertThat(watchlistResponse.getName(), is("My Watchlist"));
    }

    @Test
    @DisplayName("Should fail when getting non-existent watchlist")
    public void getWatchlistNotFoundFails() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // When: Getting non-existent watchlist
        final MockHttpServletRequestBuilder getRequest = get(USER_WATCHLIST_PATH.replace("{id}", "99999"))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(getRequest);

        // Then: Response should be NOT_FOUND
        response.andExpect(status().is(SC_NOT_FOUND));
    }

    @Test
    @DisplayName("Should fail when getting watchlist of another user")
    public void getWatchlistOfAnotherUserFails() throws Exception {
        // Given: Two authenticated users
        final User user1 = aValidatedUser();
        final User user2 = aValidatedUser();

        // And: User1 has a watchlist
        final CreateWatchlistRequest createRequest = new CreateWatchlistRequest()
            .setName("User1 Watchlist");

        final ResultActions createResponse = mockMvc.perform(
            post(USER_WATCHLISTS_PATH)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user1.getAccessToken().getValue())
                .content(toJson(createRequest))
        );

        final String createContent = createResponse.andReturn().getResponse().getContentAsString();
        final WatchlistDetailsResponse createdWatchlist = fromJson(createContent, WatchlistDetailsResponse.class);

        // When: User2 attempts to get User1's watchlist
        final MockHttpServletRequestBuilder getRequest = get(USER_WATCHLIST_PATH.replace("{id}", createdWatchlist.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user2.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(getRequest);

        // Then: Response should be NOT_FOUND
        response.andExpect(status().is(SC_NOT_FOUND));
    }

    @Test
    @DisplayName("Should update watchlist successfully")
    public void updateWatchlistSuccess() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // And: User has a watchlist
        final CreateWatchlistRequest createRequest = new CreateWatchlistRequest()
            .setName("Old Name");

        final ResultActions createResponse = mockMvc.perform(
            post(USER_WATCHLISTS_PATH)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
                .content(toJson(createRequest))
        );

        final String createContent = createResponse.andReturn().getResponse().getContentAsString();
        final WatchlistDetailsResponse createdWatchlist = fromJson(createContent, WatchlistDetailsResponse.class);

        // When: Updating watchlist
        final UpdateWatchlistRequest updateRequest = new UpdateWatchlistRequest()
            .setName("New Name")
            .setDescription("New Description");

        final MockHttpServletRequestBuilder updateRequestBuilder = put(
            USER_WATCHLIST_PATH.replace("{id}", createdWatchlist.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(updateRequest));

        final ResultActions response = mockMvc.perform(updateRequestBuilder);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Watchlist should be updated
        final String content = response.andReturn().getResponse().getContentAsString();
        final WatchlistDetailsResponse updatedWatchlist = fromJson(content, WatchlistDetailsResponse.class);

        assertThat(updatedWatchlist.getName(), is("New Name"));
        assertThat(updatedWatchlist.getDescription(), is("New Description"));
        assertThat(updatedWatchlist.getUpdatedAt(), is(notNullValue()));
    }

    @Test
    @DisplayName("Should update watchlist channel order successfully")
    public void updateWatchlistChannelOrderSuccess() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // And: User has channels
        final Channel channel1 = aChannelForUser(user, "Channel 1");
        final Channel channel2 = aChannelForUser(user, "Channel 2");
        final Channel channel3 = aChannelForUser(user, "Channel 3");

        // And: User has a watchlist with channels
        final CreateWatchlistRequest createRequest = new CreateWatchlistRequest()
            .setName("My Watchlist")
            .setChannelIds(List.of(channel1.getId(), channel2.getId(), channel3.getId()));

        final ResultActions createResponse = mockMvc.perform(
            post(USER_WATCHLISTS_PATH)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
                .content(toJson(createRequest))
        );

        final String createContent = createResponse.andReturn().getResponse().getContentAsString();
        final WatchlistDetailsResponse createdWatchlist = fromJson(createContent, WatchlistDetailsResponse.class);

        // When: Updating channel order (reversed)
        final UpdateWatchlistRequest updateRequest = new UpdateWatchlistRequest()
            .setName("My Watchlist")
            .setChannelIds(List.of(channel3.getId(), channel2.getId(), channel1.getId()));

        final MockHttpServletRequestBuilder updateRequestBuilder = put(
            USER_WATCHLIST_PATH.replace("{id}", createdWatchlist.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(updateRequest));

        final ResultActions response = mockMvc.perform(updateRequestBuilder);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Channel order should be updated
        final String content = response.andReturn().getResponse().getContentAsString();
        final WatchlistDetailsResponse updatedWatchlist = fromJson(content, WatchlistDetailsResponse.class);

        assertThat(updatedWatchlist.getChannels(), hasSize(3));
        assertThat(updatedWatchlist.getChannels().get(0).getId(), is(channel3.getId()));
        assertThat(updatedWatchlist.getChannels().get(1).getId(), is(channel2.getId()));
        assertThat(updatedWatchlist.getChannels().get(2).getId(), is(channel1.getId()));
    }

    @Test
    @DisplayName("Should delete watchlist successfully")
    public void deleteWatchlistSuccess() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // And: User has a watchlist
        final CreateWatchlistRequest createRequest = new CreateWatchlistRequest()
            .setName("Watchlist to Delete");

        final ResultActions createResponse = mockMvc.perform(
            post(USER_WATCHLISTS_PATH)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
                .content(toJson(createRequest))
        );

        final String createContent = createResponse.andReturn().getResponse().getContentAsString();
        final WatchlistDetailsResponse createdWatchlist = fromJson(createContent, WatchlistDetailsResponse.class);

        // When: Deleting the watchlist
        final MockHttpServletRequestBuilder deleteRequest = delete(USER_WATCHLIST_PATH.replace("{id}", createdWatchlist.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(deleteRequest);

        // Then: Response should be NO_CONTENT or OK
        assertThat(response.andReturn().getResponse().getStatus(), anyOf(is(SC_NO_CONTENT), is(SC_OK)));

        // And: Watchlist should not be retrievable
        final MockHttpServletRequestBuilder getRequest = get(USER_WATCHLIST_PATH.replace("{id}", createdWatchlist.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        final ResultActions getResponse = mockMvc.perform(getRequest);
        getResponse.andExpect(status().is(SC_NOT_FOUND));
    }

    @Test
    @DisplayName("Should fail when name is null")
    public void createWatchlistFailsWhenNameIsNull() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // And: Request with null name
        final CreateWatchlistRequest request = new CreateWatchlistRequest();

        // When: Creating watchlist
        final MockHttpServletRequestBuilder createRequest = post(USER_WATCHLISTS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: Response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));

        // And: Validation error should contain name field
        final String content = response.andReturn().getResponse().getContentAsString();
        final ValidationErrorResponseResource error = fromJson(content, ValidationErrorResponseResource.class);
        assertThat(error.getErrors().stream().anyMatch(e -> e.getField().equals("name")), is(true));
    }

    @Test
    @DisplayName("Should fail when name exceeds 100 characters")
    public void createWatchlistFailsWhenNameTooLong() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // And: Request with name exceeding 100 characters
        final String longName = "a".repeat(101);
        final CreateWatchlistRequest request = new CreateWatchlistRequest()
            .setName(longName);

        // When: Creating watchlist
        final MockHttpServletRequestBuilder createRequest = post(USER_WATCHLISTS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: Response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should fail when description exceeds 500 characters")
    public void createWatchlistFailsWhenDescriptionTooLong() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // And: Request with description exceeding 500 characters
        final String longDescription = "a".repeat(501);
        final CreateWatchlistRequest request = new CreateWatchlistRequest()
            .setName("Valid Name")
            .setDescription(longDescription);

        // When: Creating watchlist
        final MockHttpServletRequestBuilder createRequest = post(USER_WATCHLISTS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: Response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));
    }
}
