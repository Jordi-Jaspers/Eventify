package io.github.eventify.api.user.model.mapper;

import io.github.eventify.api.authentication.model.Permission;
import io.github.eventify.api.authentication.model.Role;
import io.github.eventify.api.authentication.model.request.RegisterUserRequest;
import io.github.eventify.api.authentication.model.response.AuthenticationResponse;
import io.github.eventify.api.authentication.model.response.RegisterResponse;
import io.github.eventify.api.organization.model.OrganizationMembership;
import io.github.eventify.api.organization.model.response.UserOrganizationResponse;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.user.model.response.UserDetailsResponse;
import io.github.jframe.datasource.search.model.mapper.PageMapper;
import io.github.jframe.util.mapper.DateTimeMapper;
import io.github.jframe.util.mapper.config.SharedMapperConfig;

import java.util.List;
import java.util.Set;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * The mapper for the user.
 */
@Mapper(
    config = SharedMapperConfig.class,
    uses = DateTimeMapper.class
)
public abstract class UserDetailsMapper extends PageMapper<UserDetailsResponse, User> {

    public abstract User toUser(RegisterUserRequest request);

    public abstract RegisterResponse toRegisterResponse(User user);

    @Mapping(
        target = "refreshToken",
        source = "refreshToken.value"
    )
    @Mapping(
        target = "accessToken",
        source = "accessToken.value"
    )
    @Mapping(
        target = "expiresAt",
        source = "accessToken.expiresAt"
    )
    public abstract AuthenticationResponse toUserResponse(User user);

    @Override
    @Mapping(
        target = "permissions",
        expression = "java(mapRoleToPermissions(user.getRole()))"
    )
    @Mapping(
        target = "organizations",
        expression = "java(mapOrganizations(user.getOrganizations()))"
    )
    @Named("toResourceObject")
    public abstract UserDetailsResponse toResourceObject(User user);

    @IterableMapping(qualifiedByName = "toResourceObject")
    public abstract List<UserDetailsResponse> toResourceObjects(List<User> users);


    public Set<Permission> mapRoleToPermissions(final Role role) {
        return role.getPermissions();
    }

    /**
     * Maps organization memberships to user organization responses.
     *
     * @param memberships the list of organization memberships
     * @return the list of user organization responses
     */
    public List<UserOrganizationResponse> mapOrganizations(final List<OrganizationMembership> memberships) {
        if (memberships == null) {
            return List.of();
        }
        return memberships.stream()
            .map(this::mapMembership)
            .toList();
    }

    /**
     * Maps a single organization membership to user organization response.
     *
     * @param membership the organization membership
     * @return the user organization response
     */
    private UserOrganizationResponse mapMembership(final OrganizationMembership membership) {
        return UserOrganizationResponse.builder()
            .organizationId(membership.getOrganization().getId())
            .organizationName(membership.getOrganization().getName())
            .organizationSlug(membership.getOrganization().getSlug())
            .role(membership.getRole())
            .joinedAt(membership.getCreatedAt())
            .build();
    }
}
