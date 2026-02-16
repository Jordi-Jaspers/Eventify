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
        final Channel channel = aPersonalChannel("my-channel", user1);

        when(channelRepository.findBySlugAndPrincipal(channel.getSlug(), principal))
            .thenReturn(Optional.of(channel));

        // When: canAccess called
        final boolean result = channelSecurityService.canAccess(channel.getSlug(), principal);

        // Then: Returns true
        assertThat(result, is(true));
    }

    @Test
    @DisplayName("Should return true when channel not in scope - service handles 404")
    public void shouldReturnTrueWhenChannelNotInScopeToLetServiceHandle404() {
        // Given: Personal principal, slug that doesn't resolve in their scope
        final ApiKeyPrincipal principal = aPersonalPrincipal(user1);
        final String slug = "other-user-channel";

        // Slug not found for user1's scope (could be another user's or non-existent)
        when(channelRepository.findBySlugAndPrincipal(slug, principal))
            .thenReturn(Optional.empty());

        // When: canAccess called
        final boolean result = channelSecurityService.canAccess(slug, principal);

        // Then: Returns true (let service layer handle 404 to prevent enumeration)
        assertThat(result, is(true));
    }

    @Test
    @DisplayName("Should return true when personal principal accesses org channel - service handles 404")
    public void shouldReturnTrueWhenPersonalPrincipalAccessesOrgChannel() {
        // Given: Personal principal, organization channel (not in personal scope)
        final ApiKeyPrincipal principal = aPersonalPrincipal(user1);
        final String slug = "org-channel";

        // Slug not found in personal scope (it's an org channel)
        when(channelRepository.findBySlugAndPrincipal(slug, principal))
            .thenReturn(Optional.empty());

        // When: canAccess called
        final boolean result = channelSecurityService.canAccess(slug, principal);

        // Then: Returns true (let service layer handle 404)
        assertThat(result, is(true));
    }

    @Test
    @DisplayName("Should return true when org principal accesses org channel")
    public void shouldReturnTrueWhenOrgPrincipalAccessesOrgChannel() {
        // Given: Org principal for Org A, channel belonging to Org A
        final ApiKeyPrincipal principal = anOrgPrincipal(user1, org1);
        final Channel channel = anOrgChannel("org-channel", user1, org1);

        when(channelRepository.findBySlugAndPrincipal(channel.getSlug(), principal))
            .thenReturn(Optional.of(channel));

        // When: canAccess called
        final boolean result = channelSecurityService.canAccess(channel.getSlug(), principal);

        // Then: Returns true
        assertThat(result, is(true));
    }

    @Test
    @DisplayName("Should return true when org principal accesses different org channel - service handles 404")
    public void shouldReturnTrueWhenOrgPrincipalAccessesDifferentOrgChannel() {
        // Given: Org principal for Org A, slug not in Org A's scope
        final ApiKeyPrincipal principal = anOrgPrincipal(user1, org1);
        final String slug = "other-org-channel";

        // Slug not found in org1's scope (it belongs to org2 or doesn't exist)
        when(channelRepository.findBySlugAndPrincipal(slug, principal))
            .thenReturn(Optional.empty());

        // When: canAccess called
        final boolean result = channelSecurityService.canAccess(slug, principal);

        // Then: Returns true (let service layer handle 404)
        assertThat(result, is(true));
    }

    @Test
    @DisplayName("Should return true when org principal accesses personal channel - service handles 404")
    public void shouldReturnTrueWhenOrgPrincipalAccessesPersonalChannel() {
        // Given: Org principal, personal channel slug (not in org scope)
        final ApiKeyPrincipal principal = anOrgPrincipal(user1, org1);
        final String slug = "personal-channel";

        // Slug not found in org scope (it's a personal channel)
        when(channelRepository.findBySlugAndPrincipal(slug, principal))
            .thenReturn(Optional.empty());

        // When: canAccess called
        final boolean result = channelSecurityService.canAccess(slug, principal);

        // Then: Returns true (let service layer handle 404)
        assertThat(result, is(true));
    }

    @Test
    @DisplayName("Should return false when principal is null")
    public void shouldReturnFalseWhenPrincipalIsNull() {
        // Given: Null principal, valid channel slug
        final ApiKeyPrincipal nullPrincipal = null;
        final String slug = "some-channel";

        // When: canAccess called
        final boolean result = channelSecurityService.canAccess(slug, nullPrincipal);

        // Then: Returns false (Spring Security handles 403)
        assertThat(result, is(false));
    }

    @Test
    @DisplayName("Should return true when slug is null - let validation handle 400")
    public void shouldReturnTrueWhenSlugIsNullToLetValidationHandle400() {
        // Given: Valid principal, null slug
        final ApiKeyPrincipal principal = aPersonalPrincipal(user1);
        final String nullSlug = null;

        // When: canAccess called
        final boolean result = channelSecurityService.canAccess(nullSlug, principal);

        // Then: Returns true (let validation layer throw 400, not security 403)
        assertThat(result, is(true));
    }

    @Test
    @DisplayName("Should return true when slug is blank - let validation handle 400")
    public void shouldReturnTrueWhenSlugIsBlankToLetValidationHandle400() {
        // Given: Valid principal, blank slug
        final ApiKeyPrincipal principal = aPersonalPrincipal(user1);
        final String blankSlug = "   ";

        // When: canAccess called
        final boolean result = channelSecurityService.canAccess(blankSlug, principal);

        // Then: Returns true (let validation layer throw 400, not security 403)
        assertThat(result, is(true));
    }

    @Test
    @DisplayName("Should return true when channel does not exist - service handles 404")
    public void shouldReturnTrueWhenChannelDoesNotExistToLetServiceHandle404() {
        // Given: Valid principal, non-existent channel slug
        final ApiKeyPrincipal principal = aPersonalPrincipal(user1);
        final String nonExistentSlug = "non-existent-channel";

        when(channelRepository.findBySlugAndPrincipal(nonExistentSlug, principal))
            .thenReturn(Optional.empty());

        // When: Checking access
        final boolean result = channelSecurityService.canAccess(nonExistentSlug, principal);

        // Then: Returns true (let service layer throw 404 to prevent enumeration)
        assertThat(result, is(true));
    }

    // ===== Factory Methods =====

    /**
     * Creates a personal channel (organization=null).
     *
     * @param slug the channel slug
     * @param user the user who owns the channel
     * @return personal channel
     */
    private Channel aPersonalChannel(final String slug, final User user) {
        final Channel channel = new Channel();
        channel.setId(1L);
        channel.setSlug(slug);
        channel.setName("Personal Channel");
        channel.setUser(user);
        channel.setOrganization(null);
        channel.setStatus(ChannelStatus.ACTIVE);
        return channel;
    }

    /**
     * Creates an organization channel (organization!=null).
     *
     * @param slug the channel slug
     * @param user the user who created the channel
     * @param org  the organization that owns the channel
     * @return organization channel
     */
    private Channel anOrgChannel(final String slug, final User user, final Organization org) {
        final Channel channel = new Channel();
        channel.setId(1L);
        channel.setSlug(slug);
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
