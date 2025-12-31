package io.github.eventify.api.organization.model.mapper;

import io.github.eventify.api.organization.model.OrganizationMembership;
import io.github.eventify.api.organization.model.response.OrganizationMembershipResponse;
import io.github.eventify.api.organization.model.response.UserOrganizationResponse;
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
public abstract class OrganizationMembershipMapper {

    /**
     * Map membership to response with full user details.
     *
     * @param membership the membership entity
     * @return the response DTO
     */
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
    @Mapping(
        source = "createdAt",
        target = "joinedAt"
    )
    public abstract OrganizationMembershipResponse toMembershipResponse(OrganizationMembership membership);

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
    @Mapping(
        source = "createdAt",
        target = "joinedAt"
    )
    public abstract UserOrganizationResponse toUserOrganizationResponse(OrganizationMembership membership);
}
