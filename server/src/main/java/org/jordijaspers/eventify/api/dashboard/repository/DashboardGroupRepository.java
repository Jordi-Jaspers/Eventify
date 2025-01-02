
package org.jordijaspers.eventify.api.dashboard.repository;

import org.jordijaspers.eventify.api.dashboard.model.Dashboard;
import org.jordijaspers.eventify.api.dashboard.model.DashboardGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;

/**
 * The repository for the {@link Dashboard} entity.
 */
@Repository
public interface DashboardGroupRepository extends JpaRepository<DashboardGroup, Long> {

    @Modifying
    @Transactional
    @Query("DELETE FROM DashboardGroup dg WHERE dg.dashboard.id = :dashboardId")
    void deleteGroupsForDashboard(Long dashboardId);
}
