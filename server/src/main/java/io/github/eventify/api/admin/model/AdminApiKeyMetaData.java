package io.github.eventify.api.admin.model;

import io.github.eventify.api.apikey.model.ApiKey;
import io.github.eventify.api.apikey.model.ApiKeyScope;
import io.github.jframe.datasource.search.SearchType;
import io.github.jframe.datasource.search.model.AbstractSortSearchMetaData;
import io.github.jframe.datasource.search.model.JpaSearchSpecification;
import io.github.jframe.datasource.search.model.SearchCriterium;
import io.github.jframe.datasource.search.model.input.SearchInput;
import io.github.jframe.datasource.search.model.input.SortablePageInput;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

/**
 * Contains search field mapping to column name and search types for Admin API Key search.
 */
@Component
public class AdminApiKeyMetaData extends AbstractSortSearchMetaData {

    public static final String SCOPE = "scope";

    public static final String STATUS = "status";

    public static final String STATUS_ACTIVE = "ACTIVE";

    public static final String STATUS_EXPIRED = "EXPIRED";

    public static final String SEARCH_TERM = "searchTerm";

    public static final String TOTAL_REQUESTS = "totalRequests";

    public static final String NAME = "name";

    public static final String CREATED_AT = "createdAt";

    public static final String EXPIRES_AT = "expiresAt";

    public static final String LAST_USED_AT = "lastUsedAt";

    private static final String USER_EMAIL_FIELD = "user.email";

    private static final String USER_FIRST_NAME_FIELD = "user.firstName";

    private static final String USER_LAST_NAME_FIELD = "user.lastName";

    private static final String ORG_NAME_FIELD = "organization.name";

    /**
     * Constructor to build admin API key metadata / dictionary.
     */
    public AdminApiKeyMetaData() {
        super();
        addField(SCOPE, SCOPE, SearchType.MULTI_ENUM, ApiKeyScope.class, true);
        addField(NAME, NAME, SearchType.FUZZY_TEXT, true);
        addField(CREATED_AT, CREATED_AT, SearchType.DATE, true);
        addField(EXPIRES_AT, EXPIRES_AT, SearchType.DATE, true);
        addField(LAST_USED_AT, LAST_USED_AT, SearchType.DATE, true);
        addField(TOTAL_REQUESTS, TOTAL_REQUESTS, SearchType.NUMERIC, true);

        addField(
            SEARCH_TERM,
            List.of(NAME, USER_EMAIL_FIELD, USER_FIRST_NAME_FIELD, USER_LAST_NAME_FIELD, ORG_NAME_FIELD),
            SearchType.MULTI_COLUMN_FUZZY,
            false
        );
    }

    /**
     * Builds a JPA Specification from the search input, including custom status filter handling.
     * The status field is computed (not stored in DB), so it requires custom specification logic.
     *
     * @param input the sortable page input containing search inputs
     * @return the JPA Specification for querying API keys
     */
    public Specification<ApiKey> toSearchSpecification(final SortablePageInput input) {
        final List<SearchInput> searchInputs = new ArrayList<>(input.getSearchInputs());
        final SearchInput statusFilter = extractStatusFilter(searchInputs);

        final List<SearchCriterium> criteria = toSearchCriteria(searchInputs);
        Specification<ApiKey> spec = new JpaSearchSpecification<>(criteria);

        if (statusFilter != null) {
            spec = spec.and(buildStatusSpecification(statusFilter.getTextValue()));
        }

        return spec;
    }

    /**
     * Extracts and removes the status filter from the search inputs list.
     *
     * @param searchInputs the mutable list of search inputs
     * @return the status filter or null if not present
     */
    private SearchInput extractStatusFilter(final List<SearchInput> searchInputs) {
        final SearchInput statusFilter = searchInputs.stream()
            .filter(si -> STATUS.equalsIgnoreCase(si.getFieldName()))
            .findFirst()
            .orElse(null);

        if (statusFilter != null) {
            searchInputs.remove(statusFilter);
        }

        return statusFilter;
    }

    /**
     * Builds a specification for the status filter.
     * ACTIVE: expiresAt is null OR expiresAt is in the future.
     * EXPIRED: expiresAt is not null AND expiresAt is in the past or now.
     *
     * @param statusValue the status value (ACTIVE or EXPIRED)
     * @return the specification for the status filter
     */
    private Specification<ApiKey> buildStatusSpecification(final String statusValue) {
        final Specification<ApiKey> result;
        if (STATUS_ACTIVE.equalsIgnoreCase(statusValue)) {
            result = (root, query, cb) -> cb.or(
                cb.isNull(root.get(EXPIRES_AT)),
                cb.greaterThan(root.get(EXPIRES_AT), OffsetDateTime.now())
            );
        } else if (STATUS_EXPIRED.equalsIgnoreCase(statusValue)) {
            result = (root, query, cb) -> cb.and(
                cb.isNotNull(root.get(EXPIRES_AT)),
                cb.lessThanOrEqualTo(root.get(EXPIRES_AT), OffsetDateTime.now())
            );
        } else {
            result = (root, query, cb) -> cb.conjunction();
        }
        return result;
    }
}
