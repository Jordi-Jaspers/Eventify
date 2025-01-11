package org.jordijaspers.smc.eventify.api.dashboard.validator;

import java.util.List;

import org.hawaiiframework.validation.ValidationError;
import org.hawaiiframework.validation.ValidationException;
import org.hawaiiframework.validation.ValidationResult;
import org.jordijaspers.eventify.api.dashboard.model.request.CreateDashboardRequest;
import org.jordijaspers.eventify.api.dashboard.model.request.UpdateDashboardDetailsRequest;
import org.jordijaspers.eventify.api.dashboard.model.validator.DashboardValidator;
import org.jordijaspers.smc.eventify.support.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.jordijaspers.eventify.api.dashboard.model.validator.DashboardValidator.*;

@DisplayName("DashboardValidator")
public class DashboardValidatorTest extends UnitTest {

    private final DashboardValidator validator = new DashboardValidator();

    @Test
    public void shouldThrowValidationExceptionWhenObjectIsNull() {
        assertThatThrownBy(() -> validator.validate(null, new ValidationResult()))
            .isInstanceOf(ValidationException.class)
            .satisfies(e -> {
                final ValidationResult result = ((ValidationException) e).getValidationResult();
                final List<ValidationError> errors = result.getErrors();
                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getCode()).isEqualTo(BODY_IS_MISSING);
            });
    }

    @Nested
    @DisplayName("Create Dashboard Validation")
    public final class CreateDashboardValidation {

        @Test
        public void shouldThrowValidationExceptionWhenRequestIsNull() {
            assertThatThrownBy(() -> validator.validateCreateDashboardRequest(null))
                .isInstanceOf(ValidationException.class)
                .satisfies(e -> {
                    final ValidationResult result = ((ValidationException) e).getValidationResult();
                    final List<ValidationError> errors = result.getErrors();
                    assertThat(errors).hasSize(1);
                    assertThat(errors.get(0).getCode()).isEqualTo(BODY_IS_MISSING);
                });
        }

        @Test
        public void shouldThrowValidationExceptionWhenNameIsNull() {
            final CreateDashboardRequest request = aCreateDashboardRequestWithName(null);

            assertThatThrownBy(() -> validator.validateCreateDashboardRequest(request))
                .isInstanceOf(ValidationException.class)
                .satisfies(e -> {
                    final ValidationResult result = ((ValidationException) e).getValidationResult();
                    final List<ValidationError> errors = result.getErrors();
                    errors.stream()
                        .filter(error -> error.getField().equals(NAME))
                        .findFirst()
                        .ifPresent(error -> {
                            assertThat(error.getCode()).isEqualTo(NAME_MUST_NOT_BE_EMPTY);
                        });
                });
        }

        @Test
        public void shouldThrowValidationExceptionWhenNameIsEmpty() {
            final CreateDashboardRequest request = aCreateDashboardRequestWithName("");

            assertThatThrownBy(() -> validator.validateCreateDashboardRequest(request))
                .isInstanceOf(ValidationException.class)
                .satisfies(e -> {
                    final ValidationResult result = ((ValidationException) e).getValidationResult();
                    final List<ValidationError> errors = result.getErrors();
                    errors.stream()
                        .filter(error -> error.getField().equals(NAME))
                        .findFirst()
                        .ifPresent(error -> {
                            assertThat(error.getCode()).isEqualTo(NAME_MUST_NOT_BE_EMPTY);
                        });
                });
        }

        @Test
        public void shouldThrowValidationExceptionWhenNameIsTooLong() {
            final CreateDashboardRequest request = aCreateDashboardRequestWithName("a".repeat(MAX_NAME_LENGTH + 1));

            assertThatThrownBy(() -> validator.validateCreateDashboardRequest(request))
                .isInstanceOf(ValidationException.class)
                .satisfies(e -> {
                    final ValidationResult result = ((ValidationException) e).getValidationResult();
                    final List<ValidationError> errors = result.getErrors();
                    errors.stream()
                        .filter(error -> error.getField().equals(NAME))
                        .findFirst()
                        .ifPresent(error -> {
                            assertThat(error.getCode()).isEqualTo(NAME_TOO_LONG);
                        });
                });
        }

        @Test
        public void shouldThrowValidationExceptionWhenDescriptionIsNull() {
            final CreateDashboardRequest request = aCreateDashboardRequestWithDescription(null);

            assertThatThrownBy(() -> validator.validateCreateDashboardRequest(request))
                .isInstanceOf(ValidationException.class)
                .satisfies(e -> {
                    final ValidationResult result = ((ValidationException) e).getValidationResult();
                    final List<ValidationError> errors = result.getErrors();
                    errors.stream()
                        .filter(error -> error.getField().equals(DESCRIPTION))
                        .findFirst()
                        .ifPresent(error -> {
                            assertThat(error.getCode()).isEqualTo(DESCRIPTION_MUST_NOT_BE_EMPTY);
                        });
                });
        }

        @Test
        public void shouldThrowValidationExceptionWhenDescriptionIsEmpty() {
            final CreateDashboardRequest request = aCreateDashboardRequestWithDescription("");

            assertThatThrownBy(() -> validator.validateCreateDashboardRequest(request))
                .isInstanceOf(ValidationException.class)
                .satisfies(e -> {
                    final ValidationResult result = ((ValidationException) e).getValidationResult();
                    final List<ValidationError> errors = result.getErrors();
                    errors.stream()
                        .filter(error -> error.getField().equals(DESCRIPTION))
                        .findFirst()
                        .ifPresent(error -> {
                            assertThat(error.getCode()).isEqualTo(DESCRIPTION_MUST_NOT_BE_EMPTY);
                        });
                });
        }

        @Test
        public void shouldThrowValidationExceptionWhenDescriptionIsTooLong() {
            final CreateDashboardRequest request = aCreateDashboardRequestWithDescription("a".repeat(MAX_DESCRIPTION_LENGTH + 1));

            assertThatThrownBy(() -> validator.validateCreateDashboardRequest(request))
                .isInstanceOf(ValidationException.class)
                .satisfies(e -> {
                    final ValidationResult result = ((ValidationException) e).getValidationResult();
                    final List<ValidationError> errors = result.getErrors();
                    errors.stream()
                        .filter(error -> error.getField().equals(DESCRIPTION))
                        .findFirst()
                        .ifPresent(error -> {
                            assertThat(error.getCode()).isEqualTo(DESCRIPTION_TOO_LONG);
                        });
                });
        }
    }


    @Nested
    @DisplayName("Update Dashboard Details Validation")
    public final class UpdateDashboardDetailsValidation {

        @Test
        public void shouldThrowValidationExceptionWhenRequestIsNull() {
            assertThatThrownBy(() -> validator.validateUpdateDashboardDetailsRequest(null))
                .isInstanceOf(ValidationException.class)
                .satisfies(e -> {
                    final ValidationResult result = ((ValidationException) e).getValidationResult();
                    final List<ValidationError> errors = result.getErrors();
                    assertThat(errors).hasSize(1);
                    assertThat(errors.get(0).getCode()).isEqualTo(BODY_IS_MISSING);
                });
        }

        @Test
        public void shouldThrowValidationExceptionWhenNameIsNull() {
            final UpdateDashboardDetailsRequest request = anUpdateDashboardDetailsRequestWithName(null);

            assertThatThrownBy(() -> validator.validateUpdateDashboardDetailsRequest(request))
                .isInstanceOf(ValidationException.class)
                .satisfies(e -> {
                    final ValidationResult result = ((ValidationException) e).getValidationResult();
                    final List<ValidationError> errors = result.getErrors();
                    errors.stream()
                        .filter(error -> error.getField().equals(NAME))
                        .findFirst()
                        .ifPresent(error -> {
                            assertThat(error.getCode()).isEqualTo(NAME_MUST_NOT_BE_EMPTY);
                        });
                });
        }

        @Test
        public void shouldThrowValidationExceptionWhenNameIsEmpty() {
            final UpdateDashboardDetailsRequest request = anUpdateDashboardDetailsRequestWithName("");

            assertThatThrownBy(() -> validator.validateUpdateDashboardDetailsRequest(request))
                .isInstanceOf(ValidationException.class)
                .satisfies(e -> {
                    final ValidationResult result = ((ValidationException) e).getValidationResult();
                    final List<ValidationError> errors = result.getErrors();
                    errors.stream()
                        .filter(error -> error.getField().equals(NAME))
                        .findFirst()
                        .ifPresent(error -> {
                            assertThat(error.getCode()).isEqualTo(NAME_MUST_NOT_BE_EMPTY);
                        });
                });
        }

        @Test
        public void shouldThrowValidationExceptionWhenNameIsTooLong() {
            final UpdateDashboardDetailsRequest request = anUpdateDashboardDetailsRequestWithName("a".repeat(MAX_NAME_LENGTH + 1));

            assertThatThrownBy(() -> validator.validateUpdateDashboardDetailsRequest(request))
                .isInstanceOf(ValidationException.class)
                .satisfies(e -> {
                    final ValidationResult result = ((ValidationException) e).getValidationResult();
                    final List<ValidationError> errors = result.getErrors();
                    errors.stream()
                        .filter(error -> error.getField().equals(NAME))
                        .findFirst()
                        .ifPresent(error -> {
                            assertThat(error.getCode()).isEqualTo(NAME_TOO_LONG);
                        });
                });
        }

        @Test
        public void shouldThrowValidationExceptionWhenDescriptionIsNull() {
            final UpdateDashboardDetailsRequest request = anUpdateDashboardDetailsRequestWithDescription(null);

            assertThatThrownBy(() -> validator.validateUpdateDashboardDetailsRequest(request))
                .isInstanceOf(ValidationException.class)
                .satisfies(e -> {
                    final ValidationResult result = ((ValidationException) e).getValidationResult();
                    final List<ValidationError> errors = result.getErrors();
                    errors.stream()
                        .filter(error -> error.getField().equals(DESCRIPTION))
                        .findFirst()
                        .ifPresent(error -> {
                            assertThat(error.getCode()).isEqualTo(DESCRIPTION_MUST_NOT_BE_EMPTY);
                        });
                });
        }

        @Test
        public void shouldThrowValidationExceptionWhenDescriptionIsEmpty() {
            final UpdateDashboardDetailsRequest request = anUpdateDashboardDetailsRequestWithDescription("");

            assertThatThrownBy(() -> validator.validateUpdateDashboardDetailsRequest(request))
                .isInstanceOf(ValidationException.class)
                .satisfies(e -> {
                    final ValidationResult result = ((ValidationException) e).getValidationResult();
                    final List<ValidationError> errors = result.getErrors();
                    errors.stream()
                        .filter(error -> error.getField().equals(DESCRIPTION))
                        .findFirst()
                        .ifPresent(error -> {
                            assertThat(error.getCode()).isEqualTo(DESCRIPTION_MUST_NOT_BE_EMPTY);
                        });
                });
        }

        @Test
        public void shouldThrowValidationExceptionWhenDescriptionIsTooLong() {
            final UpdateDashboardDetailsRequest request = anUpdateDashboardDetailsRequestWithDescription(
                "a".repeat(
                    MAX_DESCRIPTION_LENGTH + 1
                )
            );

            assertThatThrownBy(() -> validator.validateUpdateDashboardDetailsRequest(request))
                .isInstanceOf(ValidationException.class)
                .satisfies(e -> {
                    final ValidationResult result = ((ValidationException) e).getValidationResult();
                    final List<ValidationError> errors = result.getErrors();
                    errors.stream()
                        .filter(error -> error.getField().equals(DESCRIPTION))
                        .findFirst()
                        .ifPresent(error -> {
                            assertThat(error.getCode()).isEqualTo(DESCRIPTION_TOO_LONG);
                        });
                });
        }

        @Test
        public void shouldValidateSuccessfullyWhenAllFieldsAreValid() {
            final UpdateDashboardDetailsRequest request = aValidUpdateDashboardDetailsRequest();

            assertThatNoException()
                .isThrownBy(() -> validator.validateUpdateDashboardDetailsRequest(request));

        }
    }

    private static CreateDashboardRequest aCreateDashboardRequestWithName(final String name) {
        return new CreateDashboardRequest()
            .setName(name)
            .setDescription("Valid Dashboard Description");
    }

    private static CreateDashboardRequest aCreateDashboardRequestWithDescription(final String description) {
        return new CreateDashboardRequest()
            .setName("Valid Dashboard Name")
            .setDescription(description);
    }

    private static UpdateDashboardDetailsRequest aValidUpdateDashboardDetailsRequest() {
        return new UpdateDashboardDetailsRequest()
            .setName("Valid Dashboard Name")
            .setDescription("Valid Dashboard Description");
    }

    private static UpdateDashboardDetailsRequest anUpdateDashboardDetailsRequestWithName(final String name) {
        return new UpdateDashboardDetailsRequest()
            .setName(name)
            .setDescription("Valid Dashboard Description");
    }

    private static UpdateDashboardDetailsRequest anUpdateDashboardDetailsRequestWithDescription(final String description) {
        return new UpdateDashboardDetailsRequest()
            .setName("Valid Dashboard Name")
            .setDescription(description);
    }
}
