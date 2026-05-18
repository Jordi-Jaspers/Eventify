package io.github.eventify.api.admin.model;

import io.github.eventify.common.audit.model.AuditLog;
import io.github.jframe.datasource.search.JpaSearchSpecification;
import io.github.jframe.datasource.search.SearchType;
import io.github.jframe.datasource.search.model.AbstractSortSearchMetaData;
import io.github.jframe.datasource.search.model.SearchCriterium;
import io.github.jframe.datasource.search.model.input.SearchInput;
import io.github.jframe.datasource.search.model.input.SortablePageInput;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

/** Search metadata for admin audit log queries. */
@Component
public class AdminAuditLogMetaData extends AbstractSortSearchMetaData {

    public static final String METHOD = "method";

    public static final String PATH = "path";

    public static final String STATUS_CODE = "statusCode";

    public static final String STATUS = "status";

    public static final String ACTOR = "actor";

    public static final String CREATED_AT = "createdAt";

    public static final String EXCLUDE_PATH = "excludePath";

    private static final String STATUS_2XX = "2xx";

    private static final String STATUS_4XX = "4xx";

    private static final String STATUS_5XX = "5xx";

    private static final String WILDCARD = "%";

    /** Initializes search fields for audit log metadata. */
    public AdminAuditLogMetaData() {
        super();
        addField(METHOD, METHOD, SearchType.FUZZY_TEXT, true);
        addField(PATH, PATH, SearchType.FUZZY_TEXT, true);
        addField(STATUS_CODE, STATUS_CODE, SearchType.NUMERIC, false);
        addField(CREATED_AT, CREATED_AT, SearchType.DATE, true);
        addField(ACTOR, List.of("actor.firstName", "actor.lastName", "actor.email"), SearchType.MULTI_COLUMN_FUZZY, false);
        addField(EXCLUDE_PATH, EXCLUDE_PATH, SearchType.TEXT, false);
    }

    /** Converts a sortable page input to a JPA specification for audit log queries. */
    public Specification<AuditLog> toSearchSpecification(final SortablePageInput input) {
        final List<SearchInput> searchInputs = new ArrayList<>(input.getSearchInputs());

        searchInputs.removeIf(this::isEmptySearchInput);

        final SearchInput statusFilter = extractFilter(searchInputs, STATUS);
        final SearchInput excludePathFilter = extractFilter(searchInputs, EXCLUDE_PATH);

        final List<SearchCriterium> criteria = toSearchCriteria(searchInputs);
        Specification<AuditLog> spec = criteria.isEmpty()
            ? (root, query, cb) -> cb.conjunction()
            : new JpaSearchSpecification<>(criteria);

        if (statusFilter != null && statusFilter.getTextValueList() != null && !statusFilter.getTextValueList().isEmpty()) {
            spec = spec.and(buildStatusRangeSpecification(statusFilter.getTextValueList()));
        }

        if (excludePathFilter != null && excludePathFilter.getTextValue() != null && !excludePathFilter.getTextValue().isBlank()) {
            spec = spec.and(buildExcludePathSpecification(List.of(excludePathFilter.getTextValue().split(","))));
        }

        return spec;
    }

    private boolean isEmptySearchInput(final SearchInput si) {
        final boolean hasNoText = si.getTextValue() == null && isEmptyList(si.getTextValueList());
        return hasNoText && si.getFromDateValue() == null && si.getToDateValue() == null;
    }

    private boolean isEmptyList(final List<String> list) {
        return list == null || list.isEmpty();
    }

    private SearchInput extractFilter(final List<SearchInput> searchInputs, final String fieldName) {
        final SearchInput filter = searchInputs.stream()
            .filter(si -> fieldName.equalsIgnoreCase(si.getFieldName()))
            .findFirst()
            .orElse(null);

        if (filter != null) {
            searchInputs.remove(filter);
        }

        return filter;
    }

    private Specification<AuditLog> buildStatusRangeSpecification(final List<String> statusRanges) {
        return (root, query, cb) -> {
            final List<Predicate> predicates = new ArrayList<>();
            for (final String range : statusRanges) {
                if (STATUS_2XX.equalsIgnoreCase(range)) {
                    predicates.add(cb.between(root.get(STATUS_CODE), (short) 200, (short) 299));
                } else if (STATUS_4XX.equalsIgnoreCase(range)) {
                    predicates.add(cb.between(root.get(STATUS_CODE), (short) 400, (short) 499));
                } else if (STATUS_5XX.equalsIgnoreCase(range)) {
                    predicates.add(cb.between(root.get(STATUS_CODE), (short) 500, (short) 599));
                }
            }
            if (predicates.isEmpty()) {
                return cb.conjunction();
            }
            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }

    private Specification<AuditLog> buildExcludePathSpecification(final List<String> excludedPaths) {
        return (root, query, cb) -> {
            final List<Predicate> predicates = new ArrayList<>();
            for (final String value : excludedPaths) {
                final String trimmed = value.trim();
                if (!trimmed.isEmpty()) {
                    predicates.add(cb.notLike(cb.lower(root.get(PATH)), WILDCARD + trimmed.toLowerCase() + WILDCARD));
                }
            }
            if (predicates.isEmpty()) {
                return cb.conjunction();
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
