package io.github.eventify.api.subscription.model.mapper;

import io.github.eventify.api.organization.model.OrganizationStatus;
import io.github.eventify.api.subscription.model.Subscription;
import io.github.eventify.api.subscription.model.response.SubscriptionResponse;
import io.github.eventify.api.watchlist.model.Watchlist;
import io.github.eventify.common.exception.ApiErrorCode;
import io.github.jframe.datasource.search.model.mapper.PageMapper;
import io.github.jframe.util.mapper.DateTimeMapper;
import io.github.jframe.util.mapper.config.SharedMapperConfig;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

/**
 * Mapper for Subscription entity and DTOs.
 */
@Mapper(
    config = SharedMapperConfig.class,
    uses = DateTimeMapper.class
)
public abstract class SubscriptionMapper extends PageMapper<SubscriptionResponse, Subscription> {

    /**
     * Maps Subscription entity to SubscriptionResponse.
     *
     * @param subscription the subscription entity
     * @return the response DTO
     */
    @Override
    @Named("toResourceObject")
    @Mapping(
        target = "watchlistId",
        source = "watchlist.id"
    )
    @Mapping(
        target = "watchlistName",
        source = "watchlist.name"
    )
    @Mapping(
        target = "organizationId",
        source = "organization.id"
    )
    @Mapping(
        target = "blocked",
        ignore = true
    )
    @Mapping(
        target = "blockedReason",
        ignore = true
    )
    public abstract SubscriptionResponse toResourceObject(Subscription subscription);

    /**
     * Derives blocked and blockedReason from the watchlist's organization status.
     *
     * @param subscription the source subscription entity
     * @param response     the target response DTO being built
     */
    @AfterMapping
    protected void deriveBlockedFields(final Subscription subscription,
        @MappingTarget final SubscriptionResponse response) {
        final Watchlist watchlist = subscription.getWatchlist();
        if (watchlist == null || watchlist.getOrganization() == null) {
            response.setBlocked(false);
            response.setBlockedReason(null);
            return;
        }
        final boolean blocked = OrganizationStatus.SUSPENDED.equals(watchlist.getOrganization().getStatus());
        response.setBlocked(blocked);
        response.setBlockedReason(blocked ? ApiErrorCode.ORGANIZATION_SUSPENDED_ERROR.getReason() : null);
    }
}
