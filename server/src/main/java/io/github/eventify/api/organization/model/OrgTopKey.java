package io.github.eventify.api.organization.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/** Domain model for a top API key by request volume. */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class OrgTopKey {

    private String name;
    private String suffix;
    private long totalRequests;
}
