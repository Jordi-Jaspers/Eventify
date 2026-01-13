package io.github.eventify.api.channel.model.mapper;

import io.github.eventify.api.channel.model.Channel;
import io.github.eventify.api.channel.model.response.ChannelDetailsResponse;
import io.github.jframe.datasource.search.model.mapper.PageMapper;
import io.github.jframe.util.mapper.DateTimeMapper;
import io.github.jframe.util.mapper.config.SharedMapperConfig;

import java.util.List;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * Mapper for channel entities to DTOs.
 */
@Mapper(
    config = SharedMapperConfig.class,
    uses = DateTimeMapper.class
)
public abstract class ChannelMapper extends PageMapper<ChannelDetailsResponse, Channel> {

    /**
     * Maps Channel entity to ChannelDetailsResponse.
     *
     * @param channel the channel entity
     * @return the response DTO
     */
    @Mapping(
        target = "status",
        expression = "java(channel.getStatus().name())"
    )
    @Override
    @Named("toResourceObject")
    public abstract ChannelDetailsResponse toResourceObject(Channel channel);

    /**
     * Maps list of Channel entities to list of ChannelDetailsResponse.
     *
     * @param channels the list of channel entities
     * @return the list of response DTOs
     */
    @IterableMapping(qualifiedByName = "toResourceObject")
    public abstract List<ChannelDetailsResponse> toResourceObjects(List<Channel> channels);

    /**
     * Maps Channel entity to ChannelDetailsResponse (alias for consistency).
     *
     * @param channel the channel entity
     * @return the response DTO
     */
    public ChannelDetailsResponse toDetailsResponse(final Channel channel) {
        return toResourceObject(channel);
    }

    /**
     * Maps list of Channel entities to list of ChannelDetailsResponse (alias for consistency).
     *
     * @param channels the list of channel entities
     * @return the list of response DTOs
     */
    public List<ChannelDetailsResponse> toDetailsResponseList(final List<Channel> channels) {
        return toResourceObjects(channels);
    }
}
