package io.github.eventify.api.admin.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Response object representing the storage size of a database table.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Schema(description = "Database table size information")
public class TableSizeEntry {

    @Schema(
        description = "Name of the database table",
        example = "event",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String tableName;

    @Schema(
        description = "Size of the table in bytes",
        example = "1048576",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long sizeBytes;

    @Schema(
        description = "Human-readable formatted size",
        example = "1 MB",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String sizeFormatted;
}
