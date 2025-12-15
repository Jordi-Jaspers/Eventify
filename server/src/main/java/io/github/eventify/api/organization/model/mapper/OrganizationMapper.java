package io.github.eventify.api.organization.model.mapper;

import io.github.eventify.api.organization.model.Organization;
import io.github.eventify.api.organization.model.response.OrganizationResponse;
import io.github.jframe.util.mapper.DateTimeMapper;
import io.github.jframe.util.mapper.config.SharedMapperConfig;

import org.mapstruct.Mapper;

/**
 * Mapper for organization entities and DTOs.
 */
@Mapper(
    config = SharedMapperConfig.class,
    uses = DateTimeMapper.class
)
public abstract class OrganizationMapper {

    /**
     * Map Organization entity to OrganizationResponse DTO.
     *
     * @param organization the organization entity
     * @return the response DTO
     */
    public abstract OrganizationResponse toOrganizationResponse(Organization organization);
}
