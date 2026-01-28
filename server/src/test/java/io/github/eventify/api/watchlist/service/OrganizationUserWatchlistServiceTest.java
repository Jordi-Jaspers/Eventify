package io.github.eventify.api.watchlist.service;

import io.github.eventify.api.channel.model.Channel;
import io.github.eventify.api.channel.model.ChannelStatus;
import io.github.eventify.api.channel.repository.ChannelRepository;
import io.github.eventify.api.organization.model.Organization;
import io.github.eventify.api.organization.service.OrganizationService;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.watchlist.model.Watchlist;
import io.github.eventify.api.watchlist.model.WatchlistConfiguration;
import io.github.eventify.api.watchlist.model.WatchlistFilters;
import io.github.eventify.api.watchlist.model.WatchlistMetaData;
import io.github.eventify.api.watchlist.repository.WatchlistRepository;
import io.github.eventify.common.exception.DuplicateWatchlistNameException;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Unit Test - Organization Watchlist Service")
@SuppressWarnings("unchecked")
public class OrganizationUserWatchlistServiceTest extends UnitTest {

    @Mock
    private WatchlistRepository watchlistRepository;

    @Mock
    private ChannelRepository channelRepository;

    @Mock
    private OrganizationService organizationService;

    @Mock
    private WatchlistMetaData watchlistMetaData;

    @InjectMocks
    private OrganizationWatchlistService organizationWatchlistService;

    private MockedStatic<SecurityUtil> securityUtilMock;
    private User user;
    private Organization organization;

