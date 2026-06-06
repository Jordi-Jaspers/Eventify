package io.github.eventify.api.subscription.model.mapper;

import io.github.eventify.api.event.model.Severity;
import io.github.eventify.api.organization.model.Organization;
import io.github.eventify.api.organization.model.OrganizationStatus;
import io.github.eventify.api.subscription.model.Subscription;
import io.github.eventify.api.subscription.model.response.SubscriptionResponse;
import io.github.eventify.api.watchlist.model.Watchlist;
import io.github.eventify.support.UnitTest;

import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

@DisplayName("Unit Test - Subscription Mapper")
public class SubscriptionMapperTest extends UnitTest {

    private SubscriptionMapper mapper;

    @BeforeEach
    public void setUp() {
        mapper = new SubscriptionMapperImpl();
    }

    @Test
    @DisplayName("Should map blocked=true when watchlist org is SUSPENDED")
    public void shouldMapBlockedTrueWhenWatchlistOrgIsSuspended() {
        // Given: personal subscription whose watchlist belongs to a SUSPENDED org
        final Organization suspendedOrg = anOrganization(OrganizationStatus.SUSPENDED);
        final Watchlist watchlist = anOrgWatchlist(10L, "Org Watchlist", aValidUser(), suspendedOrg);
        final Subscription subscription = aSubscription(1L, watchlist, null);

        // When: mapping to response
        final SubscriptionResponse response = mapper.toResourceObject(subscription);

        // Then: blocked is true and blockedReason explains the suspension
        assertThat(response.getBlocked(), is(true));
        assertThat(response.getBlockedReason(), is(notNullValue()));
    }

    @Test
    @DisplayName("Should map blocked=false when watchlist org is ACTIVE")
    public void shouldMapBlockedFalseWhenWatchlistOrgIsActive() {
        // Given: org subscription whose watchlist belongs to an ACTIVE org
        final Organization activeOrg = anOrganization(OrganizationStatus.ACTIVE);
        final Watchlist watchlist = anOrgWatchlist(10L, "Org Watchlist", aValidUser(), activeOrg);
        final Subscription subscription = aSubscription(1L, watchlist, activeOrg);

        // When: mapping to response
        final SubscriptionResponse response = mapper.toResourceObject(subscription);

        // Then: blocked is false, no reason
        assertThat(response.getBlocked(), is(false));
        assertThat(response.getBlockedReason(), is(nullValue()));
    }

    @Test
    @DisplayName("Should map blocked=false when watchlist has no org (null-safe)")
    public void shouldMapBlockedFalseWhenWatchlistHasNoOrg() {
        // Given: subscription with a personal watchlist (org=null on watchlist)
        final Watchlist watchlist = aWatchlist(10L, "Personal Watchlist", aValidUser());
        final Subscription subscription = aSubscription(1L, watchlist, null);

        // When: mapping to response
        final SubscriptionResponse response = mapper.toResourceObject(subscription);

        // Then: blocked is false, no reason — null org must not cause NPE
        assertThat(response.getBlocked(), is(false));
        assertThat(response.getBlockedReason(), is(nullValue()));
    }

    @Test
    @DisplayName("Should map blocked=false when subscription is personal with no org on watchlist")
    public void shouldMapBlockedFalseWhenSubscriptionIsPersonalWithNoOrg() {
        // Given: personal subscription (sub.org=null) with personal watchlist (watchlist.org=null)
        final Watchlist watchlist = aWatchlist(10L, "Personal Watchlist", aValidUser());
        final Subscription subscription = aSubscription(1L, watchlist, null);

        // When: mapping to response
        final SubscriptionResponse response = mapper.toResourceObject(subscription);

        // Then: blocked is false — no org context means not blocked
        assertThat(response.getBlocked(), is(false));
        assertThat(response.getBlockedReason(), is(nullValue()));
    }

    // ========================= FACTORY METHODS =========================

    private static Organization anOrganization(final OrganizationStatus status) {
        final Organization org = new Organization();
        org.setId(1L);
        org.setStatus(status);
        return org;
    }

    private static Subscription aSubscription(final Long id, final Watchlist watchlist, final Organization org) {
        final Subscription subscription = new Subscription();
        subscription.setId(id);
        subscription.setWatchlist(watchlist);
        subscription.setOrganization(org);
        subscription.setTargetSeverities(List.of(Severity.CRITICAL));
        subscription.setAdapterConfigIds(List.of(1L));
        subscription.setCreatedAt(OffsetDateTime.now().minusDays(1));
        return subscription;
    }
}
