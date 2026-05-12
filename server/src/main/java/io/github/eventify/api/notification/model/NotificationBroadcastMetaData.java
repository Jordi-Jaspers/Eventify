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
 * Contains search field mapping to column name and search types for notification broadcast search.
 */
@Component
public class NotificationBroadcastMetaData extends AbstractSortSearchMetaData {

    public static final String CATEGORY = "category";

    public static final String AUDIENCE_TYPE = "audienceType";

    public static final String TITLE = "title";

    public static final String CREATED_AT = "createdAt";

    private static final String SENT_BY_EMAIL_TERM = "sentByEmail";

    private static final String SENT_BY_EMAIL_FIELD = "sentBy.email";

    /**
     * Constructor to build notification broadcast metadata / dictionary.
     */
    public NotificationBroadcastMetaData() {
        super();
        addField(CATEGORY, CATEGORY, SearchType.ENUM, NotificationCategory.class, true);
        addField(AUDIENCE_TYPE, AUDIENCE_TYPE, SearchType.TEXT, true);
        addField(TITLE, TITLE, SearchType.FUZZY_TEXT, true);
        addField(CREATED_AT, CREATED_AT, SearchType.DATE, true);
        addField(SENT_BY_EMAIL_TERM, SENT_BY_EMAIL_FIELD, SearchType.FUZZY_TEXT, true);
    }

    /**
     * Builds a JPA Specification from the search input.
     *
     * @param input the sortable page input containing search inputs
     * @return the JPA Specification for querying notification broadcasts
     */
    public Specification<NotificationBroadcast> toSearchSpecification(final SortablePageInput input) {
        final List<SearchCriterium> criteria = toSearchCriteria(input.getSearchInputs());
        return new JpaSearchSpecification<>(criteria);
    }
}
