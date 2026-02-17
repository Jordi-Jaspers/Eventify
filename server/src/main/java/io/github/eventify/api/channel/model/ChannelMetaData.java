package io.github.eventify.api.channel.model;

import io.github.jframe.datasource.search.SearchType;
import io.github.jframe.datasource.search.model.AbstractSortSearchMetaData;
import io.github.jframe.datasource.search.model.JpaSearchSpecification;
import io.github.jframe.datasource.search.model.SearchCriterium;
import io.github.jframe.datasource.search.model.input.SortablePageInput;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

/**
 * Contains search field mapping to column name and search types for {@link Channel}.
 */
@Component
public class ChannelMetaData extends AbstractSortSearchMetaData {

    public static final String SEARCH_TERM = "search";

    public static final String NAME = "name";

    public static final String DESCRIPTION = "description";

    public static final String STATUS = "status";

    public static final String USER_TERM = "user";

    public static final String USER_FIELD = "user.id";

    public static final String ORGANIZATION_TERM = "organizationId";

    public static final String ORGANIZATION_ID_FIELD = "organization.id";

    public static final String CREATED_AT = "createdAt";

    public static final String UPDATED_AT = "updatedAt";

    public static final String LAST_EVENT_AT = "lastEventAt";

    public static final String IS_STALE = "isStale";

    private static final String ORGANIZATION_ENTITY_FIELD = "organization";

    /**
     * Constructor to build channel metadata / dictionary.
     */
    public ChannelMetaData() {
        super();
        addField(NAME, NAME, SearchType.FUZZY_TEXT, true);
        addField(DESCRIPTION, DESCRIPTION, SearchType.FUZZY_TEXT, true);
        addField(STATUS, STATUS, SearchType.ENUM, ChannelStatus.class, true);
        addField(USER_TERM, USER_FIELD, SearchType.NUMERIC, true);
        addField(ORGANIZATION_TERM, ORGANIZATION_ID_FIELD, SearchType.NUMERIC, true);
        addField(CREATED_AT, CREATED_AT, SearchType.DATE, true);
        addField(UPDATED_AT, UPDATED_AT, SearchType.DATE, true);
        addField(LAST_EVENT_AT, LAST_EVENT_AT, SearchType.DATE, true);
        addField(IS_STALE, IS_STALE, SearchType.BOOLEAN, true);

        addField(
            SEARCH_TERM,
            List.of(NAME, DESCRIPTION),
            SearchType.MULTI_COLUMN_FUZZY,
            false
        );
    }

    /**
     * Builds a JPA Specification for searching user (personal) channels.
     * Includes: search criteria, organization IS NULL, status != PENDING_DELETION.
     *
     * @param input the sortable page input containing search inputs
     * @return the JPA Specification for querying personal channels
     */
    public Specification<Channel> toUserChannelSpecification(final SortablePageInput input) {
        final List<SearchCriterium> criteria = toSearchCriteria(input.getSearchInputs());
        final Specification<Channel> searchSpec = new JpaSearchSpecification<>(criteria);

        return Specification
            .where(searchSpec)
            .and(buildPersonalChannelSpecification())
            .and(buildNotDeletedSpecification());
    }

    /**
     * Builds a JPA Specification for searching organization channels.
     * Includes: search criteria, status != PENDING_DELETION.
     * Note: Organization ID filtering is already handled via search input.
     *
     * @param input the sortable page input containing search inputs
     * @return the JPA Specification for querying organization channels
     */
    public Specification<Channel> toOrganizationChannelSpecification(final SortablePageInput input) {
        final List<SearchCriterium> criteria = toSearchCriteria(input.getSearchInputs());
        final Specification<Channel> searchSpec = new JpaSearchSpecification<>(criteria);

        return Specification
            .where(searchSpec)
            .and(buildNotDeletedSpecification());
    }

    /**
     * Builds a specification for personal channels (organization IS NULL).
     *
     * @return the specification for personal channels
     */
    private Specification<Channel> buildPersonalChannelSpecification() {
        return (root, query, cb) -> cb.isNull(root.get(ORGANIZATION_ENTITY_FIELD));
    }

    /**
     * Builds a specification to exclude soft-deleted channels (status != PENDING_DELETION).
     *
     * @return the specification for non-deleted channels
     */
    private Specification<Channel> buildNotDeletedSpecification() {
        return (root, query, cb) -> cb.notEqual(root.get(STATUS), ChannelStatus.PENDING_DELETION);
    }
}
