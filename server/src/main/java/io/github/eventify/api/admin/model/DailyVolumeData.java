package io.github.eventify.api.admin.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/** Domain object holding event count for a single day. */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyVolumeData {

    private LocalDate date;

    private long eventCount;
}
