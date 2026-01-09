package io.github.eventify.api.admin.model;

import io.github.eventify.api.apikey.model.ApiKeyScope;
import io.github.jframe.datasource.search.SearchType;
import io.github.jframe.datasource.search.model.AbstractSortSearchMetaData;

import org.springframework.stereotype.Component;

/**
 * Contains search field mapping to column name and search types for Admin API Key Audit search.
 */
@Component
public class AdminApiKeyAuditMetaData extends AbstractSortSearchMetaData {

    public static final String KEY_NAME = "keyName";

    public static final String SCOPE = "scope";

    public static final String REVOKED_AT = "revokedAt";

    public static final String CREATED_AT = "createdAt";

    /**
     * Constructor to build admin API key audit metadata / dictionary.
     */
    public AdminApiKeyAuditMetaData() {
        super();
        addField(KEY_NAME, KEY_NAME, SearchType.FUZZY_TEXT, true);
        addField(SCOPE, SCOPE, SearchType.MULTI_ENUM, ApiKeyScope.class, true);
        addField(REVOKED_AT, REVOKED_AT, SearchType.DATE, true);
        addField(CREATED_AT, CREATED_AT, SearchType.DATE, true);
    }
}
