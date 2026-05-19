package io.github.eventify.api.admin.repository;

import io.github.eventify.api.admin.model.projection.StorageSizeProjection;
import io.github.eventify.api.event.model.Event;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Repository for admin storage statistics using native queries.
 */
@Repository
public interface AdminStorageRepository extends JpaRepository<Event, Long> {

    @Query(
        value = """
            SELECT t.display_name AS tableName,
                   pg_total_relation_size(t.table_name::regclass) AS sizeBytes,
                   pg_size_pretty(pg_total_relation_size(t.table_name::regclass)) AS sizeFormatted
            FROM (VALUES
                ('event', 'event'),
                ('notification', 'notification'),
                ('channel', 'channel'),
                ('organization', 'organization'),
                ('app_user', '"user"')
            ) AS t(display_name, table_name)
            """,
        nativeQuery = true
    )
    List<StorageSizeProjection> findStorageSizes();
}
