package io.github.eventify.api.channel.model.mapper;

import io.github.eventify.api.channel.model.Channel;
import io.github.eventify.api.channel.model.response.ChannelDetailsResponse;
import io.github.eventify.api.channel.model.response.ChannelResponse;
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
     * Maps a list of Channels to ChannelResponses.
     *
     * @param channels the channels
     * @return channel responses
     */
    @IterableMapping(qualifiedByName = "toChannelResponse")
    public abstract List<ChannelResponse> toChannelResponses(List<Channel> channels);

    /**
     * Maps a Channel to ChannelResponse.
     *
     * @param channel the channel
     * @return channel response
     */
    @Named("toChannelResponse")
    @Mapping(
        source = "id",
        target = "channelId"
    )
    @Mapping(
        source = "name",
        target = "channelName"
    )
    public abstract ChannelResponse toChannelResponse(Channel channel);
}
