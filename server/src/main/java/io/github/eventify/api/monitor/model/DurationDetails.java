package io.github.eventify.api.monitor.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/** Domain class holding duration details for a specific timestamp. */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DurationDetails {

    private List<TimelineDuration> durations;
    private int selectedIndex;
    private boolean hasPrevious;
    private boolean hasNext;
}
