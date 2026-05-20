package io.github.eventify.api.notification.model.mapper;

import io.github.eventify.api.notification.model.Notification;
import io.github.eventify.api.notification.model.response.NotificationResponse;
import io.github.jframe.datasource.search.model.mapper.PageMapper;
import io.github.jframe.datasource.search.model.resource.PageResource;
import io.github.jframe.util.mapper.DateTimeMapper;
import io.github.jframe.util.mapper.config.SharedMapperConfig;

import java.util.ArrayList;

import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.springframework.data.domain.Page;

/**
 * Mapper for Notification entity to response DTOs.
 */
@Mapper(
    config = SharedMapperConfig.class,
    uses = DateTimeMapper.class
)
public abstract class NotificationMapper extends PageMapper<NotificationResponse, Notification> {

    @Override
    @Named("toResourceObject")
    public abstract NotificationResponse toResourceObject(Notification notification);

    /**
     * Converts a page of notifications to a page resource, ensuring content is never null.
     *
     * @param source the page of notifications
     * @return the page resource with non-null content list
     */
    @Override
    public PageResource<NotificationResponse> toPageResource(final Page<Notification> source) {
        final PageResource<NotificationResponse> pageResource = super.toPageResource(source);
        if (pageResource != null && pageResource.getContent() == null) {
            pageResource.setContent(new ArrayList<>());
        }
        return pageResource;
    }
}
