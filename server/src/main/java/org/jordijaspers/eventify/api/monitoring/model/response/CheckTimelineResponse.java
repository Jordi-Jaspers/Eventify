package org.jordijaspers.eventify.api.monitoring.model.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import org.jordijaspers.eventify.api.check.model.Check;

@Data
@NoArgsConstructor
public class CheckTimelineResponse {

    private Long id;

    private String name;

    private TimelineResponse timeline;

    /**
     * A default constructor to create an existing check timeline.
     */
    public CheckTimelineResponse(final Check check) {
        this.id = check.getId();
        this.name = check.getName();
    }
}
