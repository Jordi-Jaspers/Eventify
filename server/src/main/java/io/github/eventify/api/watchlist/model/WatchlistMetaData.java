package io.github.eventify.api.watchlist.model;

import io.github.jframe.datasource.search.JpaSearchSpecification;
import io.github.jframe.datasource.search.SearchType;
import io.github.jframe.datasource.search.model.AbstractSortSearchMetaData;
import io.github.jframe.datasource.search.model.SearchCriterium;
import io.github.jframe.datasource.search.model.input.SortablePageInput;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

/**
 * Contains search field mapping to column name and search types for {@link Watchlist}.
 */
@Component
public class WatchlistMetaData extends AbstractSortSearchMetaData {

    public static final String SEARCH_TERM = "search";

    public static final String NAME = "name";

    public static final String DESCRIPTION = "description";

    public static final String USER_TERM = "user";

    public static final String USER_FIELD = "user.id";

    public static final String CREATED_AT = "createdAt";

    public static final String UPDATED_AT = "updatedAt";

    public static final String ORGANIZATION_TERM = "organization";

    public static final String ORGANIZATION_FIELD = "organization.id";

    /**
     * Constructor to build watchlist metadata / dictionary.
     */
    public WatchlistMetaData() {
        super();
        addField(NAME, NAME, SearchType.FUZZY_TEXT, true);
        addField(DESCRIPTION, DESCRIPTION, SearchType.FUZZY_TEXT, true);
        addField(USER_TERM, USER_FIELD, SearchType.NUMERIC, true);
        addField(ORGANIZATION_TERM, ORGANIZATION_FIELD, SearchType.NUMERIC, true);
        addField(CREATED_AT, CREATED_AT, SearchType.DATE, true);
        addField(UPDATED_AT, UPDATED_AT, SearchType.DATE, true);

        addField(
            SEARCH_TERM,
            List.of(NAME, DESCRIPTION),
            SearchType.MULTI_COLUMN_FUZZY,
            false
        );
    }

    /**
     * Builds a JPA Specification for searching user (personal) watchlists.
     * Includes: search criteria, organization IS NULL.
     *
     * @param input the sortable page input containing search inputs
     * @return the JPA Specification for querying personal watchlists
     */
    public Specification<Watchlist> toUserWatchlistSpecification(final SortablePageInput input) {
        final List<SearchCriterium> criteria = toSearchCriteria(input.getSearchInputs());
        final Specification<Watchlist> searchSpec = new JpaSearchSpecification<>(criteria);

        return Specification
            .where(searchSpec)
            .and(buildPersonalWatchlistSpecification());
    }

    /**
     * Builds a specification for personal watchlists (organization IS NULL).
     *
     * @return the specification for personal watchlists
     */
    private Specification<Watchlist> buildPersonalWatchlistSpecification() {
        return (root, query, cb) -> cb.isNull(root.get(ORGANIZATION_TERM));
    }

    /**
     * Builds a JPA Specification for searching organization watchlists.
     * Includes: search criteria, organization ID filter, organization IS NOT NULL.
     *
     * @param input          the sortable page input containing search inputs
     * @param organizationId the organization ID to filter by
     * @return the JPA Specification for querying organization watchlists
     */
    public Specification<Watchlist> toOrganizationWatchlistSpecification(final SortablePageInput input, final Long organizationId) {
        final List<SearchCriterium> criteria = toSearchCriteria(input.getSearchInputs());
        final Specification<Watchlist> searchSpec = new JpaSearchSpecification<>(criteria);

        return Specification
            .where(searchSpec)
            .and(buildOrganizationWatchlistSpecification(organizationId));
    }

    /**
     * Builds a specification for organization watchlists (organization IS NOT NULL and matches ID).
     *
     * @param organizationId the organization ID to filter by
     * @return the specification for organization watchlists
     */
    private Specification<Watchlist> buildOrganizationWatchlistSpecification(final Long organizationId) {
        return (root, query, cb) -> cb.and(
            cb.isNotNull(root.get(ORGANIZATION_TERM)),
            cb.equal(root.get(ORGANIZATION_TERM).get("id"), organizationId)
        );
    }
}
