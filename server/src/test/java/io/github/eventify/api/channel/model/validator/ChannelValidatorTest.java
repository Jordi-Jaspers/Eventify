package io.github.eventify.api.channel.model.validator;

import io.github.eventify.api.channel.model.request.CreateChannelRequest;
import io.github.eventify.api.channel.model.request.UpdateChannelRequest;
import io.github.eventify.support.UnitTest;
import io.github.jframe.exception.core.ValidationException;
import io.github.jframe.validation.ValidationResult;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.github.eventify.api.channel.model.validator.ChannelValidator.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit Test - Channel Validator.
 */
@DisplayName("Unit Test - Channel Validator")
public class ChannelValidatorTest extends UnitTest {

    private ChannelValidator validator;

    @BeforeEach
    public void setUp() {
        validator = new ChannelValidator();
    }

    // ==================== Create Request Tests ====================

    @Test
    @DisplayName("Should accept valid create request")
    public void shouldAcceptValidCreateRequest() {
        // Given: Valid create request
        final CreateChannelRequest request = new CreateChannelRequest()
            .setName("Valid Channel Name")
            .setDescription("Valid description");
        final ValidationResult result = new ValidationResult();

        // When: Validating request
        validator.validate(request, result);

        // Then: Validation should pass
        assertThat(result.hasErrors(), is(false));
    }

    @Test
    @DisplayName("Should accept create request without description")
    public void shouldAcceptCreateRequestWithoutDescription() {
        // Given: Valid request without description
        final CreateChannelRequest request = new CreateChannelRequest()
            .setName("Valid Channel Name");
        final ValidationResult result = new ValidationResult();

        // When: Validating request
        validator.validate(request, result);

        // Then: Validation should pass
        assertThat(result.hasErrors(), is(false));
    }

