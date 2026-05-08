package io.github.eventify.api.channel.controller;

import io.github.eventify.api.authentication.model.Role;
import io.github.eventify.api.channel.model.request.CreateChannelRequest;
import io.github.eventify.api.channel.model.request.UpdateChannelRequest;
import io.github.eventify.api.channel.model.response.ChannelDetailsResponse;
import io.github.eventify.api.user.model.User;
import io.github.eventify.support.IntegrationTest;
import io.github.jframe.datasource.search.model.input.SearchInput;
import io.github.jframe.datasource.search.model.input.SortablePageInput;
import io.github.jframe.datasource.search.model.resource.PageResource;
import io.github.jframe.exception.resource.ApiErrorResponseResource;
import io.github.jframe.exception.resource.ValidationErrorResponseResource;

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

@DisplayName("Integration Test - User Channel Controller")
public class UserChannelControllerTest extends IntegrationTest {

    @Test
    @DisplayName("Should create personal channel successfully")
    public void createChannelSuccess() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // And: A valid create channel request
        final CreateChannelRequest request = new CreateChannelRequest()
            .setName("My App Errors")
            .setSlug("test.channel.1")
            .setDescription("Error logs from production");

        // When: Creating personal channel
        final MockHttpServletRequestBuilder createRequest = post(USER_CHANNELS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: Response should be CREATED
        response.andExpect(status().is(SC_CREATED));

        // And: Response should contain channel details
        final String content = response.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse channelResponse = fromJson(content, ChannelDetailsResponse.class);

        assertThat(channelResponse.getId(), is(notNullValue()));
        assertThat(channelResponse.getName(), is("My App Errors"));
        assertThat(channelResponse.getDescription(), is("Error logs from production"));
        assertThat(channelResponse.getStatus(), is("ACTIVE"));
        assertThat(channelResponse.getCreatedAt(), is(notNullValue()));
    }

    @Test
    @DisplayName("Should create channel without description")
    public void createChannelWithoutDescriptionSuccess() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // And: Request without description
        final CreateChannelRequest request = new CreateChannelRequest()
            .setName("Simple Channel")
            .setSlug("test.channel.2");

        // When: Creating personal channel
        final MockHttpServletRequestBuilder createRequest = post(USER_CHANNELS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: Response should be CREATED
        response.andExpect(status().is(SC_CREATED));

        // And: Description should be null
        final String content = response.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse channelResponse = fromJson(content, ChannelDetailsResponse.class);

        assertThat(channelResponse.getDescription(), is(nullValue()));
    }

    @Test
    @DisplayName("Should fail when channel name is empty")
    public void createChannelFailsWhenNameIsEmpty() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // And: Request with empty name
        final CreateChannelRequest request = new CreateChannelRequest()
            .setName("")
            .setSlug("test.channel.3");

