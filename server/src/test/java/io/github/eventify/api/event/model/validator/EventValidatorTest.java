package io.github.eventify.api.event.model.validator;

import io.github.eventify.api.event.model.Severity;
import io.github.eventify.api.event.model.request.CreateEventRequest;
import io.github.eventify.support.UnitTest;
import io.github.jframe.exception.core.ValidationException;
import io.github.jframe.validation.ValidationResult;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.github.eventify.api.event.model.validator.EventValidator.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit Test - Event Validator.
 */
@DisplayName("Unit Test - Event Validator")
public class EventValidatorTest extends UnitTest {

    private EventValidator validator;

    @BeforeEach
    public void setUp() {
        validator = new EventValidator();
    }

    @Test
    @DisplayName("Should accept valid request with all fields")
    public void shouldAcceptValidRequestWithAllFields() {
        // Given: Valid request with all fields
        final Map<String, Object> metadata = new HashMap<>();
        metadata.put("key", "value");

        final CreateEventRequest request = new CreateEventRequest()
            .setSlug("test-slug")
            .setSeverity(Severity.CRITICAL)
            .setTitle("Production Server Down")
            .setMessage("Server experienced critical failure")
            .setMetadata(metadata);

        final ValidationResult result = new ValidationResult();

        // When: Validating request
        validator.validate(request, result);

        // Then: Validation should pass
        assertThat(result.hasErrors(), is(false));
    }

    @Test
    @DisplayName("Should accept valid request with minimal fields")
    public void shouldAcceptValidRequestWithMinimalFields() {
        // Given: Valid request with only required fields
        final CreateEventRequest request = new CreateEventRequest()
            .setSlug("test-slug")
            .setSeverity(Severity.OK)
            .setTitle("System healthy");

        final ValidationResult result = new ValidationResult();

        // When: Validating request
        validator.validate(request, result);

        // Then: Validation should pass
        assertThat(result.hasErrors(), is(false));
    }

    @Test
    @DisplayName("Should reject null request body")
    public void shouldRejectNullRequestBody() {
        // Given: Null request
        final CreateEventRequest request = null;
        final ValidationResult result = new ValidationResult();

        // When & Then: Should throw ValidationException
        final ValidationException exception = assertThrows(
            ValidationException.class,
            () -> validator.validate(request, result)
        );

        assertThat(exception.getValidationResult().getErrors().size(), is(1));
        assertThat(
            exception.getValidationResult().getErrors().get(0).getCode(),
            is(equalTo(BODY_IS_MISSING))
        );
    }

    @Test
    @DisplayName("Should reject missing slug")
    public void shouldRejectMissingSlug() {
        // Given: Request without slug
        final CreateEventRequest request = new CreateEventRequest()
            .setSeverity(Severity.CRITICAL)
            .setTitle("Test Event");

        final ValidationResult result = new ValidationResult();

        // When & Then: Should throw ValidationException
        final ValidationException exception = assertThrows(
            ValidationException.class,
            () -> validator.validate(request, result)
        );

        assertThat(exception.getValidationResult().hasErrors(), is(true));
        assertThat(
            exception.getValidationResult().getErrors().stream()
                .anyMatch(
                    error -> error.getField().equals(SLUG) &&
                        error.getCode().equals(SLUG_REQUIRED)
                ),
            is(true)
        );
    }

    @Test
    @DisplayName("Should reject null severity")
    public void shouldRejectNullSeverity() {
        // Given: Request with null severity
        final CreateEventRequest request = new CreateEventRequest()
            .setSlug("test-slug")
            .setTitle("Test Event");

        final ValidationResult result = new ValidationResult();

        // When & Then: Should throw ValidationException
        final ValidationException exception = assertThrows(
            ValidationException.class,
            () -> validator.validate(request, result)
        );

        assertThat(exception.getValidationResult().hasErrors(), is(true));
        assertThat(
            exception.getValidationResult().getErrors().stream()
                .anyMatch(
                    error -> error.getField().equals(SEVERITY) &&
                        error.getCode().equals(SEVERITY_REQUIRED)
                ),
            is(true)
        );
    }

