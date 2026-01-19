package io.github.eventify.api.event.service;

import io.github.eventify.api.event.repository.EventRepository;
import io.github.eventify.support.UnitTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * Unit Test - Event Retention Cleanup Service
 */
@DisplayName("Unit Test - Event Retention Cleanup Service")
public class EventRetentionCleanupServiceTest extends UnitTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventRetentionCleanupService eventRetentionCleanupService;

    @BeforeEach
    public void setUp() {
        // Given: Service is initialized with mocked EventRepository
    }

    @Test
    @DisplayName("Should delete expired personal channel events when retention exceeded")
    public void shouldDeleteExpiredPersonalChannelEventsWhenRetentionExceeded() {
        // Given: 50 expired events in personal channels
        when(eventRepository.deleteExpiredPersonalChannelEvents(anyInt())).thenReturn(50);

        // When: Deleting expired personal channel events
        final int deletedCount = eventRetentionCleanupService.deleteExpiredPersonalChannelEvents();

        // Then: Should return count of deleted events
        assertThat(deletedCount, is(equalTo(50)));
        verify(eventRepository).deleteExpiredPersonalChannelEvents(anyInt());
    }

    @Test
    @DisplayName("Should delete expired organization channel events when retention exceeded")
    public void shouldDeleteExpiredOrganizationChannelEventsWhenRetentionExceeded() {
        // Given: 75 expired events in organization channels
        when(eventRepository.deleteExpiredOrganizationChannelEvents(anyInt())).thenReturn(75);

        // When: Deleting expired organization channel events
        final int deletedCount = eventRetentionCleanupService.deleteExpiredOrganizationChannelEvents();

        // Then: Should return count of deleted events
        assertThat(deletedCount, is(equalTo(75)));
        verify(eventRepository).deleteExpiredOrganizationChannelEvents(anyInt());
    }

    @Test
    @DisplayName("Should return zero when no personal channel events expired")
    public void shouldReturnZeroWhenNoPersonalChannelEventsExpired() {
        // Given: No expired events in personal channels
        when(eventRepository.deleteExpiredPersonalChannelEvents(anyInt())).thenReturn(0);

        // When: Deleting expired personal channel events
        final int deletedCount = eventRetentionCleanupService.deleteExpiredPersonalChannelEvents();

        // Then: Should return zero
        assertThat(deletedCount, is(equalTo(0)));
        verify(eventRepository).deleteExpiredPersonalChannelEvents(anyInt());
    }

    @Test
    @DisplayName("Should return zero when no organization channel events expired")
    public void shouldReturnZeroWhenNoOrganizationChannelEventsExpired() {
        // Given: No expired events in organization channels
        when(eventRepository.deleteExpiredOrganizationChannelEvents(anyInt())).thenReturn(0);

        // When: Deleting expired organization channel events
        final int deletedCount = eventRetentionCleanupService.deleteExpiredOrganizationChannelEvents();

        // Then: Should return zero
        assertThat(deletedCount, is(equalTo(0)));
        verify(eventRepository).deleteExpiredOrganizationChannelEvents(anyInt());
    }

    @Test
    @DisplayName("Should orchestrate cleanup of both channel types when called")
    public void shouldOrchestrateCleanupOfBothChannelTypesWhenCalled() {
        // Given: Multiple batches of expired events
        when(eventRepository.deleteExpiredPersonalChannelEvents(anyInt()))
            .thenReturn(100)  // First batch personal
            .thenReturn(50)   // Second batch personal
            .thenReturn(0);   // Third batch personal (done)
        when(eventRepository.deleteExpiredOrganizationChannelEvents(anyInt()))
            .thenReturn(80)   // First batch org
            .thenReturn(40)   // Second batch org
            .thenReturn(0);   // Third batch org (done)

        // When: Running cleanup
        eventRetentionCleanupService.cleanupExpiredEvents();

        // Then: Should delete from both personal and org channels until 0 returned
        verify(eventRepository, times(3)).deleteExpiredPersonalChannelEvents(anyInt());
        verify(eventRepository, times(3)).deleteExpiredOrganizationChannelEvents(anyInt());
    }

    @Test
    @DisplayName("Should continue batch processing until zero deleted")
    public void shouldContinueBatchProcessingUntilZeroDeleted() {
        // Given: Multiple batches needed for personal channels
        when(eventRepository.deleteExpiredPersonalChannelEvents(anyInt()))
            .thenReturn(1000)  // First batch
            .thenReturn(1000)  // Second batch
            .thenReturn(500)   // Third batch
            .thenReturn(0);    // Fourth batch (done)
        when(eventRepository.deleteExpiredOrganizationChannelEvents(anyInt()))
            .thenReturn(0);    // Org channels (none)

        // When: Running cleanup for personal channels
        eventRetentionCleanupService.cleanupExpiredEvents();

        // Then: Should loop until zero returned
        verify(eventRepository, times(4)).deleteExpiredPersonalChannelEvents(anyInt());
        verify(eventRepository, times(1)).deleteExpiredOrganizationChannelEvents(anyInt());
    }

    @Test
    @DisplayName("Should complete gracefully when no events need deletion")
    public void shouldCompleteGracefullyWhenNoEventsNeedDeletion() {
        // Given: No expired events
        when(eventRepository.deleteExpiredPersonalChannelEvents(anyInt())).thenReturn(0);
        when(eventRepository.deleteExpiredOrganizationChannelEvents(anyInt())).thenReturn(0);

        // When: Running cleanup
        eventRetentionCleanupService.cleanupExpiredEvents();

        // Then: Should query both channel types once each
        verify(eventRepository, times(1)).deleteExpiredPersonalChannelEvents(anyInt());
        verify(eventRepository, times(1)).deleteExpiredOrganizationChannelEvents(anyInt());
    }

    @Test
    @DisplayName("Should handle database exception during personal channel cleanup")
    public void shouldHandleDatabaseExceptionDuringPersonalChannelCleanup() {
        // Given: Database throws exception
        when(eventRepository.deleteExpiredPersonalChannelEvents(anyInt()))
            .thenThrow(new RuntimeException("Database error"));

        // When & Then: Exception should propagate
        try {
            eventRetentionCleanupService.deleteExpiredPersonalChannelEvents();
        } catch (final RuntimeException e) {
            assertThat(e.getMessage(), is(equalTo("Database error")));
        }

        verify(eventRepository).deleteExpiredPersonalChannelEvents(anyInt());
    }

    @Test
    @DisplayName("Should handle database exception during organization channel cleanup")
    public void shouldHandleDatabaseExceptionDuringOrganizationChannelCleanup() {
        // Given: Database throws exception
        when(eventRepository.deleteExpiredOrganizationChannelEvents(anyInt()))
            .thenThrow(new RuntimeException("Connection timeout"));

        // When & Then: Exception should propagate
        try {
            eventRetentionCleanupService.deleteExpiredOrganizationChannelEvents();
        } catch (final RuntimeException e) {
            assertThat(e.getMessage(), is(equalTo("Connection timeout")));
        }

        verify(eventRepository).deleteExpiredOrganizationChannelEvents(anyInt());
    }

    @Test
    @DisplayName("Should process large batch counts correctly")
    public void shouldProcessLargeBatchCountsCorrectly() {
        // Given: Large number of expired events
        when(eventRepository.deleteExpiredPersonalChannelEvents(anyInt())).thenReturn(10000);

        // When: Deleting expired events
        final int deletedCount = eventRetentionCleanupService.deleteExpiredPersonalChannelEvents();

        // Then: Should return correct count
        assertThat(deletedCount, is(equalTo(10000)));
        verify(eventRepository).deleteExpiredPersonalChannelEvents(anyInt());
    }

    @Test
    @DisplayName("Should process negative return gracefully")
    public void shouldProcessNegativeReturnGracefully() {
        // Given: Unexpected negative count from database
        when(eventRepository.deleteExpiredPersonalChannelEvents(anyInt())).thenReturn(-1);

        // When: Deleting expired events
        final int deletedCount = eventRetentionCleanupService.deleteExpiredPersonalChannelEvents();

        // Then: Should return the value as-is
        assertThat(deletedCount, is(equalTo(-1)));
        verify(eventRepository).deleteExpiredPersonalChannelEvents(anyInt());
    }
}
