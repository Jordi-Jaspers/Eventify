package io.github.eventify.api.notification.model.mapper;

import io.github.eventify.api.notification.model.Notification;
import io.github.eventify.api.notification.model.response.RecipientResponse;
import io.github.jframe.datasource.search.model.mapper.PageMapper;
import io.github.jframe.util.mapper.config.SharedMapperConfig;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * Mapper for Notification entity to RecipientResponse DTO.
 */
@Mapper(config = SharedMapperConfig.class)
public abstract class RecipientMapper extends PageMapper<RecipientResponse, Notification> {

    @Override
    @Named("toResourceObject")
    @Mapping(
        source = "user.id",
        target = "userId"
    )
    @Mapping(
        source = "user.email",
        target = "email"
    )
    @Mapping(
        expression = "java(notification.getUser().getFirstName() + \" \" + notification.getUser().getLastName())",
        target = "name"
    )
    public abstract RecipientResponse toResourceObject(Notification notification);
}
