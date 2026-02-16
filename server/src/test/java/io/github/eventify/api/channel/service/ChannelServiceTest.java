package io.github.eventify.api.channel.service;

import io.github.eventify.api.channel.model.Channel;
import io.github.eventify.api.channel.model.ChannelMetaData;
import io.github.eventify.api.channel.model.ChannelStatus;
import io.github.eventify.api.channel.model.request.CreateChannelRequest;
import io.github.eventify.api.channel.model.request.UpdateChannelRequest;
import io.github.eventify.api.channel.repository.ChannelRepository;
import io.github.eventify.api.user.model.User;
import io.github.eventify.common.exception.DuplicateChannelNameException;
import io.github.eventify.common.security.SecurityUtil;
import io.github.eventify.support.UnitTest;
import io.github.jframe.datasource.search.model.input.SortablePageInput;
import io.github.jframe.exception.core.DataNotFoundException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Unit Test - Channel Service")
@SuppressWarnings("unchecked")
public class ChannelServiceTest extends UnitTest {

    @Mock
    private ChannelRepository channelRepository;

    @Spy
    private ChannelMetaData channelMetaData = new ChannelMetaData();

    @InjectMocks
    private ChannelService channelService;

    private MockedStatic<SecurityUtil> securityUtilMock;
    private User user;

    @BeforeEach
    public void setUp() {
        user = aValidUser();
        user.setId(1L);

        securityUtilMock = mockStatic(SecurityUtil.class);
        securityUtilMock.when(SecurityUtil::getLoggedInUser).thenReturn(user);
    }

    @AfterEach
    public void tearDown() {
        if (securityUtilMock != null) {
            securityUtilMock.close();
        }
    }

    @Test
    @DisplayName("Should create personal channel successfully")
    public void shouldCreatePersonalChannelSuccessfully() {
        // Given: A valid create request
        final CreateChannelRequest request = new CreateChannelRequest()
            .setName("My App Errors")
            .setSlug("test.channel.1")
            .setDescription("Error logs from production");

        when(channelRepository.findByUserIdAndNameAndOrganizationIdIsNull(user.getId(), "My App Errors"))
            .thenReturn(Optional.empty());
        when(channelRepository.save(any(Channel.class))).thenAnswer(invocation -> {
            final Channel channel = invocation.getArgument(0);
            channel.setId(1L);
            return channel;
        });

        // When: Creating personal channel
        final Channel channel = channelService.createUserChannel(request);

        // Then: Channel should be created with correct properties
        assertThat(channel, is(notNullValue()));
        assertThat(channel.getId(), is(1L));
        assertThat(channel.getName(), is("My App Errors"));
        assertThat(channel.getDescription(), is("Error logs from production"));
        assertThat(channel.getStatus(), is(ChannelStatus.ACTIVE));
        assertThat(channel.getUser().getId(), is(user.getId()));

        verify(channelRepository).save(any(Channel.class));
    }

    @Test
    @DisplayName("Should create channel without description")
    public void shouldCreateChannelWithoutDescription() {
        // Given: Request without description
        final CreateChannelRequest request = new CreateChannelRequest()
            .setName("Simple Channel")
            .setSlug("test.channel.2");

        when(channelRepository.findByUserIdAndNameAndOrganizationIdIsNull(user.getId(), "Simple Channel"))
            .thenReturn(Optional.empty());
        when(channelRepository.save(any(Channel.class))).thenAnswer(invocation -> {
            final Channel channel = invocation.getArgument(0);
            channel.setId(1L);
            return channel;
        });

        // When: Creating channel
        final Channel channel = channelService.createUserChannel(request);

        // Then: Description should be null
        assertThat(channel.getDescription(), is(nullValue()));
    }

    @Test
    @DisplayName("Should throw when duplicate channel name for same user")
    public void shouldThrowWhenDuplicateChannelName() {
        // Given: User already has channel named "Errors"
        final CreateChannelRequest request = new CreateChannelRequest()
            .setName("Errors")
            .setSlug("test.channel.3");

        final Channel existingChannel = new Channel("Errors", "errors", user, null);
        when(channelRepository.findByUserIdAndNameAndOrganizationIdIsNull(user.getId(), "Errors"))
            .thenReturn(Optional.of(existingChannel));

        // When & Then: Should throw DuplicateChannelNameException
        assertThrows(
            DuplicateChannelNameException.class,
            () -> channelService.createUserChannel(request)
        );

        verify(channelRepository, never()).save(any(Channel.class));
    }

