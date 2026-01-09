package io.github.eventify.api.quota.model.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDate;

/**
 * Response DTO for user quota status.
 */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class UserQuotaResponse {

    private Integer used;

    private Integer limit;

    private Integer remaining;

    private LocalDate periodStart;

    private LocalDate periodEnd;

    private Double percentUsed;
}
