package io.github.eventify.api.channel.service;

import io.github.eventify.api.apikey.model.ApiKeyScope;
import io.github.eventify.api.channel.model.Channel;
import io.github.eventify.api.channel.model.request.CreateChannelRequest;
import io.github.eventify.api.organization.model.Organization;
import io.github.eventify.api.organization.repository.OrganizationRepository;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.user.repository.UserRepository;
import io.github.eventify.common.exception.DuplicateChannelNameException;
import io.github.eventify.common.exception.DuplicateChannelSlugException;
import io.github.eventify.common.security.principal.ApiKeyPrincipal;
import io.github.eventify.support.UnitTest;
import io.github.jframe.exception.core.DataNotFoundException;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Unit Test - API Channel Service")
public class ApiChannelServiceTest extends UnitTest {

    @Mock
    private ChannelCreationService channelCreationService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @InjectMocks
    private ApiChannelService apiChannelService;

    private User user;
    private Organization organization;

    @BeforeEach
    public void setUp() {
        user = aValidUser();
        user.setId(1L);

        organization = new Organization();
        organization.setId(10L);
        organization.setName("Test Organization");
        organization.setSlug("test-org");
    }

    @Nested
    @DisplayName("Personal Channel Creation")
    class PersonalChannelCreation {

        @Test
        @DisplayName("Should create personal channel with personal API key")
        public void shouldCreatePersonalChannel() {
            // Given: Personal API key principal
            final ApiKeyPrincipal principal = aPersonalPrincipal(user);
            final CreateChannelRequest request = new CreateChannelRequest()
                .setName("My Channel")
                .setSlug("my.channel")
                .setDescription("Test description");

            final Channel createdChannel = new Channel("My Channel", "my.channel", user, null);
            createdChannel.setId(100L);
            createdChannel.setDescription("Test description");

            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
            when(channelCreationService.createPersonalChannel(request, user)).thenReturn(createdChannel);

            // When: Creating channel
            final Channel result = apiChannelService.createChannel(request, principal);

            // Then: Channel should be created with correct attributes
            assertThat(result, is(notNullValue()));
            assertThat(result.getName(), is("My Channel"));
            assertThat(result.getSlug(), is("my.channel"));
            assertThat(result.getDescription(), is("Test description"));
            assertThat(result.getOrganization(), is(nullValue()));
            assertThat(result.getUser().getId(), is(user.getId()));

            verify(channelCreationService).createPersonalChannel(request, user);
        }

        @Test
        @DisplayName("Should throw DuplicateChannelSlugException when slug exists")
        public void shouldThrowWhenSlugExists() {
            // Given: Personal API key principal and existing slug
            final ApiKeyPrincipal principal = aPersonalPrincipal(user);
            final CreateChannelRequest request = new CreateChannelRequest()
                .setName("My Channel")
                .setSlug("existing.slug");

            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
            when(channelCreationService.createPersonalChannel(request, user))
                .thenThrow(new DuplicateChannelSlugException());

            // When/Then: Should throw exception
            assertThrows(
                DuplicateChannelSlugException.class,
                () -> apiChannelService.createChannel(request, principal)
            );
        }

        @Test
        @DisplayName("Should throw DuplicateChannelNameException when name exists")
        public void shouldThrowWhenNameExists() {
            // Given: Personal API key principal and existing name
            final ApiKeyPrincipal principal = aPersonalPrincipal(user);
            final CreateChannelRequest request = new CreateChannelRequest()
                .setName("Existing Name")
                .setSlug("new.slug");

            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
            when(channelCreationService.createPersonalChannel(request, user))
                .thenThrow(new DuplicateChannelNameException());

            // When/Then: Should throw exception
            assertThrows(
                DuplicateChannelNameException.class,
                () -> apiChannelService.createChannel(request, principal)
            );
        }

