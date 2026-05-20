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
 * MetaData for broadcast recipient search — defines searchable fields on Notification entity.
 */
@Component
public class NotificationRecipientMetaData extends AbstractSortSearchMetaData {

    public static final String SEARCH = "search";

    private static final String USER_EMAIL_FIELD = "user.email";

    private static final String USER_FIRST_NAME_FIELD = "user.firstName";

    private static final String USER_LAST_NAME_FIELD = "user.lastName";

    /**
     * Constructor to build recipient metadata / dictionary.
     */
    public NotificationRecipientMetaData() {
        super();
        addField(
            SEARCH,
            List.of(USER_EMAIL_FIELD, USER_FIRST_NAME_FIELD, USER_LAST_NAME_FIELD),
            SearchType.MULTI_COLUMN_FUZZY,
            false
        );
    }

    /**
     * Builds a JPA Specification from the search input, always filtering by broadcastId.
     *
     * @param input       the sortable page input containing search inputs
     * @param broadcastId the broadcast ID to filter by
     * @return the JPA Specification for querying notifications
     */
    public Specification<Notification> toRecipientSpecification(
        final SortablePageInput input, final Long broadcastId) {
        final List<SearchCriterium> criteria = toSearchCriteria(input.getSearchInputs());
        final Specification<Notification> searchSpec = new JpaSearchSpecification<>(criteria);
        return Specification.where(searchSpec).and(broadcastIdEquals(broadcastId));
    }

    private Specification<Notification> broadcastIdEquals(final Long broadcastId) {
        return (root, query, cb) -> cb.equal(root.get("broadcast").get("id"), broadcastId);
    }
}
