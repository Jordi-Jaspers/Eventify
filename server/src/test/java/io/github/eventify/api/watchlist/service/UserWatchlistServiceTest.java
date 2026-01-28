package io.github.eventify.api.watchlist.service;

import io.github.eventify.api.channel.model.Channel;
import io.github.eventify.api.channel.model.ChannelStatus;
import io.github.eventify.api.channel.repository.ChannelRepository;
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

@DisplayName("Unit Test - Watchlist Service")
@SuppressWarnings("unchecked")
public class UserWatchlistServiceTest extends UnitTest {

    @Mock
    private WatchlistRepository watchlistRepository;

    @Mock
    private ChannelRepository channelRepository;

    @Mock
    private WatchlistMetaData watchlistMetaData;

    @InjectMocks
    private UserWatchlistService userWatchlistService;

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
    @DisplayName("Should create watchlist successfully")
    public void shouldCreateWatchlistSuccessfully() {
        // Given: Valid watchlist
        final Watchlist input = new Watchlist();
        input.setName("My Watchlist");
        input.setDescription("Production errors");
        input.setFilters(WatchlistFilters.defaults());

        when(watchlistRepository.findByUserIdAndName(user.getId(), "My Watchlist"))
            .thenReturn(Optional.empty());
        when(watchlistRepository.save(any(Watchlist.class))).thenAnswer(invocation -> {
            final Watchlist watchlist = invocation.getArgument(0);
            watchlist.setId(1L);
            return watchlist;
        });

        // When: Creating watchlist
        final Watchlist watchlist = userWatchlistService.createWatchlist(input);

        // Then: Watchlist should be created with correct properties
        assertThat(watchlist, is(notNullValue()));
        assertThat(watchlist.getId(), is(1L));
        assertThat(watchlist.getName(), is("My Watchlist"));
        assertThat(watchlist.getDescription(), is("Production errors"));
        assertThat(watchlist.getUser().getId(), is(user.getId()));
        assertThat(watchlist.getFilters().getTimeRange(), is(io.github.eventify.api.monitor.model.TimeRange.LAST_24H));
        assertThat(watchlist.getFilters().isOnlyCritical(), is(false));
        assertThat(watchlist.getFilters().isSortBySeverity(), is(true));

        verify(watchlistRepository).save(any(Watchlist.class));
    }

    @Test
    @DisplayName("Should create watchlist with configuration successfully")
    public void shouldCreateWatchlistWithConfigurationSuccessfully() {
        // Given: Watchlist with configuration
        final WatchlistConfiguration configuration = WatchlistConfiguration.builder()
            .channels(channelsWithIds(1L, 2L))
            .build();

        final Watchlist input = new Watchlist();
        input.setName("My Watchlist");
        input.setConfiguration(configuration);

        final Channel channel1 = createChannel(1L, "Channel 1", user);
        final Channel channel2 = createChannel(2L, "Channel 2", user);

        when(watchlistRepository.findByUserIdAndName(user.getId(), "My Watchlist"))
            .thenReturn(Optional.empty());
        when(channelRepository.findAllByIdInAndUserId(List.of(1L, 2L), user.getId()))
            .thenReturn(List.of(channel1, channel2));
        when(watchlistRepository.save(any(Watchlist.class))).thenAnswer(invocation -> {
            final Watchlist watchlist = invocation.getArgument(0);
            watchlist.setId(1L);
            return watchlist;
        });

        // When: Creating watchlist with configuration
        final Watchlist watchlist = userWatchlistService.createWatchlist(input);

        // Then: Configuration should be set
        assertThat(watchlist, is(notNullValue()));
        assertThat(watchlist.getConfiguration().getChannelIds(), hasItems(1L, 2L));
        verify(watchlistRepository).save(any(Watchlist.class));
    }

