package io.github.eventify.api.event.repository;

import io.github.eventify.api.event.model.Event;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for Event entity.
 */
@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    /**
     * Finds all events for a given channel, ordered by timestamp descending.
     *
     * @param channelId the channel ID
     * @return list of events ordered by timestamp (newest first)
     */
    List<Event> findByChannelIdOrderByTimestampDesc(Long channelId);
}
