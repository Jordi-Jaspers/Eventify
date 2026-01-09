package io.github.eventify.api.apikey.model;

import io.github.jframe.datasource.search.SearchType;
import io.github.jframe.datasource.search.model.AbstractSortSearchMetaData;

import java.util.List;

import org.springframework.stereotype.Component;

/**
 * Contains search field mapping to column name and search types for {@link ApiKey}.
 */
@Component
public class ApiKeyMetaData extends AbstractSortSearchMetaData {

    public static final String SEARCH_TERM = "search";

    public static final String NAME = "name";

    public static final String ORGANIZATION_TERM = "organization";

    public static final String ORGANIZATION_FIELD = "organization.id";

    public static final String CREATED_AT = "createdAt";

    public static final String EXPIRES_AT = "expiresAt";

    public static final String LAST_USED_AT = "lastUsedAt";

    private static final String USER_EMAIL_FIELD = "user.email";

    private static final String USER_FIRST_NAME_FIELD = "user.firstName";

    private static final String USER_LAST_NAME_FIELD = "user.lastName";

    /**
     * Constructor to build API key metadata / dictionary.
     */
    public ApiKeyMetaData() {
        super();
        addField(NAME, NAME, SearchType.FUZZY_TEXT, true);
        addField(ORGANIZATION_TERM, ORGANIZATION_FIELD, SearchType.NUMERIC, true);
        addField(CREATED_AT, CREATED_AT, SearchType.DATE, true);
        addField(EXPIRES_AT, EXPIRES_AT, SearchType.DATE, true);
        addField(LAST_USED_AT, LAST_USED_AT, SearchType.DATE, true);

        addField(
            SEARCH_TERM,
            List.of(NAME, USER_EMAIL_FIELD, USER_FIRST_NAME_FIELD, USER_LAST_NAME_FIELD),
            SearchType.MULTI_COLUMN_FUZZY,
            false
        );
    }
}
