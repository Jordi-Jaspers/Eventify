package io.github.eventify.api.user.model;

import io.github.eventify.api.authentication.model.Role;
import io.github.jframe.datasource.search.SearchType;
import io.github.jframe.datasource.search.model.AbstractSortSearchMetaData;

import java.util.List;

import org.springframework.stereotype.Component;

/**
 * Contains search field mapping to column name and search types for {@link User}.
 */
@Component
public class UserMetaData extends AbstractSortSearchMetaData {

    public static final String EMAIL = "email";

    public static final String FIRST_NAME = "firstName";

    public static final String LAST_NAME = "lastName";

    public static final String ENABLED = "enabled";

    public static final String VALIDATED = "validated";

    public static final String LAST_LOGIN = "lastLogin";

    public static final String CREATED_AT = "createdAt";

    public static final String ROLE = "role";

    public static final String ORGANIZATION_FIELD = "organizations.organization.id";

    public static final String ORGANIZATION_TERM = "organization";

    public static final String SEARCH_TERM = "search";

    /**
     * Constructor to build organization metadata / dictionary.
     */
    public UserMetaData() {
        super();
        addField(EMAIL, EMAIL, SearchType.TEXT, true);
        addField(ENABLED, ENABLED, SearchType.BOOLEAN, true);
        addField(VALIDATED, VALIDATED, SearchType.BOOLEAN, true);
        addField(LAST_LOGIN, LAST_LOGIN, SearchType.DATE, true);
        addField(CREATED_AT, CREATED_AT, SearchType.DATE, true);
        addField(ROLE, ROLE, SearchType.MULTI_ENUM, Role.class, true);

        addField(ORGANIZATION_TERM, ORGANIZATION_FIELD, SearchType.NUMERIC, true);
        addField(SEARCH_TERM, List.of(EMAIL, FIRST_NAME, LAST_NAME), SearchType.MULTI_COLUMN_FUZZY, true);
    }
}
