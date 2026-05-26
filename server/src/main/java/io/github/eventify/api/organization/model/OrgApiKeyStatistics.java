package io.github.eventify.api.organization.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/** Domain model for API key statistics of an organization. */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class OrgApiKeyStatistics {

    private long revokedThisMonth;
    private long expiringThisMonth;
    private long neverUsed;
    private List<OrgTopKey> topKeys;
}
