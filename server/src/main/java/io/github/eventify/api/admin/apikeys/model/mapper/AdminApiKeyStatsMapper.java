package io.github.eventify.api.admin.apikeys.model.mapper;

import io.github.eventify.api.admin.apikeys.model.ApiKeyStats;
import io.github.eventify.api.admin.apikeys.model.response.ApiKeyStatsResponse;
import io.github.jframe.util.mapper.config.SharedMapperConfig;

import org.mapstruct.Mapper;

/** MapStruct mapper for ApiKeyStats domain record to ApiKeyStatsResponse DTO. */
@Mapper(config = SharedMapperConfig.class)
public abstract class AdminApiKeyStatsMapper {

    /** Maps ApiKeyStats domain record to ApiKeyStatsResponse DTO. */
    public abstract ApiKeyStatsResponse toResponse(ApiKeyStats stats);
}