    @Test
    @DisplayName("Should accept OK severity")
    public void shouldAcceptOkSeverity() {
        // Given: Request with OK severity
        final CreateEventRequest request = new CreateEventRequest()
            .setSlug("test-slug")
            .setSeverity(Severity.OK)
            .setTitle("Test Event");

        final ValidationResult result = new ValidationResult();

        // When: Validating request
        validator.validate(request, result);

        // Then: Validation should pass
        assertThat(result.hasErrors(), is(false));
    }

    @Test
    @DisplayName("Should accept WARNING severity")
    public void shouldAcceptWarningSeverity() {
        // Given: Request with WARNING severity
        final CreateEventRequest request = new CreateEventRequest()
            .setSlug("test-slug")
            .setSeverity(Severity.WARNING)
            .setTitle("Test Event");

        final ValidationResult result = new ValidationResult();

        // When: Validating request
        validator.validate(request, result);

        // Then: Validation should pass
        assertThat(result.hasErrors(), is(false));
    }

    @Test
    @DisplayName("Should accept CRITICAL severity")
    public void shouldAcceptCriticalSeverity() {
        // Given: Request with CRITICAL severity
        final CreateEventRequest request = new CreateEventRequest()
            .setSlug("test-slug")
            .setSeverity(Severity.CRITICAL)
            .setTitle("Test Event");

        final ValidationResult result = new ValidationResult();

        // When: Validating request
        validator.validate(request, result);

        // Then: Validation should pass
        assertThat(result.hasErrors(), is(false));
    }

    @Test
    @DisplayName("Should reject null title")
    public void shouldRejectNullTitle() {
        // Given: Request with null title
        final CreateEventRequest request = new CreateEventRequest()
            .setSlug("test-slug")
            .setSeverity(Severity.CRITICAL);

        final ValidationResult result = new ValidationResult();

        // When & Then: Should throw ValidationException
        final ValidationException exception = assertThrows(
            ValidationException.class,
            () -> validator.validate(request, result)
        );

        assertThat(exception.getValidationResult().hasErrors(), is(true));
        assertThat(
            exception.getValidationResult().getErrors().stream()
                .anyMatch(
                    error -> error.getField().equals(TITLE) &&
                        error.getCode().equals(TITLE_REQUIRED)
                ),
            is(true)
        );
    }

    @Test
    @DisplayName("Should reject empty title")
    public void shouldRejectEmptyTitle() {
        // Given: Request with empty title
        final CreateEventRequest request = new CreateEventRequest()
            .setSlug("test-slug")
            .setSeverity(Severity.CRITICAL)
            .setTitle("");

        final ValidationResult result = new ValidationResult();

        // When & Then: Should throw ValidationException
        final ValidationException exception = assertThrows(
            ValidationException.class,
            () -> validator.validate(request, result)
        );

        assertThat(exception.getValidationResult().hasErrors(), is(true));
        assertThat(
            exception.getValidationResult().getErrors().stream()
                .anyMatch(
                    error -> error.getField().equals(TITLE) &&
                        error.getCode().equals(TITLE_REQUIRED)
                ),
            is(true)
        );
    }

    @Test
    @DisplayName("Should reject blank title")
    public void shouldRejectBlankTitle() {
        // Given: Request with blank title
        final CreateEventRequest request = new CreateEventRequest()
            .setSlug("test-slug")
            .setSeverity(Severity.CRITICAL)
            .setTitle("   ");

        final ValidationResult result = new ValidationResult();

        // When & Then: Should throw ValidationException
        final ValidationException exception = assertThrows(
            ValidationException.class,
            () -> validator.validate(request, result)
        );

        assertThat(exception.getValidationResult().hasErrors(), is(true));
        assertThat(
            exception.getValidationResult().getErrors().stream()
                .anyMatch(
                    error -> error.getField().equals(TITLE) &&
                        error.getCode().equals(TITLE_REQUIRED)
                ),
            is(true)
        );
    }

