package org.jordijaspers.eventify.api.team.validator;

import java.util.List;

import org.hawaiiframework.validation.ValidationError;
import org.hawaiiframework.validation.ValidationResult;
import org.jordijaspers.eventify.api.team.model.request.TeamRequest;
import org.jordijaspers.eventify.api.team.model.validator.TeamValidator;
import org.jordijaspers.eventify.support.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jordijaspers.eventify.api.team.model.validator.TeamValidator.*;

@DisplayName("TeamValidator")
public class TeamValidatorTest extends UnitTest {

    private final TeamValidator validator = new TeamValidator();
    private final ValidationResult validationResult = new ValidationResult();

    @Nested
    @DisplayName("Name Validation")
    public final class NameValidation {

        @Test
        public void shouldRejectWhenNameIsNull() {
            final TeamRequest request = aTeamRequestWithName(null);
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
            final TeamRequest request = aTeamRequestWithName("");
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
            final TeamRequest request = aTeamRequestWithName("   ");
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
            final TeamRequest request = aTeamRequestWithName("a".repeat(MAX_NAME_LENGTH + 1));
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
            final TeamRequest request = aTeamRequestWithDescription(null);
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
            final TeamRequest request = aTeamRequestWithDescription("");
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
            final TeamRequest request = aTeamRequestWithDescription("   ");
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
            final TeamRequest request = aTeamRequestWithDescription("a".repeat(MAX_DESCRIPTION_LENGTH + 1));
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
        final TeamRequest request = aValidTeamRequest();
        validator.validate(request, validationResult);

        assertThat(validationResult.hasErrors()).isFalse();
    }

    private static TeamRequest aValidTeamRequest() {
        return new TeamRequest()
            .setName("Valid Team Name")
            .setDescription("Valid Team Description");
    }

    private static TeamRequest aTeamRequestWithName(final String name) {
        return new TeamRequest()
            .setName(name)
            .setDescription("Valid Team Description");
    }

    private static TeamRequest aTeamRequestWithDescription(final String description) {
        return new TeamRequest()
            .setName("Valid Team Name")
            .setDescription(description);
    }
}
