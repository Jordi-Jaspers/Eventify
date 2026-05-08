package io.github.eventify.api.organization.model;

import io.github.jframe.datasource.search.SearchType;
import io.github.jframe.datasource.search.model.AbstractSortSearchMetaData;

import java.util.List;

import org.springframework.stereotype.Component;

/**
 * Contains search field mapping to column name and search types for {@link OrganizationMembership}.
 */
@Component
public class OrganizationMembershipMetaData extends AbstractSortSearchMetaData {

    private static final String SEARCH = "search";

    private static final String ROLE = "role";

    private static final String JOINED_AT = "joinedAt";

    private static final String ORGANIZATION_TERM = "organization";

    private static final String ORGANIZATION_FIELD = "organization.id";

    private static final String USER_EMAIL_TERM = "email";

    private static final String USER_EMAIL_FIELD = "user.email";

    private static final String USER_FIRST_NAME_FIELD = "user.firstName";

    private static final String USER_LAST_NAME_FIELD = "user.lastName";

    /**
     * Constructor to build organization membership metadata / dictionary.
     */
    public OrganizationMembershipMetaData() {
        super();
        addField(USER_EMAIL_TERM, USER_EMAIL_FIELD, SearchType.FUZZY_TEXT, true);
        addField(ORGANIZATION_TERM, ORGANIZATION_FIELD, SearchType.NUMERIC, true);
        addField(ROLE, ROLE, SearchType.MULTI_ENUM, OrganizationalRole.class, true);
        addField(JOINED_AT, JOINED_AT, SearchType.DATE, true);

        addField(SEARCH, List.of(USER_EMAIL_FIELD, USER_FIRST_NAME_FIELD, USER_LAST_NAME_FIELD), SearchType.MULTI_COLUMN_FUZZY, false);
    }
}
