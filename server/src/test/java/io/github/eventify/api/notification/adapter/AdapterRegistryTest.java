package io.github.eventify.api.notification.adapter;

import io.github.eventify.api.notification.adapter.model.AdapterType;
import io.github.eventify.support.UnitTest;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@DisplayName("Unit Test - Adapter Registry")
public class AdapterRegistryTest extends UnitTest {

    private NoOpNotificationAdapter inAppAdapter;
    private NoOpNotificationAdapter slackAdapter;
    private AdapterRegistry registry;

    @BeforeEach
    public void setUp() {
        inAppAdapter = new NoOpNotificationAdapter(AdapterType.IN_APP);
        slackAdapter = new NoOpNotificationAdapter(AdapterType.SLACK);
        registry = new AdapterRegistry(List.of(inAppAdapter, slackAdapter));
    }

    // ========================= getByAdapterType =========================

    @Test
    @DisplayName("Should return adapter when type matches")
    public void shouldReturnAdapterWhenTypeMatches() {
        // Given: Registry with IN_APP and SLACK adapters
        // When: Looking up IN_APP
        final Optional<NotificationAdapter> result = registry.getByAdapterType(AdapterType.IN_APP);

        // Then: The correct adapter is returned
        assertThat(result.isPresent(), is(true));
        assertThat(result.get(), is(inAppAdapter));
    }

    @Test
    @DisplayName("Should return empty when type has no registered adapter")
    public void shouldReturnEmptyWhenTypeNotFound() {
        // Given: Registry without MATTERMOST adapter
        // When: Looking up MATTERMOST
        final Optional<NotificationAdapter> result = registry.getByAdapterType(AdapterType.MATTERMOST);

        // Then: Empty is returned
        assertThat(result.isPresent(), is(false));
    }

    // ========================= filterByAdapterTypes =========================

    @Test
    @DisplayName("Should return all matching adapters for given types")
    public void shouldReturnMatchingAdaptersForTypes() {
        // Given: Types for both registered adapters
        final List<AdapterType> types = List.of(AdapterType.IN_APP, AdapterType.SLACK);

        // When: Filtering
        final List<NotificationAdapter> result = registry.filterByAdapterTypes(types);

        // Then: Both adapters are returned
        assertThat(result, hasSize(2));
        assertThat(result, containsInAnyOrder(inAppAdapter, slackAdapter));
    }

    @Test
    @DisplayName("Should skip unknown types and return only known adapters")
    public void shouldSkipUnknownTypes() {
        // Given: One registered and one unregistered type (MATTERMOST has no adapter here)
        final List<AdapterType> types = List.of(AdapterType.IN_APP, AdapterType.MATTERMOST);

        // When: Filtering
        final List<NotificationAdapter> result = registry.filterByAdapterTypes(types);

        // Then: Only the known adapter is returned
        assertThat(result, hasSize(1));
        assertThat(result, contains(inAppAdapter));
    }

    @Test
    @DisplayName("Should return empty list when all types are unregistered")
    public void shouldReturnEmptyWhenAllTypesUnregistered() {
        // Given: Only unregistered types
        final List<AdapterType> types = List.of(AdapterType.MATTERMOST);

        // When: Filtering
        final List<NotificationAdapter> result = registry.filterByAdapterTypes(types);

        // Then: Empty list returned
        assertThat(result, is(empty()));
    }

    @Test
    @DisplayName("Should return empty list when types list is empty")
    public void shouldReturnEmptyWhenTypesListIsEmpty() {
        // Given: Empty type list
        final List<AdapterType> types = List.of();

        // When: Filtering
        final List<NotificationAdapter> result = registry.filterByAdapterTypes(types);

        // Then: Empty list returned
        assertThat(result, is(empty()));
    }

    @Test
    @DisplayName("Should return single adapter when only one type matches")
    public void shouldReturnSingleAdapterWhenOneTypeMatches() {
        // Given: Single known type
        final List<AdapterType> types = List.of(AdapterType.SLACK);

        // When: Filtering
        final List<NotificationAdapter> result = registry.filterByAdapterTypes(types);

        // Then: Only the SLACK adapter is returned
        assertThat(result, hasSize(1));
        assertThat(result, contains(slackAdapter));
    }
}
