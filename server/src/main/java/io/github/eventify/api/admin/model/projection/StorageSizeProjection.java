package io.github.eventify.api.admin.model.projection;

/**
 * Projection interface for database table storage size data.
 */
public interface StorageSizeProjection {

    /** Database table name. */
    String getTableName();

    /** Size in bytes. */
    Long getSizeBytes();

    /** Human-readable formatted size. */
    String getSizeFormatted();
}
