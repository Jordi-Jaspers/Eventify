package io.github.eventify.api.channel.service;

import io.github.eventify.api.apikey.model.ApiKeyScope;
import io.github.eventify.api.channel.cache.ChannelCache;
import io.github.eventify.api.channel.model.Channel;
import io.github.eventify.api.channel.model.ChannelStatus;
import io.github.eventify.api.channel.repository.ChannelRepository;
import io.github.eventify.api.organization.model.Organization;
import io.github.eventify.api.user.model.User;
import io.github.eventify.common.security.principal.ApiKeyPrincipal;
import io.github.eventify.support.UnitTest;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@DisplayName("Unit Test - Channel Security Service")
public class ChannelSecurityServiceTest extends UnitTest {

    @Mock
    private ChannelRepository channelRepository;

    @Mock
    private ChannelCache channelCache;

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

        when(channelRepository.findActiveChannelById(channel.getId())).thenReturn(Optional.of(channel));

        // When: canAccess called
        final boolean result = channelSecurityService.canAccess(channel.getId(), principal);

        // Then: Returns true
        assertThat(result, is(true));
    }

    @Test
    @DisplayName("Should return false when personal principal accesses another users channel")
    public void shouldReturnFalseWhenPersonalPrincipalAccessesAnotherUsersChannel() {
        // Given: Personal principal, personal channel owned by different user
        final ApiKeyPrincipal principal = aPersonalPrincipal(user1);
        final Channel channel = aPersonalChannel(user2);

        when(channelRepository.findActiveChannelById(channel.getId())).thenReturn(Optional.of(channel));

        // When: canAccess called
        final boolean result = channelSecurityService.canAccess(channel.getId(), principal);

        // Then: Returns false (Spring Security handles 403)
        assertThat(result, is(false));
    }

    @Test
    @DisplayName("Should return false when personal principal accesses org channel")
    public void shouldReturnFalseWhenPersonalPrincipalAccessesOrgChannel() {
        // Given: Personal principal, organization channel
        final ApiKeyPrincipal principal = aPersonalPrincipal(user1);
        final Channel channel = anOrgChannel(user1, org1);

        when(channelRepository.findActiveChannelById(channel.getId())).thenReturn(Optional.of(channel));

        // When: canAccess called
        final boolean result = channelSecurityService.canAccess(channel.getId(), principal);

        // Then: Returns false (Spring Security handles 403)
        assertThat(result, is(false));
    }

    @Test
    @DisplayName("Should return true when org principal accesses org channel")
    public void shouldReturnTrueWhenOrgPrincipalAccessesOrgChannel() {
        // Given: Org principal for Org A, channel belonging to Org A
        final ApiKeyPrincipal principal = anOrgPrincipal(user1, org1);
        final Channel channel = anOrgChannel(user1, org1);

        when(channelRepository.findActiveChannelById(channel.getId())).thenReturn(Optional.of(channel));

        // When: canAccess called
        final boolean result = channelSecurityService.canAccess(channel.getId(), principal);

        // Then: Returns true
        assertThat(result, is(true));
    }

    @Test
    @DisplayName("Should return false when org principal accesses different org channel")
    public void shouldReturnFalseWhenOrgPrincipalAccessesDifferentOrgChannel() {
        // Given: Org principal for Org A, channel belonging to Org B
        final ApiKeyPrincipal principal = anOrgPrincipal(user1, org1);
        final Channel channel = anOrgChannel(user2, org2);

        when(channelRepository.findActiveChannelById(channel.getId())).thenReturn(Optional.of(channel));

        // When: canAccess called
        final boolean result = channelSecurityService.canAccess(channel.getId(), principal);

        // Then: Returns false (Spring Security handles 403)
        assertThat(result, is(false));
    }

    @Test
    @DisplayName("Should return false when org principal accesses personal channel")
    public void shouldReturnFalseWhenOrgPrincipalAccessesPersonalChannel() {
        // Given: Org principal, personal channel
        final ApiKeyPrincipal principal = anOrgPrincipal(user1, org1);
        final Channel channel = aPersonalChannel(user1);

        when(channelRepository.findActiveChannelById(channel.getId())).thenReturn(Optional.of(channel));

        // When: canAccess called
        final boolean result = channelSecurityService.canAccess(channel.getId(), principal);

        // Then: Returns false (Spring Security handles 403)
        assertThat(result, is(false));
    }

    @Test
    @DisplayName("Should return false when principal is null")
    public void shouldReturnFalseWhenPrincipalIsNull() {
        // Given: Null principal, valid channel ID
        final ApiKeyPrincipal nullPrincipal = null;
        final Long channelId = 1L;

        // When: canAccess called
        final boolean result = channelSecurityService.canAccess(channelId, nullPrincipal);

        // Then: Returns false (Spring Security handles 403)
        assertThat(result, is(false));
    }

    @Test
    @DisplayName("Should return false when channel does not exist (no 404 to avoid leaking existence)")
    public void shouldReturnFalseWhenChannelDoesNotExist() {
        // Given: Valid principal, non-existent channel ID
        final ApiKeyPrincipal principal = aPersonalPrincipal(user1);
        final Long nonExistentChannelId = 999L;

        when(channelRepository.findActiveChannelById(nonExistentChannelId)).thenReturn(Optional.empty());

        // When: Checking access
        final boolean result = channelSecurityService.canAccess(nonExistentChannelId, principal);

        // Then: Returns false (Spring Security will convert to 403)
        assertThat(result, is(false));
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
            null,
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
            org.getId(),
            null
        );
    }
}
