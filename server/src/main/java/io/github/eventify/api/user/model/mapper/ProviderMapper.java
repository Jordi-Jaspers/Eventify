package io.github.eventify.api.user.model.mapper;

import io.github.eventify.api.user.model.AuthProvider;
import io.github.eventify.api.user.model.ProviderInfo;
import io.github.eventify.api.user.model.response.ProviderResponse;
import io.github.jframe.util.mapper.config.SharedMapperConfig;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * MapStruct mapper for converting ProviderInfo domain record to ProviderResponse DTO.
 */
@Mapper(config = SharedMapperConfig.class)
public abstract class ProviderMapper {

    @Mapping(
        target = "id",
        ignore = true
    )
    @Mapping(
        target = "provider",
        source = "provider",
        qualifiedByName = "stringToAuthProvider"
    )
    public abstract ProviderResponse toResponse(ProviderInfo providerInfo);

    public abstract List<ProviderResponse> toResponses(List<ProviderInfo> providerInfos);

    @Named("stringToAuthProvider")
    public AuthProvider stringToAuthProvider(final String provider) {
        return AuthProvider.valueOf(provider);
    }
}