    @Test
    @DisplayName("Should accept title with exactly 255 characters")
    public void shouldAcceptTitleWithExactly255Characters() {
        // Given: Request with title of exactly 255 characters
        final String maxTitle = "a".repeat(255);
        final CreateEventRequest request = new CreateEventRequest()
            .setSlug("test-slug")
            .setSeverity(Severity.CRITICAL)
            .setTitle(maxTitle);

        final ValidationResult result = new ValidationResult();

        // When: Validating request
        validator.validate(request, result);

        // Then: Validation should pass
        assertThat(result.hasErrors(), is(false));
    }

    @Test
    @DisplayName("Should reject title exceeding 255 characters")
    public void shouldRejectTitleExceeding255Characters() {
        // Given: Request with title exceeding 255 characters
        final String longTitle = "a".repeat(256);
        final CreateEventRequest request = new CreateEventRequest()
            .setSlug("test-slug")
            .setSeverity(Severity.CRITICAL)
            .setTitle(longTitle);

        final ValidationResult result = new ValidationResult();

        // When & Then: Should throw ValidationException
        final ValidationException exception = assertThrows(
            ValidationException.class,
            () -> validator.validate(request, result)
        );

        assertThat(exception.getValidationResult().hasErrors(), is(true));
        assertThat(
            exception.getValidationResult().getErrors().stream()
                .anyMatch(
                    error -> error.getField().equals(TITLE) &&
                        error.getCode().equals(TITLE_TOO_LONG)
                ),
            is(true)
        );
    }

    @Test
    @DisplayName("Should accept null message")
    public void shouldAcceptNullMessage() {
        // Given: Request with null message
        final CreateEventRequest request = new CreateEventRequest()
            .setSlug("test-slug")
            .setSeverity(Severity.CRITICAL)
            .setTitle("Test Event")
            .setMessage(null);

        final ValidationResult result = new ValidationResult();

        // When: Validating request
        validator.validate(request, result);

        // Then: Validation should pass
        assertThat(result.hasErrors(), is(false));
    }

    @Test
    @DisplayName("Should accept message with exactly 10240 bytes")
    public void shouldAcceptMessageWithExactly10240Bytes() {
        // Given: Request with message of exactly 10240 bytes (10KB)
        final String maxMessage = "a".repeat(10240);
        final CreateEventRequest request = new CreateEventRequest()
            .setSlug("test-slug")
            .setSeverity(Severity.CRITICAL)
            .setTitle("Test Event")
            .setMessage(maxMessage);

        final ValidationResult result = new ValidationResult();

        // When: Validating request
        validator.validate(request, result);

        // Then: Validation should pass
        assertThat(result.hasErrors(), is(false));
    }

    @Test
    @DisplayName("Should reject message exceeding 10240 bytes")
    public void shouldRejectMessageExceeding10240Bytes() {
        // Given: Request with message exceeding 10240 bytes (10KB)
        final String longMessage = "a".repeat(10241);
        final CreateEventRequest request = new CreateEventRequest()
            .setSlug("test-slug")
            .setSeverity(Severity.CRITICAL)
            .setTitle("Test Event")
            .setMessage(longMessage);

        final ValidationResult result = new ValidationResult();

        // When & Then: Should throw ValidationException
        final ValidationException exception = assertThrows(
            ValidationException.class,
            () -> validator.validate(request, result)
        );

        assertThat(exception.getValidationResult().hasErrors(), is(true));
        assertThat(
            exception.getValidationResult().getErrors().stream()
                .anyMatch(
                    error -> error.getField().equals(MESSAGE) &&
                        error.getCode().equals(MESSAGE_TOO_LARGE)
                ),
            is(true)
        );
    }

