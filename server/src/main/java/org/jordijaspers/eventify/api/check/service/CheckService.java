package org.jordijaspers.eventify.api.check.service;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.jordijaspers.eventify.api.check.model.Check;
import org.jordijaspers.eventify.api.check.repository.CheckRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * A service to handle everything around {@link Check}.
 */
@Service
@RequiredArgsConstructor
public class CheckService {

    private static final int MIN_SEARCH_LENGTH = 2;

    private static final double SIMILARITY_THRESHOLD = 0.2;

    private final CheckRepository checkRepository;

    /**
     * Retrieves a page of checks that match the given query. Fuzzy search is used to find similar checks.
     *
     * @param query The query to search for.
     * @param page  The page number.
     * @param size  The page size.
     * @return The page of checks.
     */
    @Transactional(readOnly = true)
    public Page<Check> fuzzySearchChecks(final String query, final int page, final int size) {
        if (query == null || query.trim().length() < MIN_SEARCH_LENGTH) {
            return Page.empty();
        }

        final String normalizedQuery = query.trim();
        final long total = checkRepository.countByNameFuzzy(normalizedQuery, SIMILARITY_THRESHOLD);
        final List<Check> checks = checkRepository.findByNameFuzzy(
            normalizedQuery,
            SIMILARITY_THRESHOLD,
            size,
            page * size
        );

        return new PageImpl<>(checks, PageRequest.of(page, size), total);
    }
}
