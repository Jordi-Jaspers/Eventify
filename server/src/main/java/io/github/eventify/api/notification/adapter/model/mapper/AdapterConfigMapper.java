package io.github.eventify.api.notification.adapter.model.mapper;

import io.github.eventify.api.notification.adapter.model.AdapterConfig;
import io.github.eventify.api.notification.adapter.model.response.AdapterConfigResponse;
import io.github.jframe.util.mapper.DateTimeMapper;
import io.github.jframe.util.mapper.config.SharedMapperConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

/**
 * Mapper for AdapterConfig entity to response DTOs.
 */
@Mapper(
    config = SharedMapperConfig.class,
    uses = DateTimeMapper.class
)
public abstract class AdapterConfigMapper {

    private static final String WEBHOOK_URL_KEY = "webhookUrl";

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

    /**
     * Post-mapping hook that masks webhookUrl using the entity masking method.
     *
     * @param adapterConfig the source entity
     * @param response      the target response (mutated)
     */
    @AfterMapping
    protected void maskWebhookUrl(final AdapterConfig adapterConfig, @MappingTarget final AdapterConfigResponse response) {
        if (adapterConfig.getConfig() == null || !adapterConfig.getConfig().containsKey(WEBHOOK_URL_KEY)) {
            return;
        }
        final Map<String, Object> maskedConfig = new HashMap<>(response.getConfig());
        maskedConfig.put(WEBHOOK_URL_KEY, adapterConfig.getMaskedWebhookUrl());
        response.setConfig(maskedConfig);
    }
}
