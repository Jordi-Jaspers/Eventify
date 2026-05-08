package io.github.eventify.api.event.service;

import io.github.eventify.api.event.model.Event;
import io.github.eventify.api.event.model.EventMetaData;
import io.github.eventify.api.event.repository.EventRepository;
import io.github.eventify.common.security.principal.UserTokenPrincipal;
import io.github.jframe.datasource.search.model.input.SortablePageInput;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for event search operations.
 */
@Service
@RequiredArgsConstructor
public class EventService {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final EventRepository eventRepository;

    private final EventMetaData eventMetaData;

    /**
     * Searches events for user's personal channels.
     *
     * @param input the search input with pagination and filters
     * @return page of events
     */
    @Transactional(readOnly = true)
    public Page<Event> searchUserEvents(final SortablePageInput input) {
        final UserTokenPrincipal principal = getLoggedInUser();

        // Build sort and pageable
        Sort sort = eventMetaData.toSort(input.getSortOrder());
        if (sort.isUnsorted()) {
            sort = Sort.by(Sort.Direction.DESC, EventMetaData.TIMESTAMP);
        }
        final int pageSize = input.getPageSize() > 0 ? input.getPageSize() : DEFAULT_PAGE_SIZE;
        final Pageable pageable = PageRequest.of(input.getPageNumber(), pageSize, sort);

        // Build specification and execute
        final Specification<Event> specification = eventMetaData.toUserEventSpecification(
            principal.getUser().getId(),
            input
        );
        return eventRepository.findAll(specification, pageable);
    }

    /**
     * Searches events for organization channels.
     *
     * @param orgId the organization ID
     * @param input the search input with pagination and filters
     * @return page of events
     */
    @Transactional(readOnly = true)
    public Page<Event> searchOrganizationEvents(final Long orgId, final SortablePageInput input) {
        // Build sort and pageable
        Sort sort = eventMetaData.toSort(input.getSortOrder());
        if (sort.isUnsorted()) {
            sort = Sort.by(Sort.Direction.DESC, EventMetaData.TIMESTAMP);
        }
        final int pageSize = input.getPageSize() > 0 ? input.getPageSize() : DEFAULT_PAGE_SIZE;
        final Pageable pageable = PageRequest.of(input.getPageNumber(), pageSize, sort);

        // Build specification and execute
        final Specification<Event> specification = eventMetaData.toOrganizationEventSpecification(orgId, input);
        return eventRepository.findAll(specification, pageable);
    }

    /**
     * Gets the currently logged-in user from security context.
     *
     * @return the user token principal
     */
    private UserTokenPrincipal getLoggedInUser() {
        return (UserTokenPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
