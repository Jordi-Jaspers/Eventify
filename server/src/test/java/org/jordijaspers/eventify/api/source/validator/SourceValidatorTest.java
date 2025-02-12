package org.jordijaspers.eventify.api.source.validator;

import java.util.List;

import org.hawaiiframework.validation.ValidationError;
import org.hawaiiframework.validation.ValidationResult;
import org.jordijaspers.eventify.api.source.model.request.SourceRequest;
import org.jordijaspers.eventify.api.source.model.validator.SourceValidator;
import org.jordijaspers.eventify.support.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jordijaspers.eventify.api.source.model.validator.SourceValidator.*;

@DisplayName("Source Validator Unit Tests")
public final class SourceValidatorTest extends UnitTest {

    private final SourceValidator validator = new SourceValidator();

    private final ValidationResult validationResult = new ValidationResult();

    @Nested
    @DisplayName("Name Validation")
    public final class NameValidation {

        @Test
        public void shouldRejectWhenNameIsNull() {
            final SourceRequest request = aSourceRequestWithName(null);
            validator.validate(request, validationResult);

            final List<ValidationError> errors = validationResult.getErrors();
            errors.stream()
                .filter(error -> error.getField().equals(NAME))
                .findFirst()
                .ifPresent(error -> {
                    assertThat(error.getCode()).isEqualTo(NAME_MUST_NOT_BE_EMPTY);
                });
        }

        @Test
        public void shouldRejectWhenNameIsEmpty() {
            final SourceRequest request = aSourceRequestWithName("");
            validator.validate(request, validationResult);

            final List<ValidationError> errors = validationResult.getErrors();
            errors.stream()
                .filter(error -> error.getField().equals(NAME))
                .findFirst()
                .ifPresent(error -> {
                    assertThat(error.getCode()).isEqualTo(NAME_MUST_NOT_BE_EMPTY);
                });
        }

        @Test
        public void shouldRejectWhenNameIsBlank() {
            final SourceRequest request = aSourceRequestWithName("   ");
            validator.validate(request, validationResult);

            final List<ValidationError> errors = validationResult.getErrors();
            errors.stream()
                .filter(error -> error.getField().equals(NAME))
                .findFirst()
                .ifPresent(error -> {
                    assertThat(error.getCode()).isEqualTo(NAME_MUST_NOT_BE_EMPTY);
                });
        }

        @Test
        public void shouldRejectWhenNameExceedsMaxLength() {
            final SourceRequest request = aSourceRequestWithName("a".repeat(MAX_NAME_LENGTH + 1));
            validator.validate(request, validationResult);

            final List<ValidationError> errors = validationResult.getErrors();
            errors.stream()
                .filter(error -> error.getField().equals(NAME))
                .findFirst()
                .ifPresent(error -> {
                    assertThat(error.getCode()).isEqualTo(MAX_CHARACTERS_NAME);
                });
        }
    }


    @Nested
    @DisplayName("Description Validation")
    public final class DescriptionValidation {

        @Test
        public void shouldRejectWhenDescriptionIsNull() {
            final SourceRequest request = aSourceRequestWithDescription(null);
            validator.validate(request, validationResult);

            final List<ValidationError> errors = validationResult.getErrors();
            errors.stream()
                .filter(error -> error.getField().equals(DESCRIPTION))
                .findFirst()
                .ifPresent(error -> {
                    assertThat(error.getCode()).isEqualTo(DESCRIPTION_MUST_NOT_BE_EMPTY);
                });
        }

        @Test
        public void shouldRejectWhenDescriptionIsEmpty() {
            final SourceRequest request = aSourceRequestWithDescription("");
            validator.validate(request, validationResult);

            final List<ValidationError> errors = validationResult.getErrors();
            errors.stream()
                .filter(error -> error.getField().equals(DESCRIPTION))
                .findFirst()
                .ifPresent(error -> {
                    assertThat(error.getCode()).isEqualTo(DESCRIPTION_MUST_NOT_BE_EMPTY);
                });
        }

        @Test
        public void shouldRejectWhenDescriptionIsBlank() {
            final SourceRequest request = aSourceRequestWithDescription("   ");
            validator.validate(request, validationResult);

            final List<ValidationError> errors = validationResult.getErrors();
            errors.stream()
                .filter(error -> error.getField().equals(DESCRIPTION))
                .findFirst()
                .ifPresent(error -> {
                    assertThat(error.getCode()).isEqualTo(DESCRIPTION_MUST_NOT_BE_EMPTY);
                });
        }

        @Test
        public void shouldRejectWhenDescriptionExceedsMaxLength() {
            final SourceRequest request = aSourceRequestWithDescription("a".repeat(MAX_DESCRIPTION_LENGTH + 1));
            validator.validate(request, validationResult);

            final List<ValidationError> errors = validationResult.getErrors();
            errors.stream()
                .filter(error -> error.getField().equals(DESCRIPTION))
                .findFirst()
                .ifPresent(error -> {
                    assertThat(error.getCode()).isEqualTo(MAX_CHARACTERS_DESCRIPTION);
                });
        }
    }

    @Test
    public void shouldValidateSuccessfullyWhenAllFieldsAreValid() {
        final SourceRequest request = aValidSourceRequest();
        validator.validate(request, validationResult);

        assertThat(validationResult.hasErrors()).isFalse();
    }

    private static SourceRequest aValidSourceRequest() {
        return new SourceRequest()
            .setName("Valid Source Name")
            .setDescription("Valid Source Description");
    }

    private static SourceRequest aSourceRequestWithName(final String name) {
        return new SourceRequest()
            .setName(name)
            .setDescription("Valid Source Description");
    }

    private static SourceRequest aSourceRequestWithDescription(final String description) {
        return new SourceRequest()
            .setName("Valid Source Name")
            .setDescription(description);
    }
}
