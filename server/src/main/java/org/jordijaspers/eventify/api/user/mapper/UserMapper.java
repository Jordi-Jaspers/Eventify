package org.jordijaspers.eventify.api.user.mapper;

import org.jordijaspers.eventify.api.authentication.model.Role;
import org.jordijaspers.eventify.api.authentication.model.request.RegisterUserRequest;
import org.jordijaspers.eventify.api.authentication.model.response.RegisterResponse;
import org.jordijaspers.eventify.api.authentication.model.response.UserDetailsResponse;
import org.jordijaspers.eventify.api.authentication.model.response.UserResponse;
import org.jordijaspers.eventify.api.team.mapper.TeamMapper;
import org.jordijaspers.eventify.api.user.model.User;
import org.jordijaspers.eventify.common.mapper.DateTimeMapper;
import org.jordijaspers.eventify.common.mapper.config.SharedMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


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
    public abstract UserResponse toUserResponse(User user);

    @Mapping(
        target = "authority",
        expression = "java(mapRoleToAuthority(user.getRole()))"
    )
    public abstract UserDetailsResponse toUserDetailsResponse(User user);

    public String mapRoleToAuthority(final Role role) {
        return role.getAuthority();
    }
}
