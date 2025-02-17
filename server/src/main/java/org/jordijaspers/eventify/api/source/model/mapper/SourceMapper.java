package org.jordijaspers.eventify.api.source.model.mapper;

import java.util.List;

import org.jordijaspers.eventify.api.source.model.Source;
import org.jordijaspers.eventify.api.source.model.response.DetailedSourceResponse;
import org.jordijaspers.eventify.api.source.model.response.SourceResponse;
import org.jordijaspers.eventify.common.mapper.DateTimeMapper;
import org.jordijaspers.eventify.common.mapper.config.SharedMapperConfig;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

@Mapper(
    config = SharedMapperConfig.class,
    uses = DateTimeMapper.class
)
public abstract class SourceMapper {

    @Named("toSourceResponse")
    public abstract SourceResponse toSourceResponse(Source request);

    @Named("toSourceDetailedResponse")
    public abstract DetailedSourceResponse toDetailedSourceResponse(Source request);

    @IterableMapping(qualifiedByName = "toSourceResponse")
    public abstract List<SourceResponse> toSourcesResponse(List<Source> sources);

}
