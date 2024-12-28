package org.jordijaspers.eventify.api.dashboard.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import jakarta.persistence.*;

import static org.jordijaspers.eventify.Application.SERIAL_VERSION_UID;

@Data
@Entity
@NoArgsConstructor
@EqualsAndHashCode
@Table(name = "dashboard_group")
public class DashboardGroup implements Serializable {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "dashboard_id",
        nullable = false
    )
    private Dashboard dashboard;

    @Column(nullable = false)
    private String name;

    @CreationTimestamp
    @Column(
        updatable = false,
        nullable = false
    )
    private LocalDateTime created;

    @Column(name = "display_order")
    private int displayOrder;

    /**
     * A constructor to create a new dashboard group.
     *
     * @param dashboard The dashboard to which the group belongs.
     * @param name      The name of the group.
     */
    public DashboardGroup(final Dashboard dashboard, final String name, final int displayOrder) {
        this.dashboard = dashboard;
        this.name = name;
        this.displayOrder = displayOrder;
    }
}