    @Test
    @DisplayName("Should create channel with organization null for personal")
    public void shouldCreateChannelWithOrganizationNull() {
        // Given: A valid personal channel request
        final CreateChannelRequest request = new CreateChannelRequest()
            .setName("Personal Channel")
            .setSlug("test.channel.4");

        when(channelRepository.findByUserIdAndNameAndOrganizationIdIsNull(user.getId(), "Personal Channel"))
            .thenReturn(Optional.empty());
        when(channelRepository.save(any(Channel.class))).thenAnswer(invocation -> {
            final Channel channel = invocation.getArgument(0);
            channel.setId(1L);
            return channel;
        });

        // When: Creating channel
        channelService.createUserChannel(request);

        // Then: Should save with organization null
        verify(channelRepository).save(
            argThat(
                channel -> channel.getOrganization() == null &&
                    channel.getUser().getId().equals(user.getId())
            )
        );
    }

    @Test
    @DisplayName("Should search personal channels successfully")
    public void shouldSearchPersonalChannelsSuccessfully() {
        // Given: User has 3 personal channels
        final Channel channel1 = createChannel(1L, "Channel 1", user);
        final Channel channel2 = createChannel(2L, "Channel 2", user);
        final Channel channel3 = createChannel(3L, "Channel 3", user);

        final Page<Channel> mockPage = new PageImpl<>(List.of(channel1, channel2, channel3));

        when(channelRepository.findAll(any(Specification.class), any(Pageable.class)))
            .thenReturn(mockPage);

        // When: Searching personal channels
        final SortablePageInput input = createDefaultPageInput();
        final Page<Channel> channels = channelService.searchUserChannels(input);

        // Then: Should return all 3 channels
        assertThat(channels, is(notNullValue()));
        assertThat(channels.getContent(), hasSize(3));
        assertThat(channels.getContent().get(0).getName(), is("Channel 1"));
        assertThat(channels.getContent().get(1).getName(), is("Channel 2"));
        assertThat(channels.getContent().get(2).getName(), is("Channel 3"));
    }

    @Test
    @DisplayName("Should exclude deleted channels from search")
    public void shouldExcludeDeletedChannelsFromSearch() {
        // Given: User has channels, some deleted
        final Channel activeChannel = createChannel(1L, "Active", user);

        final Page<Channel> mockPage = new PageImpl<>(List.of(activeChannel));

        when(channelRepository.findAll(any(Specification.class), any(Pageable.class)))
            .thenReturn(mockPage);

        // When: Searching channels
        final SortablePageInput input = createDefaultPageInput();
        final Page<Channel> channels = channelService.searchUserChannels(input);

        // Then: Only active channel should be returned
        assertThat(channels.getContent(), hasSize(1));
        assertThat(channels.getContent().get(0).getName(), is("Active"));
    }

    @Test
    @DisplayName("Should return empty list when user has no channels")
    public void shouldReturnEmptyListWhenUserHasNoChannels() {
        // Given: User has no channels
        final Page<Channel> emptyPage = new PageImpl<>(List.of());

        when(channelRepository.findAll(any(Specification.class), any(Pageable.class)))
            .thenReturn(emptyPage);

        // When: Searching channels
        final SortablePageInput input = createDefaultPageInput();
        final Page<Channel> channels = channelService.searchUserChannels(input);

        // Then: Should return empty list
        assertThat(channels.getContent(), is(empty()));
    }

    @Test
    @DisplayName("Should get channel by ID successfully")
    public void shouldGetChannelByIdSuccessfully() {
        // Given: User has a channel
        final Channel channel = createChannel(1L, "Test Channel", user);

        when(channelRepository.findByIdAndUserIdAndStatusNot(1L, user.getId(), ChannelStatus.PENDING_DELETION))
            .thenReturn(Optional.of(channel));

        // When: Getting channel by ID
        final Channel result = channelService.getUserChannel(1L);

        // Then: Should return channel
        assertThat(result, is(notNullValue()));
        assertThat(result.getId(), is(1L));
        assertThat(result.getName(), is("Test Channel"));
    }

    @Test
    @DisplayName("Should throw when channel not found")
    public void shouldThrowWhenChannelNotFound() {
        // Given: Channel does not exist
        when(channelRepository.findByIdAndUserIdAndStatusNot(999L, user.getId(), ChannelStatus.PENDING_DELETION))
            .thenReturn(Optional.empty());

        // When & Then: Should throw DataNotFoundException
        assertThrows(
            DataNotFoundException.class,
            () -> channelService.getUserChannel(999L)
        );
    }

    @Test
    @DisplayName("Should throw when channel belongs to another user")
    public void shouldThrowWhenChannelBelongsToAnotherUser() {
        // Given: Channel does not belong to current user
        when(channelRepository.findByIdAndUserIdAndStatusNot(1L, user.getId(), ChannelStatus.PENDING_DELETION))
            .thenReturn(Optional.empty());

        // When & Then: Should throw DataNotFoundException
        assertThrows(
            DataNotFoundException.class,
            () -> channelService.getUserChannel(1L)
        );
    }

