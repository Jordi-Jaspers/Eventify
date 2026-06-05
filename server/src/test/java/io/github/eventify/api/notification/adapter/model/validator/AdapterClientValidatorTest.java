package io.github.eventify.api.notification.adapter.model.validator;

import io.github.eventify.api.notification.adapter.model.AdapterType;
import io.github.eventify.api.notification.adapter.model.request.CreateAdapterConfigRequest;
import io.github.eventify.support.UnitTest;
import io.github.jframe.exception.core.ValidationException;
import io.github.jframe.validation.ValidationResult;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.github.eventify.api.notification.adapter.model.validator.AdapterConfigValidator.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Unit Test - Adapter Config Validator")
public class AdapterClientValidatorTest extends UnitTest {

    private AdapterConfigValidator validator;

    @BeforeEach
    public void setUp() {
        validator = new AdapterConfigValidator();
    }

    // ========================= Valid requests =========================

    @Test
    @DisplayName("Should pass when MATTERMOST request has webhookUrl in config")
    public void shouldPassWhenMattermostRequestHasWebhookUrl() {
        // Given: Valid MATTERMOST request with webhookUrl
        final CreateAdapterConfigRequest request = new CreateAdapterConfigRequest()
            .setAdapterType(AdapterType.MATTERMOST)
            .setLabel("My Mattermost")
            .setConfig(Map.of("webhookUrl", "https://example.com/webhook/abc123"));
        final ValidationResult result = new ValidationResult();

        // When / Then: No exception
        assertDoesNotThrow(() -> validator.validate(request, result));
    }

    @Test
    @DisplayName("Should pass when SLACK request has webhookUrl in config")
    public void shouldPassWhenSlackRequestHasWebhookUrl() {
        // Given: Valid SLACK request with webhookUrl
        final CreateAdapterConfigRequest request = new CreateAdapterConfigRequest()
            .setAdapterType(AdapterType.SLACK)
            .setLabel("My Slack Channel")
            .setConfig(Map.of("webhookUrl", "https://example.com/webhook/services/abc"));
        final ValidationResult result = new ValidationResult();

        // When / Then: No exception
        assertDoesNotThrow(() -> validator.validate(request, result));
    }

    // ========================= system-managed type rejection =========================

    @Test
    @DisplayName("Should reject IN_APP adapter type as system-managed")
    public void shouldRejectInAppAsSystemManaged() {
        // Given: Request with IN_APP type (system-managed)
        final CreateAdapterConfigRequest request = new CreateAdapterConfigRequest()
            .setAdapterType(AdapterType.IN_APP)
            .setLabel("My In-App Notifier")
            .setConfig(Map.of());
        final ValidationResult result = new ValidationResult();

        // When / Then: ValidationException thrown
        final ValidationException ex = assertThrows(
            ValidationException.class,
            () -> validator.validate(request, result)
        );
        assertThat(fieldError(ex, FIELD_ADAPTER_TYPE), is(true));
    }

    @Test
    @DisplayName("Should reject EMAIL adapter type as system-managed")
    public void shouldRejectEmailAsSystemManaged() {
        // Given: Request with EMAIL type (system-managed)
        final CreateAdapterConfigRequest request = new CreateAdapterConfigRequest()
            .setAdapterType(AdapterType.EMAIL)
            .setLabel("My Email Notifier")
            .setConfig(Map.of());
        final ValidationResult result = new ValidationResult();

        // When / Then: ValidationException thrown
        final ValidationException ex = assertThrows(
            ValidationException.class,
            () -> validator.validate(request, result)
        );
        assertThat(fieldError(ex, FIELD_ADAPTER_TYPE), is(true));
    }

    // ========================= adapterType validation =========================

    @Test
    @DisplayName("Should reject null adapterType")
    public void shouldRejectNullAdapterType() {
        // Given: Request with null adapterType
        final CreateAdapterConfigRequest request = new CreateAdapterConfigRequest()
            .setAdapterType(null)
            .setLabel("My Config")
            .setConfig(Map.of());
        final ValidationResult result = new ValidationResult();

        // When / Then: ValidationException thrown
        final ValidationException ex = assertThrows(
            ValidationException.class,
            () -> validator.validate(request, result)
        );
        assertThat(fieldError(ex, FIELD_ADAPTER_TYPE), is(true));
    }

    // ========================= label validation =========================

