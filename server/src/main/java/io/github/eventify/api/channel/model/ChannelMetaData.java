package io.github.eventify.api.channel.model;

import io.github.jframe.datasource.search.SearchType;
import io.github.jframe.datasource.search.model.AbstractSortSearchMetaData;

import java.util.List;

import org.springframework.stereotype.Component;

/**
 * Contains search field mapping to column name and search types for {@link Channel}.
 */
@Component
public class ChannelMetaData extends AbstractSortSearchMetaData {

    public static final String SEARCH_TERM = "search";

    public static final String NAME = "name";

    public static final String DESCRIPTION = "description";

    public static final String STATUS = "status";

    public static final String USER_TERM = "user";

    public static final String USER_FIELD = "user.id";

    public static final String ORGANIZATION_TERM = "organization";

    public static final String ORGANIZATION_FIELD = "organization.id";

    public static final String CREATED_AT = "createdAt";

    public static final String UPDATED_AT = "updatedAt";

    /**
     * Constructor to build channel metadata / dictionary.
     */
    public ChannelMetaData() {
        super();
        addField(NAME, NAME, SearchType.FUZZY_TEXT, true);
        addField(DESCRIPTION, DESCRIPTION, SearchType.FUZZY_TEXT, true);
        addField(STATUS, STATUS, SearchType.ENUM, true);
        addField(USER_TERM, USER_FIELD, SearchType.NUMERIC, true);
        addField(ORGANIZATION_TERM, ORGANIZATION_FIELD, SearchType.NUMERIC, true);
        addField(CREATED_AT, CREATED_AT, SearchType.DATE, true);
        addField(UPDATED_AT, UPDATED_AT, SearchType.DATE, true);

        addField(
            SEARCH_TERM,
            List.of(NAME, DESCRIPTION),
            SearchType.MULTI_COLUMN_FUZZY,
            false
        );
    }
}
