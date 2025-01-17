package org.jordijaspers.eventify.api.monitoring.model.response.delta;

import lombok.Data;
import lombok.NoArgsConstructor;

import org.jordijaspers.eventify.api.monitoring.model.response.TimelineDurationResponse;

@Data
@NoArgsConstructor
public class CheckDeltaResponse {

    private Long checkId;

    private TimelineDurationResponse checkDuration;

    private GroupDeltaResponse groupDuration;
}
