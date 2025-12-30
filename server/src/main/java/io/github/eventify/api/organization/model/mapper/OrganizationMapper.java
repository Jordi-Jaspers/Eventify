package io.github.eventify.api.organization.model.mapper;

import io.github.eventify.api.organization.model.Organization;
import io.github.eventify.api.organization.model.response.OrganizationResponse;
import io.github.eventify.api.user.model.mapper.UserMapper;
import io.github.jframe.datasource.search.model.mapper.PageMapper;
import io.github.jframe.util.mapper.DateTimeMapper;
import io.github.jframe.util.mapper.config.SharedMapperConfig;

import org.mapstruct.Mapper;

/**
 * Mapper for organization entities and DTOs.
 */
@Mapper(
    config = SharedMapperConfig.class,
    uses = {
        DateTimeMapper.class,
        UserMapper.class
    }
)
public abstract class OrganizationMapper extends PageMapper<OrganizationResponse, Organization> {

}
