package io.github.eventify.api.subscription.service;

import io.github.eventify.api.subscription.model.Subscription;
import io.github.jframe.datasource.search.JpaSearchSpecification;
import io.github.jframe.datasource.search.SearchType;
import io.github.jframe.datasource.search.model.AbstractSortSearchMetaData;
import io.github.jframe.datasource.search.model.SearchCriterium;
import io.github.jframe.datasource.search.model.input.SortablePageInput;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

/**
 * MetaData for Subscription search and sort operations.
 */
@Component
public class SubscriptionMetaData extends AbstractSortSearchMetaData {

    public static final String WATCHLIST_NAME = "watchlistName";

    public static final String CREATED_AT = "createdAt";

    private static final String USER_FIELD = "user";
    private static final String ORGANIZATION_FIELD = "organization";
    private static final String ID_FIELD = "id";

    /**
     * Constructs the SubscriptionMetaData with registered searchable and sortable fields.
     */
    public SubscriptionMetaData() {
        super();
        addField(WATCHLIST_NAME, "watchlist.name", SearchType.FUZZY_TEXT, true);
        addField(CREATED_AT, CREATED_AT, SearchType.DATE, true);
    }

    /**
     * Builds a JPA specification for personal subscriptions (no org, filtered by user).
     *
     * @param input  the sortable page input
     * @param userId the user ID
     * @return the specification
     */
    public Specification<Subscription> toPersonalSpecification(final SortablePageInput input, final Long userId) {
        final List<SearchCriterium> criteria = toSearchCriteria(input.getSearchInputs());
        final Specification<Subscription> searchSpec = new JpaSearchSpecification<>(criteria);
        return Specification.where(searchSpec)
            .and(userEquals(userId))
            .and(organizationIsNull());
    }

    /**
     * Builds a JPA specification for organization subscriptions.
     *
     * @param input the sortable page input
     * @param orgId the organization ID
     * @return the specification
     */
    public Specification<Subscription> toOrgSpecification(final SortablePageInput input, final Long orgId) {
        final List<SearchCriterium> criteria = toSearchCriteria(input.getSearchInputs());
        final Specification<Subscription> searchSpec = new JpaSearchSpecification<>(criteria);
        return Specification.where(searchSpec)
            .and(organizationEquals(orgId));
    }

    private Specification<Subscription> userEquals(final Long userId) {
        return (root, query, cb) -> cb.equal(root.get(USER_FIELD).get(ID_FIELD), userId);
    }

    private Specification<Subscription> organizationIsNull() {
        return (root, query, cb) -> cb.isNull(root.get(ORGANIZATION_FIELD));
    }

    private Specification<Subscription> organizationEquals(final Long orgId) {
        return (root, query, cb) -> cb.equal(root.get(ORGANIZATION_FIELD).get(ID_FIELD), orgId);
    }
}