    @Test
    @DisplayName("Should reject null create request body")
    public void shouldRejectNullCreateRequestBody() {
        // Given: Null request
        final CreateChannelRequest request = null;
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
    @DisplayName("Should reject null name in create request")
    public void shouldRejectNullNameInCreateRequest() {
        // Given: Request with null name
        final CreateChannelRequest request = new CreateChannelRequest();
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
                    error -> error.getField().equals(NAME) &&
                        error.getCode().equals(NAME_REQUIRED)
                ),
            is(true)
        );
    }

    @Test
    @DisplayName("Should reject empty name in create request")
    public void shouldRejectEmptyNameInCreateRequest() {
        // Given: Request with empty name
        final CreateChannelRequest request = new CreateChannelRequest()
            .setName("");
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
                    error -> error.getField().equals(NAME) &&
                        error.getCode().equals(NAME_REQUIRED)
                ),
            is(true)
        );
    }

    @Test
    @DisplayName("Should reject blank name in create request")
    public void shouldRejectBlankNameInCreateRequest() {
        // Given: Request with blank name
        final CreateChannelRequest request = new CreateChannelRequest()
            .setName("   ");
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
                    error -> error.getField().equals(NAME) &&
                        error.getCode().equals(NAME_REQUIRED)
                ),
            is(true)
        );
    }

    @Test
    @DisplayName("Should reject name exceeding 100 characters in create request")
    public void shouldRejectNameExceeding100CharactersInCreateRequest() {
        // Given: Request with name exceeding 100 characters
        final String longName = "a".repeat(101);
        final CreateChannelRequest request = new CreateChannelRequest()
            .setName(longName);
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
                    error -> error.getField().equals(NAME) &&
                        error.getCode().equals(NAME_TOO_LONG)
                ),
            is(true)
        );
    }

    @Test
    @DisplayName("Should accept name with exactly 100 characters in create request")
    public void shouldAcceptNameWithExactly100CharactersInCreateRequest() {
        // Given: Request with name of exactly 100 characters
        final String maxName = "a".repeat(100);
        final CreateChannelRequest request = new CreateChannelRequest()
            .setName(maxName);
        final ValidationResult result = new ValidationResult();

        // When: Validating request
        validator.validate(request, result);

        // Then: Validation should pass
        assertThat(result.hasErrors(), is(false));
    }

    @Test
    @DisplayName("Should reject description exceeding 500 characters in create request")
    public void shouldRejectDescriptionExceeding500CharactersInCreateRequest() {
        // Given: Request with description exceeding 500 characters
        final String longDescription = "a".repeat(501);
        final CreateChannelRequest request = new CreateChannelRequest()
            .setName("Valid Name")
            .setDescription(longDescription);
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
                    error -> error.getField().equals(DESCRIPTION) &&
                        error.getCode().equals(DESCRIPTION_TOO_LONG)
                ),
            is(true)
        );
    }

    @Test
    @DisplayName("Should accept description with exactly 500 characters in create request")
    public void shouldAcceptDescriptionWithExactly500CharactersInCreateRequest() {
        // Given: Request with description of exactly 500 characters
        final String maxDescription = "a".repeat(500);
        final CreateChannelRequest request = new CreateChannelRequest()
            .setName("Valid Name")
            .setDescription(maxDescription);
        final ValidationResult result = new ValidationResult();

        // When: Validating request
        validator.validate(request, result);

        // Then: Validation should pass
        assertThat(result.hasErrors(), is(false));
    }

    // ==================== Update Request Tests ====================

    @Test
    @DisplayName("Should accept valid update request")
    public void shouldAcceptValidUpdateRequest() {
        // Given: Valid update request
        final UpdateChannelRequest request = new UpdateChannelRequest()
            .setName("Updated Channel Name")
            .setDescription("Updated description");
        final ValidationResult result = new ValidationResult();

        // When: Validating request
        validator.validate(request, result);

        // Then: Validation should pass
        assertThat(result.hasErrors(), is(false));
    }

    @Test
    @DisplayName("Should accept update request without description")
    public void shouldAcceptUpdateRequestWithoutDescription() {
        // Given: Valid update request without description
        final UpdateChannelRequest request = new UpdateChannelRequest()
            .setName("Updated Channel Name");
        final ValidationResult result = new ValidationResult();

        // When: Validating request
        validator.validate(request, result);

        // Then: Validation should pass
        assertThat(result.hasErrors(), is(false));
    }

    @Test
    @DisplayName("Should reject null update request body")
    public void shouldRejectNullUpdateRequestBody() {
        // Given: Null update request
        final UpdateChannelRequest request = null;
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
    @DisplayName("Should reject null name in update request")
    public void shouldRejectNullNameInUpdateRequest() {
        // Given: Update request with null name
        final UpdateChannelRequest request = new UpdateChannelRequest();
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
                    error -> error.getField().equals(NAME) &&
                        error.getCode().equals(NAME_REQUIRED)
                ),
            is(true)
        );
    }

    @Test
    @DisplayName("Should reject empty name in update request")
    public void shouldRejectEmptyNameInUpdateRequest() {
        // Given: Update request with empty name
        final UpdateChannelRequest request = new UpdateChannelRequest()
            .setName("");
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
                    error -> error.getField().equals(NAME) &&
                        error.getCode().equals(NAME_REQUIRED)
                ),
            is(true)
        );
    }

    @Test
    @DisplayName("Should reject blank name in update request")
    public void shouldRejectBlankNameInUpdateRequest() {
        // Given: Update request with blank name
        final UpdateChannelRequest request = new UpdateChannelRequest()
            .setName("   ");
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
                    error -> error.getField().equals(NAME) &&
                        error.getCode().equals(NAME_REQUIRED)
                ),
            is(true)
        );
    }

    @Test
    @DisplayName("Should reject name exceeding 100 characters in update request")
    public void shouldRejectNameExceeding100CharactersInUpdateRequest() {
        // Given: Update request with name exceeding 100 characters
        final String longName = "a".repeat(101);
        final UpdateChannelRequest request = new UpdateChannelRequest()
            .setName(longName);
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
                    error -> error.getField().equals(NAME) &&
                        error.getCode().equals(NAME_TOO_LONG)
                ),
            is(true)
        );
    }

    @Test
    @DisplayName("Should accept name with exactly 100 characters in update request")
    public void shouldAcceptNameWithExactly100CharactersInUpdateRequest() {
        // Given: Update request with name of exactly 100 characters
        final String maxName = "a".repeat(100);
        final UpdateChannelRequest request = new UpdateChannelRequest()
            .setName(maxName);
        final ValidationResult result = new ValidationResult();

        // When: Validating request
        validator.validate(request, result);

        // Then: Validation should pass
        assertThat(result.hasErrors(), is(false));
    }

    @Test
    @DisplayName("Should reject description exceeding 500 characters in update request")
    public void shouldRejectDescriptionExceeding500CharactersInUpdateRequest() {
        // Given: Update request with description exceeding 500 characters
        final String longDescription = "a".repeat(501);
        final UpdateChannelRequest request = new UpdateChannelRequest()
            .setName("Valid Name")
            .setDescription(longDescription);
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
                    error -> error.getField().equals(DESCRIPTION) &&
                        error.getCode().equals(DESCRIPTION_TOO_LONG)
                ),
            is(true)
        );
    }

    @Test
    @DisplayName("Should accept description with exactly 500 characters in update request")
    public void shouldAcceptDescriptionWithExactly500CharactersInUpdateRequest() {
        // Given: Update request with description of exactly 500 characters
        final String maxDescription = "a".repeat(500);
        final UpdateChannelRequest request = new UpdateChannelRequest()
            .setName("Valid Name")
            .setDescription(maxDescription);
        final ValidationResult result = new ValidationResult();

        // When: Validating request
        validator.validate(request, result);

        // Then: Validation should pass
        assertThat(result.hasErrors(), is(false));
    }

    @Test
    @DisplayName("Should accept update request with null description")
    public void shouldAcceptUpdateRequestWithNullDescription() {
        // Given: Update request with null description
        final UpdateChannelRequest request = new UpdateChannelRequest()
            .setName("Valid Name")
            .setDescription(null);
        final ValidationResult result = new ValidationResult();

        // When: Validating request
        validator.validate(request, result);

        // Then: Validation should pass
        assertThat(result.hasErrors(), is(false));
    }

    @Test
    @DisplayName("Should accept update request with empty description")
    public void shouldAcceptUpdateRequestWithEmptyDescription() {
        // Given: Update request with empty description
        final UpdateChannelRequest request = new UpdateChannelRequest()
            .setName("Valid Name")
            .setDescription("");
        final ValidationResult result = new ValidationResult();

        // When: Validating request (empty description is valid)
        validator.validate(request, result);

        // Then: Validation should pass
        assertThat(result.hasErrors(), is(false));
    }
}
