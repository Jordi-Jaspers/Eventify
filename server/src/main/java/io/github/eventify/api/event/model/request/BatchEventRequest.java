package io.github.eventify.api.event.model.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Request for batch event ingestion.
 * Contains a list of CreateEventRequest, where each event must have a timestamp.
 */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class BatchEventRequest {

    private List<CreateEventRequest> events;
}
