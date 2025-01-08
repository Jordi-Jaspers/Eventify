package org.jordijaspers.eventify.api.user.model.mapper;

import java.util.List;

import org.jordijaspers.eventify.api.authentication.model.Role;
import org.jordijaspers.eventify.api.authentication.model.request.RegisterUserRequest;
import org.jordijaspers.eventify.api.authentication.model.response.RegisterResponse;
import org.jordijaspers.eventify.api.authentication.model.response.UserDetailsResponse;
import org.jordijaspers.eventify.api.authentication.model.response.UserResponse;
import org.jordijaspers.eventify.api.team.model.mapper.TeamMapper;
import org.jordijaspers.eventify.api.user.model.User;
import org.jordijaspers.eventify.common.mapper.DateTimeMapper;
import org.jordijaspers.eventify.common.mapper.config.SharedMapperConfig;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;


/**
 * The mapper for the user.
 */
@Mapper(
    config = SharedMapperConfig.class,
    uses = {
        DateTimeMapper.class,
        TeamMapper.class
    }
)
public abstract class UserMapper {

    public abstract User toUser(RegisterUserRequest request);

    @Mapping(
        target = "authority",
        expression = "java(mapRoleToAuthority(user.getRole()))"
    )
    public abstract RegisterResponse toRegisterResponse(User user);

    @Mapping(
        target = "authority",
        expression = "java(mapRoleToAuthority(user.getRole()))"
    )
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
    @Mapping(
        target = "teams",
        qualifiedByName = "toTeamResponsesWithoutMembers"
    )
    public abstract UserResponse toUserResponse(User user);

    @Mapping(
        target = "authority",
        expression = "java(mapRoleToAuthority(user.getRole()))"
    )
    @Mapping(
        target = "teams",
        qualifiedByName = "toTeamResponsesWithoutMembers"
    )
    @Named("toUserDetailsResponse")
    public abstract UserDetailsResponse toUserDetailsResponse(User user);

    @IterableMapping(qualifiedByName = "toUserDetailsResponse")
    public abstract List<UserDetailsResponse> toUserDetailsResponseList(List<User> users);

    public String mapRoleToAuthority(final Role role) {
        return role.getAuthority();
    }
}
