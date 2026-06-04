package io.github.eventify.api.notification.adapter.service;

import io.github.eventify.api.notification.adapter.AdapterRegistry;
import io.github.eventify.api.notification.adapter.adapters.NotificationAdapter;
import io.github.eventify.api.notification.adapter.model.AdapterConfig;
import io.github.eventify.api.notification.adapter.model.response.TestConnectionResponse;
import io.github.eventify.api.notification.adapter.repository.AdapterConfigRepository;
import io.github.eventify.api.notification.core.model.NotificationCategory;
import io.github.eventify.api.notification.core.model.NotificationPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

/**
 * Service for testing adapter configuration connectivity.
 * Sends a canned test payload to the adapter and returns a success/failure response.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdapterTestService {

    private static final String CONFIG_NOT_FOUND = "Adapter config not found";

    private final AdapterConfigRepository adapterConfigRepository;
    private final AdapterRegistry adapterRegistry;

    /**
     * Tests the connection for the given adapter config owned by the specified user.
     */
    @SuppressWarnings("PMD.UnitTestShouldUseTestAnnotation")
    public TestConnectionResponse testConnection(final Long configId, final Long userId) {
        return adapterConfigRepository.findByIdAndUserId(configId, userId)
            .map(this::sendTestNotification)
            .orElse(TestConnectionResponse.failed(CONFIG_NOT_FOUND));
    }

    /**
     * Tests the connection for the given adapter config owned by the specified organization.
     */
    @SuppressWarnings("PMD.UnitTestShouldUseTestAnnotation")
    public TestConnectionResponse testConnectionForOrg(final Long configId, final Long orgId) {
        return adapterConfigRepository.findByIdAndOrganizationId(configId, orgId)
            .map(this::sendTestNotification)
            .orElse(TestConnectionResponse.failed(CONFIG_NOT_FOUND));
    }

    private TestConnectionResponse sendTestNotification(final AdapterConfig config) {
        final NotificationAdapter adapter = adapterRegistry.getByAdapterType(config.getAdapterType()).orElse(null);
        if (adapter == null) {
            return TestConnectionResponse.failed("No adapter registered for type: " + config.getAdapterType());
        }
        return doSend(adapter, config);
    }

    private TestConnectionResponse doSend(final NotificationAdapter adapter, final AdapterConfig config) {
        try {
            adapter.send(config.getUser(), createTestPayload(), config);
            return TestConnectionResponse.passed();
        } catch (final Exception e) {
            return TestConnectionResponse.failed(maskError(e.getMessage()));
        }
    }

    private NotificationPayload createTestPayload() {
        return new NotificationPayload(
            NotificationCategory.ANNOUNCEMENT,
            "Test Notification",
            "This is a test notification from Eventify. If you see this, your adapter is configured correctly.",
            "/",
            "Open Eventify",
            false,
            null
        );
    }

    private String maskError(final String message) {
        if (message == null) {
            return null;
        }
        return message.replaceAll("https?://[^\\s/]+\\S*", "https://***");
    }
}
