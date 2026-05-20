package io.github.eventify.api.quota.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** Domain class representing a user's quota status. */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuotaStatus {

    private Integer used;
    private Integer limit;
    private Integer remaining;
    private Double percentUsed;
    private LocalDate periodStart;
    private LocalDate periodEnd;
}
