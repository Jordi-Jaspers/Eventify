package io.github.eventify.api.admin.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Domain model for event severity breakdown. */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeverityBreakdown {

    private Long critical;

    private Long warning;

    private Long ok;
}
