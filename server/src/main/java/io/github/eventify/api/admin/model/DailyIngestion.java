package io.github.eventify.api.admin.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/** Domain model for a single day's event ingestion total. */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyIngestion {

    private LocalDate date;

    private Long eventCount;
}
