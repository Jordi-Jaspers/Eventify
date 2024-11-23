package org.jordijaspers.eventify.api.dashboard.model;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.jordijaspers.eventify.api.check.model.Check;
import org.jordijaspers.eventify.api.team.model.Team;

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
@Table(name = "dashboard")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Dashboard implements Serializable {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "global")
    private boolean global;

    @CreationTimestamp
    @Column(
        name = "created",
        updatable = false
    )
    private LocalDateTime created;

    @Column(
        name = "created_by",
        updatable = false
    )
    private String createdBy;

    @ManyToMany(
        fetch = FetchType.LAZY,
        cascade = CascadeType.MERGE
    )
    @JoinTable(
        name = "dashboard_team",
        joinColumns = @JoinColumn(name = "team_id"),
        inverseJoinColumns = @JoinColumn(name = "dashboard_id")
    )
    private List<Team> teams = new ArrayList<>();

    @OneToMany(
        mappedBy = "dashboard",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @OrderBy("group_id NULLS LAST, display_order")
    private List<DashboardCheck> dashboardChecks = new ArrayList<>();

    @OneToMany(
        mappedBy = "dashboard",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<DashboardGroup> groups = new ArrayList<>();

    /**
     * Add an existing check to the dashboard.
     *
     * @param check        The check to add.
     * @param group        The group to add the check to.
     * @param displayOrder The display order of the check.
     */
    public void addCheck(final Check check, final DashboardGroup group, final int displayOrder) {
        final DashboardCheck dashboardCheck = new DashboardCheck(this, check, group, displayOrder);
        dashboardChecks.add(dashboardCheck);
    }

    /**
     * Add a new group to the dashboard.
     *
     * @param name The name of the group.
     */
    public void addGroup(final String name) {
        groups.add(new DashboardGroup(this, name));
    }

    /**
     * Clear the dashboard configuration.
     */
    public void clearConfiguration() {
        dashboardChecks.clear();
        groups.clear();
    }
}