    @Test
    @DisplayName("Should accept empty metadata object")
    public void shouldAcceptEmptyMetadataObject() {
        // Given: Request with empty metadata
        final Map<String, Object> emptyMetadata = new HashMap<>();
        final CreateEventRequest request = new CreateEventRequest()
            .setSlug("test-slug")
            .setSeverity(Severity.CRITICAL)
            .setTitle("Test Event")
            .setMetadata(emptyMetadata);

        final ValidationResult result = new ValidationResult();

        // When: Validating request
        validator.validate(request, result);

        // Then: Validation should pass
        assertThat(result.hasErrors(), is(false));
    }

    @Test
    @DisplayName("Should accept null metadata")
    public void shouldAcceptNullMetadata() {
        // Given: Request with null metadata
        final CreateEventRequest request = new CreateEventRequest()
            .setSlug("test-slug")
            .setSeverity(Severity.CRITICAL)
            .setTitle("Test Event")
            .setMetadata(null);

        final ValidationResult result = new ValidationResult();

        // When: Validating request
        validator.validate(request, result);

        // Then: Validation should pass
        assertThat(result.hasErrors(), is(false));
    }

    @Test
    @DisplayName("Should accept metadata with exactly 10240 bytes JSON")
    public void shouldAcceptMetadataWithExactly10240BytesJson() {
        // Given: Request with metadata JSON of exactly 10240 bytes (10KB)
        final Map<String, Object> metadata = new HashMap<>();
        // Create a string that will result in ~10KB JSON when serialized
        metadata.put("data", "a".repeat(10200));

        final CreateEventRequest request = new CreateEventRequest()
            .setSlug("test-slug")
            .setSeverity(Severity.CRITICAL)
            .setTitle("Test Event")
            .setMetadata(metadata);

        final ValidationResult result = new ValidationResult();

        // When: Validating request
        validator.validate(request, result);

        // Then: Validation should pass
        assertThat(result.hasErrors(), is(false));
    }

    @Test
    @DisplayName("Should reject metadata exceeding 10240 bytes JSON")
    public void shouldRejectMetadataExceeding10240BytesJson() {
        // Given: Request with metadata JSON exceeding 10240 bytes (10KB)
        final Map<String, Object> metadata = new HashMap<>();
        // Create a string that will result in >10KB JSON when serialized
        metadata.put("data", "a".repeat(10500));

        final CreateEventRequest request = new CreateEventRequest()
            .setSlug("test-slug")
            .setSeverity(Severity.CRITICAL)
            .setTitle("Test Event")
            .setMetadata(metadata);

        final ValidationResult result = new ValidationResult();

        // When & Then: Should throw ValidationException
        final ValidationException exception = assertThrows(
            ValidationException.class,
            () -> validator.validate(request, result)
        );

        assertThat(exception.getValidationResult().hasErrors(), is(true));
        assertThat(
            exception.getValidationResult().getErrors().stream()
                .anyMatch(
                    error -> error.getField().equals(METADATA) &&
                        error.getCode().equals(METADATA_TOO_LARGE)
                ),
            is(true)
        );
    }

    @Test
    @DisplayName("Should reject multiple validation errors")
    public void shouldRejectMultipleValidationErrors() {
        // Given: Request with multiple validation errors (missing slug, severity, title)
        final CreateEventRequest request = new CreateEventRequest();

        final ValidationResult result = new ValidationResult();

        // When & Then: Should throw ValidationException
        final ValidationException exception = assertThrows(
            ValidationException.class,
            () -> validator.validate(request, result)
        );

        // Then: Should have multiple errors (slug, severity, title)
        assertThat(exception.getValidationResult().hasErrors(), is(true));
        assertThat(exception.getValidationResult().getErrors().size(), is(greaterThanOrEqualTo(3)));
    }
}
