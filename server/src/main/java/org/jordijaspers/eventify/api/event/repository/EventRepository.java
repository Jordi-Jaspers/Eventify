
package org.jordijaspers.eventify.api.event.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.jordijaspers.eventify.api.event.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


/**
 * The repository for the {@link Event} entity.
 */
@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    /**
     * Finds the last event for the provided check id.
     *
     * @param checkId the check id to find the last event for
     * @return the latest event for the provided check id
     */
    @Query(
        """
            FROM Event e
            WHERE e.id.checkId = :checkId
            AND e.id.timestamp >= :since
            ORDER BY e.id.timestamp
            """
    )
    Optional<Event> findRecentEventSince(@Param("checkId") Long checkId, @Param("since") LocalDateTime since);

}
