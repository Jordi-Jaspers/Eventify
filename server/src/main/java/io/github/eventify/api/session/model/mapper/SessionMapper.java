package io.github.eventify.api.session.model.mapper;

import io.github.eventify.api.session.model.response.SessionResponse;
import io.github.eventify.api.token.model.Token;
import io.github.jframe.util.mapper.DateTimeMapper;
import io.github.jframe.util.mapper.config.SharedMapperConfig;

import java.util.List;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * MapStruct mapper for converting Token to SessionResponse.
 */
@Mapper(
    config = SharedMapperConfig.class,
    uses = DateTimeMapper.class
)
public abstract class SessionMapper {

    @Named("toResponse")
    @Mapping(
        target = "current",
        ignore = true
    )
    @Mapping(
        target = "createdAt",
        ignore = true
    )
    public abstract SessionResponse toResponse(Token token);

    @IterableMapping(qualifiedByName = "toResponse")
    public abstract List<SessionResponse> toResponses(List<Token> tokens);
}
