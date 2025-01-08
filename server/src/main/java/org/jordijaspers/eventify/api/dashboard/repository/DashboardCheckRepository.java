
package org.jordijaspers.eventify.api.dashboard.repository;

import jakarta.transaction.Transactional;

import org.jordijaspers.eventify.api.dashboard.model.Dashboard;
import org.jordijaspers.eventify.api.dashboard.model.DashboardCheck;
import org.jordijaspers.eventify.api.dashboard.model.DashboardCheckId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * The repository for the {@link Dashboard} entity.
 */
@Repository
public interface DashboardCheckRepository extends JpaRepository<DashboardCheck, DashboardCheckId> {

    @Modifying
    @Transactional
    @Query("DELETE FROM DashboardCheck dc WHERE dc.dashboard.id = :dashboardId")
    void deleteConfigurationForDashboard(Long dashboardId);
}
