package io.github.eventify.api.subscription.model.validator;

import io.github.eventify.api.subscription.model.request.SubscribeRequest;
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
    @DisplayName("Should accept valid request with CRITICAL and IN_APP")
    public void shouldAcceptValidRequestWithCriticalAndInApp() {
        // Given: Valid subscribe request
        final SubscribeRequest request = new SubscribeRequest();
        request.setTargetSeverities(List.of("CRITICAL"));
        request.setAdapters(List.of("IN_APP"));
        final ValidationResult result = new ValidationResult();

        // When: Validating request
        validator.validate(request, result);

        // Then: Validation should pass
        assertThat(result.hasErrors(), is(false));
    }

    @Test
    @DisplayName("Should accept valid request with multiple valid severities")
    public void shouldAcceptValidRequestWithMultipleValidSeverities() {
        // Given: Request with CRITICAL, WARNING, OK
        final SubscribeRequest request = new SubscribeRequest();
        request.setTargetSeverities(List.of("CRITICAL", "WARNING", "OK"));
        request.setAdapters(List.of("IN_APP"));
        final ValidationResult result = new ValidationResult();

        // When: Validating request
        validator.validate(request, result);

        // Then: Validation should pass
        assertThat(result.hasErrors(), is(false));
    }

    // ========================= targetSeverities validation =========================

    @Test
    @DisplayName("Should reject null targetSeverities")
    public void shouldRejectNullTargetSeverities() {
        // Given: Request with null targetSeverities
        final SubscribeRequest request = new SubscribeRequest();
        request.setTargetSeverities(null);
        request.setAdapters(List.of("IN_APP"));
        final ValidationResult result = new ValidationResult();

        // When & Then: Should throw ValidationException
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
        // Given: Request with empty targetSeverities
        final SubscribeRequest request = new SubscribeRequest();
        request.setTargetSeverities(List.of());
        request.setAdapters(List.of("IN_APP"));
        final ValidationResult result = new ValidationResult();

        // When & Then: Should throw ValidationException
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
        // Given: Request with NO_DATA in targetSeverities
        final SubscribeRequest request = new SubscribeRequest();
        request.setTargetSeverities(List.of("CRITICAL", "NO_DATA"));
        request.setAdapters(List.of("IN_APP"));
        final ValidationResult result = new ValidationResult();

        // When & Then: Should throw ValidationException
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

    @Test
    @DisplayName("Should reject invalid severity value in targetSeverities")
    public void shouldRejectInvalidSeverityValueInTargetSeverities() {
        // Given: Request with invalid severity
        final SubscribeRequest request = new SubscribeRequest();
        request.setTargetSeverities(List.of("CRITICAL", "UNKNOWN_SEVERITY"));
        request.setAdapters(List.of("IN_APP"));
        final ValidationResult result = new ValidationResult();

        // When & Then: Should throw ValidationException
        final ValidationException exception = assertThrows(
            ValidationException.class,
            () -> validator.validate(request, result)
        );

        assertThat(exception.getValidationResult().hasErrors(), is(true));
        assertThat(
            exception.getValidationResult().getErrors().stream()
                .anyMatch(e -> e.getField().equals(TARGET_SEVERITIES) && e.getCode().equals(TARGET_SEVERITIES_INVALID)),
            is(true)
        );
    }

    // ========================= adapters validation =========================

    @Test
    @DisplayName("Should reject null adapters")
    public void shouldRejectNullAdapters() {
        // Given: Request with null adapters
        final SubscribeRequest request = new SubscribeRequest();
        request.setTargetSeverities(List.of("CRITICAL"));
        request.setAdapters(null);
        final ValidationResult result = new ValidationResult();

        // When & Then: Should throw ValidationException
        final ValidationException exception = assertThrows(
            ValidationException.class,
            () -> validator.validate(request, result)
        );

        assertThat(exception.getValidationResult().hasErrors(), is(true));
        assertThat(
            exception.getValidationResult().getErrors().stream()
                .anyMatch(e -> e.getField().equals(ADAPTERS) && e.getCode().equals(ADAPTERS_REQUIRED)),
            is(true)
        );
    }

    @Test
    @DisplayName("Should reject empty adapters")
    public void shouldRejectEmptyAdapters() {
        // Given: Request with empty adapters
        final SubscribeRequest request = new SubscribeRequest();
        request.setTargetSeverities(List.of("CRITICAL"));
        request.setAdapters(List.of());
        final ValidationResult result = new ValidationResult();

        // When & Then: Should throw ValidationException
        final ValidationException exception = assertThrows(
            ValidationException.class,
            () -> validator.validate(request, result)
        );

        assertThat(exception.getValidationResult().hasErrors(), is(true));
        assertThat(
            exception.getValidationResult().getErrors().stream()
                .anyMatch(e -> e.getField().equals(ADAPTERS) && e.getCode().equals(ADAPTERS_REQUIRED)),
            is(true)
        );
    }

    @Test
    @DisplayName("Should reject adapters without IN_APP")
    public void shouldRejectAdaptersWithoutInApp() {
        // Given: Request with adapters that don't include IN_APP
        final SubscribeRequest request = new SubscribeRequest();
        request.setTargetSeverities(List.of("CRITICAL"));
        request.setAdapters(List.of("EMAIL"));
        final ValidationResult result = new ValidationResult();

        // When & Then: Should throw ValidationException
        final ValidationException exception = assertThrows(
            ValidationException.class,
            () -> validator.validate(request, result)
        );

        assertThat(exception.getValidationResult().hasErrors(), is(true));
        assertThat(
            exception.getValidationResult().getErrors().stream()
                .anyMatch(e -> e.getField().equals(ADAPTERS) && e.getCode().equals(ADAPTERS_IN_APP_REQUIRED)),
            is(true)
        );
    }

    @Test
    @DisplayName("Should reject null request body")
    public void shouldRejectNullRequestBody() {
        // Given: Null request
        final SubscribeRequest request = null;
        final ValidationResult result = new ValidationResult();

        // When & Then: Should throw ValidationException
        final ValidationException exception = assertThrows(
            ValidationException.class,
            () -> validator.validate(request, result)
        );

        assertThat(exception.getValidationResult().getErrors().size(), is(greaterThanOrEqualTo(1)));
    }
}
