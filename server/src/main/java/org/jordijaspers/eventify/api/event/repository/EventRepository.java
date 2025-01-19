
package org.jordijaspers.eventify.api.event.repository;

import java.util.Optional;

import org.jordijaspers.eventify.api.event.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
    @Query("FROM Event e WHERE e.id.checkId = :checkId ORDER BY e.id.timestamp DESC LIMIT 1")
    Optional<Event> findLastEventForCheckId(Long checkId);

}
