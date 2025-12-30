package io.github.eventify.api.organization.model;

import io.github.jframe.datasource.search.SearchType;
import io.github.jframe.datasource.search.model.AbstractSortSearchMetaData;

import org.springframework.stereotype.Component;

/**
 * Contains search field mapping to column name and search types for {@link Organization}.
 */
@Component
public class OrganisationMetaData extends AbstractSortSearchMetaData {

    private static final String NAME = "name";

    private static final String STATUS = "status";

    private static final String MEMBER_COUNT = "memberCount";

    /**
     * Constructor to build organization metadata / dictionary.
     */
    public OrganisationMetaData() {
        super();
        addField(NAME, NAME, SearchType.TEXT, true, false);
        addField(STATUS, STATUS, SearchType.ENUM, OrganizationStatus.class, true, false);
        addField(MEMBER_COUNT, MEMBER_COUNT, SearchType.NUMBER, true, false);
    }
}
