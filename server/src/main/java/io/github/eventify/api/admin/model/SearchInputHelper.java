package io.github.eventify.api.admin.model;

import io.github.jframe.datasource.search.model.input.SearchInput;
import lombok.experimental.UtilityClass;

import java.util.List;

/** Shared utility methods for admin search input processing. */
@UtilityClass
class SearchInputHelper {

    /**
     * Extracts and removes the first search input matching the given field name.
     *
     * @param searchInputs the mutable list of search inputs
     * @param fieldName    the field name to match (case-insensitive)
     * @return the matching search input, or null if not found
     */
    static SearchInput extractFilter(final List<SearchInput> searchInputs, final String fieldName) {
        final SearchInput filter = searchInputs.stream()
            .filter(si -> fieldName.equalsIgnoreCase(si.getFieldName()))
            .findFirst()
            .orElse(null);

        if (filter != null) {
            searchInputs.remove(filter);
        }

        return filter;
    }
}
