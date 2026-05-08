package io.github.eventify.api.apikey.model.validator;

import io.github.eventify.api.apikey.model.request.CreateApiKeyRequest;
import io.github.eventify.support.UnitTest;
import io.github.jframe.exception.core.ValidationException;
import io.github.jframe.validation.ValidationResult;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.github.eventify.api.apikey.model.validator.CreateApiKeyValidator.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Unit Test - Create API Key Validator")
public class CreateApiKeyValidatorTest extends UnitTest {

    private CreateApiKeyValidator validator;

    @BeforeEach
    public void setUp() {
        validator = new CreateApiKeyValidator();
    }

    @Nested
    @DisplayName("Valid Request Validation")
    public class ValidRequestTests {

        @Test
        @DisplayName("Should pass validation when name is valid and no expiration")
        public void shouldPassValidationWhenNameIsValidAndNoExpiration() {
            // Given: A valid request with name only
            final CreateApiKeyRequest request = new CreateApiKeyRequest()
                .setName("Production Server");

            // When & Then: Validation should pass
            assertDoesNotThrow(() -> validator.validateAndThrow(request));
        }

        @Test
        @DisplayName("Should pass validation when name is valid with future expiration")
        public void shouldPassValidationWhenNameIsValidWithFutureExpiration() {
            // Given: A valid request with future expiration
            final CreateApiKeyRequest request = new CreateApiKeyRequest()
                .setName("Temporary Key")
                .setExpiresAt(OffsetDateTime.now().plusDays(30));

            // When & Then: Validation should pass
            assertDoesNotThrow(() -> validator.validateAndThrow(request));
        }

        @Test
        @DisplayName("Should pass validation when name is exactly 100 characters")
        public void shouldPassValidationWhenNameIsExactly100Characters() {
            // Given: A request with name of exactly 100 characters
            final String maxLengthName = "a".repeat(100);
            final CreateApiKeyRequest request = new CreateApiKeyRequest()
                .setName(maxLengthName);

            // When & Then: Validation should pass
            assertDoesNotThrow(() -> validator.validateAndThrow(request));
        }
    }


    @Nested
    @DisplayName("Null Request Validation")
    public class NullRequestTests {

        @Test
        @DisplayName("Should throw ValidationException when request is null")
        public void shouldThrowValidationExceptionWhenRequestIsNull() {
            // Given: A null request
            final CreateApiKeyRequest request = null;

            // And: A validation result
            final ValidationResult result = new ValidationResult();

            // When & Then: Validating the null request should throw ValidationException
            final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> validator.validate(request, result)
            );

