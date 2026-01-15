package io.github.eventify.api.channel.service;

import io.github.eventify.api.apikey.model.ApiKey;
import io.github.eventify.api.channel.model.Channel;
import io.github.eventify.api.channel.model.ChannelStatus;
import io.github.eventify.api.channel.repository.ChannelRepository;
import io.github.eventify.api.organization.model.Organization;
import io.github.eventify.api.user.model.User;
import io.github.eventify.common.exception.ChannelAccessDeniedException;
import io.github.eventify.common.exception.ChannelPausedException;
import io.github.eventify.support.UnitTest;
import io.github.jframe.exception.core.DataNotFoundException;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@DisplayName("Unit Test - Channel Access Service")
public class ChannelAccessServiceTest extends UnitTest {

    @Mock
    private ChannelRepository channelRepository;

    @InjectMocks
    private ChannelAccessService channelAccessService;

    private User user1;
    private User user2;
    private Organization org1;
    private Organization org2;

    @BeforeEach
    public void setUp() {
        user1 = aValidUser();
        user1.setId(1L);

        user2 = aValidUser();
        user2.setId(2L);
        user2.setEmail("user2@example.com");

        org1 = anOrganization(1L, "Test Org 1", "test-org-1");
        org2 = anOrganization(2L, "Test Org 2", "test-org-2");
    }

    @Test
    @DisplayName("Should allow access when personal API key accesses own channel")
    public void shouldAllowAccessWhenPersonalApiKeyAccessesOwnChannel() {
        // Given: Personal API key (org=null), personal channel owned by same user
        final ApiKey apiKey = aPersonalApiKey(user1);
        final Channel channel = aPersonalChannel(user1);

        when(channelRepository.findById(channel.getId())).thenReturn(Optional.of(channel));

        // When: validateAccess called
        channelAccessService.validateAccess(apiKey, channel.getId());

        // Then: No exception thrown (access granted)
        assertThat(apiKey.getUser().getId(), is(user1.getId()));
        assertThat(channel.getUser().getId(), is(user1.getId()));
    }

    @Test
    @DisplayName("Should deny access when personal API key accesses another users channel")
    public void shouldDenyAccessWhenPersonalApiKeyAccessesAnotherUsersChannel() {
        // Given: Personal API key, personal channel owned by different user
        final ApiKey apiKey = aPersonalApiKey(user1);
        final Channel channel = aPersonalChannel(user2);

        when(channelRepository.findById(channel.getId())).thenReturn(Optional.of(channel));

        // When: validateAccess called
        // Then: ChannelAccessDeniedException thrown (403)
        assertThrows(
            ChannelAccessDeniedException.class,
            () -> channelAccessService.validateAccess(apiKey, channel.getId())
        );
    }

    @Test
    @DisplayName("Should deny access when personal API key accesses org channel")
    public void shouldDenyAccessWhenPersonalApiKeyAccessesOrgChannel() {
        // Given: Personal API key, organization channel
        final ApiKey apiKey = aPersonalApiKey(user1);
        final Channel channel = anOrgChannel(user1, org1);

        when(channelRepository.findById(channel.getId())).thenReturn(Optional.of(channel));

        // When: validateAccess called
        // Then: ChannelAccessDeniedException thrown (403)
        assertThrows(
            ChannelAccessDeniedException.class,
            () -> channelAccessService.validateAccess(apiKey, channel.getId())
        );
    }

    @Test
    @DisplayName("Should allow access when org API key accesses org channel")
    public void shouldAllowAccessWhenOrgApiKeyAccessesOrgChannel() {
        // Given: Org API key for Org A, channel belonging to Org A
        final ApiKey apiKey = anOrgApiKey(user1, org1);
        final Channel channel = anOrgChannel(user1, org1);

        when(channelRepository.findById(channel.getId())).thenReturn(Optional.of(channel));

        // When: validateAccess called
        channelAccessService.validateAccess(apiKey, channel.getId());

        // Then: No exception thrown (access granted)
        assertThat(apiKey.getOrganization().getId(), is(org1.getId()));
        assertThat(channel.getOrganization().getId(), is(org1.getId()));
    }

    @Test
    @DisplayName("Should deny access when org API key accesses different org channel")
    public void shouldDenyAccessWhenOrgApiKeyAccessesDifferentOrgChannel() {
        // Given: Org API key for Org A, channel belonging to Org B
        final ApiKey apiKey = anOrgApiKey(user1, org1);
        final Channel channel = anOrgChannel(user2, org2);

        when(channelRepository.findById(channel.getId())).thenReturn(Optional.of(channel));

        // When: validateAccess called
        // Then: ChannelAccessDeniedException thrown (403)
        assertThrows(
            ChannelAccessDeniedException.class,
            () -> channelAccessService.validateAccess(apiKey, channel.getId())
        );
    }

    @Test
    @DisplayName("Should deny access when org API key accesses personal channel")
    public void shouldDenyAccessWhenOrgApiKeyAccessesPersonalChannel() {
        // Given: Org API key, personal channel (org=null)
        final ApiKey apiKey = anOrgApiKey(user1, org1);
        final Channel channel = aPersonalChannel(user1);

        when(channelRepository.findById(channel.getId())).thenReturn(Optional.of(channel));

        // When: validateAccess called
        // Then: ChannelAccessDeniedException thrown (403)
        assertThrows(
            ChannelAccessDeniedException.class,
            () -> channelAccessService.validateAccess(apiKey, channel.getId())
        );
    }

