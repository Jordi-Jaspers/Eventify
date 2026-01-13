package io.github.eventify.api.channel.controller;

import io.github.eventify.api.channel.model.request.CreateChannelRequest;
import io.github.eventify.api.channel.model.request.UpdateChannelRequest;
import io.github.eventify.api.channel.model.response.ChannelDetailsResponse;
import io.github.eventify.api.user.model.User;
import io.github.eventify.support.IntegrationTest;
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
            .setName("Simple Channel");

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
            .setName("");

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
            .setName(longName);

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
            .setName("Errors");

        mockMvc.perform(
            post(USER_CHANNELS_PATH)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
                .content(toJson(firstRequest))
        );

        // When: Creating another channel with same name
        final CreateChannelRequest duplicateRequest = new CreateChannelRequest()
            .setName("Errors");

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
            .setName("Unauthorized Channel");

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
                .setName("Channel " + i);

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
                .setName("User1 Channel " + i);

            mockMvc.perform(
                post(USER_CHANNELS_PATH)
                    .contentType(APPLICATION_JSON)
                    .header(AUTHORIZATION, BEARER + user1.getAccessToken().getValue())
                    .content(toJson(request))
            );
        }

        // And: User2 has 1 channel
        final CreateChannelRequest user2Request = new CreateChannelRequest()
            .setName("User2 Channel");

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
            .setName("Test Channel");

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

        // Then: Response should be NOT_FOUND
        response.andExpect(status().is(SC_NOT_FOUND));
    }

    @Test
    @DisplayName("Should fail when getting another user channel")
    public void getChannelByIdFailsWhenNotOwner() throws Exception {
        // Given: Two authenticated users
        final User user1 = aValidatedUser();
        final User user2 = aValidatedUser();

        // And: User1 has a channel
        final CreateChannelRequest createRequest = new CreateChannelRequest()
            .setName("User1 Channel");

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

        // Then: Response should be NOT_FOUND (not FORBIDDEN to avoid leaking)
        response.andExpect(status().is(SC_NOT_FOUND));
    }

    @Test
    @DisplayName("Should update channel details successfully")
    public void updateChannelSuccess() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // And: User has a channel
        final CreateChannelRequest createRequest = new CreateChannelRequest()
            .setName("Old Name");

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

        // Then: Response should be NOT_FOUND
        response.andExpect(status().is(SC_NOT_FOUND));
    }

    @Test
    @DisplayName("Should pause channel successfully")
    public void pauseChannelSuccess() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // And: User has an active channel
        final CreateChannelRequest createRequest = new CreateChannelRequest()
            .setName("Active Channel");

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
            .setName("Paused Channel");

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
            .setName("Paused Channel");

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
            .setName("Active Channel");

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
            .setName("Channel to Delete");

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
            .setName("Active Channel");

        mockMvc.perform(
            post(USER_CHANNELS_PATH)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
                .content(toJson(createRequest1))
        );

        final CreateChannelRequest createRequest2 = new CreateChannelRequest()
            .setName("Deleted Channel");

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
            .setName("Deleted Channel");

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

        // Then: Response should be NOT_FOUND
        response.andExpect(status().is(SC_NOT_FOUND));
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

        // Then: Response should be NOT_FOUND
        response.andExpect(status().is(SC_NOT_FOUND));
    }

    @Test
    @DisplayName("Should fail when deleting another user channel")
    public void deleteChannelFailsWhenNotOwner() throws Exception {
        // Given: Two authenticated users
        final User user1 = aValidatedUser();
        final User user2 = aValidatedUser();

        // And: User1 has a channel
        final CreateChannelRequest createRequest = new CreateChannelRequest()
            .setName("User1 Channel");

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

        // Then: Response should be NOT_FOUND (not FORBIDDEN to avoid leaking)
        response.andExpect(status().is(SC_NOT_FOUND));
    }
}