            // And: The exception should contain the correct error message
            assertThat(exception.getValidationResult().getErrors().size(), is(1));
            assertThat(
                exception.getValidationResult().getErrors().get(0).getCode(),
                is(equalTo(BODY_IS_MISSING))
            );
        }
    }


    @Nested
    @DisplayName("Name Validation")
    public class NameTests {

        @Test
        @DisplayName("Should throw ValidationException when name is null")
        public void shouldThrowValidationExceptionWhenNameIsNull() {
            // Given: A request with null name
            final CreateApiKeyRequest request = new CreateApiKeyRequest()
                .setName(null);

            // And: A validation result
            final ValidationResult result = new ValidationResult();

            // When & Then: Validating the request should throw ValidationException
            final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> validator.validate(request, result)
            );

            // And: The exception should contain error for name field
            assertThat(exception.getValidationResult().hasErrors(), is(true));
            assertThat(
                exception.getValidationResult().getErrors().stream()
                    .anyMatch(
                        error -> error.getField().equals(NAME) &&
                            error.getCode().equals(NAME_MUST_NOT_BE_EMPTY)
                    ),
                is(true)
            );
        }

        @Test
        @DisplayName("Should throw ValidationException when name is empty")
        public void shouldThrowValidationExceptionWhenNameIsEmpty() {
            // Given: A request with empty name
            final CreateApiKeyRequest request = new CreateApiKeyRequest()
                .setName("");

            // And: A validation result
            final ValidationResult result = new ValidationResult();

            // When & Then: Validating the request should throw ValidationException
            final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> validator.validate(request, result)
            );

            // And: The exception should contain error for name field
            assertThat(exception.getValidationResult().hasErrors(), is(true));
            assertThat(
                exception.getValidationResult().getErrors().stream()
                    .anyMatch(
                        error -> error.getField().equals(NAME) &&
                            error.getCode().equals(NAME_MUST_NOT_BE_EMPTY)
                    ),
                is(true)
            );
        }

        @Test
        @DisplayName("Should throw ValidationException when name is blank")
        public void shouldThrowValidationExceptionWhenNameIsBlank() {
            // Given: A request with blank name (only whitespace)
            final CreateApiKeyRequest request = new CreateApiKeyRequest()
                .setName("   ");

            // And: A validation result
            final ValidationResult result = new ValidationResult();

            // When & Then: Validating the request should throw ValidationException
            final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> validator.validate(request, result)
            );

            // And: The exception should contain error for name field
            assertThat(exception.getValidationResult().hasErrors(), is(true));
            assertThat(
                exception.getValidationResult().getErrors().stream()
                    .anyMatch(
                        error -> error.getField().equals(NAME) &&
                            error.getCode().equals(NAME_MUST_NOT_BE_EMPTY)
                    ),
                is(true)
            );
        }

        @Test
        @DisplayName("Should throw ValidationException when name exceeds 100 characters")
        public void shouldThrowValidationExceptionWhenNameExceeds100Characters() {
            // Given: A request with name exceeding 100 characters
            final String longName = "a".repeat(101);
            final CreateApiKeyRequest request = new CreateApiKeyRequest()
                .setName(longName);

            // And: A validation result
            final ValidationResult result = new ValidationResult();

            // When & Then: Validating the request should throw ValidationException
            final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> validator.validate(request, result)
            );

            // And: The exception should contain error for name field
            assertThat(exception.getValidationResult().hasErrors(), is(true));
            assertThat(
                exception.getValidationResult().getErrors().stream()
                    .anyMatch(
                        error -> error.getField().equals(NAME) &&
                            error.getCode().equals(NAME_TOO_LONG)
                    ),
                is(true)
            );
        }
    }


    @Nested
    @DisplayName("Expiration Date Validation")
    public class ExpirationDateTests {

        @Test
        @DisplayName("Should throw ValidationException when expiration is in the past")
        public void shouldThrowValidationExceptionWhenExpirationIsInPast() {
            // Given: A request with past expiration
            final CreateApiKeyRequest request = new CreateApiKeyRequest()
                .setName("Valid Name")
                .setExpiresAt(OffsetDateTime.now().minusDays(1));

            // And: A validation result
            final ValidationResult result = new ValidationResult();

            // When & Then: Validating the request should throw ValidationException
            final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> validator.validate(request, result)
            );

            // And: The exception should contain error for expiresAt field
            assertThat(exception.getValidationResult().hasErrors(), is(true));
            assertThat(
                exception.getValidationResult().getErrors().stream()
                    .anyMatch(
                        error -> error.getField().equals(EXPIRES_AT) &&
                            error.getCode().equals(EXPIRATION_MUST_BE_FUTURE)
                    ),
                is(true)
            );
        }

        @Test
        @DisplayName("Should pass validation when expiration is null")
        public void shouldPassValidationWhenExpirationIsNull() {
            // Given: A request with null expiration (never expires)
            final CreateApiKeyRequest request = new CreateApiKeyRequest()
                .setName("Valid Name")
                .setExpiresAt(null);

            // When & Then: Validation should pass
            assertDoesNotThrow(() -> validator.validateAndThrow(request));
        }

        @Test
        @DisplayName("Should pass validation when expiration is far in the future")
        public void shouldPassValidationWhenExpirationIsFarInFuture() {
            // Given: A request with expiration far in the future
            final CreateApiKeyRequest request = new CreateApiKeyRequest()
                .setName("Valid Name")
                .setExpiresAt(OffsetDateTime.now().plusYears(1));

            // When & Then: Validation should pass
            assertDoesNotThrow(() -> validator.validateAndThrow(request));
        }
    }


    @Nested
    @DisplayName("Multiple Validation Errors")
    public class MultipleErrorsTests {

        @Test
        @DisplayName("Should throw ValidationException with multiple errors when name is empty and expiration is past")
        public void shouldThrowValidationExceptionWithMultipleErrorsWhenNameIsEmptyAndExpirationIsPast() {
            // Given: A request with empty name and past expiration
            final CreateApiKeyRequest request = new CreateApiKeyRequest()
                .setName("")
                .setExpiresAt(OffsetDateTime.now().minusDays(1));

            // And: A validation result
            final ValidationResult result = new ValidationResult();

            // When & Then: Validating the request should throw ValidationException
            final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> validator.validate(request, result)
            );

            // And: The exception should contain errors for both fields
            assertThat(exception.getValidationResult().hasErrors(), is(true));
            assertThat(exception.getValidationResult().getErrors().size(), is(2));

            // And: The errors should be for the correct fields
            assertThat(
                exception.getValidationResult().getErrors().stream()
                    .anyMatch(error -> error.getField().equals(NAME)),
                is(true)
            );
            assertThat(
                exception.getValidationResult().getErrors().stream()
                    .anyMatch(error -> error.getField().equals(EXPIRES_AT)),
                is(true)
            );
        }
    }
}
