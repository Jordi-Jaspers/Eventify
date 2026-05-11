package io.github.eventify.api.notification.model;

import io.github.jframe.datasource.search.JpaSearchSpecification;
import io.github.jframe.datasource.search.SearchType;
import io.github.jframe.datasource.search.model.AbstractSortSearchMetaData;
import io.github.jframe.datasource.search.model.SearchCriterium;
import io.github.jframe.datasource.search.model.input.SortablePageInput;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

/**
 * MetaData for notification search and pagination.
 */
@Component
public class NotificationMetaData extends AbstractSortSearchMetaData {

    private static final String CATEGORY = "category";

    private static final String CREATED_AT = "createdAt";

    /**
     * Initializes the notification metadata with searchable and sortable fields.
     */
    public NotificationMetaData() {
        super();
        addField(CATEGORY, CATEGORY, SearchType.ENUM, NotificationCategory.class, true);
        addField(CREATED_AT, CREATED_AT, SearchType.DATE, true);
        addField("read", "readAt", SearchType.DATE, true);
    }

    /**
     * Builds a specification for user notifications with the given search input.
     *
     * @param input  the search input
     * @param userId the user ID to filter by
     * @return the specification
     */
    public Specification<Notification> toUserNotificationSpecification(final SortablePageInput input, final Long userId) {
        final List<SearchCriterium> criteria = toSearchCriteria(input.getSearchInputs());
        final Specification<Notification> searchSpec = new JpaSearchSpecification<>(criteria);
        final Specification<Notification> userSpec = (root, query, cb) -> cb.equal(root.get("user").get("id"), userId);
        return Specification.where(searchSpec).and(userSpec);
    }
}
