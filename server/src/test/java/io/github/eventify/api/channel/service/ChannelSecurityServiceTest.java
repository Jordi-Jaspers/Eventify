package io.github.eventify.api.channel.service;

import io.github.eventify.api.apikey.model.ApiKeyScope;
import io.github.eventify.api.channel.model.Channel;
import io.github.eventify.api.channel.model.ChannelStatus;
import io.github.eventify.api.channel.repository.ChannelRepository;
import io.github.eventify.api.organization.model.Organization;
import io.github.eventify.api.user.model.User;
import io.github.eventify.common.exception.ChannelAccessDeniedException;
import io.github.eventify.common.exception.ChannelPausedException;
import io.github.eventify.common.security.principal.ApiKeyPrincipal;
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

@DisplayName("Unit Test - Channel Security Service")
public class ChannelSecurityServiceTest extends UnitTest {

    @Mock
    private ChannelRepository channelRepository;

    @InjectMocks
    private ChannelSecurityService channelSecurityService;

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
    @DisplayName("Should return true when personal principal accesses own channel")
    public void shouldReturnTrueWhenPersonalPrincipalAccessesOwnChannel() {
        // Given: Personal API key principal, personal channel owned by same user
        final ApiKeyPrincipal principal = aPersonalPrincipal(user1);
        final Channel channel = aPersonalChannel(user1);

        when(channelRepository.findById(channel.getId())).thenReturn(Optional.of(channel));

        // When: canAccess called
        final boolean result = channelSecurityService.canAccess(channel.getId(), principal);

        // Then: Returns true
        assertThat(result, is(true));
    }

    @Test
    @DisplayName("Should deny when personal principal accesses another users channel")
    public void shouldDenyWhenPersonalPrincipalAccessesAnotherUsersChannel() {
        // Given: Personal principal, personal channel owned by different user
        final ApiKeyPrincipal principal = aPersonalPrincipal(user1);
        final Channel channel = aPersonalChannel(user2);

        when(channelRepository.findById(channel.getId())).thenReturn(Optional.of(channel));

        // When/Then: ChannelAccessDeniedException thrown
        assertThrows(
            ChannelAccessDeniedException.class,
            () -> channelSecurityService.canAccess(channel.getId(), principal)
        );
    }

    @Test
    @DisplayName("Should deny when personal principal accesses org channel")
    public void shouldDenyWhenPersonalPrincipalAccessesOrgChannel() {
        // Given: Personal principal, organization channel
        final ApiKeyPrincipal principal = aPersonalPrincipal(user1);
        final Channel channel = anOrgChannel(user1, org1);

        when(channelRepository.findById(channel.getId())).thenReturn(Optional.of(channel));

        // When/Then: ChannelAccessDeniedException thrown
        assertThrows(
            ChannelAccessDeniedException.class,
            () -> channelSecurityService.canAccess(channel.getId(), principal)
        );
    }

    @Test
    @DisplayName("Should return true when org principal accesses org channel")
    public void shouldReturnTrueWhenOrgPrincipalAccessesOrgChannel() {
        // Given: Org principal for Org A, channel belonging to Org A
        final ApiKeyPrincipal principal = anOrgPrincipal(user1, org1);
        final Channel channel = anOrgChannel(user1, org1);

        when(channelRepository.findById(channel.getId())).thenReturn(Optional.of(channel));

        // When: canAccess called
        final boolean result = channelSecurityService.canAccess(channel.getId(), principal);

        // Then: Returns true
        assertThat(result, is(true));
    }

    @Test
    @DisplayName("Should deny when org principal accesses different org channel")
    public void shouldDenyWhenOrgPrincipalAccessesDifferentOrgChannel() {
        // Given: Org principal for Org A, channel belonging to Org B
        final ApiKeyPrincipal principal = anOrgPrincipal(user1, org1);
        final Channel channel = anOrgChannel(user2, org2);

        when(channelRepository.findById(channel.getId())).thenReturn(Optional.of(channel));

        // When/Then: ChannelAccessDeniedException thrown
        assertThrows(
            ChannelAccessDeniedException.class,
            () -> channelSecurityService.canAccess(channel.getId(), principal)
        );
    }