    @BeforeEach
    public void setUp() {
        user = aValidUser();
        user.setId(1L);

        organization = new Organization();
        organization.setId(10L);
        organization.setName("Test Org");

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
    @DisplayName("Should create organization watchlist successfully")
    public void shouldCreateOrganizationWatchlistSuccessfully() {
        // Given: Valid organization watchlist
        final Watchlist input = new Watchlist();
        input.setName("Org Watchlist");
        input.setDescription("Production monitoring");
        input.setFilters(WatchlistFilters.defaults());

        when(organizationService.getOrganization(organization.getId())).thenReturn(organization);
        when(watchlistRepository.findByOrganizationIdAndName(organization.getId(), "Org Watchlist"))
            .thenReturn(Optional.empty());
        when(watchlistRepository.save(any(Watchlist.class))).thenAnswer(invocation -> {
            final Watchlist watchlist = invocation.getArgument(0);
            watchlist.setId(1L);
            return watchlist;
        });

        // When: Creating organization watchlist
        final Watchlist watchlist = organizationWatchlistService.createWatchlist(organization.getId(), input);

        // Then: Watchlist should be created with correct properties
        assertThat(watchlist, is(notNullValue()));
        assertThat(watchlist.getId(), is(1L));
        assertThat(watchlist.getName(), is("Org Watchlist"));
        assertThat(watchlist.getDescription(), is("Production monitoring"));
        assertThat(watchlist.getUser().getId(), is(user.getId()));
        assertThat(watchlist.getOrganization().getId(), is(organization.getId()));
        assertThat(watchlist.getFilters().getTimeRange(), is(io.github.eventify.api.monitor.model.TimeRange.LAST_24H));

        verify(watchlistRepository).save(any(Watchlist.class));
    }

    @Test
    @DisplayName("Should create organization watchlist with configuration successfully")
    public void shouldCreateOrganizationWatchlistWithConfigurationSuccessfully() {
        // Given: Watchlist with configuration
        final WatchlistConfiguration configuration = WatchlistConfiguration.builder()
            .channels(channelsWithIds(1L, 2L))
            .build();

        final Watchlist input = new Watchlist();
        input.setName("Org Watchlist");
        input.setConfiguration(configuration);

        final Channel channel1 = createOrgChannel(1L, "Channel 1", user, organization);
        final Channel channel2 = createOrgChannel(2L, "Channel 2", user, organization);

        when(organizationService.getOrganization(organization.getId())).thenReturn(organization);
        when(watchlistRepository.findByOrganizationIdAndName(organization.getId(), "Org Watchlist"))
            .thenReturn(Optional.empty());
        when(channelRepository.findAllByIdInAndOrganizationId(List.of(1L, 2L), organization.getId()))
            .thenReturn(List.of(channel1, channel2));
        when(watchlistRepository.save(any(Watchlist.class))).thenAnswer(invocation -> {
            final Watchlist watchlist = invocation.getArgument(0);
            watchlist.setId(1L);
            return watchlist;
        });

        // When: Creating watchlist with configuration
        final Watchlist watchlist = organizationWatchlistService.createWatchlist(organization.getId(), input);

        // Then: Configuration should be set
        assertThat(watchlist, is(notNullValue()));
        assertThat(watchlist.getConfiguration().getChannelIds(), hasItems(1L, 2L));
        verify(watchlistRepository).save(any(Watchlist.class));
    }

    @Test
    @DisplayName("Should fail to create when duplicate name")
    public void shouldFailToCreateWhenDuplicateName() {
        // Given: Organization already has watchlist with same name
        final Watchlist existingWatchlist = new Watchlist();
        existingWatchlist.setName("Org Watchlist");
        existingWatchlist.setOrganization(organization);

        when(organizationService.getOrganization(organization.getId())).thenReturn(organization);
        when(watchlistRepository.findByOrganizationIdAndName(organization.getId(), "Org Watchlist"))
            .thenReturn(Optional.of(existingWatchlist));

        final Watchlist input = new Watchlist();
        input.setName("Org Watchlist");

        // When & Then: Should throw DuplicateWatchlistNameException
        assertThrows(
            DuplicateWatchlistNameException.class,
            () -> organizationWatchlistService.createWatchlist(organization.getId(), input)
        );

        verify(watchlistRepository, never()).save(any(Watchlist.class));
    }

    @Test
    @DisplayName("Should fail when channel not found")
    public void shouldFailWhenChannelNotFound() {
        // Given: Watchlist with non-existent channel ID
        final WatchlistConfiguration configuration = WatchlistConfiguration.builder()
            .channels(channelsWithIds(999L))
            .build();

        final Watchlist input = new Watchlist();
        input.setName("Org Watchlist");
        input.setConfiguration(configuration);

        when(organizationService.getOrganization(organization.getId())).thenReturn(organization);
        when(watchlistRepository.findByOrganizationIdAndName(organization.getId(), "Org Watchlist"))
            .thenReturn(Optional.empty());
        when(channelRepository.findAllByIdInAndOrganizationId(List.of(999L), organization.getId()))
            .thenReturn(List.of());

        // When & Then: Should throw DataNotFoundException
        assertThrows(
            DataNotFoundException.class,
            () -> organizationWatchlistService.createWatchlist(organization.getId(), input)
        );

        verify(watchlistRepository, never()).save(any(Watchlist.class));
    }

    @Test
    @DisplayName("Should fail when channel belongs to user")
    public void shouldFailWhenChannelBelongsToUser() {
        // Given: Channel belongs to user (personal channel)
        final WatchlistConfiguration configuration = WatchlistConfiguration.builder()
            .channels(channelsWithIds(1L))
            .build();

        final Watchlist input = new Watchlist();
        input.setName("Org Watchlist");
        input.setConfiguration(configuration);

        when(organizationService.getOrganization(organization.getId())).thenReturn(organization);
        when(watchlistRepository.findByOrganizationIdAndName(organization.getId(), "Org Watchlist"))
            .thenReturn(Optional.empty());
        // Personal channels are not returned by findAllByIdInAndOrganizationId query
        when(channelRepository.findAllByIdInAndOrganizationId(List.of(1L), organization.getId()))
            .thenReturn(List.of());

        // When & Then: Should throw DataNotFoundException
        assertThrows(
            DataNotFoundException.class,
            () -> organizationWatchlistService.createWatchlist(organization.getId(), input)
        );

        verify(watchlistRepository, never()).save(any(Watchlist.class));
    }

    @Test
    @DisplayName("Should fail when channel belongs to other organization")
    public void shouldFailWhenChannelBelongsToOtherOrg() {
        // Given: Channel belongs to different organization
        final Organization otherOrg = new Organization();
        otherOrg.setId(20L);
        otherOrg.setName("Other Org");

        final WatchlistConfiguration configuration = WatchlistConfiguration.builder()
            .channels(channelsWithIds(1L))
            .build();

        final Watchlist input = new Watchlist();
        input.setName("Org Watchlist");
        input.setConfiguration(configuration);

        when(organizationService.getOrganization(organization.getId())).thenReturn(organization);
        when(watchlistRepository.findByOrganizationIdAndName(organization.getId(), "Org Watchlist"))
            .thenReturn(Optional.empty());
        // Other org channels are not returned by findAllByIdInAndOrganizationId query
        when(channelRepository.findAllByIdInAndOrganizationId(List.of(1L), organization.getId()))
            .thenReturn(List.of());

        // When & Then: Should throw DataNotFoundException
        assertThrows(
            DataNotFoundException.class,
            () -> organizationWatchlistService.createWatchlist(organization.getId(), input)
        );

        verify(watchlistRepository, never()).save(any(Watchlist.class));
    }

    @Test
    @DisplayName("Should get organization watchlist successfully")
    public void shouldGetOrganizationWatchlistSuccessfully() {
        // Given: Organization has a watchlist
        final Watchlist watchlist = createOrgWatchlist(1L, "Org Watchlist", user, organization);

        when(organizationService.getOrganization(organization.getId())).thenReturn(organization);
        when(watchlistRepository.findByIdAndOrganizationId(1L, organization.getId()))
            .thenReturn(Optional.of(watchlist));

        // When: Getting watchlist by ID
        final Watchlist result = organizationWatchlistService.getWatchlist(organization.getId(), 1L);

        // Then: Should return watchlist
        assertThat(result, is(notNullValue()));
        assertThat(result.getId(), is(1L));
        assertThat(result.getName(), is("Org Watchlist"));
        assertThat(result.getOrganization().getId(), is(organization.getId()));
    }

    @Test
    @DisplayName("Should fail to get watchlist from other organization")
    public void shouldFailToGetWatchlistFromOtherOrg() {
        // Given: Watchlist does not belong to current organization
        when(organizationService.getOrganization(organization.getId())).thenReturn(organization);
        when(watchlistRepository.findByIdAndOrganizationId(1L, organization.getId()))
            .thenReturn(Optional.empty());

        // When & Then: Should throw DataNotFoundException
        assertThrows(
            DataNotFoundException.class,
            () -> organizationWatchlistService.getWatchlist(organization.getId(), 1L)
        );
    }

    @Test
    @DisplayName("Should update organization watchlist successfully")
    public void shouldUpdateOrganizationWatchlistSuccessfully() {
        // Given: Organization has a watchlist
        final Watchlist existing = createOrgWatchlist(1L, "Old Name", user, organization);

        final Watchlist updated = new Watchlist();
        updated.setName("New Name");
        updated.setDescription("New Description");

        when(organizationService.getOrganization(organization.getId())).thenReturn(organization);
        when(watchlistRepository.findByIdAndOrganizationId(1L, organization.getId()))
            .thenReturn(Optional.of(existing));
        when(watchlistRepository.findByOrganizationIdAndName(organization.getId(), "New Name"))
            .thenReturn(Optional.empty());
        when(watchlistRepository.save(any(Watchlist.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When: Updating watchlist
        final Watchlist result = organizationWatchlistService.updateWatchlist(organization.getId(), 1L, updated);

        // Then: Watchlist should be updated
        assertThat(result.getName(), is("New Name"));
        assertThat(result.getDescription(), is("New Description"));
        assertThat(result.getUpdatedAt(), is(notNullValue()));

        verify(watchlistRepository).save(existing);
    }

    @Test
    @DisplayName("Should fail to update with duplicate name")
    public void shouldFailToUpdateWithDuplicateName() {
        // Given: Organization has a watchlist
        final Watchlist existing = createOrgWatchlist(1L, "Old Name", user, organization);

        // And: Another watchlist with target name exists
        final Watchlist anotherWatchlist = createOrgWatchlist(2L, "New Name", user, organization);

        final Watchlist updated = new Watchlist();
        updated.setName("New Name");

        when(organizationService.getOrganization(organization.getId())).thenReturn(organization);
        when(watchlistRepository.findByIdAndOrganizationId(1L, organization.getId()))
            .thenReturn(Optional.of(existing));
        when(watchlistRepository.findByOrganizationIdAndName(organization.getId(), "New Name"))
            .thenReturn(Optional.of(anotherWatchlist));

        // When & Then: Should throw DuplicateWatchlistNameException
        assertThrows(
            DuplicateWatchlistNameException.class,
            () -> organizationWatchlistService.updateWatchlist(organization.getId(), 1L, updated)
        );

        verify(watchlistRepository, never()).save(any(Watchlist.class));
    }

    @Test
    @DisplayName("Should delete organization watchlist successfully")
    public void shouldDeleteOrganizationWatchlistSuccessfully() {
        // Given: Organization has a watchlist
        final Watchlist watchlist = createOrgWatchlist(1L, "Org Watchlist", user, organization);

        when(organizationService.getOrganization(organization.getId())).thenReturn(organization);
        when(watchlistRepository.findByIdAndOrganizationId(1L, organization.getId()))
            .thenReturn(Optional.of(watchlist));

        // When: Deleting watchlist
        organizationWatchlistService.deleteWatchlist(organization.getId(), 1L);

        // Then: Watchlist should be deleted
        verify(watchlistRepository).delete(watchlist);
    }

    @Test
    @DisplayName("Should search organization watchlists successfully")
    public void shouldSearchOrganizationWatchlistsSuccessfully() {
        // Given: Organization has 3 watchlists
        final Watchlist watchlist1 = createOrgWatchlist(1L, "Watchlist 1", user, organization);
        final Watchlist watchlist2 = createOrgWatchlist(2L, "Watchlist 2", user, organization);
        final Watchlist watchlist3 = createOrgWatchlist(3L, "Watchlist 3", user, organization);

        final Page<Watchlist> mockPage = new PageImpl<>(List.of(watchlist1, watchlist2, watchlist3));

        when(organizationService.getOrganization(organization.getId())).thenReturn(organization);
        when(watchlistMetaData.toSort(any())).thenReturn(Sort.by(Sort.Direction.DESC, "createdAt"));
        when(watchlistMetaData.toOrganizationWatchlistSpecification(any(), eq(organization.getId())))
            .thenReturn((root, query, cb) -> cb.conjunction());
        when(watchlistRepository.findAll(any(Specification.class), any(Pageable.class)))
            .thenReturn(mockPage);

        // When: Searching watchlists
        final SortablePageInput input = createDefaultPageInput();
        final Page<Watchlist> watchlists = organizationWatchlistService.searchWatchlists(organization.getId(), input);

        // Then: Should return all 3 watchlists
        assertThat(watchlists, is(notNullValue()));
        assertThat(watchlists.getContent(), hasSize(3));
        assertThat(watchlists.getContent().get(0).getName(), is("Watchlist 1"));
        assertThat(watchlists.getContent().get(1).getName(), is("Watchlist 2"));
        assertThat(watchlists.getContent().get(2).getName(), is("Watchlist 3"));
    }

    private Watchlist createOrgWatchlist(final Long id, final String name, final User user, final Organization org) {
        final Watchlist watchlist = new Watchlist();
        watchlist.setId(id);
        watchlist.setName(name);
        watchlist.setUser(user);
        watchlist.setOrganization(org);
        watchlist.setConfiguration(WatchlistConfiguration.empty());
        watchlist.setFilters(WatchlistFilters.defaults());
        watchlist.setCreatedAt(OffsetDateTime.now().minusDays(1));
        return watchlist;
    }

    private Channel createOrgChannel(final Long id, final String name, final User user, final Organization org) {
        final Channel channel = new Channel(name, user, org);
        channel.setId(id);
        channel.setStatus(ChannelStatus.ACTIVE);
        channel.setCreatedAt(OffsetDateTime.now().minusDays(1));
        return channel;
    }

    private List<Channel> channelsWithIds(final Long... ids) {
        final List<Channel> channels = new java.util.ArrayList<>();
        for (final Long id : ids) {
            final Channel channel = new Channel();
            channel.setId(id);
            channels.add(channel);
        }
        return channels;
    }

    private SortablePageInput createDefaultPageInput() {
        final SortablePageInput input = new SortablePageInput();
        input.setPageNumber(0);
        input.setPageSize(20);
        return input;
    }
}
