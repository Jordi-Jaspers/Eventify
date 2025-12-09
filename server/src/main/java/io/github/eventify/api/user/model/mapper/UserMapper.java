package io.github.eventify.api.user.model.mapper;

import io.github.eventify.api.authentication.model.Permission;
import io.github.eventify.api.authentication.model.Role;
import io.github.eventify.api.authentication.model.request.RegisterUserRequest;
import io.github.eventify.api.authentication.model.response.AuthenticationResponse;
import io.github.eventify.api.authentication.model.response.RegisterResponse;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.user.model.response.UserDetailsResponse;
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
public abstract class UserMapper {

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

    @Mapping(
        target = "permissions",
        expression = "java(mapRoleToPermissions(user.getRole()))"
    )
    @Named("toUserDetailsResponse")
    public abstract UserDetailsResponse toUserDetailsResponse(User user);

    @IterableMapping(qualifiedByName = "toUserDetailsResponse")
    public abstract List<UserDetailsResponse> toUserDetailsResponseList(List<User> users);


    public Set<Permission> mapRoleToPermissions(final Role role) {
        return role.getPermissions();
    }

}
