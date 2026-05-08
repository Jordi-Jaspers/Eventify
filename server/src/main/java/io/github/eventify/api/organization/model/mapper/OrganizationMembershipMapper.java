package io.github.eventify.api.organization.model.mapper;

import io.github.eventify.api.organization.model.OrganizationMembership;
import io.github.eventify.api.organization.model.response.OrganizationMembershipResponse;
import io.github.eventify.api.organization.model.response.UserOrganizationResponse;
import io.github.jframe.datasource.search.model.mapper.PageMapper;
import io.github.jframe.util.mapper.DateTimeMapper;
import io.github.jframe.util.mapper.config.SharedMapperConfig;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for organization membership entities and DTOs.
 */
@Mapper(
    config = SharedMapperConfig.class,
    uses = DateTimeMapper.class
)
public abstract class OrganizationMembershipMapper extends PageMapper<OrganizationMembershipResponse, OrganizationMembership> {

    /**
     * Map membership to response with full user details.
     *
     * @param membership the membership entity
     * @return the response DTO
     */
    @Override
    @Mapping(
        source = "user.id",
        target = "userId"
    )
    @Mapping(
        source = "user.email",
        target = "userEmail"
    )
    @Mapping(
        source = "user.firstName",
        target = "userFirstName"
    )
    @Mapping(
        source = "user.lastName",
        target = "userLastName"
    )
    @Mapping(
        source = "organization.id",
        target = "organizationId"
    )
    public abstract OrganizationMembershipResponse toResourceObject(OrganizationMembership membership);

    /**
     * Map membership to response with full user details (alias for controller usage).
     *
     * @param membership the membership entity
     * @return the response DTO
     */
    public OrganizationMembershipResponse toMembershipResponse(final OrganizationMembership membership) {
        return toResourceObject(membership);
    }

    /**
     * Map membership to user organization response.
     *
     * @param membership the membership entity
     * @return the user organization response DTO
     */
    @Mapping(
        source = "organization.id",
        target = "organizationId"
    )
    @Mapping(
        source = "organization.name",
        target = "organizationName"
    )
    @Mapping(
        source = "organization.slug",
        target = "organizationSlug"
    )
    public abstract UserOrganizationResponse toUserOrganizationResponse(OrganizationMembership membership);
}
