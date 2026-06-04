package io.github.eventify.api.notification.adapter.service;

import io.github.eventify.api.notification.adapter.AdapterRegistry;
import io.github.eventify.api.notification.adapter.adapters.NotificationAdapter;
import io.github.eventify.api.notification.adapter.model.AdapterConfig;
import io.github.eventify.api.notification.adapter.model.AdapterType;
import io.github.eventify.api.notification.adapter.model.response.TestConnectionResponse;
import io.github.eventify.api.notification.core.model.NotificationCategory;
import io.github.eventify.api.notification.core.model.NotificationPayload;
import io.github.eventify.common.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import org.springframework.stereotype.Service;

/**
 * Service for testing adapter configuration connectivity.
 * Sends a canned test payload to the adapter and returns a success/failure response.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdapterTestService {

    private final AdapterRegistry adapterRegistry;

    /**
     * Tests connectivity for the given adapter type and webhook URL.
     * Creates a transient config with the provided URL and sends a test payload.
     */
    @SuppressWarnings("PMD.UnitTestShouldUseTestAnnotation")
    public TestConnectionResponse testConnection(final AdapterType adapterType, final String webhookUrl) {
        return adapterRegistry.getByAdapterType(adapterType)
            .map(adapter -> doSend(adapter, adapterType, webhookUrl))
            .orElse(TestConnectionResponse.failed("No adapter registered for type: " + adapterType));
    }

    private TestConnectionResponse doSend(final NotificationAdapter adapter, final AdapterType adapterType, final String webhookUrl) {
        final AdapterConfig transientConfig = buildTransientConfig(adapterType, webhookUrl);
        try {
            adapter.send(SecurityUtil.getLoggedInUser(), createTestPayload(), transientConfig);
            return TestConnectionResponse.passed();
        } catch (final Exception e) {
            return TestConnectionResponse.failed(maskError(e.getMessage()));
        }
    }

    private AdapterConfig buildTransientConfig(final AdapterType adapterType, final String webhookUrl) {
        final AdapterConfig config = new AdapterConfig();
        config.setAdapterType(adapterType);
        config.setLabel("Test");
        config.setConfig(Map.of("webhookUrl", webhookUrl));
        config.setEnabled(true);
        return config;
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