        @Test
        @DisplayName("Should throw DataNotFoundException when user not found")
        public void shouldThrowWhenUserNotFound() {
            // Given: Personal API key principal with non-existent user
            final ApiKeyPrincipal principal = aPersonalPrincipal(user);
            final CreateChannelRequest request = new CreateChannelRequest()
                .setName("My Channel")
                .setSlug("my.channel");

            when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

            // When/Then: Should throw exception
            assertThrows(
                DataNotFoundException.class,
                () -> apiChannelService.createChannel(request, principal)
            );
        }
    }


    @Nested
    @DisplayName("Organization Channel Creation")
    class OrganizationChannelCreation {

        @Test
        @DisplayName("Should create organization channel with org API key")
        public void shouldCreateOrganizationChannel() {
            // Given: Organization API key principal
            final ApiKeyPrincipal principal = anOrgPrincipal(user, organization);
            final CreateChannelRequest request = new CreateChannelRequest()
                .setName("Org Channel")
                .setSlug("org.channel")
                .setDescription("Organization channel");

            final Channel createdChannel = new Channel("Org Channel", "org.channel", user, organization);
            createdChannel.setId(100L);
            createdChannel.setDescription("Organization channel");

            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
            when(organizationRepository.findById(organization.getId())).thenReturn(Optional.of(organization));
            when(channelCreationService.createOrganizationChannel(request, user, organization))
                .thenReturn(createdChannel);

            // When: Creating channel
            final Channel result = apiChannelService.createChannel(request, principal);

            // Then: Channel should be created with organization
            assertThat(result, is(notNullValue()));
            assertThat(result.getName(), is("Org Channel"));
            assertThat(result.getSlug(), is("org.channel"));
            assertThat(result.getOrganization(), is(notNullValue()));
            assertThat(result.getOrganization().getId(), is(organization.getId()));

            verify(channelCreationService).createOrganizationChannel(request, user, organization);
        }

        @Test
        @DisplayName("Should throw DuplicateChannelSlugException when org slug exists")
        public void shouldThrowWhenOrgSlugExists() {
            // Given: Organization API key principal and existing slug
            final ApiKeyPrincipal principal = anOrgPrincipal(user, organization);
            final CreateChannelRequest request = new CreateChannelRequest()
                .setName("Org Channel")
                .setSlug("existing.org.slug");

            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
            when(organizationRepository.findById(organization.getId())).thenReturn(Optional.of(organization));
            when(channelCreationService.createOrganizationChannel(request, user, organization))
                .thenThrow(new DuplicateChannelSlugException());

            // When/Then: Should throw exception
            assertThrows(
                DuplicateChannelSlugException.class,
                () -> apiChannelService.createChannel(request, principal)
            );
        }

        @Test
        @DisplayName("Should throw DuplicateChannelNameException when org name exists")
        public void shouldThrowWhenOrgNameExists() {
            // Given: Organization API key principal and existing name
            final ApiKeyPrincipal principal = anOrgPrincipal(user, organization);
            final CreateChannelRequest request = new CreateChannelRequest()
                .setName("Existing Org Name")
                .setSlug("new.org.slug");

            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
            when(organizationRepository.findById(organization.getId())).thenReturn(Optional.of(organization));
            when(channelCreationService.createOrganizationChannel(request, user, organization))
                .thenThrow(new DuplicateChannelNameException());

            // When/Then: Should throw exception
            assertThrows(
                DuplicateChannelNameException.class,
                () -> apiChannelService.createChannel(request, principal)
            );
        }

        @Test
        @DisplayName("Should throw DataNotFoundException when organization not found")
        public void shouldThrowWhenOrganizationNotFound() {
            // Given: Organization API key principal with non-existent org
            final ApiKeyPrincipal principal = anOrgPrincipal(user, organization);
            final CreateChannelRequest request = new CreateChannelRequest()
                .setName("Org Channel")
                .setSlug("org.channel");

            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
            when(organizationRepository.findById(organization.getId())).thenReturn(Optional.empty());

            // When/Then: Should throw exception
            assertThrows(
                DataNotFoundException.class,
                () -> apiChannelService.createChannel(request, principal)
            );
        }
    }

    // ===== Factory Methods =====

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
