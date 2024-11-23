package org.jordijaspers.eventify.api.dashboard.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;

import static org.jordijaspers.eventify.Application.SERIAL_VERSION_UID;

@Data
@Entity
@NoArgsConstructor
@Table(name = "dashboard_group")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
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

    @OrderBy("display_order")
    @OneToMany(mappedBy = "group")
    private List<DashboardCheck> checks = new ArrayList<>();

    /**
     * A constructor to create a new dashboard group.
     *
     * @param dashboard The dashboard to which the group belongs.
     * @param name      The name of the group.
     */
    public DashboardGroup(final Dashboard dashboard, final String name) {
        this.dashboard = dashboard;
        this.name = name;
    }
}
