package org.jordijaspers.eventify.api.monitoring.model.mapper;

import java.util.List;

import org.jordijaspers.eventify.api.monitoring.model.TimelineDuration;
import org.jordijaspers.eventify.api.monitoring.model.response.TimelineDurationResponse;
import org.jordijaspers.eventify.common.mapper.DateTimeMapper;
import org.jordijaspers.eventify.common.mapper.config.SharedMapperConfig;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * The mapper for the user.
 */
@Mapper(
    config = SharedMapperConfig.class,
    uses = DateTimeMapper.class
)
public abstract class TimelineDurationMapper {

    @Mapping(
        target = "startTime",
        source = "id.startTime"
    )
    @Named("toTimelineDurationResponse")
    public abstract TimelineDurationResponse toTimelineDurationResponse(TimelineDuration duration);

    @IterableMapping(qualifiedByName = "toTimelineDurationResponse")
    public abstract List<TimelineDurationResponse> toTimelineDurationResponses(List<TimelineDuration> durations);
}
