package io.github.eventify.api.event.model.mapper;

import io.github.eventify.api.event.model.Event;
import io.github.eventify.api.event.model.response.EventCreatedResponse;
import io.github.eventify.api.event.model.response.EventSearchResponse;
import io.github.jframe.datasource.search.model.mapper.PageMapper;
import io.github.jframe.util.mapper.DateTimeMapper;
import io.github.jframe.util.mapper.config.SharedMapperConfig;

import java.util.List;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

/**
 * Mapper for event entities to DTOs.
 */
@Mapper(
    config = SharedMapperConfig.class,
    uses = DateTimeMapper.class
)
public abstract class EventMapper extends PageMapper<EventSearchResponse, Event> {

    /**
     * Maps Event entity to EventCreatedResponse.
     *
     * @param event the event entity
     * @return the response DTO containing id and timestamp
     */
    @Named("toCreatedResponse")
    public abstract EventCreatedResponse toCreatedResponse(Event event);

    /**
     * Maps list of Event entities to list of EventCreatedResponse.
     *
     * @param events the list of saved events
     * @return the list of response DTOs
     */
    @IterableMapping(qualifiedByName = "toCreatedResponse")
    public abstract List<EventCreatedResponse> toCreatedResponseList(List<Event> events);

    /**
     * Maps Event entity to EventSearchResponse.
     *
     * @param event the event entity
     * @return the search response DTO
     */
    @Override
    @Named("toResourceObject")
    public abstract EventSearchResponse toResourceObject(Event event);

    /**
     * Maps list of Event entities to list of EventSearchResponse.
     *
     * @param events the list of event entities
     * @return the list of search response DTOs
     */
    @IterableMapping(qualifiedByName = "toResourceObject")
    public abstract List<EventSearchResponse> toResourceObjects(List<Event> events);
}
