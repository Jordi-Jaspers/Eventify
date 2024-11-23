package org.jordijaspers.eventify.api.dashboard.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import jakarta.persistence.Embeddable;

import static org.jordijaspers.eventify.Application.SERIAL_VERSION_UID;

@Data
@Embeddable
@NoArgsConstructor
public class DashboardCheckId implements Serializable {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    private Long dashboardId;

    private Long checkId;

    /**
     * The all-args constructor to create a new instance.
     *
     * @param dashboardId The dashboard id.
     * @param checkId     The check id.
     */
    public DashboardCheckId(final Long dashboardId, final Long checkId) {
        this.dashboardId = dashboardId;
        this.checkId = checkId;
    }
}
