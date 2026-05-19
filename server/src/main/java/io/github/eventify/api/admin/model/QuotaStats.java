package io.github.eventify.api.admin.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Domain model for user event quota statistics. */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuotaStats {

    private Long usersNearLimit;

    private Long usersAtLimit;

    private Double averageUtilization;
}
