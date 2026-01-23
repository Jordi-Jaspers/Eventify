package io.github.eventify.api.watchlist.service;

import io.github.eventify.api.channel.model.Channel;
import io.github.eventify.api.channel.model.ChannelStatus;
import io.github.eventify.api.channel.repository.ChannelRepository;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.watchlist.model.Watchlist;
import io.github.eventify.api.watchlist.model.WatchlistMetaData;
import io.github.eventify.api.watchlist.model.request.CreateWatchlistRequest;
import io.github.eventify.api.watchlist.model.request.UpdateWatchlistRequest;
import io.github.eventify.api.watchlist.repository.WatchlistChannelRepository;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@DisplayName("Unit Test - Watchlist Service")
@SuppressWarnings("unchecked")
public class WatchlistServiceTest extends UnitTest {

    @Mock
    private WatchlistRepository watchlistRepository;

    @Mock
    private WatchlistChannelRepository watchlistChannelRepository;

    @Mock
    private ChannelRepository channelRepository;

    @Mock
    private WatchlistMetaData watchlistMetaData;

    @InjectMocks
    private WatchlistService watchlistService;

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
        // Given: A valid create request
        final CreateWatchlistRequest request = new CreateWatchlistRequest()
            .setName("My Watchlist")
            .setDescription("Production errors");

        when(watchlistRepository.findByUserIdAndName(user.getId(), "My Watchlist"))
            .thenReturn(Optional.empty());
        when(watchlistRepository.save(any(Watchlist.class))).thenAnswer(invocation -> {
            final Watchlist watchlist = invocation.getArgument(0);
            watchlist.setId(1L);
            return watchlist;
        });

        // When: Creating watchlist
        final Watchlist watchlist = watchlistService.createWatchlist(request);

        // Then: Watchlist should be created with correct properties
        assertThat(watchlist, is(notNullValue()));
        assertThat(watchlist.getId(), is(1L));
        assertThat(watchlist.getName(), is("My Watchlist"));
        assertThat(watchlist.getDescription(), is("Production errors"));
        assertThat(watchlist.getUser().getId(), is(user.getId()));
        assertThat(watchlist.getDefaultTimeRange(), is("24h"));
        assertThat(watchlist.getDefaultOnlyCritical(), is(false));
        assertThat(watchlist.getDefaultSortBySeverity(), is(true));

