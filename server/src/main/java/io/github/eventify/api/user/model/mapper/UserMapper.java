package io.github.eventify.api.user.model.mapper;

import io.github.eventify.api.user.model.User;
import io.github.eventify.api.user.model.response.UserResponse;
import io.github.jframe.datasource.search.model.mapper.PageMapper;
import io.github.jframe.util.mapper.DateTimeMapper;
import io.github.jframe.util.mapper.config.SharedMapperConfig;

import java.util.List;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

/**
 * The mapper for the user.
 */
@Mapper(
    config = SharedMapperConfig.class,
    uses = DateTimeMapper.class
)
public abstract class UserMapper extends PageMapper<UserResponse, User> {

    @Override
    @Named("toResourceObject")
    public abstract UserResponse toResourceObject(User user);

    @IterableMapping(qualifiedByName = "toResourceObject")
    public abstract List<UserResponse> toResourceObjects(List<User> users);

}