    @Test
    @DisplayName("Should reject null label")
    public void shouldRejectNullLabel() {
        // Given: Request with null label (using MATTERMOST — a non-system-managed type)
        final CreateAdapterConfigRequest request = new CreateAdapterConfigRequest()
            .setAdapterType(AdapterType.MATTERMOST)
            .setLabel(null)
            .setConfig(Map.of("webhookUrl", "https://example.com/webhook"));
        final ValidationResult result = new ValidationResult();

        // When / Then: ValidationException thrown
        final ValidationException ex = assertThrows(
            ValidationException.class,
            () -> validator.validate(request, result)
        );
        assertThat(fieldError(ex, FIELD_LABEL), is(true));
    }

    @Test
    @DisplayName("Should reject blank label")
    public void shouldRejectBlankLabel() {
        // Given: Request with blank label (using MATTERMOST — a non-system-managed type)
        final CreateAdapterConfigRequest request = new CreateAdapterConfigRequest()
            .setAdapterType(AdapterType.MATTERMOST)
            .setLabel("")
            .setConfig(Map.of("webhookUrl", "https://example.com/webhook"));
        final ValidationResult result = new ValidationResult();

        // When / Then: ValidationException thrown
        final ValidationException ex = assertThrows(
            ValidationException.class,
            () -> validator.validate(request, result)
        );
        assertThat(fieldError(ex, FIELD_LABEL), is(true));
    }

    // ========================= config validation =========================

    @Test
    @DisplayName("Should reject null config")
    public void shouldRejectNullConfig() {
        // Given: Request with null config (using MATTERMOST — a non-system-managed type)
        final CreateAdapterConfigRequest request = new CreateAdapterConfigRequest()
            .setAdapterType(AdapterType.MATTERMOST)
            .setLabel("My Config")
            .setConfig(null);
        final ValidationResult result = new ValidationResult();

        // When / Then: ValidationException thrown
        final ValidationException ex = assertThrows(
            ValidationException.class,
            () -> validator.validate(request, result)
        );
        assertThat(fieldError(ex, FIELD_CONFIG), is(true));
    }

    // ========================= webhookUrl validation for MATTERMOST / SLACK =========================

    @Test
    @DisplayName("Should reject MATTERMOST request without webhookUrl in config")
    public void shouldRejectMattermostWithoutWebhookUrl() {
        // Given: MATTERMOST request missing webhookUrl
        final CreateAdapterConfigRequest request = new CreateAdapterConfigRequest()
            .setAdapterType(AdapterType.MATTERMOST)
            .setLabel("My Mattermost")
            .setConfig(Map.of("someOtherField", "value"));
        final ValidationResult result = new ValidationResult();

        // When / Then: ValidationException thrown with config field error
        final ValidationException ex = assertThrows(
            ValidationException.class,
            () -> validator.validate(request, result)
        );
        assertThat(fieldError(ex, FIELD_CONFIG), is(true));
    }

    @Test
    @DisplayName("Should reject SLACK request without webhookUrl in config")
    public void shouldRejectSlackWithoutWebhookUrl() {
        // Given: SLACK request missing webhookUrl
        final CreateAdapterConfigRequest request = new CreateAdapterConfigRequest()
            .setAdapterType(AdapterType.SLACK)
            .setLabel("My Slack")
            .setConfig(Map.of());
        final ValidationResult result = new ValidationResult();

        // When / Then: ValidationException thrown with config field error
        final ValidationException ex = assertThrows(
            ValidationException.class,
            () -> validator.validate(request, result)
        );
        assertThat(fieldError(ex, FIELD_CONFIG), is(true));
    }

    @Test
    @DisplayName("Should reject MATTERMOST with empty webhookUrl in config")
    public void shouldRejectMattermostWithEmptyWebhookUrl() {
        // Given: MATTERMOST request with blank webhookUrl
        final CreateAdapterConfigRequest request = new CreateAdapterConfigRequest()
            .setAdapterType(AdapterType.MATTERMOST)
            .setLabel("My Mattermost")
            .setConfig(Map.of("webhookUrl", ""));
        final ValidationResult result = new ValidationResult();

        // When / Then: ValidationException thrown
        final ValidationException ex = assertThrows(
            ValidationException.class,
            () -> validator.validate(request, result)
        );
        assertThat(fieldError(ex, FIELD_CONFIG), is(true));
    }

    @Test
    @DisplayName("Should reject null request body")
    public void shouldRejectNullRequestBody() {
        // Given: Null request
        final ValidationResult result = new ValidationResult();

        // When / Then: ValidationException thrown
        final ValidationException ex = assertThrows(
            ValidationException.class,
            () -> validator.validate(null, result)
        );
        assertThat(ex.getValidationResult().hasErrors(), is(true));
    }

    // ========================= helper =========================

    private static boolean fieldError(final ValidationException ex, final String field) {
        return ex.getValidationResult().getErrors().stream()
            .anyMatch(e -> e.getField().equals(field));
    }
}
