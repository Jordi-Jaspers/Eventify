package io.github.eventify.api.notification.adapter;

import io.github.eventify.api.notification.adapter.adapters.NotificationAdapter;
import io.github.eventify.api.notification.adapter.model.AdapterType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

/**
 * Registry of all available {@link NotificationAdapter} implementations.
 * Provides lookup and filtering by adapter type.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdapterRegistry {

    private final List<NotificationAdapter> adapters;

    /**
     * Returns the adapter matching the given type.
     *
     * @param adapterType the adapter type
     * @return Optional containing the adapter if found
     */
    public Optional<NotificationAdapter> getByAdapterType(final AdapterType adapterType) {
        return adapters.stream()
            .filter(adapter -> adapter.getAdapterType() == adapterType)
            .findFirst();
    }

    /**
     * Returns adapters matching the given types. Unknown types are logged and skipped.
     *
     * @param adapterTypes the list of adapter types
     * @return list of matching adapters
     */
    public List<NotificationAdapter> filterByAdapterTypes(final List<AdapterType> adapterTypes) {
        return adapterTypes.stream()
            .map(adapterType -> {
                final Optional<NotificationAdapter> adapter = getByAdapterType(adapterType);
                if (adapter.isEmpty()) {
                    log.warn("Unknown notification adapter type: '{}' — skipping", adapterType);
                }
                return adapter;
            })
            .filter(Optional::isPresent)
            .map(Optional::get)
            .toList();
    }
}
