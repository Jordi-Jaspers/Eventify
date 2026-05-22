package io.github.eventify.api.dashboard.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/** Organization status summary response. */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Schema(description = "Organization status summary")
@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
public class OrganizationStatusResponse {

    @Schema(
        description = "Organization unique identifier",
        example = "10",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long id;

    @Schema(
        description = "Organization name",
        example = "Acme Corp",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String name;

    @Schema(
        description = "Organization status",
        example = "ACTIVE",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String status;

    @Schema(
        description = "User's role within this organization",
        example = "ADMIN",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String role;

    @Schema(
        description = "Number of events received today",
        example = "1500",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private long eventVolumeToday;

    @Schema(
        description = "Number of channels currently in alert",
        example = "2",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private int channelsInAlertCount;

    /** Returns the organization status. */
    public String status() {
        return status;
    }
}
