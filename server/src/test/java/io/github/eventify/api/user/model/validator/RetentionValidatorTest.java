package io.github.eventify.api.user.model.validator;

import io.github.eventify.api.user.model.request.UpdateRetentionRequest;
import io.github.eventify.support.UnitTest;
import io.github.jframe.exception.core.ValidationException;
import io.github.jframe.validation.ValidationResult;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Unit Test - Retention Validator")
public class RetentionValidatorTest extends UnitTest {

    private RetentionValidator validator;

    @BeforeEach
    public void setUp() {
        validator = new RetentionValidator();
    }

    @Test
    @DisplayName("Should accept valid retention value 90")
    public void shouldAcceptValidRetentionValue90() {
        // Given: Request with retention 90 days
        final UpdateRetentionRequest request = new UpdateRetentionRequest();
        request.setRetentionDays(90);

        // When/Then: Validating the request should not throw exception
        assertDoesNotThrow(() -> validator.validate(request, new ValidationResult()));
    }

    @Test
    @DisplayName("Should accept valid retention value 180")
    public void shouldAcceptValidRetentionValue180() {
        // Given: Request with retention 180 days
        final UpdateRetentionRequest request = new UpdateRetentionRequest();
        request.setRetentionDays(180);

        // When/Then: Validating the request should not throw exception
        assertDoesNotThrow(() -> validator.validate(request, new ValidationResult()));
    }

    @Test
    @DisplayName("Should accept valid retention value 365")
    public void shouldAcceptValidRetentionValue365() {
        // Given: Request with retention 365 days
        final UpdateRetentionRequest request = new UpdateRetentionRequest();
        request.setRetentionDays(365);

        // When/Then: Validating the request should not throw exception
        assertDoesNotThrow(() -> validator.validate(request, new ValidationResult()));
    }

    @Test
    @DisplayName("Should accept valid retention value 730")
    public void shouldAcceptValidRetentionValue730() {
        // Given: Request with retention 730 days
        final UpdateRetentionRequest request = new UpdateRetentionRequest();
        request.setRetentionDays(730);

        // When/Then: Validating the request should not throw exception
        assertDoesNotThrow(() -> validator.validate(request, new ValidationResult()));
    }

    @Test
    @DisplayName("Should accept valid retention value 1095")
    public void shouldAcceptValidRetentionValue1095() {
        // Given: Request with retention 1095 days
        final UpdateRetentionRequest request = new UpdateRetentionRequest();
        request.setRetentionDays(1095);

        // When/Then: Validating the request should not throw exception
        assertDoesNotThrow(() -> validator.validate(request, new ValidationResult()));
    }

    @Test
    @DisplayName("Should accept valid retention value 1825")
    public void shouldAcceptValidRetentionValue1825() {
        // Given: Request with retention 1825 days
        final UpdateRetentionRequest request = new UpdateRetentionRequest();
        request.setRetentionDays(1825);

        // When/Then: Validating the request should not throw exception
        assertDoesNotThrow(() -> validator.validate(request, new ValidationResult()));
    }

    @Test
    @DisplayName("Should reject invalid retention value 100")
    public void shouldRejectInvalidRetentionValue100() {
        // Given: Request with invalid retention value
        final UpdateRetentionRequest request = new UpdateRetentionRequest();
        request.setRetentionDays(100);

        // When: Validating the request
        final ValidationException exception = assertThrows(
            ValidationException.class,
            () -> validator.validate(request, new ValidationResult())
        );

        // Then: Exception should contain validation error
        assertThat(exception.getValidationResult().hasErrors(), is(true));
        assertThat(exception.getValidationResult().getErrors().size(), is(1));
    }

    @Test
    @DisplayName("Should reject invalid retention value 500")
    public void shouldRejectInvalidRetentionValue500() {
        // Given: Request with invalid retention value
        final UpdateRetentionRequest request = new UpdateRetentionRequest();
        request.setRetentionDays(500);

        // When/Then: Validating the request should throw ValidationException
        assertThrows(
            ValidationException.class,
            () -> validator.validate(request, new ValidationResult())
        );
    }

    @Test
    @DisplayName("Should reject invalid retention value 1000")
    public void shouldRejectInvalidRetentionValue1000() {
        // Given: Request with invalid retention value
        final UpdateRetentionRequest request = new UpdateRetentionRequest();
        request.setRetentionDays(1000);

        // When/Then: Validating the request should throw ValidationException
        assertThrows(
            ValidationException.class,
            () -> validator.validate(request, new ValidationResult())
        );
    }

    @Test
    @DisplayName("Should reject negative retention value")
    public void shouldRejectNegativeRetentionValue() {
        // Given: Request with negative retention value
        final UpdateRetentionRequest request = new UpdateRetentionRequest();
        request.setRetentionDays(-1);

        // When/Then: Validating the request should throw ValidationException
        assertThrows(
            ValidationException.class,
            () -> validator.validate(request, new ValidationResult())
        );
    }

    @Test
    @DisplayName("Should reject zero retention value")
    public void shouldRejectZeroRetentionValue() {
        // Given: Request with zero retention value
        final UpdateRetentionRequest request = new UpdateRetentionRequest();
        request.setRetentionDays(0);

        // When/Then: Validating the request should throw ValidationException
        assertThrows(
            ValidationException.class,
            () -> validator.validate(request, new ValidationResult())
        );
    }

    @Test
    @DisplayName("Should reject null retention value")
    public void shouldRejectNullRetentionValue() {
        // Given: Request with null retention value
        final UpdateRetentionRequest request = new UpdateRetentionRequest();
        request.setRetentionDays(null);

        // When/Then: Validating the request should throw ValidationException
        assertThrows(
            ValidationException.class,
            () -> validator.validate(request, new ValidationResult())
        );
    }

    @Test
    @DisplayName("Should reject retention value slightly above 365")
    public void shouldRejectRetentionValue366() {
        // Given: Request with invalid retention value 366
        final UpdateRetentionRequest request = new UpdateRetentionRequest();
        request.setRetentionDays(366);

        // When/Then: Validating the request should throw ValidationException
        assertThrows(
            ValidationException.class,
            () -> validator.validate(request, new ValidationResult())
        );
    }

    @Test
    @DisplayName("Should reject retention value slightly below 365")
    public void shouldRejectRetentionValue364() {
        // Given: Request with invalid retention value 364
        final UpdateRetentionRequest request = new UpdateRetentionRequest();
        request.setRetentionDays(364);

        // When/Then: Validating the request should throw ValidationException
        assertThrows(
            ValidationException.class,
            () -> validator.validate(request, new ValidationResult())
        );
    }

    @Test
    @DisplayName("Should reject retention value slightly above 1825")
    public void shouldRejectRetentionValue1826() {
        // Given: Request with invalid retention value 1826
        final UpdateRetentionRequest request = new UpdateRetentionRequest();
        request.setRetentionDays(1826);

        // When/Then: Validating the request should throw ValidationException
        assertThrows(
            ValidationException.class,
            () -> validator.validate(request, new ValidationResult())
        );
    }

    @Test
    @DisplayName("Should reject very large retention value")
    public void shouldRejectVeryLargeRetentionValue() {
        // Given: Request with very large retention value
        final UpdateRetentionRequest request = new UpdateRetentionRequest();
        request.setRetentionDays(999999);

        // When/Then: Validating the request should throw ValidationException
        assertThrows(
            ValidationException.class,
            () -> validator.validate(request, new ValidationResult())
        );
    }
}
