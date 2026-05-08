package io.github.eventify.support;

import io.github.eventify.api.apikey.model.ApiKey;
import io.github.eventify.api.apikey.model.ApiKeyScope;
import io.github.eventify.api.channel.model.Channel;
import io.github.eventify.api.channel.model.ChannelStatus;
import io.github.eventify.api.event.model.Event;
import io.github.eventify.api.event.model.Severity;
import io.github.eventify.api.organization.model.Organization;
import io.github.eventify.api.user.model.AuthProvider;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.user.model.UserAuthProvider;
import io.github.eventify.api.watchlist.model.Watchlist;
import io.github.eventify.api.watchlist.model.WatchlistConfiguration;
import io.github.eventify.api.watchlist.model.WatchlistFilters;
import io.github.jframe.datasource.search.model.input.SortablePageInput;

import java.time.Instant;
import java.time.OffsetDateTime;

/**
 * Static factory methods for test data objects.
 * No Spring dependency — safe for unit tests.
 */
public final class TestBuilders {

    private TestBuilders() {}

    // ========================= PAGE INPUT =========================

    public static SortablePageInput aPageInput() {
        final SortablePageInput input = new SortablePageInput();
        input.setPageNumber(0);
        input.setPageSize(20);
        return input;
    }

    // ========================= CHANNEL =========================

    public static Channel aChannel(final Long id, final String name, final User user) {
        final Channel channel = new Channel(name, "test.slug." + id, user, null);
        channel.setId(id);
        channel.setStatus(ChannelStatus.ACTIVE);
        channel.setCreatedAt(OffsetDateTime.now().minusDays(1));
        return channel;
    }

    public static Channel aChannel(final Long id, final String name, final User user, final Organization org) {
        final Channel channel = new Channel(name, "test.slug." + id, user, org);
        channel.setId(id);
        channel.setStatus(ChannelStatus.ACTIVE);
        channel.setCreatedAt(OffsetDateTime.now().minusDays(1));
        return channel;
    }

    // ========================= API KEY =========================

    public static ApiKey anApiKey(final Long id, final String suffix, final String name, final User user) {
        final ApiKey key = new ApiKey();
        key.setId(id);
        key.setSuffix(suffix);
        key.setName(name);
        key.setHashedKey("hashed_" + suffix);
        key.setScope(ApiKeyScope.USER);
        key.setUser(user);
        key.setOrganization(null);
        key.setCreatedAt(OffsetDateTime.now().minusDays(1));
        key.setTotalRequests(0L);
        return key;
    }

    public static ApiKey anOrgApiKey(final Long id, final String suffix, final String name, final User user, final Organization org) {
        final ApiKey key = new ApiKey();
        key.setId(id);
        key.setSuffix(suffix);
        key.setName(name);
        key.setHashedKey("hashed_" + suffix);
        key.setScope(ApiKeyScope.ORGANIZATION);
        key.setUser(user);
        key.setOrganization(org);
        key.setCreatedAt(OffsetDateTime.now().minusDays(1));
        key.setTotalRequests(0L);
        return key;
    }

    // ========================= WATCHLIST =========================

    public static Watchlist aWatchlist(final Long id, final String name, final User user) {
        final Watchlist watchlist = new Watchlist();
        watchlist.setId(id);
        watchlist.setName(name);
        watchlist.setUser(user);
        watchlist.setConfiguration(WatchlistConfiguration.empty());
        watchlist.setFilters(WatchlistFilters.defaults());
        watchlist.setCreatedAt(OffsetDateTime.now().minusDays(1));
        return watchlist;
    }

    public static Watchlist anOrgWatchlist(final Long id, final String name, final User user, final Organization org) {
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

    // ========================= EVENT =========================

    public static Event anEvent(final Long id, final Channel channel, final Severity severity, final OffsetDateTime timestamp) {
        final Event event = new Event();
        event.setId(id);
        event.setChannel(channel);
        event.setSeverity(severity);
        event.setTitle("Test Event");
        event.setTimestamp(timestamp);
        return event;
    }

    public static Event anEvent(final Long id, final Channel channel, final Severity severity) {
        return anEvent(id, channel, severity, OffsetDateTime.now().minusMinutes(30));
    }

    public static Event anEvent(final Channel channel, final Severity severity, final OffsetDateTime timestamp) {
        return anEvent(1L, channel, severity, timestamp);
    }

    // ========================= ORGANIZATION =========================

    public static Organization anOrganization(final Long id, final String name, final String slug) {
        final Organization org = new Organization();
        org.setId(id);
        org.setName(name);
        org.setSlug(slug);
        return org;
    }

    public static Organization anOrganization(final Long id) {
        return anOrganization(id, "Test Organization", "test-organization");
    }

    // ========================= USER AUTH PROVIDER =========================

    public static UserAuthProvider aUserAuthProvider(final User user, final AuthProvider provider) {
        final UserAuthProvider authProvider = new UserAuthProvider();
        authProvider.setUser(user);
        authProvider.setProvider(provider);
        authProvider.setProviderEmail(user.getEmail());
        authProvider.setLinkedAt(Instant.now());
        return authProvider;
    }

    public static UserAuthProvider aLocalUserAuthProvider(final User user) {
        return aUserAuthProvider(user, AuthProvider.LOCAL);
    }

    public static UserAuthProvider aGoogleUserAuthProvider(final User user) {
        return aUserAuthProvider(user, AuthProvider.GOOGLE);
    }

    public static UserAuthProvider aGitHubUserAuthProvider(final User user) {
        return aUserAuthProvider(user, AuthProvider.GITHUB);
    }

    public static UserAuthProvider aUserAuthProvider(
        final Long id,
        final User user,
        final AuthProvider provider,
        final String providerEmail) {
        final UserAuthProvider authProvider = aUserAuthProvider(user, provider);
        authProvider.setId(id);
        authProvider.setProviderEmail(providerEmail);
        return authProvider;
    }
}
