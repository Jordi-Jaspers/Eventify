package org.jordijaspers.eventify.api.user.validator;

import java.util.List;

import org.hawaiiframework.validation.ValidationError;
import org.hawaiiframework.validation.ValidationException;
import org.hawaiiframework.validation.ValidationResult;
import org.jordijaspers.eventify.api.authentication.model.validator.CustomPasswordValidator;
import org.jordijaspers.eventify.api.user.model.request.PasswordRequest;
import org.jordijaspers.eventify.api.user.model.validator.ChangePasswordValidator;
import org.jordijaspers.eventify.support.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.passay.RuleResult;

import static org.assertj.core.api.Assertions.*;
import static org.jordijaspers.eventify.api.user.model.validator.ChangePasswordValidator.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("ChangePasswordValidator")
public class ChangePasswordValidatorTest extends UnitTest {

    private final CustomPasswordValidator passwordValidator = mock(CustomPasswordValidator.class);
    private final ChangePasswordValidator validator = new ChangePasswordValidator(passwordValidator);
    private final ValidationResult validationResult = new ValidationResult();

    @Test
    public void shouldThrowValidationExceptionWhenRequestIsNull() {
        assertThatThrownBy(() -> validator.validate(null, validationResult))
            .isInstanceOf(ValidationException.class)
            .satisfies(e -> {
                final ValidationResult result = ((ValidationException) e).getValidationResult();
                final List<ValidationError> errors = result.getErrors();
                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getCode()).isEqualTo(BODY_IS_MISSING);
            });
    }

    @Nested
    @DisplayName("New Password Validation")
    public final class NewPasswordValidation {

        @Test
        public void shouldThrowValidationExceptionWhenNewPasswordIsNull() {
            final PasswordRequest request = aPasswordRequestWithNewPassword(null);

            assertThatThrownBy(() -> validator.validate(request, validationResult))
                .isInstanceOf(ValidationException.class)
                .satisfies(e -> {
                    final ValidationResult result = ((ValidationException) e).getValidationResult();
                    final List<ValidationError> errors = result.getErrors();
                    errors.stream()
                        .filter(error -> error.getField().equals(NEW_PASSWORD))
                        .findFirst()
                        .ifPresent(error -> {
                            assertThat(error.getCode()).isEqualTo(PASSWORD_MUST_NOT_BE_EMPTY);
                        });
                });
        }

        @Test
        public void shouldThrowValidationExceptionWhenNewPasswordIsEmpty() {
            final PasswordRequest request = aPasswordRequestWithNewPassword("");

            assertThatThrownBy(() -> validator.validate(request, validationResult))
                .isInstanceOf(ValidationException.class)
                .satisfies(e -> {
                    final ValidationResult result = ((ValidationException) e).getValidationResult();
                    final List<ValidationError> errors = result.getErrors();
                    errors.stream()
                        .filter(error -> error.getField().equals(NEW_PASSWORD))
                        .findFirst()
                        .ifPresent(error -> {
                            assertThat(error.getCode()).isEqualTo(PASSWORD_MUST_NOT_BE_EMPTY);
                        });
                });
        }
    }


    @Nested
    @DisplayName("Password Confirmation Validation")
    public final class PasswordConfirmationValidation {

        @Test
        public void shouldThrowValidationExceptionWhenPasswordsDoNotMatch() {
            final PasswordRequest request = aPasswordRequest("password123", "differentPassword");

            assertThatThrownBy(() -> validator.validate(request, validationResult))
                .isInstanceOf(ValidationException.class)
                .satisfies(e -> {
                    final ValidationResult result = ((ValidationException) e).getValidationResult();
                    final List<ValidationError> errors = result.getErrors();
                    errors.stream()
                        .filter(error -> error.getField().equals(CONFIRM_PASSWORD))
                        .findFirst()
                        .ifPresent(error -> {
                            assertThat(error.getCode()).isEqualTo(PASSWORD_DOES_NOT_MATCH_THE_CONFIRMATION);
                        });
                });
        }

        @Test
        public void shouldThrowValidationExceptionWhenConfirmPasswordIsNull() {
            final PasswordRequest request = aPasswordRequestWithConfirmPassword(null);

            assertThatThrownBy(() -> validator.validate(request, validationResult))
                .isInstanceOf(ValidationException.class)
                .satisfies(e -> {
                    final ValidationResult result = ((ValidationException) e).getValidationResult();
                    final List<ValidationError> errors = result.getErrors();
                    errors.stream()
                        .filter(error -> error.getField().equals(CONFIRM_PASSWORD))
                        .findFirst()
                        .ifPresent(error -> {
                            assertThat(error.getCode()).isEqualTo(PASSWORD_DOES_NOT_MATCH_THE_CONFIRMATION);
                        });
                });
        }
    }

    @Test
    public void shouldThrowValidationExceptionWhenPasswordIsNotStrongEnough() {
        final PasswordRequest request = aValidPasswordRequest();
        when(passwordValidator.validatePassword(anyString())).thenReturn(new RuleResult(false));

        assertThatThrownBy(() -> validator.validate(request, validationResult))
            .isInstanceOf(ValidationException.class)
            .satisfies(e -> {
                final ValidationResult result = ((ValidationException) e).getValidationResult();
                final List<ValidationError> errors = result.getErrors();
                errors.stream()
                    .filter(error -> error.getField().equals(PASSWORD))
                    .findFirst()
                    .ifPresent(error -> {
                        assertThat(error.getCode()).isEqualTo(PASSWORD_IS_NOT_STRONG_ENOUGH);
                    });
            });
    }

    @Test
    public void shouldValidateSuccessfullyWhenAllFieldsAreValid() {
        final PasswordRequest request = aValidPasswordRequest();
        when(passwordValidator.validatePassword(anyString())).thenReturn(new RuleResult(true));

        assertThatNoException().isThrownBy(() -> validator.validate(request, validationResult));
    }

    private static PasswordRequest aValidPasswordRequest() {
        return aPasswordRequest("ValidPass123!", "ValidPass123!");
    }

    private static PasswordRequest aPasswordRequest(final String newPassword, final String confirmPassword) {
        return new PasswordRequest()
            .setNewPassword(newPassword)
            .setConfirmPassword(confirmPassword);
    }

    private static PasswordRequest aPasswordRequestWithNewPassword(final String newPassword) {
        return aPasswordRequest(newPassword, "ValidPass123!");
    }

    private static PasswordRequest aPasswordRequestWithConfirmPassword(final String confirmPassword) {
        return aPasswordRequest("ValidPass123!", confirmPassword);
    }
}