        // When: Creating personal channel
        final MockHttpServletRequestBuilder createRequest = post(USER_CHANNELS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: Response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));

        // And: Validation error should contain name field
        final String content = response.andReturn().getResponse().getContentAsString();
        final ValidationErrorResponseResource error = fromJson(content, ValidationErrorResponseResource.class);
        assertThat(error.getErrors().getFirst().getField(), is("name"));
    }

    @Test
    @DisplayName("Should fail when channel name exceeds 100 characters")
    public void createChannelFailsWhenNameTooLong() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // And: Request with name exceeding 100 characters
        final String longName = "a".repeat(101);
        final CreateChannelRequest request = new CreateChannelRequest()
            .setName(longName)
            .setSlug("test.channel.4");

        // When: Creating personal channel
        final MockHttpServletRequestBuilder createRequest = post(USER_CHANNELS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: Response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should fail when description exceeds 500 characters")
    public void createChannelFailsWhenDescriptionTooLong() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // And: Request with description exceeding 500 characters
        final String longDescription = "a".repeat(501);
        final CreateChannelRequest request = new CreateChannelRequest()
            .setName("Valid Name")
            .setSlug("test.channel.5")
            .setDescription(longDescription);

        // When: Creating personal channel
        final MockHttpServletRequestBuilder createRequest = post(USER_CHANNELS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: Response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should fail when duplicate channel name for same user")
    public void createChannelFailsWhenDuplicateName() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // And: User already has channel named "Errors"
        final CreateChannelRequest firstRequest = new CreateChannelRequest()
            .setName("Errors")
            .setSlug("test.channel.6");

        mockMvc.perform(
            post(USER_CHANNELS_PATH)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
                .content(toJson(firstRequest))
        );

        // When: Creating another channel with same name
        final CreateChannelRequest duplicateRequest = new CreateChannelRequest()
            .setName("Errors")
            .setSlug("test.channel.7");

        final MockHttpServletRequestBuilder createRequest = post(USER_CHANNELS_PATH)
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
    @DisplayName("Should fail when unauthenticated user creates channel")
    public void createChannelFailsWhenUnauthenticated() throws Exception {
        // Given: A valid request
        final CreateChannelRequest request = new CreateChannelRequest()
            .setName("Unauthorized Channel")
            .setSlug("test.channel.8");

        // When: Creating channel without authentication
        final MockHttpServletRequestBuilder createRequest = post(USER_CHANNELS_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: Response should be UNAUTHORIZED
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }

    @Test
    @DisplayName("Should search personal channels successfully")
    public void searchChannelsSuccess() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // And: User has 3 personal channels
        for (int i = 1; i <= 3; i++) {
            final CreateChannelRequest createRequest = new CreateChannelRequest()
                .setName("Channel " + i)
                .setSlug("test.channel.9." + i);

            mockMvc.perform(
                post(USER_CHANNELS_PATH)
                    .contentType(APPLICATION_JSON)
                    .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
                    .content(toJson(createRequest))
            );
        }

        // When: Searching personal channels
        final MockHttpServletRequestBuilder searchRequest = post(USER_CHANNELS_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content("{}");

        final ResultActions response = mockMvc.perform(searchRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should contain 3 channels
        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<?> searchResponse = fromJson(content, PageResource.class);

        assertThat(searchResponse.getContent(), hasSize(3));
        assertThat(searchResponse.getTotalElements(), is(3L));
    }

    @Test
    @DisplayName("Should only see own channels in search")
    public void searchChannelsReturnsOnlyOwnChannels() throws Exception {
        // Given: Two authenticated users
        final User user1 = aValidatedUser();
        final User user2 = aValidatedUser();

        // And: User1 has 2 channels
        for (int i = 1; i <= 2; i++) {
            final CreateChannelRequest request = new CreateChannelRequest()
                .setName("User1 Channel " + i)
                .setSlug("test.channel.10." + i);

            mockMvc.perform(
                post(USER_CHANNELS_PATH)
                    .contentType(APPLICATION_JSON)
                    .header(AUTHORIZATION, BEARER + user1.getAccessToken().getValue())
                    .content(toJson(request))
            );
        }

        // And: User2 has 1 channel
        final CreateChannelRequest user2Request = new CreateChannelRequest()
            .setName("User2 Channel")
            .setSlug("test.channel.11");

        mockMvc.perform(
            post(USER_CHANNELS_PATH)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user2.getAccessToken().getValue())
                .content(toJson(user2Request))
        );

        // When: User1 searches channels
        final MockHttpServletRequestBuilder searchRequest = post(USER_CHANNELS_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user1.getAccessToken().getValue())
            .content("{}");

        final ResultActions response = mockMvc.perform(searchRequest);

        // Then: Response should contain only User1 channels
        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<?> searchResponse = fromJson(content, PageResource.class);

        assertThat(searchResponse.getContent(), hasSize(2));
        assertThat(searchResponse.getTotalElements(), is(2L));
    }

    @Test
    @DisplayName("Should return empty list when user has no channels")
    public void searchChannelsReturnsEmptyWhenNoChannels() throws Exception {
        // Given: An authenticated user with no channels
        final User user = aValidatedUser();

        // When: Searching channels
        final MockHttpServletRequestBuilder searchRequest = post(USER_CHANNELS_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content("{}");

        final ResultActions response = mockMvc.perform(searchRequest);

        // Then: Response should be OK with empty list
        response.andExpect(status().is(SC_OK));

        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<?> searchResponse = fromJson(content, PageResource.class);

        assertThat(searchResponse.getContent(), anyOf(nullValue(), is(empty())));
        assertThat(searchResponse.getTotalElements(), is(0L));
    }

    @Test
    @DisplayName("Should get channel by ID successfully")
    public void getChannelByIdSuccess() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // And: User has a channel
        final CreateChannelRequest createRequest = new CreateChannelRequest()
            .setName("Test Channel")
            .setSlug("test.channel.12");

        final ResultActions createResponse = mockMvc.perform(
            post(USER_CHANNELS_PATH)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
                .content(toJson(createRequest))
        );

        final String createContent = createResponse.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse createdChannel = fromJson(createContent, ChannelDetailsResponse.class);

        // When: Getting channel by ID
        final MockHttpServletRequestBuilder getRequest = get(USER_CHANNEL_PATH.replace("{id}", createdChannel.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(getRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should contain channel details
        final String content = response.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse channelResponse = fromJson(content, ChannelDetailsResponse.class);

        assertThat(channelResponse.getId(), is(createdChannel.getId()));
        assertThat(channelResponse.getName(), is("Test Channel"));
    }

    @Test
    @DisplayName("Should fail when getting non-existent channel")
    public void getChannelByIdFailsWhenNotFound() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // When: Getting non-existent channel
        final MockHttpServletRequestBuilder getRequest = get(USER_CHANNEL_PATH.replace("{id}", "99999"))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(getRequest);

        // Then: Response should be FORBIDDEN (security check fails before reaching service layer)
        // This is intentional - we don't reveal whether a resource exists or not
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should fail when getting another user channel")
    public void getChannelByIdFailsWhenNotOwner() throws Exception {
        // Given: Two authenticated users
        final User user1 = aValidatedUser();
        final User user2 = aValidatedUser();

        // And: User1 has a channel
        final CreateChannelRequest createRequest = new CreateChannelRequest()
            .setName("User1 Channel")
            .setSlug("test.channel.13");

        final ResultActions createResponse = mockMvc.perform(
            post(USER_CHANNELS_PATH)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user1.getAccessToken().getValue())
                .content(toJson(createRequest))
        );

        final String createContent = createResponse.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse createdChannel = fromJson(createContent, ChannelDetailsResponse.class);

        // When: User2 attempts to get User1's channel
        final MockHttpServletRequestBuilder getRequest = get(USER_CHANNEL_PATH.replace("{id}", createdChannel.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user2.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(getRequest);

        // Then: Response should be FORBIDDEN (access denied by security)
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should update channel details successfully")
    public void updateChannelSuccess() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // And: User has a channel
        final CreateChannelRequest createRequest = new CreateChannelRequest()
            .setName("Old Name")
            .setSlug("test.channel.14");

        final ResultActions createResponse = mockMvc.perform(
            post(USER_CHANNELS_PATH)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
                .content(toJson(createRequest))
        );

        final String createContent = createResponse.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse createdChannel = fromJson(createContent, ChannelDetailsResponse.class);

        // When: Updating channel details
        final UpdateChannelRequest updateRequest = new UpdateChannelRequest()
            .setName("New Name")

            .setDescription("New Description");

        final MockHttpServletRequestBuilder updateRequestBuilder = put(USER_CHANNEL_PATH.replace("{id}", createdChannel.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(updateRequest));

        final ResultActions response = mockMvc.perform(updateRequestBuilder);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Channel should be updated
        final String content = response.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse updatedChannel = fromJson(content, ChannelDetailsResponse.class);

        assertThat(updatedChannel.getName(), is("New Name"));
        assertThat(updatedChannel.getDescription(), is("New Description"));
        assertThat(updatedChannel.getUpdatedAt(), is(notNullValue()));
    }

    @Test
    @DisplayName("Should fail when updating non-existent channel")
    public void updateChannelFailsWhenNotFound() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // And: Update request
        final UpdateChannelRequest updateRequest = new UpdateChannelRequest()
            .setName("New Name");

        // When: Updating non-existent channel
        final MockHttpServletRequestBuilder updateRequestBuilder = put(USER_CHANNEL_PATH.replace("{id}", "99999"))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(updateRequest));

        final ResultActions response = mockMvc.perform(updateRequestBuilder);

        // Then: Response should be FORBIDDEN (security check fails before reaching service layer)
        // This is intentional - we don't reveal whether a resource exists or not
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should pause channel successfully")
    public void pauseChannelSuccess() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // And: User has an active channel
        final CreateChannelRequest createRequest = new CreateChannelRequest()
            .setName("Active Channel")
            .setSlug("test.channel.15");

        final ResultActions createResponse = mockMvc.perform(
            post(USER_CHANNELS_PATH)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
                .content(toJson(createRequest))
        );

        final String createContent = createResponse.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse createdChannel = fromJson(createContent, ChannelDetailsResponse.class);

        // When: Pausing the channel
        final MockHttpServletRequestBuilder pauseRequest = post(USER_CHANNEL_PAUSE_PATH.replace("{id}", createdChannel.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(pauseRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Channel status should be PAUSED
        final String content = response.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse pausedChannel = fromJson(content, ChannelDetailsResponse.class);

        assertThat(pausedChannel.getStatus(), is("PAUSED"));
    }

    @Test
    @DisplayName("Should be idempotent when pausing already paused channel")
    public void pauseChannelIdempotentWhenAlreadyPaused() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // And: User has a paused channel
        final CreateChannelRequest createRequest = new CreateChannelRequest()
            .setName("Paused Channel")
            .setSlug("test.channel.16");

        final ResultActions createResponse = mockMvc.perform(
            post(USER_CHANNELS_PATH)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
                .content(toJson(createRequest))
        );

        final String createContent = createResponse.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse createdChannel = fromJson(createContent, ChannelDetailsResponse.class);

        // And: Channel is already paused
        mockMvc.perform(
            post(USER_CHANNEL_PAUSE_PATH.replace("{id}", createdChannel.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
        );

        // When: Pausing again
        final MockHttpServletRequestBuilder pauseRequest = post(USER_CHANNEL_PAUSE_PATH.replace("{id}", createdChannel.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(pauseRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Channel should still be PAUSED
        final String content = response.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse channel = fromJson(content, ChannelDetailsResponse.class);

        assertThat(channel.getStatus(), is("PAUSED"));
    }

    @Test
    @DisplayName("Should resume channel successfully")
    public void resumeChannelSuccess() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // And: User has a paused channel
        final CreateChannelRequest createRequest = new CreateChannelRequest()
            .setName("Paused Channel")
            .setSlug("test.channel.17");

        final ResultActions createResponse = mockMvc.perform(
            post(USER_CHANNELS_PATH)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
                .content(toJson(createRequest))
        );

        final String createContent = createResponse.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse createdChannel = fromJson(createContent, ChannelDetailsResponse.class);

        // And: Channel is paused
        mockMvc.perform(
            post(USER_CHANNEL_PAUSE_PATH.replace("{id}", createdChannel.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
        );

        // When: Resuming the channel
        final MockHttpServletRequestBuilder resumeRequest = post(
            USER_CHANNEL_RESUME_PATH.replace("{id}", createdChannel.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(resumeRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Channel status should be ACTIVE
        final String content = response.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse resumedChannel = fromJson(content, ChannelDetailsResponse.class);

        assertThat(resumedChannel.getStatus(), is("ACTIVE"));
    }

    @Test
    @DisplayName("Should be idempotent when resuming already active channel")
    public void resumeChannelIdempotentWhenAlreadyActive() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // And: User has an active channel
        final CreateChannelRequest createRequest = new CreateChannelRequest()
            .setName("Active Channel")
            .setSlug("test.channel.18");

        final ResultActions createResponse = mockMvc.perform(
            post(USER_CHANNELS_PATH)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
                .content(toJson(createRequest))
        );

        final String createContent = createResponse.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse createdChannel = fromJson(createContent, ChannelDetailsResponse.class);

        // When: Resuming active channel
        final MockHttpServletRequestBuilder resumeRequest = post(
            USER_CHANNEL_RESUME_PATH.replace("{id}", createdChannel.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(resumeRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Channel should still be ACTIVE
        final String content = response.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse channel = fromJson(content, ChannelDetailsResponse.class);

        assertThat(channel.getStatus(), is("ACTIVE"));
    }

    @Test
    @DisplayName("Should delete channel successfully")
    public void deleteChannelSuccess() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // And: User has a channel
        final CreateChannelRequest createRequest = new CreateChannelRequest()
            .setName("Channel to Delete")
            .setSlug("test.channel.19");

        final ResultActions createResponse = mockMvc.perform(
            post(USER_CHANNELS_PATH)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
                .content(toJson(createRequest))
        );

        final String createContent = createResponse.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse createdChannel = fromJson(createContent, ChannelDetailsResponse.class);

        // When: Deleting the channel
        final MockHttpServletRequestBuilder deleteRequest = delete(USER_CHANNEL_PATH.replace("{id}", createdChannel.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(deleteRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Channel status should be PENDING_DELETION
        final String content = response.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse deletedChannel = fromJson(content, ChannelDetailsResponse.class);

        assertThat(deletedChannel.getStatus(), is("PENDING_DELETION"));
    }

    @Test
    @DisplayName("Should not show deleted channel in search")
    public void searchDoesNotShowDeletedChannels() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // And: User has 2 channels, one deleted
        final CreateChannelRequest createRequest1 = new CreateChannelRequest()
            .setName("Active Channel")
            .setSlug("test.channel.20");

        mockMvc.perform(
            post(USER_CHANNELS_PATH)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
                .content(toJson(createRequest1))
        );

        final CreateChannelRequest createRequest2 = new CreateChannelRequest()
            .setName("Deleted Channel")
            .setSlug("test.channel.21");

        final ResultActions createResponse = mockMvc.perform(
            post(USER_CHANNELS_PATH)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
                .content(toJson(createRequest2))
        );

        final String createContent = createResponse.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse createdChannel = fromJson(createContent, ChannelDetailsResponse.class);

        // And: Second channel is deleted
        mockMvc.perform(
            delete(USER_CHANNEL_PATH.replace("{id}", createdChannel.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
        );

        // When: Searching channels
        final MockHttpServletRequestBuilder searchRequest = post(USER_CHANNELS_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content("{}");

        final ResultActions response = mockMvc.perform(searchRequest);

        // Then: Only active channel should be returned
        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<?> searchResponse = fromJson(content, PageResource.class);

        assertThat(searchResponse.getContent(), hasSize(1));
        assertThat(searchResponse.getTotalElements(), is(1L));
    }

    @Test
    @DisplayName("Should fail when deleting already deleted channel")
    public void deleteChannelFailsWhenAlreadyDeleted() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // And: User has a deleted channel
        final CreateChannelRequest createRequest = new CreateChannelRequest()
            .setName("Deleted Channel")
            .setSlug("test.channel.22");

        final ResultActions createResponse = mockMvc.perform(
            post(USER_CHANNELS_PATH)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
                .content(toJson(createRequest))
        );

        final String createContent = createResponse.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse createdChannel = fromJson(createContent, ChannelDetailsResponse.class);

        // And: Channel is already deleted
        mockMvc.perform(
            delete(USER_CHANNEL_PATH.replace("{id}", createdChannel.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
        );

        // When: Attempting to delete again
        final MockHttpServletRequestBuilder deleteRequest = delete(USER_CHANNEL_PATH.replace("{id}", createdChannel.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(deleteRequest);

        // Then: Response should be FORBIDDEN (deleted channels are not accessible via security check)
        // This is intentional - we don't reveal whether a resource exists or is deleted
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should fail when deleting non-existent channel")
    public void deleteChannelFailsWhenNotFound() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // When: Deleting non-existent channel
        final MockHttpServletRequestBuilder deleteRequest = delete(USER_CHANNEL_PATH.replace("{id}", "99999"))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(deleteRequest);

        // Then: Response should be FORBIDDEN (security check fails before reaching service layer)
        // This is intentional - we don't reveal whether a resource exists or not
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should fail when deleting another user channel")
    public void deleteChannelFailsWhenNotOwner() throws Exception {
        // Given: Two authenticated users
        final User user1 = aValidatedUser();
        final User user2 = aValidatedUser();

        // And: User1 has a channel
        final CreateChannelRequest createRequest = new CreateChannelRequest()
            .setName("User1 Channel")
            .setSlug("test.channel.23");

        final ResultActions createResponse = mockMvc.perform(
            post(USER_CHANNELS_PATH)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user1.getAccessToken().getValue())
                .content(toJson(createRequest))
        );

        final String createContent = createResponse.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse createdChannel = fromJson(createContent, ChannelDetailsResponse.class);

        // When: User2 attempts to delete User1's channel
        final MockHttpServletRequestBuilder deleteRequest = delete(USER_CHANNEL_PATH.replace("{id}", createdChannel.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user2.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(deleteRequest);

        // Then: Response should be FORBIDDEN (access denied by security)
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should return only active channels when filtering by status ACTIVE")
    public void searchByStatus_shouldReturnOnlyActiveChannels() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // And: User has 2 active channels
        final CreateChannelRequest activeRequest1 = new CreateChannelRequest()
            .setName("Active Channel 1")
            .setSlug("test.channel.24");
        final ResultActions createActiveResponse1 = mockMvc.perform(
            post(USER_CHANNELS_PATH)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
                .content(toJson(activeRequest1))
        );
        createActiveResponse1.andExpect(status().is(SC_CREATED));

        final CreateChannelRequest activeRequest2 = new CreateChannelRequest()
            .setName("Active Channel 2")
            .setSlug("test.channel.25");
        final ResultActions createActiveResponse2 = mockMvc.perform(
            post(USER_CHANNELS_PATH)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
                .content(toJson(activeRequest2))
        );
        createActiveResponse2.andExpect(status().is(SC_CREATED));

        // And: User has 1 paused channel
        final CreateChannelRequest pausedRequest = new CreateChannelRequest()
            .setName("Paused Channel")
            .setSlug("test.channel.26");
        final ResultActions createPausedResponse = mockMvc.perform(
            post(USER_CHANNELS_PATH)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
                .content(toJson(pausedRequest))
        );
        createPausedResponse.andExpect(status().is(SC_CREATED));

        final String pausedContent = createPausedResponse.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse pausedChannel = fromJson(pausedContent, ChannelDetailsResponse.class);

        // And: Pause the third channel
        mockMvc.perform(
            post(USER_CHANNEL_PAUSE_PATH.replace("{id}", pausedChannel.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
        );

        // When: Searching channels with status filter ACTIVE
        final SortablePageInput searchInput = new SortablePageInput();
        searchInput.setPageNumber(0);
        searchInput.setPageSize(10);

        final SearchInput statusFilter = new SearchInput();
        statusFilter.setFieldName("status");
        statusFilter.setTextValue("ACTIVE");
        searchInput.getSearchInputs().add(statusFilter);

        final MockHttpServletRequestBuilder searchRequest = post(USER_CHANNELS_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(searchInput));

        final ResultActions response = mockMvc.perform(searchRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Only active channels should be returned
        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<?> searchResponse = fromJson(content, PageResource.class);

        assertThat(searchResponse.getContent(), hasSize(2));
        assertThat(searchResponse.getTotalElements(), is(2L));
    }

    @Test
    @DisplayName("Should fail when updating another user's channel")
    public void updateChannelFailsWhenNotOwner() throws Exception {
        // Given: Two authenticated users
        final User user1 = aValidatedUser();
        final User user2 = aValidatedUser();

        // And: User1 has a channel
        final CreateChannelRequest createRequest = new CreateChannelRequest()
            .setName("User1 Channel")
            .setSlug("test.channel.27");

        final ResultActions createResponse = mockMvc.perform(
            post(USER_CHANNELS_PATH)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user1.getAccessToken().getValue())
                .content(toJson(createRequest))
        );

        final String createContent = createResponse.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse createdChannel = fromJson(createContent, ChannelDetailsResponse.class);

        // When: User2 attempts to update User1's channel
        final UpdateChannelRequest updateRequest = new UpdateChannelRequest()
            .setName("Updated by User2");

        final MockHttpServletRequestBuilder updateRequestBuilder = put(USER_CHANNEL_PATH.replace("{id}", createdChannel.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user2.getAccessToken().getValue())
            .content(toJson(updateRequest));

        final ResultActions response = mockMvc.perform(updateRequestBuilder);

        // Then: Response should be FORBIDDEN (access denied by security)
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should fail when pausing another user's channel")
    public void pauseChannelFailsWhenNotOwner() throws Exception {
        // Given: Two authenticated users
        final User user1 = aValidatedUser();
        final User user2 = aValidatedUser();

        // And: User1 has an active channel
        final CreateChannelRequest createRequest = new CreateChannelRequest()
            .setName("User1 Active Channel")
            .setSlug("test.channel.28");

        final ResultActions createResponse = mockMvc.perform(
            post(USER_CHANNELS_PATH)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user1.getAccessToken().getValue())
                .content(toJson(createRequest))
        );

        final String createContent = createResponse.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse createdChannel = fromJson(createContent, ChannelDetailsResponse.class);

        // When: User2 attempts to pause User1's channel
        final MockHttpServletRequestBuilder pauseRequest = post(USER_CHANNEL_PAUSE_PATH.replace("{id}", createdChannel.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user2.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(pauseRequest);

        // Then: Response should be FORBIDDEN (access denied by security)
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should fail when resuming another user's channel")
    public void resumeChannelFailsWhenNotOwner() throws Exception {
        // Given: Two authenticated users
        final User user1 = aValidatedUser();
        final User user2 = aValidatedUser();

        // And: User1 has a paused channel
        final CreateChannelRequest createRequest = new CreateChannelRequest()
            .setName("User1 Paused Channel")
            .setSlug("test.channel.29");

        final ResultActions createResponse = mockMvc.perform(
            post(USER_CHANNELS_PATH)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user1.getAccessToken().getValue())
                .content(toJson(createRequest))
        );

        final String createContent = createResponse.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse createdChannel = fromJson(createContent, ChannelDetailsResponse.class);

        // And: Channel is paused
        mockMvc.perform(
            post(USER_CHANNEL_PAUSE_PATH.replace("{id}", createdChannel.getId().toString()))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user1.getAccessToken().getValue())
        );

        // When: User2 attempts to resume User1's channel
        final MockHttpServletRequestBuilder resumeRequest = post(
            USER_CHANNEL_RESUME_PATH.replace("{id}", createdChannel.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user2.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(resumeRequest);

        // Then: Response should be FORBIDDEN (access denied by security)
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should allow admin to access any user's channel")
    public void adminCanAccessAnyUserChannel() throws Exception {
        // Given: Regular user owns a channel
        final User regularUser = aValidatedUser();

        final CreateChannelRequest createRequest = new CreateChannelRequest()
            .setName("Regular User Channel")
            .setSlug("test.channel.30");

        final ResultActions createResponse = mockMvc.perform(
            post(USER_CHANNELS_PATH)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + regularUser.getAccessToken().getValue())
                .content(toJson(createRequest))
        );

        final String createContent = createResponse.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse createdChannel = fromJson(createContent, ChannelDetailsResponse.class);

        // And: Admin user exists
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);

        // When: Admin calls GET /channels/{id}
        final MockHttpServletRequestBuilder getRequest = get(USER_CHANNEL_PATH.replace("{id}", createdChannel.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(getRequest);

        // Then: Response should be OK with channel details
        response.andExpect(status().is(SC_OK));

        final String content = response.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse channelResponse = fromJson(content, ChannelDetailsResponse.class);

        assertThat(channelResponse.getId(), is(createdChannel.getId()));
        assertThat(channelResponse.getName(), is("Regular User Channel"));
    }

    @Test
    @DisplayName("Should allow admin to delete any user's channel")
    public void adminCanDeleteAnyUserChannel() throws Exception {
        // Given: Regular user owns a channel
        final User regularUser = aValidatedUser();

        final CreateChannelRequest createRequest = new CreateChannelRequest()
            .setName("Regular User Channel")
            .setSlug("test.channel.31");

        final ResultActions createResponse = mockMvc.perform(
            post(USER_CHANNELS_PATH)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + regularUser.getAccessToken().getValue())
                .content(toJson(createRequest))
        );

        final String createContent = createResponse.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse createdChannel = fromJson(createContent, ChannelDetailsResponse.class);

        // And: Admin user exists
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);

        // When: Admin calls DELETE /channels/{id}
        final MockHttpServletRequestBuilder deleteRequest = delete(USER_CHANNEL_PATH.replace("{id}", createdChannel.getId().toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(deleteRequest);

        // Then: Response should be OK, channel status is PENDING_DELETION
        response.andExpect(status().is(SC_OK));

        final String content = response.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse deletedChannel = fromJson(content, ChannelDetailsResponse.class);

        assertThat(deletedChannel.getStatus(), is("PENDING_DELETION"));
    }
}
