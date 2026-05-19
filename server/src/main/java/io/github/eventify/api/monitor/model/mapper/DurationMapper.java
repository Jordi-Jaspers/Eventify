package io.github.eventify.api.monitor.model.mapper;

import io.github.eventify.api.monitor.model.DurationDetails;
import io.github.eventify.api.monitor.model.response.DurationDetailsResponse;
import io.github.jframe.util.mapper.config.SharedMapperConfig;

import org.mapstruct.Mapper;

/** MapStruct mapper for DurationDetails domain record to DurationDetailsResponse DTO. */
@Mapper(config = SharedMapperConfig.class)
public abstract class DurationMapper {

    /** Maps DurationDetails domain record to DurationDetailsResponse DTO. */
    public abstract DurationDetailsResponse toResponse(DurationDetails details);
}
