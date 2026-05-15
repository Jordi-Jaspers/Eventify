package io.github.eventify.api.channel.controller;

import io.github.eventify.api.authentication.model.Role;
import io.github.eventify.api.channel.model.request.ChannelBatchRequest;
import io.github.eventify.api.channel.model.request.CreateChannelRequest;
import io.github.eventify.api.channel.model.request.UpdateChannelRequest;
import io.github.eventify.api.channel.model.response.ChannelDetailsResponse;
import io.github.eventify.api.user.model.User;
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
    @DisplayName("Should pause channels in batch successfully")
    public void batchPauseChannelsSuccess() throws Exception {
        // Given: An authenticated user with two active channels
        final User user = aValidatedUser();

        final ChannelDetailsResponse channel1 = createUserChannel(user, "Batch Pause Channel 1", "batch.pause.1");
        final ChannelDetailsResponse channel2 = createUserChannel(user, "Batch Pause Channel 2", "batch.pause.2");

        // When: Pausing both channels in a batch
        final ChannelBatchRequest batchRequest = new ChannelBatchRequest()
            .setChannelIds(List.of(channel1.getId(), channel2.getId()));

        final MockHttpServletRequestBuilder pauseRequest = post(USER_CHANNELS_PAUSE_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(batchRequest));

        final ResultActions response = mockMvc.perform(pauseRequest);

        // Then: Response should be NO_CONTENT
        response.andExpect(status().is(SC_NO_CONTENT));

        // And: Both channels should be PAUSED
        assertUserChannelStatus(user, channel1.getId(), "PAUSED");
        assertUserChannelStatus(user, channel2.getId(), "PAUSED");
    }

    @Test
    @DisplayName("Should be idempotent when batch pausing already paused channels")
    public void batchPauseChannelsIdempotentWhenAlreadyPaused() throws Exception {
        // Given: An authenticated user with a paused channel
        final User user = aValidatedUser();

        final ChannelDetailsResponse channel = createUserChannel(user, "Already Paused Channel", "batch.pause.idempotent");

        // And: Channel is already paused
        mockMvc.perform(
            post(USER_CHANNELS_PAUSE_PATH)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
                .content(toJson(new ChannelBatchRequest().setChannelIds(List.of(channel.getId()))))
        );

        // When: Pausing again
        final MockHttpServletRequestBuilder pauseRequest = post(USER_CHANNELS_PAUSE_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(new ChannelBatchRequest().setChannelIds(List.of(channel.getId()))));

        final ResultActions response = mockMvc.perform(pauseRequest);

        // Then: Response should be NO_CONTENT (idempotent)
        response.andExpect(status().is(SC_NO_CONTENT));
    }

    @Test
    @DisplayName("Should resume channels in batch successfully")
    public void batchResumeChannelsSuccess() throws Exception {
        // Given: An authenticated user with two paused channels
        final User user = aValidatedUser();

        final ChannelDetailsResponse channel1 = createUserChannel(user, "Batch Resume Channel 1", "batch.resume.1");
        final ChannelDetailsResponse channel2 = createUserChannel(user, "Batch Resume Channel 2", "batch.resume.2");

        // And: Both channels are paused
        mockMvc.perform(
            post(USER_CHANNELS_PAUSE_PATH)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
                .content(toJson(new ChannelBatchRequest().setChannelIds(List.of(channel1.getId(), channel2.getId()))))
        );

        // When: Resuming both channels in a batch
        final ChannelBatchRequest batchRequest = new ChannelBatchRequest()
            .setChannelIds(List.of(channel1.getId(), channel2.getId()));

        final MockHttpServletRequestBuilder resumeRequest = post(USER_CHANNELS_RESUME_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(batchRequest));

        final ResultActions response = mockMvc.perform(resumeRequest);

        // Then: Response should be NO_CONTENT
        response.andExpect(status().is(SC_NO_CONTENT));

        // And: Both channels should be ACTIVE
        assertUserChannelStatus(user, channel1.getId(), "ACTIVE");
        assertUserChannelStatus(user, channel2.getId(), "ACTIVE");
    }

    @Test
    @DisplayName("Should be idempotent when batch resuming already active channels")
    public void batchResumeChannelsIdempotentWhenAlreadyActive() throws Exception {
        // Given: An authenticated user with an active channel
        final User user = aValidatedUser();

        final ChannelDetailsResponse channel = createUserChannel(user, "Already Active Channel", "batch.resume.idempotent");

        // When: Resuming an already active channel
        final MockHttpServletRequestBuilder resumeRequest = post(USER_CHANNELS_RESUME_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(new ChannelBatchRequest().setChannelIds(List.of(channel.getId()))));

        final ResultActions response = mockMvc.perform(resumeRequest);

        // Then: Response should be NO_CONTENT (idempotent)
        response.andExpect(status().is(SC_NO_CONTENT));
    }

    @Test
    @DisplayName("Should delete channels in batch successfully")
    public void batchDeleteChannelsSuccess() throws Exception {
        // Given: An authenticated user with two channels
        final User user = aValidatedUser();

        final ChannelDetailsResponse channel1 = createUserChannel(user, "Batch Delete Channel 1", "batch.delete.1");
        final ChannelDetailsResponse channel2 = createUserChannel(user, "Batch Delete Channel 2", "batch.delete.2");

        // When: Deleting both channels in a batch
        final ChannelBatchRequest batchRequest = new ChannelBatchRequest()
            .setChannelIds(List.of(channel1.getId(), channel2.getId()));

        final MockHttpServletRequestBuilder deleteRequest = delete(USER_CHANNELS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(batchRequest));

        final ResultActions response = mockMvc.perform(deleteRequest);

        // Then: Response should be NO_CONTENT
        response.andExpect(status().is(SC_NO_CONTENT));
    }

    @Test
    @DisplayName("Should not show batch-deleted channels in search")
    public void searchDoesNotShowBatchDeletedChannels() throws Exception {
        // Given: An authenticated user with two channels
        final User user = aValidatedUser();

        final ChannelDetailsResponse activeChannel = createUserChannel(user, "Active Channel", "batch.delete.search.active");
        final ChannelDetailsResponse deletedChannel = createUserChannel(user, "Deleted Channel", "batch.delete.search.deleted");

        // And: One channel is batch-deleted
        mockMvc.perform(
            delete(USER_CHANNELS_PATH)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
                .content(toJson(new ChannelBatchRequest().setChannelIds(List.of(deletedChannel.getId()))))
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
    @DisplayName("Should fail when batch deleting already deleted channels")
    public void batchDeleteChannelsFailsWhenAlreadyDeleted() throws Exception {
        // Given: An authenticated user with a deleted channel
        final User user = aValidatedUser();

        final ChannelDetailsResponse channel = createUserChannel(user, "Already Deleted Channel", "batch.delete.already.deleted");

        // And: Channel is already deleted
        mockMvc.perform(
            delete(USER_CHANNELS_PATH)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
                .content(toJson(new ChannelBatchRequest().setChannelIds(List.of(channel.getId()))))
        );

        // When: Attempting to delete again
        final MockHttpServletRequestBuilder deleteRequest = delete(USER_CHANNELS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(new ChannelBatchRequest().setChannelIds(List.of(channel.getId()))));

        final ResultActions response = mockMvc.perform(deleteRequest);

        // Then: Response should be FORBIDDEN (deleted channels are not accessible via security check)
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should fail when batch deleting non-existent channels")
    public void batchDeleteChannelsFailsWhenNotFound() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // When: Deleting non-existent channels
        final MockHttpServletRequestBuilder deleteRequest = delete(USER_CHANNELS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(new ChannelBatchRequest().setChannelIds(List.of(99999L))));

        final ResultActions response = mockMvc.perform(deleteRequest);

        // Then: Response should be FORBIDDEN (security check fails before reaching service layer)
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should fail when batch deleting another user's channels")
    public void batchDeleteChannelsFailsWhenNotOwner() throws Exception {
        // Given: Two authenticated users
        final User user1 = aValidatedUser();
        final User user2 = aValidatedUser();

        // And: User1 has a channel
        final ChannelDetailsResponse channel = createUserChannel(user1, "User1 Channel", "batch.delete.not.owner");

        // When: User2 attempts to batch-delete User1's channel
        final MockHttpServletRequestBuilder deleteRequest = delete(USER_CHANNELS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user2.getAccessToken().getValue())
            .content(toJson(new ChannelBatchRequest().setChannelIds(List.of(channel.getId()))));

        final ResultActions response = mockMvc.perform(deleteRequest);

        // Then: Response should be FORBIDDEN (access denied by security)
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should fail when batch pausing another user's channels")
    public void batchPauseChannelsFailsWhenNotOwner() throws Exception {
        // Given: Two authenticated users
        final User user1 = aValidatedUser();
        final User user2 = aValidatedUser();

        // And: User1 has an active channel
        final ChannelDetailsResponse channel = createUserChannel(user1, "User1 Active Channel", "batch.pause.not.owner");

        // When: User2 attempts to batch-pause User1's channel
        final MockHttpServletRequestBuilder pauseRequest = post(USER_CHANNELS_PAUSE_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user2.getAccessToken().getValue())
            .content(toJson(new ChannelBatchRequest().setChannelIds(List.of(channel.getId()))));

        final ResultActions response = mockMvc.perform(pauseRequest);

        // Then: Response should be FORBIDDEN (access denied by security)
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should fail when batch resuming another user's channels")
    public void batchResumeChannelsFailsWhenNotOwner() throws Exception {
        // Given: Two authenticated users
        final User user1 = aValidatedUser();
        final User user2 = aValidatedUser();

        // And: User1 has a channel
        final ChannelDetailsResponse channel = createUserChannel(user1, "User1 Paused Channel", "batch.resume.not.owner");

        // And: Channel is paused
        mockMvc.perform(
            post(USER_CHANNELS_PAUSE_PATH)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user1.getAccessToken().getValue())
                .content(toJson(new ChannelBatchRequest().setChannelIds(List.of(channel.getId()))))
        );

        // When: User2 attempts to batch-resume User1's channel
        final MockHttpServletRequestBuilder resumeRequest = post(USER_CHANNELS_RESUME_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user2.getAccessToken().getValue())
            .content(toJson(new ChannelBatchRequest().setChannelIds(List.of(channel.getId()))));

        final ResultActions response = mockMvc.perform(resumeRequest);

        // Then: Response should be FORBIDDEN (access denied by security)
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should allow admin to batch delete any user's channels")
    public void adminCanBatchDeleteAnyUserChannel() throws Exception {
        // Given: Regular user owns a channel
        final User regularUser = aValidatedUser();

        final ChannelDetailsResponse channel = createUserChannel(regularUser, "Regular User Channel", "batch.delete.admin");

        // And: Admin user exists
        final User adminUser = aValidatedUserWithRole(Role.ADMIN);

        // When: Admin batch-deletes the channel
        final MockHttpServletRequestBuilder deleteRequest = delete(USER_CHANNELS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + adminUser.getAccessToken().getValue())
            .content(toJson(new ChannelBatchRequest().setChannelIds(List.of(channel.getId()))));

        final ResultActions response = mockMvc.perform(deleteRequest);

        // Then: Response should be NO_CONTENT
        response.andExpect(status().is(SC_NO_CONTENT));
    }

    @Test
    @DisplayName("Should fail when batch request has empty channelIds")
    public void batchPauseChannelsFailsWhenEmptyIds() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // When: Sending batch request with empty channelIds
        final ChannelBatchRequest batchRequest = new ChannelBatchRequest()
            .setChannelIds(List.of());

        final MockHttpServletRequestBuilder pauseRequest = post(USER_CHANNELS_PAUSE_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(batchRequest));

        final ResultActions response = mockMvc.perform(pauseRequest);

        // Then: Response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should fail when batch request body is null")
    public void batchDeleteChannelsFailsWhenNullBody() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // When: Sending batch delete with no body
        final MockHttpServletRequestBuilder deleteRequest = delete(USER_CHANNELS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(deleteRequest);

        // Then: Response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should fail entire batch when mix of valid and invalid channel IDs")
    public void batchPauseChannelsFailsWhenMixedValidInvalidIds() throws Exception {
        // Given: An authenticated user with one valid channel
        final User user = aValidatedUser();

        final ChannelDetailsResponse channel = createUserChannel(user, "Valid Channel", "batch.pause.mixed");

        // When: Pausing with a mix of valid and non-existent IDs
        final ChannelBatchRequest batchRequest = new ChannelBatchRequest()
            .setChannelIds(List.of(channel.getId(), 99999L));

        final MockHttpServletRequestBuilder pauseRequest = post(USER_CHANNELS_PAUSE_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(batchRequest));

        final ResultActions response = mockMvc.perform(pauseRequest);

        // Then: Entire batch should fail (all-or-nothing)
        response.andExpect(status().is(SC_FORBIDDEN));

        // And: Valid channel should NOT be paused
        assertUserChannelStatus(user, channel.getId(), "ACTIVE");
    }

    @Test
    @DisplayName("Should work with single channel ID in batch (backward compat)")
    public void batchPauseSingleChannelSuccess() throws Exception {
        // Given: An authenticated user with one channel
        final User user = aValidatedUser();

        final ChannelDetailsResponse channel = createUserChannel(user, "Single Batch Channel", "batch.pause.single");

        // When: Pausing with a single-element batch
        final ChannelBatchRequest batchRequest = new ChannelBatchRequest()
            .setChannelIds(List.of(channel.getId()));

        final MockHttpServletRequestBuilder pauseRequest = post(USER_CHANNELS_PAUSE_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(batchRequest));

        final ResultActions response = mockMvc.perform(pauseRequest);

        // Then: Response should be NO_CONTENT
        response.andExpect(status().is(SC_NO_CONTENT));

        // And: Channel should be PAUSED
        assertUserChannelStatus(user, channel.getId(), "PAUSED");
    }

    @Test
    @DisplayName("Should fail entire batch delete when mix of valid and non-existent channel IDs")
    public void batchDeleteChannelsFailsWhenMixedValidInvalidIds() throws Exception {
        // Given: An authenticated user with two channels
        final User user = aValidatedUser();

        final ChannelDetailsResponse channel1 = createUserChannel(user, "Valid Delete Channel 1", "batch.delete.mixed.valid.1");
        createUserChannel(user, "Valid Delete Channel 2", "batch.delete.mixed.valid.2");

        // When: Deleting with a mix of valid and non-existent IDs
        final ChannelBatchRequest batchRequest = new ChannelBatchRequest()
            .setChannelIds(List.of(channel1.getId(), 99999L));

        final MockHttpServletRequestBuilder deleteRequest = delete(USER_CHANNELS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(batchRequest));

        final ResultActions response = mockMvc.perform(deleteRequest);

        // Then: Entire batch should fail (all-or-nothing)
        response.andExpect(status().is(SC_FORBIDDEN));

        // And: Valid channel should NOT be deleted (still ACTIVE)
        assertUserChannelStatus(user, channel1.getId(), "ACTIVE");
    }

    @Test
    @DisplayName("Should fail entire batch pause when mix of own and another user's channel IDs")
    public void batchPauseChannelsFailsWhenMixedValidAndOtherUsersChannel() throws Exception {
        // Given: Two authenticated users, each with a channel
        final User user1 = aValidatedUser();
        final User user2 = aValidatedUser();

        final ChannelDetailsResponse ownChannel = createUserChannel(user1, "User1 Own Channel Pause", "batch.pause.mixed.own");
        final ChannelDetailsResponse otherChannel = createUserChannel(user2, "User2 Channel Pause", "batch.pause.mixed.other");

        // When: User1 batch pauses own channel and user2's channel
        final ChannelBatchRequest batchRequest = new ChannelBatchRequest()
            .setChannelIds(List.of(ownChannel.getId(), otherChannel.getId()));

        final MockHttpServletRequestBuilder pauseRequest = post(USER_CHANNELS_PAUSE_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user1.getAccessToken().getValue())
            .content(toJson(batchRequest));

        final ResultActions response = mockMvc.perform(pauseRequest);

        // Then: Entire batch should fail (all-or-nothing)
        response.andExpect(status().is(SC_FORBIDDEN));

        // And: User1's own channel should NOT be paused (still ACTIVE)
        assertUserChannelStatus(user1, ownChannel.getId(), "ACTIVE");
    }

    @Test
    @DisplayName("Should fail entire batch delete when mix of own and another user's channel IDs")
    public void batchDeleteChannelsFailsWhenMixedValidAndOtherUsersChannel() throws Exception {
        // Given: Two authenticated users, each with a channel
        final User user1 = aValidatedUser();
        final User user2 = aValidatedUser();

        final ChannelDetailsResponse ownChannel = createUserChannel(user1, "User1 Own Channel Delete", "batch.delete.mixed.own");
        final ChannelDetailsResponse otherChannel = createUserChannel(user2, "User2 Channel Delete", "batch.delete.mixed.other");

        // When: User1 batch deletes own channel and user2's channel
        final ChannelBatchRequest batchRequest = new ChannelBatchRequest()
            .setChannelIds(List.of(ownChannel.getId(), otherChannel.getId()));

        final MockHttpServletRequestBuilder deleteRequest = delete(USER_CHANNELS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user1.getAccessToken().getValue())
            .content(toJson(batchRequest));

        final ResultActions response = mockMvc.perform(deleteRequest);

        // Then: Entire batch should fail (all-or-nothing)
        response.andExpect(status().is(SC_FORBIDDEN));

        // And: User1's own channel should NOT be deleted (still ACTIVE)
        assertUserChannelStatus(user1, ownChannel.getId(), "ACTIVE");
    }

    @Test
    @DisplayName("Should fail entire batch delete when mix of active and already-deleted channel IDs")
    public void batchDeleteChannelsFailsWhenMixedValidAndAlreadyDeleted() throws Exception {
        // Given: An authenticated user with two channels
        final User user = aValidatedUser();

        final ChannelDetailsResponse remainingChannel = createUserChannel(user, "Remaining Channel", "batch.delete.mixed.remaining");
        final ChannelDetailsResponse deletedChannel = createUserChannel(user, "Pre-Deleted Channel", "batch.delete.mixed.predeleted");

        // And: One channel is already deleted
        mockMvc.perform(
            delete(USER_CHANNELS_PATH)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
                .content(toJson(new ChannelBatchRequest().setChannelIds(List.of(deletedChannel.getId()))))
        );

        // When: Batch deleting remaining channel together with already-deleted channel
        final ChannelBatchRequest batchRequest = new ChannelBatchRequest()
            .setChannelIds(List.of(remainingChannel.getId(), deletedChannel.getId()));

        final MockHttpServletRequestBuilder deleteRequest = delete(USER_CHANNELS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(batchRequest));

        final ResultActions response = mockMvc.perform(deleteRequest);

        // Then: Entire batch should fail (all-or-nothing)
        response.andExpect(status().is(SC_FORBIDDEN));

        // And: Remaining channel should NOT be deleted (still ACTIVE)
        assertUserChannelStatus(user, remainingChannel.getId(), "ACTIVE");
    }

    @Test
    @DisplayName("Should fail entire batch resume when mix of valid and non-existent channel IDs")
    public void batchResumeChannelsFailsWhenMixedValidInvalidIds() throws Exception {
        // Given: An authenticated user with a paused channel
        final User user = aValidatedUser();

        final ChannelDetailsResponse channel = createUserChannel(user, "Paused Channel Resume Mixed", "batch.resume.mixed");

        // And: Channel is paused
        mockMvc.perform(
            post(USER_CHANNELS_PAUSE_PATH)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
                .content(toJson(new ChannelBatchRequest().setChannelIds(List.of(channel.getId()))))
        );

        // When: Resuming with a mix of valid and non-existent IDs
        final ChannelBatchRequest batchRequest = new ChannelBatchRequest()
            .setChannelIds(List.of(channel.getId(), 99999L));

        final MockHttpServletRequestBuilder resumeRequest = post(USER_CHANNELS_RESUME_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(batchRequest));

        final ResultActions response = mockMvc.perform(resumeRequest);

        // Then: Entire batch should fail (all-or-nothing)
        response.andExpect(status().is(SC_FORBIDDEN));

        // And: Valid channel should NOT be resumed (still PAUSED)
        assertUserChannelStatus(user, channel.getId(), "PAUSED");
    }

    // ========================= PRIVATE HELPERS =========================

    private ChannelDetailsResponse createUserChannel(final User user, final String name, final String slug) throws Exception {
        final CreateChannelRequest request = new CreateChannelRequest()
            .setName(name)
            .setSlug(slug);

        final ResultActions createResponse = mockMvc.perform(
            post(USER_CHANNELS_PATH)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
                .content(toJson(request))
        );

        final String content = createResponse.andReturn().getResponse().getContentAsString();
        return fromJson(content, ChannelDetailsResponse.class);
    }

    private void assertUserChannelStatus(final User user, final Long channelId, final String expectedStatus) throws Exception {
        final MockHttpServletRequestBuilder getRequest = get(USER_CHANNEL_PATH.replace("{id}", channelId.toString()))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        final ResultActions getResponse = mockMvc.perform(getRequest);
        final String content = getResponse.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse channel = fromJson(content, ChannelDetailsResponse.class);

        assertThat(channel.getStatus(), is(expectedStatus));
    }
}
