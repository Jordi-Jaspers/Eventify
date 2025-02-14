package org.jordijaspers.eventify.api.event.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import org.jordijaspers.eventify.api.event.model.request.EventRequest;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventBatch {

    private Long checkId;

    private List<EventRequest> events;

}