        verify(watchlistRepository).save(any(Watchlist.class));
    }

    @Test
    @DisplayName("Should create watchlist with channels successfully")
    public void shouldCreateWatchlistWithChannelsSuccessfully() {
        // Given: A valid request with channel IDs
        final CreateWatchlistRequest request = new CreateWatchlistRequest()
            .setName("My Watchlist")
            .setChannelIds(List.of(1L, 2L));

        final Channel channel1 = createChannel(1L, "Channel 1", user);
        final Channel channel2 = createChannel(2L, "Channel 2", user);

        when(watchlistRepository.findByUserIdAndName(user.getId(), "My Watchlist"))
            .thenReturn(Optional.empty());
        when(channelRepository.findByIdAndUserId(1L, user.getId()))
            .thenReturn(Optional.of(channel1));
        when(channelRepository.findByIdAndUserId(2L, user.getId()))
            .thenReturn(Optional.of(channel2));
        when(watchlistRepository.save(any(Watchlist.class))).thenAnswer(invocation -> {
            final Watchlist watchlist = invocation.getArgument(0);
            watchlist.setId(1L);
            return watchlist;
        });
        when(watchlistChannelRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // When: Creating watchlist with channels
        final Watchlist watchlist = watchlistService.createWatchlist(request);

        // Then: Channels should be added
        assertThat(watchlist, is(notNullValue()));
        verify(watchlistChannelRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Should fail to create when duplicate name")
    public void shouldFailToCreateWhenDuplicateName() {
        // Given: User already has watchlist with same name
        final CreateWatchlistRequest request = new CreateWatchlistRequest()
            .setName("My Watchlist");

        final Watchlist existingWatchlist = new Watchlist();
        existingWatchlist.setName("My Watchlist");
        when(watchlistRepository.findByUserIdAndName(user.getId(), "My Watchlist"))
            .thenReturn(Optional.of(existingWatchlist));

        // When & Then: Should throw DuplicateWatchlistNameException
        assertThrows(
            DuplicateWatchlistNameException.class,
            () -> watchlistService.createWatchlist(request)
        );

        verify(watchlistRepository, never()).save(any(Watchlist.class));
    }

    @Test
    @DisplayName("Should fail when channel not found")
    public void shouldFailWhenChannelNotFound() {
        // Given: Request with non-existent channel ID
        final CreateWatchlistRequest request = new CreateWatchlistRequest()
            .setName("My Watchlist")
            .setChannelIds(List.of(999L));

        when(watchlistRepository.findByUserIdAndName(user.getId(), "My Watchlist"))
            .thenReturn(Optional.empty());
        when(channelRepository.findByIdAndUserId(999L, user.getId()))
            .thenReturn(Optional.empty());

        // When & Then: Should throw DataNotFoundException
        assertThrows(
            DataNotFoundException.class,
            () -> watchlistService.createWatchlist(request)
        );

        verify(watchlistRepository, never()).save(any(Watchlist.class));
    }

    @Test
    @DisplayName("Should fail when channel belongs to organization")
    public void shouldFailWhenChannelBelongsToOrganization() {
        // Given: Channel belongs to organization (so findByIdAndUserId returns empty)
        final CreateWatchlistRequest request = new CreateWatchlistRequest()
            .setName("My Watchlist")
            .setChannelIds(List.of(1L));

        when(watchlistRepository.findByUserIdAndName(user.getId(), "My Watchlist"))
            .thenReturn(Optional.empty());
        // Org channels are not returned by findByIdAndUserId query
        when(channelRepository.findByIdAndUserId(1L, user.getId()))
            .thenReturn(Optional.empty());

        // When & Then: Should throw DataNotFoundException
        assertThrows(
            DataNotFoundException.class,
            () -> watchlistService.createWatchlist(request)
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
        final Watchlist result = watchlistService.getWatchlist(1L);

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
            () -> watchlistService.getWatchlist(1L)
        );
    }

    @Test
    @DisplayName("Should update watchlist successfully")
    public void shouldUpdateWatchlistSuccessfully() {
        // Given: User has a watchlist
        final Watchlist watchlist = createWatchlist(1L, "Old Name", user);

        when(watchlistRepository.findByIdAndUserId(1L, user.getId()))
            .thenReturn(Optional.of(watchlist));
        when(watchlistRepository.findByUserIdAndName(user.getId(), "New Name"))
            .thenReturn(Optional.empty());
        when(watchlistRepository.save(any(Watchlist.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When: Updating watchlist
        final UpdateWatchlistRequest request = new UpdateWatchlistRequest()
            .setName("New Name")
            .setDescription("New Description");

        final Watchlist updated = watchlistService.updateWatchlist(1L, request);

        // Then: Watchlist should be updated
        assertThat(updated.getName(), is("New Name"));
        assertThat(updated.getDescription(), is("New Description"));
        assertThat(updated.getUpdatedAt(), is(notNullValue()));

        verify(watchlistRepository).save(watchlist);
    }

    @Test
    @DisplayName("Should fail to update with duplicate name")
    public void shouldFailToUpdateWithDuplicateName() {
        // Given: User has a watchlist
        final Watchlist watchlist = createWatchlist(1L, "Old Name", user);

        // And: Another watchlist with target name exists
        final Watchlist existingWatchlist = createWatchlist(2L, "New Name", user);

        when(watchlistRepository.findByIdAndUserId(1L, user.getId()))
            .thenReturn(Optional.of(watchlist));
        when(watchlistRepository.findByUserIdAndName(user.getId(), "New Name"))
            .thenReturn(Optional.of(existingWatchlist));

        // When & Then: Should throw DuplicateWatchlistNameException
        final UpdateWatchlistRequest request = new UpdateWatchlistRequest()
            .setName("New Name");

        assertThrows(
            DuplicateWatchlistNameException.class,
            () -> watchlistService.updateWatchlist(1L, request)
        );

        verify(watchlistRepository, never()).save(any(Watchlist.class));
    }

    @Test
    @DisplayName("Should update channel order successfully")
    public void shouldUpdateChannelOrder() {
        // Given: User has a watchlist with channels
        final Watchlist watchlist = createWatchlist(1L, "My Watchlist", user);
        final Channel channel1 = createChannel(1L, "Channel 1", user);
        final Channel channel2 = createChannel(2L, "Channel 2", user);
        final Channel channel3 = createChannel(3L, "Channel 3", user);

        when(watchlistRepository.findByIdAndUserId(1L, user.getId()))
            .thenReturn(Optional.of(watchlist));
        when(watchlistRepository.findByUserIdAndName(user.getId(), "My Watchlist"))
            .thenReturn(Optional.of(watchlist));
        when(channelRepository.findByIdAndUserId(2L, user.getId()))
            .thenReturn(Optional.of(channel2));
        when(channelRepository.findByIdAndUserId(1L, user.getId()))
            .thenReturn(Optional.of(channel1));
        when(channelRepository.findByIdAndUserId(3L, user.getId()))
            .thenReturn(Optional.of(channel3));
        when(watchlistRepository.save(any(Watchlist.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(watchlistChannelRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // When: Updating channel order (reversed)
        final UpdateWatchlistRequest request = new UpdateWatchlistRequest()
            .setName("My Watchlist")
            .setChannelIds(List.of(2L, 1L, 3L));

        final Watchlist updated = watchlistService.updateWatchlist(1L, request);

        // Then: Channel order should be updated
        verify(watchlistChannelRepository).deleteAllByWatchlistId(1L);
        verify(watchlistChannelRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Should delete watchlist successfully")
    public void shouldDeleteWatchlistSuccessfully() {
        // Given: User has a watchlist
        final Watchlist watchlist = createWatchlist(1L, "My Watchlist", user);

        when(watchlistRepository.findByIdAndUserId(1L, user.getId()))
            .thenReturn(Optional.of(watchlist));

        // When: Deleting watchlist
        watchlistService.deleteWatchlist(1L);

        // Then: Watchlist should be deleted
        verify(watchlistChannelRepository).deleteAllByWatchlistId(1L);
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
            () -> watchlistService.deleteWatchlist(1L)
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
        final Page<Watchlist> watchlists = watchlistService.searchWatchlists(input);

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
        final Page<Watchlist> watchlists = watchlistService.searchWatchlists(input);

        // Then: Should return empty list
        assertThat(watchlists.getContent(), is(empty()));
    }

    private Watchlist createWatchlist(final Long id, final String name, final User user) {
        final Watchlist watchlist = new Watchlist();
        watchlist.setId(id);
        watchlist.setName(name);
        watchlist.setUser(user);
        watchlist.setDefaultTimeRange("24h");
        watchlist.setDefaultOnlyCritical(false);
        watchlist.setDefaultSortBySeverity(true);
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

    private SortablePageInput createDefaultPageInput() {
        final SortablePageInput input = new SortablePageInput();
        input.setPageNumber(0);
        input.setPageSize(20);
        return input;
    }
}
