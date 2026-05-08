package io.github.eventify.api.event.service;

import io.github.eventify.api.event.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.function.IntSupplier;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for cleaning up expired events based on retention policies.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class EventRetentionCleanupService {

    private static final int BATCH_SIZE = 10_000;

    private final EventRepository eventRepository;

    /**
     * Cleans up expired events from both personal and organization channels.
     */
    public void cleanupExpiredEvents() {
        final int totalPersonalDeleted = deleteInBatches(this::deleteExpiredPersonalChannelEvents);
        final int totalOrgDeleted = deleteInBatches(this::deleteExpiredOrganizationChannelEvents);
        log.info(
            "Retention cleanup completed. Deleted '{}' in total: personal='{}', organization='{}'",
            totalPersonalDeleted + totalOrgDeleted,
            totalPersonalDeleted,
            totalOrgDeleted
        );
    }

    /**
     * Deletes a batch of expired events from personal channels.
     *
     * @return the number of deleted events in this batch
     */
    public int deleteExpiredPersonalChannelEvents() {
        return eventRepository.deleteExpiredPersonalChannelEvents(BATCH_SIZE);
    }

    /**
     * Deletes a batch of expired events from organization channels.
     *
     * @return the number of deleted events in this batch
     */
    public int deleteExpiredOrganizationChannelEvents() {
        return eventRepository.deleteExpiredOrganizationChannelEvents(BATCH_SIZE);
    }

    private int deleteInBatches(final IntSupplier deleteBatch) {
        return Stream.generate(deleteBatch::getAsInt)
            .takeWhile(deleted -> deleted > 0)
            .mapToInt(Integer::intValue)
            .sum();
    }
}
