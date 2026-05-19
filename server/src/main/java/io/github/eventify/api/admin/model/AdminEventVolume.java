package io.github.eventify.api.admin.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/** Domain object holding daily event volume for a time window. */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminEventVolume {

    private long totalEvents;

    private List<DailyVolumeData> dailyVolume;
}