    @Test
    @DisplayName("Should deny when org principal accesses personal channel")
    public void shouldDenyWhenOrgPrincipalAccessesPersonalChannel() {
        // Given: Org principal, personal channel
        final ApiKeyPrincipal principal = anOrgPrincipal(user1, org1);
        final Channel channel = aPersonalChannel(user1);

        when(channelRepository.findById(channel.getId())).thenReturn(Optional.of(channel));

        // When/Then: ChannelAccessDeniedException thrown
        assertThrows(
            ChannelAccessDeniedException.class,
            () -> channelSecurityService.canAccess(channel.getId(), principal)
        );
    }

    @Test
    @DisplayName("Should deny when principal is null")
    public void shouldDenyWhenPrincipalIsNull() {
        // Given: Null principal, valid channel ID
        final ApiKeyPrincipal nullPrincipal = null;
        final Long channelId = 1L;

        // When/Then: ChannelAccessDeniedException thrown
        assertThrows(
            ChannelAccessDeniedException.class,
            () -> channelSecurityService.canAccess(channelId, nullPrincipal)
        );
    }

    @Test
    @DisplayName("Should throw not found when channel does not exist")
    public void shouldThrowNotFoundWhenChannelDoesNotExist() {
        // Given: Valid principal, non-existent channel ID
        final ApiKeyPrincipal principal = aPersonalPrincipal(user1);
        final Long nonExistentChannelId = 999L;

        when(channelRepository.findById(nonExistentChannelId)).thenReturn(Optional.empty());

        // When/Then: DataNotFoundException thrown
        assertThrows(
            DataNotFoundException.class,
            () -> channelSecurityService.canAccess(nonExistentChannelId, principal)
        );
    }

    @Test
    @DisplayName("Should throw paused when channel is paused")
    public void shouldThrowPausedWhenChannelIsPaused() {
        // Given: Valid principal, paused channel
        final ApiKeyPrincipal principal = aPersonalPrincipal(user1);
        final Channel channel = aPersonalChannel(user1);
        channel.setStatus(ChannelStatus.PAUSED);

        when(channelRepository.findById(channel.getId())).thenReturn(Optional.of(channel));

        // When/Then: ChannelPausedException thrown
        assertThrows(
            ChannelPausedException.class,
            () -> channelSecurityService.canAccess(channel.getId(), principal)
        );
    }

    @Test
    @DisplayName("Should throw not found when channel is pending deletion")
    public void shouldThrowNotFoundWhenChannelIsPendingDeletion() {
        // Given: Valid principal, channel with PENDING_DELETION status
        final ApiKeyPrincipal principal = aPersonalPrincipal(user1);
        final Channel channel = aPersonalChannel(user1);
        channel.setStatus(ChannelStatus.PENDING_DELETION);

        when(channelRepository.findById(channel.getId())).thenReturn(Optional.of(channel));

        // When/Then: DataNotFoundException thrown
        assertThrows(
            DataNotFoundException.class,
            () -> channelSecurityService.canAccess(channel.getId(), principal)
        );
    }

    // ===== Factory Methods =====

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

    /**
     * Creates a personal API key principal (organizationId=null).
     *
     * @param user the user who owns the API key
     * @return personal API key principal
     */
    private ApiKeyPrincipal aPersonalPrincipal(final User user) {
        return new ApiKeyPrincipal(
            1L,
            "evt_",
            ApiKeyScope.USER,
            user.getId(),
            user,
            null
        );
    }

    /**
     * Creates an organization API key principal (organizationId!=null).
     *
     * @param user the user who created the API key
     * @param org  the organization that owns the API key
     * @return organization API key principal
     */
    private ApiKeyPrincipal anOrgPrincipal(final User user, final Organization org) {
        return new ApiKeyPrincipal(
            1L,
            "org_",
            ApiKeyScope.ORGANIZATION,
            user.getId(),
            user,
            org.getId()
        );
    }
}
