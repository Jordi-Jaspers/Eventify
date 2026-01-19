package io.github.eventify.api.event.model.mapper;

import io.github.eventify.api.event.model.Event;
import io.github.eventify.api.event.model.response.EventCreatedResponse;
import io.github.jframe.util.mapper.DateTimeMapper;
import io.github.jframe.util.mapper.config.SharedMapperConfig;

import org.mapstruct.Mapper;

/**
 * Mapper for event entities to DTOs.
 */
@Mapper(
    config = SharedMapperConfig.class,
    uses = DateTimeMapper.class
)
public abstract class EventMapper {

    /**
     * Maps Event entity to EventCreatedResponse.
     *
     * @param event the event entity
     * @return the response DTO containing id and timestamp
     */
    public abstract EventCreatedResponse toCreatedResponse(Event event);
}
