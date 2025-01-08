package org.jordijaspers.eventify.api.dashboard.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import jakarta.persistence.*;

import org.jordijaspers.eventify.api.check.model.Check;

import static org.jordijaspers.eventify.Application.SERIAL_VERSION_UID;

@Data
@Entity
@EqualsAndHashCode
@NoArgsConstructor
@Table(name = "dashboard_check")
public class DashboardCheck implements Serializable {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    @EmbeddedId
    private DashboardCheckId id;

    @MapsId("dashboardId")
    @ManyToOne(fetch = FetchType.LAZY)
    private Dashboard dashboard;

    @MapsId("checkId")
    @ManyToOne(fetch = FetchType.EAGER)
    private Check check;

    @JoinColumn(name = "group_id")
    @ManyToOne(fetch = FetchType.EAGER)
    private DashboardGroup group;

    @Column(name = "display_order")
    private int displayOrder;

    /**
     * The all-args constructor to create a new instance.
     *
     * @param dashboard    The dashboard.
     * @param check        The check.
     * @param group        The group.
     * @param displayOrder The display order.
     */
    public DashboardCheck(final Dashboard dashboard, final Check check, final DashboardGroup group, final int displayOrder) {
        this.id = new DashboardCheckId(dashboard.getId(), check.getId());
        this.dashboard = dashboard;
        this.check = check;
        this.group = group;
        this.displayOrder = displayOrder;
    }
}