    @Test
    @DisplayName("Should throw not found when channel does not exist")
    public void shouldThrowNotFoundWhenChannelDoesNotExist() {
        // Given: Any valid API key, non-existent channel ID
        final ApiKey apiKey = aPersonalApiKey(user1);
        final Long nonExistentChannelId = 999L;

        when(channelRepository.findById(nonExistentChannelId)).thenReturn(Optional.empty());

        // When: validateAccess called
        // Then: ResourceNotFoundException thrown (404)
        assertThrows(
            DataNotFoundException.class,
            () -> channelAccessService.validateAccess(apiKey, nonExistentChannelId)
        );
    }

    @Test
    @DisplayName("Should throw unprocessable when channel is paused")
    public void shouldThrowUnprocessableWhenChannelIsPaused() {
        // Given: Valid API key with proper access, channel with status=PAUSED
        final ApiKey apiKey = aPersonalApiKey(user1);
        final Channel channel = aPersonalChannel(user1);
        channel.setStatus(ChannelStatus.PAUSED);

        when(channelRepository.findById(channel.getId())).thenReturn(Optional.of(channel));

        // When: validateAccess called
        // Then: ChannelPausedException thrown (422) with message "Channel is paused"
        assertThrows(
            ChannelPausedException.class,
            () -> channelAccessService.validateAccess(apiKey, channel.getId())
        );
    }

    @Test
    @DisplayName("Should throw not found when channel is pending deletion")
    public void shouldThrowNotFoundWhenChannelIsPendingDeletion() {
        // Given: Valid API key, channel with status=PENDING_DELETION
        final ApiKey apiKey = aPersonalApiKey(user1);
        final Channel channel = aPersonalChannel(user1);
        channel.setStatus(ChannelStatus.PENDING_DELETION);

        when(channelRepository.findById(channel.getId())).thenReturn(Optional.of(channel));

        // When: validateAccess called
        // Then: ResourceNotFoundException thrown (404, treat as non-existent)
        assertThrows(
            DataNotFoundException.class,
            () -> channelAccessService.validateAccess(apiKey, channel.getId())
        );
    }

    @Test
    @DisplayName("Should throw not found when channel ID is null")
    public void shouldThrowNotFoundWhenChannelIdIsNull() {
        // Given: Valid API key, null channel ID
        final ApiKey apiKey = aPersonalApiKey(user1);
        final Long nullChannelId = null;

        when(channelRepository.findById(nullChannelId)).thenReturn(Optional.empty());

        // When: validateAccess called
        // Then: ResourceNotFoundException thrown (404)
        assertThrows(
            DataNotFoundException.class,
            () -> channelAccessService.validateAccess(apiKey, nullChannelId)
        );
    }

    @Test
    @DisplayName("Should throw illegal argument when API key is null")
    public void shouldThrowIllegalArgumentWhenApiKeyIsNull() {
        // Given: Null API key, valid channel ID
        final ApiKey nullApiKey = null;
        final Long channelId = 1L;

        // When: validateAccess called
        // Then: IllegalArgumentException thrown
        assertThrows(
            IllegalArgumentException.class,
            () -> channelAccessService.validateAccess(nullApiKey, channelId)
        );
    }

    // ===== Factory Methods =====

    /**
     * Creates a personal API key (organization=null).
     *
     * @param user the user who owns the API key
     * @return personal API key
     */
    private ApiKey aPersonalApiKey(final User user) {
        final ApiKey apiKey = new ApiKey();
        apiKey.setId(1L);
        apiKey.setUser(user);
        apiKey.setOrganization(null);
        apiKey.setName("Personal API Key");
        return apiKey;
    }

    /**
     * Creates an organization API key (organization!=null).
     *
     * @param user the user who created the API key
     * @param org  the organization that owns the API key
     * @return organization API key
     */
    private ApiKey anOrgApiKey(final User user, final Organization org) {
        final ApiKey apiKey = new ApiKey();
        apiKey.setId(1L);
        apiKey.setUser(user);
        apiKey.setOrganization(org);
        apiKey.setName("Organization API Key");
        return apiKey;
    }

    /**
     * Creates a personal channel (organization=null).
     *
     * @param user the user who owns the channel
     * @return personal channel
     */
    private Channel aPersonalChannel(final User user) {
        final Channel channel = new Channel();
        channel.setId(1L);
        channel.setName("Personal Channel");
        channel.setUser(user);
        channel.setOrganization(null);
        channel.setStatus(ChannelStatus.ACTIVE);
        return channel;
    }

    /**
     * Creates an organization channel (organization!=null).
     *
     * @param user the user who created the channel
     * @param org  the organization that owns the channel
     * @return organization channel
     */
    private Channel anOrgChannel(final User user, final Organization org) {
        final Channel channel = new Channel();
        channel.setId(1L);
        channel.setName("Organization Channel");
        channel.setUser(user);
        channel.setOrganization(org);
        channel.setStatus(ChannelStatus.ACTIVE);
        return channel;
    }

    /**
     * Creates an organization without invoking the constructor that requires security context.
     *
     * @param id   the organization ID
     * @param name the organization name
     * @param slug the organization slug
     * @return organization
     */
    private Organization anOrganization(final Long id, final String name, final String slug) {
        final Organization org = new Organization();
        org.setId(id);
        org.setName(name);
        org.setSlug(slug);
        return org;
    }
}
