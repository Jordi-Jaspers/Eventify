package io.github.eventify.api.admin.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/** Domain object holding storage size data for a single database table. */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class StorageStats {

    private String tableName;

    private Long sizeBytes;

    private String sizeFormatted;
}
