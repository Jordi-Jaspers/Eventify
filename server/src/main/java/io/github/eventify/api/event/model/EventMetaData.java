package io.github.eventify.api.event.model;

import io.github.jframe.datasource.search.JpaSearchSpecification;
import io.github.jframe.datasource.search.SearchType;
import io.github.jframe.datasource.search.model.AbstractSortSearchMetaData;
import io.github.jframe.datasource.search.model.SearchCriterium;
import io.github.jframe.datasource.search.model.input.SortablePageInput;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

/**
 * Contains search field mapping to column name and search types for {@link Event}.
 */
@Component
public class EventMetaData extends AbstractSortSearchMetaData {

    public static final String CHANNEL_ID_TERM = "channelId";

    public static final String CHANNEL_ID_FIELD = "channel.id";

    public static final String TIMESTAMP = "timestamp";

    public static final String SEVERITY = "severity";

    private static final String CHANNEL_FIELD = "channel";

    private static final String ORGANIZATION_FIELD = "organization";

    private static final String USER_FIELD = "user";

    private static final String ID_FIELD = "id";

    /**
     * Constructor to build event metadata / dictionary.
     */
    public EventMetaData() {
        super();
        addField(CHANNEL_ID_TERM, CHANNEL_ID_FIELD, SearchType.NUMERIC, true);
        addField(TIMESTAMP, TIMESTAMP, SearchType.DATE, true);
        addField(SEVERITY, SEVERITY, SearchType.ENUM, Severity.class, true);
    }

    /**
     * Builds a JPA Specification for searching user (personal) events.
     * Includes: search criteria, channel.organization IS NULL, channel.user.id = userId.
     *
     * @param userId the user ID
     * @param input  the sortable page input containing search inputs
     * @return the JPA Specification for querying personal channel events
     */
    public Specification<Event> toUserEventSpecification(final Long userId, final SortablePageInput input) {
        final List<SearchCriterium> criteria = toSearchCriteria(input.getSearchInputs());
        final Specification<Event> searchSpec = new JpaSearchSpecification<>(criteria);

        return Specification
            .where(searchSpec)
            .and(buildPersonalChannelSpecification())
            .and(buildUserOwnershipSpecification(userId));
    }

    /**
     * Builds a JPA Specification for searching organization events.
     * Includes: search criteria, channel.organization IS NOT NULL.
     *
     * @param orgId the organization ID
     * @param input the sortable page input containing search inputs
     * @return the JPA Specification for querying organization channel events
     */
    public Specification<Event> toOrganizationEventSpecification(final Long orgId, final SortablePageInput input) {
        final List<SearchCriterium> criteria = toSearchCriteria(input.getSearchInputs());
        final Specification<Event> searchSpec = new JpaSearchSpecification<>(criteria);

        return Specification
            .where(searchSpec)
            .and(buildOrganizationSpecification(orgId));
    }

    /**
     * Builds a specification for personal channel events (channel.organization IS NULL).
     *
     * @return the specification for personal channel events
     */
    private Specification<Event> buildPersonalChannelSpecification() {
        return (root, query, cb) -> cb.isNull(root.get(CHANNEL_FIELD).get(ORGANIZATION_FIELD));
    }

    /**
     * Builds a specification for user ownership (channel.user.id = userId).
     *
     * @param userId the user ID
     * @return the specification for user-owned channel events
     */
    private Specification<Event> buildUserOwnershipSpecification(final Long userId) {
        return (root, query, cb) -> cb.equal(root.get(CHANNEL_FIELD).get(USER_FIELD).get(ID_FIELD), userId);
    }

    /**
     * Builds a specification for organization channel events (channel.organization.id = orgId).
     *
     * @param orgId the organization ID
     * @return the specification for organization channel events
     */
    private Specification<Event> buildOrganizationSpecification(final Long orgId) {
        return (root, query, cb) -> cb.equal(root.get(CHANNEL_FIELD).get(ORGANIZATION_FIELD).get(ID_FIELD), orgId);
    }
}
