package org.jordijaspers.eventify.api.check.model.mapper;

import org.jordijaspers.eventify.api.check.model.Check;
import org.jordijaspers.eventify.api.check.model.response.CheckResponse;
import org.jordijaspers.eventify.common.mapper.DateTimeMapper;
import org.jordijaspers.eventify.common.mapper.config.SharedMapperConfig;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

import java.util.List;

/**
 * The mapper for the dashboards.
 */
@Mapper(
    config = SharedMapperConfig.class,
    uses = DateTimeMapper.class
)
public abstract class CheckMapper {

    @Named("toCheckResponse")
    public abstract CheckResponse toCheckResponse(Check check);

    @IterableMapping(qualifiedByName = "toCheckResponse")
    public abstract List<CheckResponse> toCheckResponses(List<Check> checks);

}
