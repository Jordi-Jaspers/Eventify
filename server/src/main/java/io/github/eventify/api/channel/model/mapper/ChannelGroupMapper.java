package io.github.eventify.api.channel.model.mapper;

import io.github.eventify.api.channel.model.Channel;
import io.github.eventify.api.channel.model.ChannelGroup;
import io.github.eventify.api.channel.model.response.ChannelGroupResponse;
import io.github.eventify.api.watchlist.model.request.ChannelGroupRequest;
import io.github.jframe.util.mapper.config.SharedMapperConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * Mapper for ChannelGroup domain objects and DTOs.
 *
 * <p>This mapper handles conversions between:
 * <ul>
 * <li>{@link ChannelGroup} → {@link ChannelGroupResponse} (for API responses)</li>
 * <li>{@link ChannelGroupRequest} → {@link ChannelGroup} (for API requests)</li>
 * </ul>
 *
 * <p>Used by {@code WatchlistMapper} and {@code MonitorMapper} via the {@code uses} attribute.
 */
@Mapper(
    config = SharedMapperConfig.class,
    uses = ChannelMapper.class
)
public abstract class ChannelGroupMapper {

    // ==================== Domain -> Response ====================

    /**
     * Maps a ChannelGroup to ChannelGroupResponse. MapStruct auto-maps id, name, channelIds (via getter), timeline (via getter), and
     * channels.
     *
     * @param group the channel group
     * @return the response DTO
     */
    public abstract ChannelGroupResponse toResponse(ChannelGroup group);

    /**
     * Maps a list of ChannelGroups to ChannelGroupResponses.
     *
     * @param groups the channel groups
     * @return the response DTOs
     */
    public abstract List<ChannelGroupResponse> toResponseList(List<ChannelGroup> groups);

    /**
     * Maps a ChannelGroup to a simple response with only IDs (no enriched data). Used for watchlist configuration responses where full
     * channel data is not needed.
     *
     * @param group the channel group
     * @return the response DTO with channelIds only
     */
    @Named("toSimpleResponse")
    @Mapping(
        target = "timeline",
        ignore = true
    )
    @Mapping(
        target = "channels",
        ignore = true
    )
    public abstract ChannelGroupResponse toSimpleResponse(ChannelGroup group);

    /**
     * Maps a list of ChannelGroups to simple responses.
     *
     * @param groups the channel groups
     * @return the response DTOs
     */
    @Named("toSimpleResponseList")
    @IterableMapping(qualifiedByName = "toSimpleResponse")
    public abstract List<ChannelGroupResponse> toSimpleResponseList(List<ChannelGroup> groups);

    // ==================== Request -> Domain ====================

    /**
     * Maps a ChannelGroupRequest to ChannelGroup domain object. Generates a new UUID if not provided in the request.
     *
     * @param request the group request
     * @return the channel group
     */
    @Mapping(
        target = "channels",
        source = "channelIds",
        qualifiedByName = "channelIdsToChannels"
    )
    @Mapping(
        target = "id",
        source = "request",
        qualifiedByName = "resolveGroupId"
    )
    public abstract ChannelGroup toChannelGroup(ChannelGroupRequest request);

    /**
     * Maps a list of ChannelGroupRequests to ChannelGroups.
     *
     * @param requests the group requests
     * @return the channel groups
     */
    public abstract List<ChannelGroup> toChannelGroupList(List<ChannelGroupRequest> requests);

    // ==================== Helper Methods ====================

    /**
     * Resolves the group ID, generating a new UUID if not provided.
     *
     * @param request the group request
     * @return the group ID
     */
    @Named("resolveGroupId")
    protected UUID resolveGroupId(final ChannelGroupRequest request) {
        return request.getId() != null ? request.getId() : UUID.randomUUID();
    }

    /**
     * Converts channel IDs to Channel objects with only the id field populated.
     *
     * @param channelIds the list of channel IDs
     * @return the list of Channel objects
     */
    @Named("channelIdsToChannels")
    protected List<Channel> channelIdsToChannels(final List<Long> channelIds) {
        if (channelIds == null) {
            return new ArrayList<>();
        }
        return channelIds.stream()
            .map(Channel::new)
            .toList();
    }
}
