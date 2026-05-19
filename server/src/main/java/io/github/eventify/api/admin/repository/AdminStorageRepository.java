package io.github.eventify.api.admin.repository;

import io.github.eventify.api.admin.model.response.TableSizeEntry;

import java.util.List;
import jakarta.persistence.EntityManager;

import org.springframework.stereotype.Repository;

/**
 * Repository for admin storage statistics using native queries.
 */
@Repository
public class AdminStorageRepository {

    private static final String STORAGE_QUERY = """
        SELECT t.display_name,
               pg_total_relation_size(t.table_name::regclass),
               pg_size_pretty(pg_total_relation_size(t.table_name::regclass))
        FROM (VALUES
            ('event', 'event'),
            ('notification', 'notification'),
            ('channel', 'channel'),
            ('organization', 'organization'),
            ('app_user', '"user"')
        ) AS t(display_name, table_name)
        """;

    private final EntityManager entityManager;

    public AdminStorageRepository(final EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @SuppressWarnings("unchecked")
    public List<TableSizeEntry> getStorageStats() {
        final List<Object[]> rows = entityManager.createNativeQuery(STORAGE_QUERY).getResultList();
        return rows.stream()
            .map(
                row -> TableSizeEntry.builder()
                    .tableName((String) row[0])
                    .sizeBytes(((Number) row[1]).longValue())
                    .sizeFormatted((String) row[2])
                    .build()
            )
            .toList();
    }
}