    @Test
    @DisplayName("Should fail to create when duplicate name")
    public void shouldFailToCreateWhenDuplicateName() {
        // Given: User already has watchlist with same name
        final Watchlist existingWatchlist = new Watchlist();
        existingWatchlist.setName("My Watchlist");
        when(watchlistRepository.findByUserIdAndName(user.getId(), "My Watchlist"))
            .thenReturn(Optional.of(existingWatchlist));

        final Watchlist input = new Watchlist();
        input.setName("My Watchlist");

        // When & Then: Should throw DuplicateWatchlistNameException
        assertThrows(
            DuplicateWatchlistNameException.class,
            () -> userWatchlistService.createWatchlist(input)
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
        input.setName("My Watchlist");
        input.setConfiguration(configuration);

        when(watchlistRepository.findByUserIdAndName(user.getId(), "My Watchlist"))
            .thenReturn(Optional.empty());
        when(channelRepository.findAllByIdInAndUserId(List.of(999L), user.getId()))
            .thenReturn(List.of());

        // When & Then: Should throw DataNotFoundException
        assertThrows(
            DataNotFoundException.class,
            () -> userWatchlistService.createWatchlist(input)
        );

        verify(watchlistRepository, never()).save(any(Watchlist.class));
    }

    @Test
    @DisplayName("Should fail when channel belongs to organization")
    public void shouldFailWhenChannelBelongsToOrganization() {
        // Given: Channel belongs to organization (so findByIdAndUserId returns empty)
        final WatchlistConfiguration configuration = WatchlistConfiguration.builder()
            .channels(channelsWithIds(1L))
            .build();

        final Watchlist input = new Watchlist();
        input.setName("My Watchlist");
        input.setConfiguration(configuration);

        when(watchlistRepository.findByUserIdAndName(user.getId(), "My Watchlist"))
            .thenReturn(Optional.empty());
        // Org channels are not returned by findAllByIdInAndUserId query
        when(channelRepository.findAllByIdInAndUserId(List.of(1L), user.getId()))
            .thenReturn(List.of());

        // When & Then: Should throw DataNotFoundException
        assertThrows(
            DataNotFoundException.class,
            () -> userWatchlistService.createWatchlist(input)
        );

        verify(watchlistRepository, never()).save(any(Watchlist.class));
    }

    @Test
    @DisplayName("Should get user watchlist successfully")
    public void shouldGetUserWatchlistSuccessfully() {
        // Given: User has a watchlist
        final Watchlist watchlist = createWatchlist(1L, "My Watchlist", user);

        when(watchlistRepository.findByIdAndUserId(1L, user.getId()))
            .thenReturn(Optional.of(watchlist));

        // When: Getting watchlist by ID
        final Watchlist result = userWatchlistService.getWatchlist(1L);

        // Then: Should return watchlist
        assertThat(result, is(notNullValue()));
        assertThat(result.getId(), is(1L));
        assertThat(result.getName(), is("My Watchlist"));
    }

    @Test
    @DisplayName("Should fail to get watchlist of another user")
    public void shouldFailToGetWatchlistOfAnotherUser() {
        // Given: Watchlist does not belong to current user
        when(watchlistRepository.findByIdAndUserId(1L, user.getId()))
            .thenReturn(Optional.empty());

        // When & Then: Should throw DataNotFoundException
        assertThrows(
            DataNotFoundException.class,
            () -> userWatchlistService.getWatchlist(1L)
        );
    }

    @Test
    @DisplayName("Should update watchlist successfully")
    public void shouldUpdateWatchlistSuccessfully() {
        // Given: User has a watchlist
        final Watchlist existing = createWatchlist(1L, "Old Name", user);

        final Watchlist updated = new Watchlist();
        updated.setName("New Name");
        updated.setDescription("New Description");

        when(watchlistRepository.findByIdAndUserId(1L, user.getId()))
            .thenReturn(Optional.of(existing));
        when(watchlistRepository.findByUserIdAndName(user.getId(), "New Name"))
            .thenReturn(Optional.empty());
        when(watchlistRepository.save(any(Watchlist.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When: Updating watchlist
        final Watchlist result = userWatchlistService.updateWatchlist(1L, updated);

        // Then: Watchlist should be updated
        assertThat(result.getName(), is("New Name"));
        assertThat(result.getDescription(), is("New Description"));
        assertThat(result.getUpdatedAt(), is(notNullValue()));

        verify(watchlistRepository).save(existing);
    }

    @Test
    @DisplayName("Should fail to update with duplicate name")
    public void shouldFailToUpdateWithDuplicateName() {
        // Given: User has a watchlist
        final Watchlist existing = createWatchlist(1L, "Old Name", user);

        // And: Another watchlist with target name exists
        final Watchlist anotherWatchlist = createWatchlist(2L, "New Name", user);

        final Watchlist updated = new Watchlist();
        updated.setName("New Name");

        when(watchlistRepository.findByIdAndUserId(1L, user.getId()))
            .thenReturn(Optional.of(existing));
        when(watchlistRepository.findByUserIdAndName(user.getId(), "New Name"))
            .thenReturn(Optional.of(anotherWatchlist));

        // When & Then: Should throw DuplicateWatchlistNameException
        assertThrows(
            DuplicateWatchlistNameException.class,
            () -> userWatchlistService.updateWatchlist(1L, updated)
        );

        verify(watchlistRepository, never()).save(any(Watchlist.class));
    }

    @Test
    @DisplayName("Should update configuration successfully")
    public void shouldUpdateConfigurationSuccessfully() {
        // Given: User has a watchlist
        final Watchlist existing = createWatchlist(1L, "My Watchlist", user);
        final Channel channel1 = createChannel(1L, "Channel 1", user);
        final Channel channel2 = createChannel(2L, "Channel 2", user);

        final WatchlistConfiguration configuration = WatchlistConfiguration.builder()
            .channels(channelsWithIds(2L, 1L))
            .build();

        final Watchlist updated = new Watchlist();
        updated.setName("My Watchlist");
        updated.setConfiguration(configuration);

        when(watchlistRepository.findByIdAndUserId(1L, user.getId()))
            .thenReturn(Optional.of(existing));
        when(watchlistRepository.findByUserIdAndName(user.getId(), "My Watchlist"))
            .thenReturn(Optional.of(existing));
        when(channelRepository.findAllByIdInAndUserId(List.of(2L, 1L), user.getId()))
            .thenReturn(List.of(channel1, channel2));
        when(watchlistRepository.save(any(Watchlist.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When: Updating configuration
        final Watchlist result = userWatchlistService.updateWatchlist(1L, updated);

        // Then: Configuration should be updated
        assertThat(result.getConfiguration().getChannelIds(), contains(2L, 1L));
        verify(watchlistRepository).save(existing);
    }

    @Test
    @DisplayName("Should delete watchlist successfully")
    public void shouldDeleteWatchlistSuccessfully() {
        // Given: User has a watchlist
        final Watchlist watchlist = createWatchlist(1L, "My Watchlist", user);

        when(watchlistRepository.findByIdAndUserId(1L, user.getId()))
            .thenReturn(Optional.of(watchlist));

        // When: Deleting watchlist
        userWatchlistService.deleteWatchlist(1L);

        // Then: Watchlist should be deleted
        verify(watchlistRepository).delete(watchlist);
    }

    @Test
    @DisplayName("Should fail to delete watchlist of another user")
    public void shouldFailToDeleteWatchlistOfAnotherUser() {
        // Given: Watchlist does not belong to current user
        when(watchlistRepository.findByIdAndUserId(1L, user.getId()))
            .thenReturn(Optional.empty());

        // When & Then: Should throw DataNotFoundException
        assertThrows(
            DataNotFoundException.class,
            () -> userWatchlistService.deleteWatchlist(1L)
        );

        verify(watchlistRepository, never()).delete(any(Watchlist.class));
    }

    @Test
    @DisplayName("Should search personal watchlists successfully")
    public void shouldSearchPersonalWatchlistsSuccessfully() {
        // Given: User has 3 watchlists
        final Watchlist watchlist1 = createWatchlist(1L, "Watchlist 1", user);
        final Watchlist watchlist2 = createWatchlist(2L, "Watchlist 2", user);
        final Watchlist watchlist3 = createWatchlist(3L, "Watchlist 3", user);

        final Page<Watchlist> mockPage = new PageImpl<>(List.of(watchlist1, watchlist2, watchlist3));

        when(watchlistMetaData.toSort(any())).thenReturn(Sort.by(Sort.Direction.DESC, "createdAt"));
        when(watchlistMetaData.toUserWatchlistSpecification(any())).thenReturn((root, query, cb) -> cb.conjunction());
        when(watchlistRepository.findAll(any(Specification.class), any(Pageable.class)))
            .thenReturn(mockPage);

        // When: Searching watchlists
        final SortablePageInput input = createDefaultPageInput();
        final Page<Watchlist> watchlists = userWatchlistService.searchWatchlists(input);

        // Then: Should return all 3 watchlists
        assertThat(watchlists, is(notNullValue()));
        assertThat(watchlists.getContent(), hasSize(3));
        assertThat(watchlists.getContent().get(0).getName(), is("Watchlist 1"));
        assertThat(watchlists.getContent().get(1).getName(), is("Watchlist 2"));
        assertThat(watchlists.getContent().get(2).getName(), is("Watchlist 3"));
    }

    @Test
    @DisplayName("Should return empty list when user has no watchlists")
    public void shouldReturnEmptyListWhenUserHasNoWatchlists() {
        // Given: User has no watchlists
        final Page<Watchlist> emptyPage = new PageImpl<>(List.of());

        when(watchlistMetaData.toSort(any())).thenReturn(Sort.by(Sort.Direction.DESC, "createdAt"));
        when(watchlistMetaData.toUserWatchlistSpecification(any())).thenReturn((root, query, cb) -> cb.conjunction());
        when(watchlistRepository.findAll(any(Specification.class), any(Pageable.class)))
            .thenReturn(emptyPage);

        // When: Searching watchlists
        final SortablePageInput input = createDefaultPageInput();
        final Page<Watchlist> watchlists = userWatchlistService.searchWatchlists(input);

        // Then: Should return empty list
        assertThat(watchlists.getContent(), is(empty()));
    }

    private Watchlist createWatchlist(final Long id, final String name, final User user) {
        final Watchlist watchlist = new Watchlist();
        watchlist.setId(id);
        watchlist.setName(name);
        watchlist.setUser(user);
        watchlist.setConfiguration(WatchlistConfiguration.empty());
        watchlist.setFilters(WatchlistFilters.defaults());
        watchlist.setCreatedAt(OffsetDateTime.now().minusDays(1));
        return watchlist;
    }

    private Channel createChannel(final Long id, final String name, final User user) {
        final Channel channel = new Channel(name, user, null);
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
