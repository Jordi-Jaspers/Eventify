package org.jordijaspers.eventify.api.monitoring.model.response.delta;

import lombok.Data;
import lombok.NoArgsConstructor;

import org.jordijaspers.eventify.api.monitoring.model.response.TimelineDurationResponse;

@Data
@NoArgsConstructor
public class GroupDeltaResponse {

    private Long groupId;

    private TimelineDurationResponse duration;

}
