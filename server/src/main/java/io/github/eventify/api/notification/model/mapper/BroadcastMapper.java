package io.github.eventify.api.notification.model.mapper;

import io.github.eventify.api.notification.model.NotificationBroadcast;
import io.github.eventify.api.notification.model.response.BroadcastResponse;
import io.github.jframe.datasource.search.model.mapper.PageMapper;
import io.github.jframe.util.mapper.DateTimeMapper;
import io.github.jframe.util.mapper.config.SharedMapperConfig;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * Mapper for NotificationBroadcast entity to response DTOs.
 */
@Mapper(
    config = SharedMapperConfig.class,
    uses = DateTimeMapper.class
)
public abstract class BroadcastMapper extends PageMapper<BroadcastResponse, NotificationBroadcast> {

    @Override
    @Named("toResourceObject")
    @Mapping(
        source = "sentBy.email",
        target = "sentByEmail"
    )
    public abstract BroadcastResponse toResourceObject(NotificationBroadcast notificationBroadcast);
}
