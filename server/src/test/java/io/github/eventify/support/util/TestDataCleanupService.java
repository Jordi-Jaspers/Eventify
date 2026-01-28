package io.github.eventify.support.util;


import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for cleaning up test data efficiently using bulk SQL operations.
 * All deletes run in a single transaction for performance.
 */
@Service
public class TestDataCleanupService {

    private final JdbcTemplate jdbcTemplate;

    public TestDataCleanupService(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Cleans up all test data in a single transaction using bulk SQL deletes.
     *
     * @param testUserIds the IDs of test users to clean up
     * @param testEmails  the emails of test users (for token cleanup)
     * @param namePattern the pattern to match organization names
     */
    @Transactional
    public void cleanUpTestData(
        final List<Long> testUserIds,
        final List<String> testEmails,
        final String namePattern
    ) {
        if (testUserIds.isEmpty()) {
            // Just clean up by name pattern
            cleanUpOrganizationsByNamePattern(namePattern);
            return;
        }

        // Convert to SQL array format
        final String userIdList = toSqlList(testUserIds);

        // Delete in FK dependency order (children before parents)
        // 1. Events (depends on channels)
        jdbcTemplate.execute(
            "DELETE FROM event WHERE channel_id IN (SELECT id FROM channel WHERE user_id IN " + userIdList + ")"
        );

        // 2. User event quotas
        jdbcTemplate.execute("DELETE FROM user_event_quota WHERE user_id IN " + userIdList);

        // 3. API key audit records (revoked_by and owner_user_id are the user FK columns)
        jdbcTemplate.execute("DELETE FROM api_key_audit WHERE revoked_by IN " + userIdList);
        jdbcTemplate.execute("DELETE FROM api_key_audit WHERE owner_user_id IN " + userIdList);

        // 4. API keys
        jdbcTemplate.execute("DELETE FROM api_key WHERE user_id IN " + userIdList);

        // 5. Channels
        jdbcTemplate.execute("DELETE FROM channel WHERE user_id IN " + userIdList);

        // 6. Watchlists
        jdbcTemplate.execute("DELETE FROM watchlist WHERE user_id IN " + userIdList);

        // 7. Organization memberships
        jdbcTemplate.execute("DELETE FROM organization_membership WHERE user_id IN " + userIdList);
        jdbcTemplate.execute(
            "DELETE FROM organization_membership WHERE organization_id IN " +
                "(SELECT id FROM organization WHERE created_by IN " + userIdList + ")"
        );

        // 8. Organizations
        jdbcTemplate.execute("DELETE FROM organization WHERE created_by IN " + userIdList);

        // 9. Tokens
        jdbcTemplate.execute("DELETE FROM token WHERE user_id IN " + userIdList);

        // 10. Users (table name is "user" with quotes - reserved word in PostgreSQL)
        jdbcTemplate.execute("DELETE FROM \"user\" WHERE id IN " + userIdList);

        // Also clean up by name pattern
        cleanUpOrganizationsByNamePattern(namePattern);
    }

    private void cleanUpOrganizationsByNamePattern(final String namePattern) {
        final String escapedPattern = "%" + namePattern.replace("'", "''") + "%";

        jdbcTemplate.execute(
            "DELETE FROM organization_membership WHERE organization_id IN " +
                "(SELECT id FROM organization WHERE name LIKE '" + escapedPattern + "')"
        );
        jdbcTemplate.execute("DELETE FROM organization WHERE name LIKE '" + escapedPattern + "'");
    }

    private String toSqlList(final List<Long> ids) {
        return "(" + String.join(",", ids.stream().map(String::valueOf).toList()) + ")";
    }
}
