
package org.jordijaspers.eventify.api.event.repository;

import org.jordijaspers.eventify.api.event.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * The repository for the {@link Event} entity.
 */
@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

}
