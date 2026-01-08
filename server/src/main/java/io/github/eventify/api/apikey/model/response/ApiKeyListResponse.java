package io.github.eventify.api.apikey.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Response DTO for listing API keys.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Schema(description = "Response containing a list of API keys")
public class ApiKeyListResponse {

    @Schema(
        description = "List of API keys",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private List<ApiKeyResponse> keys;

    @Schema(
        description = "Maximum number of keys allowed (null for organization keys with no limit)",
        example = "5",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Integer limit;

}
