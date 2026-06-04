package io.github.eventify.api.notification.adapter.model.mapper;

import io.github.eventify.api.notification.adapter.model.AdapterConfig;
import io.github.eventify.api.notification.adapter.model.response.AdapterConfigResponse;
import io.github.jframe.util.mapper.DateTimeMapper;
import io.github.jframe.util.mapper.config.SharedMapperConfig;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Named;

/**
 * Mapper for AdapterConfig entity to response DTOs.
 */
@Mapper(
    config = SharedMapperConfig.class,
    uses = DateTimeMapper.class
)
public abstract class AdapterConfigMapper {

    /**
     * Maps an AdapterConfig entity to a response DTO.
     *
     * @param adapterConfig the entity
     * @return the response DTO
     */
    @Named("toResourceObject")
    public abstract AdapterConfigResponse toResourceObject(AdapterConfig adapterConfig);

    /**
     * Maps a list of AdapterConfig entities to response DTOs.
     *
     * @param adapterConfigs the entities
     * @return the response DTOs
     */
    public abstract List<AdapterConfigResponse> toResourceObjects(List<AdapterConfig> adapterConfigs);
}
