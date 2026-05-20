package io.github.eventify.api.subscription.model.mapper;

import io.github.eventify.api.subscription.model.Subscription;
import io.github.eventify.api.subscription.model.response.SubscriptionResponse;
import io.github.jframe.util.mapper.DateTimeMapper;
import io.github.jframe.util.mapper.config.SharedMapperConfig;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * Mapper for Subscription entity and DTOs.
 */
@Mapper(
    config = SharedMapperConfig.class,
    uses = DateTimeMapper.class
)
public abstract class SubscriptionMapper {

    /**
     * Maps Subscription entity to SubscriptionResponse.
     *
     * @param subscription the subscription entity
     * @return the response DTO
     */
    @Named("toResourceObject")
    @Mapping(
        target = "watchlistId",
        source = "watchlist.id"
    )
    public abstract SubscriptionResponse toResourceObject(Subscription subscription);
}
