package io.github.eventify.api.quota.model.mapper;

import io.github.eventify.api.quota.model.QuotaStatus;
import io.github.eventify.api.quota.model.response.UserQuotaResponse;
import io.github.jframe.util.mapper.config.SharedMapperConfig;

import org.mapstruct.Mapper;

/**
 * MapStruct mapper for converting QuotaStatus domain record to UserQuotaResponse DTO.
 */
@Mapper(config = SharedMapperConfig.class)
public abstract class QuotaMapper {

    public abstract UserQuotaResponse toResponse(QuotaStatus quotaStatus);
}