    @Test
    @DisplayName("Should update channel details successfully")
    public void shouldUpdateChannelDetailsSuccessfully() {
        // Given: User has a channel
        final Channel channel = createChannel(1L, "Old Name", user);

        when(channelRepository.findByIdAndUserIdAndStatusNot(1L, user.getId(), ChannelStatus.PENDING_DELETION))
            .thenReturn(Optional.of(channel));
        when(channelRepository.findByUserIdAndNameAndOrganizationIdIsNull(user.getId(), "New Name"))
            .thenReturn(Optional.empty());
        when(channelRepository.save(any(Channel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When: Updating channel
        final UpdateChannelRequest request = new UpdateChannelRequest()
            .setName("New Name")

            .setDescription("New Description");

        final Channel updated = channelService.updateUserChannel(1L, request);

        // Then: Channel should be updated
        assertThat(updated.getName(), is("New Name"));
        assertThat(updated.getDescription(), is("New Description"));
        assertThat(updated.getUpdatedAt(), is(notNullValue()));

        verify(channelRepository).save(channel);
    }

    @Test
    @DisplayName("Should throw when updating to duplicate name")
    public void shouldThrowWhenUpdatingToDuplicateName() {
        // Given: User has a channel
        final Channel channel = createChannel(1L, "Old Name", user);

        // And: Another channel with target name exists
        final Channel existingChannel = createChannel(2L, "New Name", user);

        when(channelRepository.findByIdAndUserIdAndStatusNot(1L, user.getId(), ChannelStatus.PENDING_DELETION))
            .thenReturn(Optional.of(channel));
        when(channelRepository.findByUserIdAndNameAndOrganizationIdIsNull(user.getId(), "New Name"))
            .thenReturn(Optional.of(existingChannel));

        // When & Then: Should throw DuplicateChannelNameException
        final UpdateChannelRequest request = new UpdateChannelRequest()
            .setName("New Name");

        assertThrows(
            DuplicateChannelNameException.class,
            () -> channelService.updateUserChannel(1L, request)
        );

        verify(channelRepository, never()).save(any(Channel.class));
    }

    @Test
    @DisplayName("Should allow updating to same name")
    public void shouldAllowUpdatingToSameName() {
        // Given: User has a channel
        final Channel channel = createChannel(1L, "Same Name", user);

        when(channelRepository.findByIdAndUserIdAndStatusNot(1L, user.getId(), ChannelStatus.PENDING_DELETION))
            .thenReturn(Optional.of(channel));
        when(channelRepository.findByUserIdAndNameAndOrganizationIdIsNull(user.getId(), "Same Name"))
            .thenReturn(Optional.of(channel)); // Same channel
        when(channelRepository.save(any(Channel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When: Updating with same name but different description
        final UpdateChannelRequest request = new UpdateChannelRequest()
            .setName("Same Name")

            .setDescription("Updated description");

        final Channel updated = channelService.updateUserChannel(1L, request);

        // Then: Should update successfully
        assertThat(updated.getDescription(), is("Updated description"));

        verify(channelRepository).save(channel);
    }

    @Test
    @DisplayName("Should pause channel successfully")
    public void shouldPauseChannelSuccessfully() {
        // Given: User has an active channel
        final Channel channel = createChannel(1L, "Active Channel", user);
        channel.setStatus(ChannelStatus.ACTIVE);

        when(channelRepository.findByIdAndUserIdAndStatusNot(1L, user.getId(), ChannelStatus.PENDING_DELETION))
            .thenReturn(Optional.of(channel));
        when(channelRepository.save(any(Channel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When: Pausing channel
        final Channel paused = channelService.pauseUserChannel(1L);

        // Then: Channel should be paused
        assertThat(paused.getStatus(), is(ChannelStatus.PAUSED));
        assertThat(paused.getUpdatedAt(), is(notNullValue()));

        verify(channelRepository).save(channel);
    }

    @Test
    @DisplayName("Should be idempotent when pausing already paused channel")
    public void shouldBeIdempotentWhenPausingAlreadyPausedChannel() {
        // Given: User has a paused channel
        final Channel channel = createChannel(1L, "Paused Channel", user);
        channel.setStatus(ChannelStatus.PAUSED);

        when(channelRepository.findByIdAndUserIdAndStatusNot(1L, user.getId(), ChannelStatus.PENDING_DELETION))
            .thenReturn(Optional.of(channel));
        when(channelRepository.save(any(Channel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When: Pausing again
        final Channel paused = channelService.pauseUserChannel(1L);

        // Then: Channel should still be paused
        assertThat(paused.getStatus(), is(ChannelStatus.PAUSED));

        verify(channelRepository).save(channel);
    }

    @Test
    @DisplayName("Should resume channel successfully")
    public void shouldResumeChannelSuccessfully() {
        // Given: User has a paused channel
        final Channel channel = createChannel(1L, "Paused Channel", user);
        channel.setStatus(ChannelStatus.PAUSED);

        when(channelRepository.findByIdAndUserIdAndStatusNot(1L, user.getId(), ChannelStatus.PENDING_DELETION))
            .thenReturn(Optional.of(channel));
        when(channelRepository.save(any(Channel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When: Resuming channel
        final Channel resumed = channelService.resumeUserChannel(1L);

        // Then: Channel should be active
        assertThat(resumed.getStatus(), is(ChannelStatus.ACTIVE));
        assertThat(resumed.getUpdatedAt(), is(notNullValue()));

        verify(channelRepository).save(channel);
    }

    @Test
    @DisplayName("Should be idempotent when resuming already active channel")
    public void shouldBeIdempotentWhenResumingAlreadyActiveChannel() {
        // Given: User has an active channel
        final Channel channel = createChannel(1L, "Active Channel", user);
        channel.setStatus(ChannelStatus.ACTIVE);

        when(channelRepository.findByIdAndUserIdAndStatusNot(1L, user.getId(), ChannelStatus.PENDING_DELETION))
            .thenReturn(Optional.of(channel));
        when(channelRepository.save(any(Channel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When: Resuming active channel
        final Channel resumed = channelService.resumeUserChannel(1L);

        // Then: Channel should still be active
        assertThat(resumed.getStatus(), is(ChannelStatus.ACTIVE));

        verify(channelRepository).save(channel);
    }

    @Test
    @DisplayName("Should delete channel successfully")
    public void shouldDeleteChannelSuccessfully() {
        // Given: User has a channel
        final Channel channel = createChannel(1L, "Channel to Delete", user);
        channel.setStatus(ChannelStatus.ACTIVE);

        when(channelRepository.findByIdAndUserIdAndStatusNot(1L, user.getId(), ChannelStatus.PENDING_DELETION))
            .thenReturn(Optional.of(channel));
        when(channelRepository.save(any(Channel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When: Deleting channel
        final Channel deleted = channelService.deleteUserChannel(1L);

        // Then: Channel status should be PENDING_DELETION
        assertThat(deleted.getStatus(), is(ChannelStatus.PENDING_DELETION));
        assertThat(deleted.getUpdatedAt(), is(notNullValue()));

        verify(channelRepository).save(channel);
    }

    @Test
    @DisplayName("Should throw when deleting already deleted channel")
    public void shouldThrowWhenDeletingAlreadyDeletedChannel() {
        // Given: Channel is already deleted
        when(channelRepository.findByIdAndUserIdAndStatusNot(1L, user.getId(), ChannelStatus.PENDING_DELETION))
            .thenReturn(Optional.empty());

        // When & Then: Should throw DataNotFoundException
        assertThrows(
            DataNotFoundException.class,
            () -> channelService.deleteUserChannel(1L)
        );

        verify(channelRepository, never()).save(any(Channel.class));
    }

    @Test
    @DisplayName("Should delete paused channel successfully")
    public void shouldDeletePausedChannelSuccessfully() {
        // Given: User has a paused channel
        final Channel channel = createChannel(1L, "Paused Channel", user);
        channel.setStatus(ChannelStatus.PAUSED);

        when(channelRepository.findByIdAndUserIdAndStatusNot(1L, user.getId(), ChannelStatus.PENDING_DELETION))
            .thenReturn(Optional.of(channel));
        when(channelRepository.save(any(Channel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When: Deleting channel
        final Channel deleted = channelService.deleteUserChannel(1L);

        // Then: Channel should be marked for deletion
        assertThat(deleted.getStatus(), is(ChannelStatus.PENDING_DELETION));
    }

    @Test
    @DisplayName("Should only search channels excluding organization channels")
    public void shouldOnlySearchPersonalChannels() {
        // Given: User has personal channels
        final Channel personalChannel = createChannel(1L, "Personal", user);

        final Page<Channel> mockPage = new PageImpl<>(List.of(personalChannel));

        when(channelRepository.findAll(any(Specification.class), any(Pageable.class)))
            .thenReturn(mockPage);

        // When: Searching channels
        final SortablePageInput input = createDefaultPageInput();
        final Page<Channel> channels = channelService.searchUserChannels(input);

        // Then: Should return only personal channels
        assertThat(channels.getContent(), hasSize(1));

        verify(channelRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    private Channel createChannel(final Long id, final String name, final User user) {
        final Channel channel = new Channel(name, "test.slug." + id, user, null);
        channel.setId(id);
        channel.setStatus(ChannelStatus.ACTIVE);
        channel.setCreatedAt(OffsetDateTime.now().minusDays(1));
        return channel;
    }

    private SortablePageInput createDefaultPageInput() {
        final SortablePageInput input = new SortablePageInput();
        input.setPageNumber(0);
        input.setPageSize(20);
        return input;
    }
}
