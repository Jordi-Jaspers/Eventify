
package org.jordijaspers.eventify.api.dashboard.repository;

import org.jordijaspers.eventify.api.dashboard.model.Dashboard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * The repository for the {@link Dashboard} entity.
 */
@Repository
public interface DashboardRepository extends JpaRepository<Dashboard, Long> {

    @Query("""
        SELECT DISTINCT d FROM Dashboard d
        LEFT JOIN FETCH d.groups g
        LEFT JOIN FETCH d.dashboardChecks dc
        LEFT JOIN FETCH dc.check c
        WHERE d.id = :dashboardId
        """)
    Optional<Dashboard> findByIdWithConfiguration(@Param("dashboardId") Long dashboardId);

}
