package io.github.eventify.api.subscription.model.validator;

import io.github.eventify.api.event.model.Severity;
import io.github.eventify.api.subscription.model.request.CreateSubscriptionRequest;
import io.github.eventify.support.UnitTest;
import io.github.jframe.exception.core.ValidationException;
import io.github.jframe.validation.ValidationResult;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.github.eventify.api.subscription.model.validator.SubscriptionValidator.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Unit Test - Subscription Validator")
public class SubscriptionValidatorTest extends UnitTest {

    private SubscriptionValidator validator;

    @BeforeEach
    public void setUp() {
        validator = new SubscriptionValidator();
    }

    // ========================= Valid requests =========================

    @Test
    @DisplayName("Should accept valid request with severities and adapter config IDs")
    public void shouldAcceptValidRequestWithSeveritiesAndAdapterConfigIds() {
        // Given: valid create subscription request
        final CreateSubscriptionRequest request = aValidCreateRequest();
        final ValidationResult result = new ValidationResult();

        // When / Then: validation passes without exception
        assertDoesNotThrow(() -> validator.validate(request, result));
        assertThat(result.hasErrors(), is(false));
    }

    @Test
    @DisplayName("Should accept valid request with multiple severities")
    public void shouldAcceptValidRequestWithMultipleSeverities() {
        // Given: request with multiple valid severities
        final CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setWatchlistId(1L);
        request.setTargetSeverities(List.of(Severity.CRITICAL, Severity.WARNING, Severity.OK));
        request.setAdapterConfigIds(List.of(1L));
        final ValidationResult result = new ValidationResult();

        // When / Then: validation passes
        assertDoesNotThrow(() -> validator.validate(request, result));
        assertThat(result.hasErrors(), is(false));
    }

    // ========================= targetSeverities validation =========================

    @Test
    @DisplayName("Should reject null targetSeverities")
    public void shouldRejectNullTargetSeverities() {
        // Given: request with null targetSeverities
        final CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setWatchlistId(1L);
        request.setTargetSeverities(null);
        request.setAdapterConfigIds(List.of(1L));
        final ValidationResult result = new ValidationResult();

        // When / Then: validation exception is thrown with correct field error
        final ValidationException exception = assertThrows(
            ValidationException.class,
            () -> validator.validate(request, result)
        );

        assertThat(exception.getValidationResult().hasErrors(), is(true));
        assertThat(
            exception.getValidationResult().getErrors().stream()
                .anyMatch(e -> e.getField().equals(TARGET_SEVERITIES) && e.getCode().equals(TARGET_SEVERITIES_REQUIRED)),
            is(true)
        );
    }

    @Test
    @DisplayName("Should reject empty targetSeverities")
    public void shouldRejectEmptyTargetSeverities() {
        // Given: request with empty targetSeverities
        final CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setWatchlistId(1L);
        request.setTargetSeverities(List.of());
        request.setAdapterConfigIds(List.of(1L));
        final ValidationResult result = new ValidationResult();

        // When / Then: validation exception is thrown
        final ValidationException exception = assertThrows(
            ValidationException.class,
            () -> validator.validate(request, result)
        );

        assertThat(exception.getValidationResult().hasErrors(), is(true));
        assertThat(
            exception.getValidationResult().getErrors().stream()
                .anyMatch(e -> e.getField().equals(TARGET_SEVERITIES) && e.getCode().equals(TARGET_SEVERITIES_REQUIRED)),
            is(true)
        );
    }

    @Test
    @DisplayName("Should reject NO_DATA in targetSeverities")
    public void shouldRejectNoDataInTargetSeverities() {
        // Given: request with NO_DATA in targetSeverities
        final CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setWatchlistId(1L);
        request.setTargetSeverities(List.of(Severity.CRITICAL, Severity.NO_DATA));
        request.setAdapterConfigIds(List.of(1L));
        final ValidationResult result = new ValidationResult();

        // When / Then: validation exception is thrown with NO_DATA error code
        final ValidationException exception = assertThrows(
            ValidationException.class,
            () -> validator.validate(request, result)
        );

        assertThat(exception.getValidationResult().hasErrors(), is(true));
        assertThat(
            exception.getValidationResult().getErrors().stream()
                .anyMatch(e -> e.getField().equals(TARGET_SEVERITIES) && e.getCode().equals(TARGET_SEVERITIES_NO_DATA_NOT_ALLOWED)),
            is(true)
        );
    }

    // ========================= adapterConfigIds validation =========================

    @Test
    @DisplayName("Should reject null adapterConfigIds")
    public void shouldRejectNullAdapterConfigIds() {
        // Given: request with null adapterConfigIds
        final CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setWatchlistId(1L);
        request.setTargetSeverities(List.of(Severity.CRITICAL));
        request.setAdapterConfigIds(null);
        final ValidationResult result = new ValidationResult();

        // When / Then: validation exception is thrown with correct field error
        final ValidationException exception = assertThrows(
            ValidationException.class,
            () -> validator.validate(request, result)
        );

        assertThat(exception.getValidationResult().hasErrors(), is(true));
        assertThat(
            exception.getValidationResult().getErrors().stream()
                .anyMatch(e -> e.getField().equals(ADAPTER_CONFIG_IDS) && e.getCode().equals(ADAPTER_CONFIG_IDS_REQUIRED)),
            is(true)
        );
    }

    @Test
    @DisplayName("Should reject empty adapterConfigIds")
    public void shouldRejectEmptyAdapterConfigIds() {
        // Given: request with empty adapterConfigIds
        final CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setWatchlistId(1L);
        request.setTargetSeverities(List.of(Severity.CRITICAL));
        request.setAdapterConfigIds(List.of());
        final ValidationResult result = new ValidationResult();

        // When / Then: validation exception is thrown
        final ValidationException exception = assertThrows(
            ValidationException.class,
            () -> validator.validate(request, result)
        );

        assertThat(exception.getValidationResult().hasErrors(), is(true));
        assertThat(
            exception.getValidationResult().getErrors().stream()
                .anyMatch(e -> e.getField().equals(ADAPTER_CONFIG_IDS) && e.getCode().equals(ADAPTER_CONFIG_IDS_REQUIRED)),
            is(true)
        );
    }

    @Test
    @DisplayName("Should not require IN_APP — any adapter config ID is valid")
    public void shouldAcceptAnyAdapterConfigId() {
        // Given: request with arbitrary IDs (not constrained to IN_APP)
        final CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setWatchlistId(1L);
        request.setTargetSeverities(List.of(Severity.WARNING));
        request.setAdapterConfigIds(List.of(1L, 2L));
        final ValidationResult result = new ValidationResult();

        // When / Then: validation passes — no IN_APP constraint
        assertDoesNotThrow(() -> validator.validate(request, result));
        assertThat(result.hasErrors(), is(false));
    }

    @Test
    @DisplayName("Should reject null request body")
    public void shouldRejectNullRequestBody() {
        // Given: null request
        final ValidationResult result = new ValidationResult();

        // When / Then: validation exception is thrown
        final ValidationException exception = assertThrows(
            ValidationException.class,
            () -> validator.validate(null, result)
        );

        assertThat(exception.getValidationResult().getErrors().size(), is(greaterThanOrEqualTo(1)));
    }

    // ========================= FACTORY METHODS =========================

    private static CreateSubscriptionRequest aValidCreateRequest() {
        final CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setWatchlistId(1L);
        request.setTargetSeverities(List.of(Severity.CRITICAL));
        request.setAdapterConfigIds(List.of(1L));
        return request;
    }
}
