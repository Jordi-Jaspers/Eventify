package org.jordijaspers.eventify.api.event.model.mapper;

import java.util.List;

import org.jordijaspers.eventify.api.event.model.Event;
import org.jordijaspers.eventify.api.event.model.EventId;
import org.jordijaspers.eventify.api.event.model.request.EventRequest;
import org.jordijaspers.eventify.common.mapper.DateTimeMapper;
import org.jordijaspers.eventify.common.mapper.config.SharedMapperConfig;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(
    config = SharedMapperConfig.class,
    imports = EventId.class,
    uses = DateTimeMapper.class
)
public abstract class EventMapper {

    @Mapping(
        target = "id",
        expression = "java(new EventId(request.getCheckId(), request.getTimestamp().toLocalDateTime()))"
    )
    @Named("toEvent")
    public abstract Event toEvent(EventRequest request);

    @IterableMapping(qualifiedByName = "toEvent")
    public abstract List<Event> toEvents(List<EventRequest> requests);

    @Mapping(
        target = "checkId",
        source = "id.checkId"
    )
    @Mapping(
        target = "timestamp",
        source = "id.timestamp"
    )
    public abstract EventRequest toEventRequest(Event event);

}
