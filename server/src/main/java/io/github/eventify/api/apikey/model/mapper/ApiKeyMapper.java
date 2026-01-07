package io.github.eventify.api.apikey.model.mapper;

import io.github.eventify.api.apikey.model.ApiKey;
import io.github.eventify.api.apikey.model.response.ApiKeyCreationResponse;
import io.github.eventify.api.apikey.model.response.ApiKeyListResponse;
import io.github.eventify.api.apikey.model.response.ApiKeyResponse;
import io.github.jframe.datasource.search.model.mapper.PageMapper;
import io.github.jframe.util.mapper.DateTimeMapper;
import io.github.jframe.util.mapper.config.SharedMapperConfig;

import java.util.List;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * Mapper for API key entities to DTOs.
 */
@Mapper(
    config = SharedMapperConfig.class,
    uses = DateTimeMapper.class
)
public abstract class ApiKeyMapper extends PageMapper<ApiKeyResponse, ApiKey> {

    /**
     * Maps ApiKey to ApiKeyResponse with masked key.
     *
     * @param apiKey the API key entity
     * @return the response DTO
     */
    @Mapping(
        target = "maskedKey",
        expression = "java(apiKey.getMaskedKey())"
    )
    @Override
    @Named("toResourceObject")
    public abstract ApiKeyResponse toResourceObject(ApiKey apiKey);

    /**
     * Maps list of ApiKeys to list of ApiKeyResponse.
     *
     * @param apiKeys the list of API key entities
     * @return the list of response DTOs
     */
    @IterableMapping(qualifiedByName = "toResourceObject")
    public abstract List<ApiKeyResponse> toResourceObjects(List<ApiKey> apiKeys);

    /**
     * Maps ApiKeyCreationResult to ApiKeyCreationResponse.
     *
     * @param apiKey the creation result
     * @return the creation response DTO
     */
    public abstract ApiKeyCreationResponse toCreationResponse(ApiKey apiKey);

    /**
     * Maps list of ApiKeys to ApiKeyListResponse with pagination.
     *
     * @param keys  the list of API key entities
     * @param limit the maximum number of items
     * @return the list response DTO
     */
    public ApiKeyListResponse toApiKeyList(final List<ApiKey> keys, final int limit) {
        return new ApiKeyListResponse(toResourceObjects(keys), limit);
    }
}
