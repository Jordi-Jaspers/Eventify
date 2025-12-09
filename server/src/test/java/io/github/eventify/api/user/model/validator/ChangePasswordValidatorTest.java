package io.github.eventify.api.user.model.validator;

import io.github.eventify.api.authentication.model.validator.CustomPasswordValidator;
import io.github.eventify.api.user.model.request.PasswordRequest;
import io.github.eventify.support.UnitTest;
import io.github.jframe.exception.core.ValidationException;
import io.github.jframe.validation.ValidationResult;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.passay.RuleResult;

import static io.github.eventify.api.user.model.validator.ChangePasswordValidator.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@DisplayName("Unit Test - Change Password Validator")
public class ChangePasswordValidatorTest extends UnitTest {

    @Mock
    private CustomPasswordValidator customPasswordValidator;

    private ChangePasswordValidator changePasswordValidator;

    @BeforeEach
    public void setUp() {
        changePasswordValidator = new ChangePasswordValidator(customPasswordValidator);
    }

    @Nested
    @DisplayName("Valid Password Change Validation")
    public class ValidPasswordChangeTests {

        @Test
        @DisplayName("Should pass validation when all fields are valid and passwords match")
        public void shouldPassValidationWhenAllFieldsAreValidAndPasswordsMatch() {
            // Given: A valid password request with matching passwords
            final PasswordRequest request = new PasswordRequest()
                .setNewPassword(VALID_NEW_PASSWORD)
                .setConfirmPassword(VALID_CONFIRM_PASSWORD);

            // And: A validation result
            final ValidationResult result = new ValidationResult();

            // And: The password validator returns a valid result
            final RuleResult passwordRuleResult = new RuleResult(true);
            when(customPasswordValidator.validatePassword(VALID_NEW_PASSWORD))
                .thenReturn(passwordRuleResult);

            // When: Validating the password change request
            changePasswordValidator.validate(request, result);

            // Then: The validation should pass
            assertThat(result.hasErrors(), is(false));
            assertThat(result.getErrors().isEmpty(), is(true));
        }
    }


    @Nested
    @DisplayName("Null Request Validation")
    public class NullRequestTests {

        @Test
        @DisplayName("Should throw ValidationException when request is null")
        public void shouldThrowValidationExceptionWhenRequestIsNull() {
            // Given: A null request
            final PasswordRequest request = null;

            // And: A validation result
            final ValidationResult result = new ValidationResult();

            // When & Then: Validating the null request should throw ValidationException
            final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> changePasswordValidator.validate(request, result)
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
    @DisplayName("New Password Validation")
    public class NewPasswordTests {

        @Test
        @DisplayName("Should throw ValidationException when new password is null")
        public void shouldThrowValidationExceptionWhenNewPasswordIsNull() {
            // Given: A request with null new password
            final PasswordRequest request = new PasswordRequest()
                .setNewPassword(null)
                .setConfirmPassword(VALID_CONFIRM_PASSWORD);

            // And: A validation result
            final ValidationResult result = new ValidationResult();

            // When & Then: Validating the request should throw ValidationException
            final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> changePasswordValidator.validate(request, result)
            );

            // And: The exception should contain error for new password field
            assertThat(exception.getValidationResult().hasErrors(), is(true));
            assertThat(
                exception.getValidationResult().getErrors().stream()
                    .anyMatch(
                        error -> error.getField().equals(NEW_PASSWORD) &&
                            error.getCode().equals(PASSWORD_MUST_NOT_BE_EMPTY)
                    ),
                is(true)
            );
        }

        @Test
        @DisplayName("Should throw ValidationException when new password is empty")
        public void shouldThrowValidationExceptionWhenNewPasswordIsEmpty() {
            // Given: A request with empty new password
            final PasswordRequest request = new PasswordRequest()
                .setNewPassword("")
                .setConfirmPassword(VALID_CONFIRM_PASSWORD);

            // And: A validation result
            final ValidationResult result = new ValidationResult();

            // When & Then: Validating the request should throw ValidationException
            final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> changePasswordValidator.validate(request, result)
            );

            // And: The exception should contain error for new password field
            assertThat(exception.getValidationResult().hasErrors(), is(true));
            assertThat(
                exception.getValidationResult().getErrors().stream()
                    .anyMatch(
                        error -> error.getField().equals(NEW_PASSWORD) &&
                            error.getCode().equals(PASSWORD_MUST_NOT_BE_EMPTY)
                    ),
                is(true)
            );
        }
    }


    @Nested
    @DisplayName("Confirm Password Validation")
    public class ConfirmPasswordTests {

        @Test
        @DisplayName("Should throw ValidationException when confirm password is null")
        public void shouldThrowValidationExceptionWhenConfirmPasswordIsNull() {
            // Given: A request with null confirm password
            final PasswordRequest request = new PasswordRequest()
                .setNewPassword(VALID_NEW_PASSWORD)
                .setConfirmPassword(null);

            // And: A validation result
            final ValidationResult result = new ValidationResult();

            // When & Then: Validating the request should throw ValidationException
            final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> changePasswordValidator.validate(request, result)
            );

            // And: The exception should contain the correct error message
            assertThat(exception.getValidationResult().hasErrors(), is(true));
            assertThat(exception.getValidationResult().getErrors().size(), is(1));
            assertThat(
                exception.getValidationResult().getErrors().get(0).getField(),
                is(equalTo(CONFIRM_PASSWORD))
            );
            assertThat(
                exception.getValidationResult().getErrors().get(0).getCode(),
                is(equalTo(PASSWORD_DOES_NOT_MATCH_THE_CONFIRMATION))
            );
        }

        @Test
        @DisplayName("Should throw ValidationException when confirm password is empty")
        public void shouldThrowValidationExceptionWhenConfirmPasswordIsEmpty() {
            // Given: A request with empty confirm password
            final PasswordRequest request = new PasswordRequest()
                .setNewPassword(VALID_NEW_PASSWORD)
                .setConfirmPassword("");

            // And: A validation result
            final ValidationResult result = new ValidationResult();

            // When & Then: Validating the request should throw ValidationException
            final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> changePasswordValidator.validate(request, result)
            );

            // And: The exception should contain the correct error message
            assertThat(exception.getValidationResult().hasErrors(), is(true));
            assertThat(exception.getValidationResult().getErrors().size(), is(1));
            assertThat(
                exception.getValidationResult().getErrors().get(0).getField(),
                is(equalTo(CONFIRM_PASSWORD))
            );
            assertThat(
                exception.getValidationResult().getErrors().get(0).getCode(),
                is(equalTo(PASSWORD_DOES_NOT_MATCH_THE_CONFIRMATION))
            );
        }

        @Test
        @DisplayName("Should throw ValidationException when passwords do not match")
        public void shouldThrowValidationExceptionWhenPasswordsDoNotMatch() {
            // Given: A request with non-matching passwords
            final PasswordRequest request = new PasswordRequest()
                .setNewPassword(VALID_NEW_PASSWORD)
                .setConfirmPassword("DifferentPassword123!@#");

            // And: A validation result
            final ValidationResult result = new ValidationResult();

            // When & Then: Validating the request should throw ValidationException
            final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> changePasswordValidator.validate(request, result)
            );

            // And: The exception should contain the correct error message
            assertThat(exception.getValidationResult().hasErrors(), is(true));
            assertThat(exception.getValidationResult().getErrors().size(), is(1));
            assertThat(
                exception.getValidationResult().getErrors().get(0).getField(),
                is(equalTo(CONFIRM_PASSWORD))
            );
            assertThat(
                exception.getValidationResult().getErrors().get(0).getCode(),
                is(equalTo(PASSWORD_DOES_NOT_MATCH_THE_CONFIRMATION))
            );
        }
    }


    @Nested
    @DisplayName("Password Strength Validation")
    public class PasswordStrengthTests {

        @Test
        @DisplayName("Should throw ValidationException when password is not strong enough")
        public void shouldThrowValidationExceptionWhenPasswordIsNotStrongEnough() {
            // Given: A request with weak password that matches
            final PasswordRequest request = new PasswordRequest()
                .setNewPassword(WEAK_PASSWORD)
                .setConfirmPassword(WEAK_PASSWORD);

            // And: A validation result
            final ValidationResult result = new ValidationResult();

            // And: The password validator returns an invalid result
            final RuleResult passwordRuleResult = new RuleResult(false);
            when(customPasswordValidator.validatePassword(WEAK_PASSWORD))
                .thenReturn(passwordRuleResult);

            // When & Then: Validating the request should throw ValidationException
            final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> changePasswordValidator.validate(request, result)
            );

            // And: The exception should contain the correct error message
            assertThat(exception.getValidationResult().hasErrors(), is(true));
            assertThat(exception.getValidationResult().getErrors().size(), is(1));
            assertThat(
                exception.getValidationResult().getErrors().get(0).getField(),
                is(equalTo(PASSWORD))
            );
            assertThat(
                exception.getValidationResult().getErrors().get(0).getCode(),
                is(equalTo(PASSWORD_IS_NOT_STRONG_ENOUGH))
            );
        }
    }


    @Nested
    @DisplayName("Multiple Validation Errors")
    public class MultipleErrorsTests {

        @Test
        @DisplayName("Should throw ValidationException with multiple errors when both passwords are null")
        public void shouldThrowValidationExceptionWithMultipleErrorsWhenBothPasswordsAreNull() {
            // Given: A request with both passwords null
            final PasswordRequest request = new PasswordRequest()
                .setNewPassword(null)
                .setConfirmPassword(null);

            // And: A validation result
            final ValidationResult result = new ValidationResult();

            // When & Then: Validating the request should throw ValidationException
            final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> changePasswordValidator.validate(request, result)
            );

            // And: The exception should contain errors for both fields
            assertThat(exception.getValidationResult().hasErrors(), is(true));
            assertThat(exception.getValidationResult().getErrors().size(), is(2));

            // And: The errors should be for the correct fields
            assertThat(
                exception.getValidationResult().getErrors().get(0).getField(),
                is(equalTo(NEW_PASSWORD))
            );
            assertThat(
                exception.getValidationResult().getErrors().get(1).getField(),
                is(equalTo(CONFIRM_PASSWORD))
            );
        }

        @Test
        @DisplayName("Should throw ValidationException with multiple errors when both passwords are empty")
        public void shouldThrowValidationExceptionWithMultipleErrorsWhenBothPasswordsAreEmpty() {
            // Given: A request with both passwords empty
            final PasswordRequest request = new PasswordRequest()
                .setNewPassword("")
                .setConfirmPassword("");

            // And: A validation result
            final ValidationResult result = new ValidationResult();

            // When & Then: Validating the request should throw ValidationException
            final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> changePasswordValidator.validate(request, result)
            );

            // And: The exception should contain errors for both fields
            assertThat(exception.getValidationResult().hasErrors(), is(true));
            assertThat(exception.getValidationResult().getErrors().size(), is(2));

            // And: The errors should be for the correct fields
            assertThat(
                exception.getValidationResult().getErrors().get(0).getField(),
                is(equalTo(NEW_PASSWORD))
            );
            assertThat(
                exception.getValidationResult().getErrors().get(1).getField(),
                is(equalTo(CONFIRM_PASSWORD))
            );
        }
    }


    @Nested
    @DisplayName("Edge Cases")
    public class EdgeCaseTests {

        @Test
        @DisplayName("Should pass validation when password contains all required character types")
        public void shouldPassValidationWhenPasswordContainsAllRequiredCharacterTypes() {
            // Given: A request with a strong password containing all required types
            final String strongPassword = "StrongP@ssw0rd";
            final PasswordRequest request = new PasswordRequest()
                .setNewPassword(strongPassword)
                .setConfirmPassword(strongPassword);

            // And: A validation result
            final ValidationResult result = new ValidationResult();

            // And: The password validator returns a valid result
            final RuleResult passwordRuleResult = new RuleResult(true);
            when(customPasswordValidator.validatePassword(strongPassword))
                .thenReturn(passwordRuleResult);

            // When: Validating the password change request
            changePasswordValidator.validate(request, result);

            // Then: The validation should pass
            assertThat(result.hasErrors(), is(false));
        }

        @Test
        @DisplayName("Should pass validation when password is exactly minimum length with required characters")
        public void shouldPassValidationWhenPasswordIsExactlyMinimumLengthWithRequiredCharacters() {
            // Given: A request with minimum length password
            final String minLengthPassword = "Test123!";
            final PasswordRequest request = new PasswordRequest()
                .setNewPassword(minLengthPassword)
                .setConfirmPassword(minLengthPassword);

            // And: A validation result
            final ValidationResult result = new ValidationResult();

            // And: The password validator returns a valid result
            final RuleResult passwordRuleResult = new RuleResult(true);
            when(customPasswordValidator.validatePassword(minLengthPassword))
                .thenReturn(passwordRuleResult);

            // When: Validating the password change request
            changePasswordValidator.validate(request, result);

            // Then: The validation should pass
            assertThat(result.hasErrors(), is(false));
        }

        @Test
        @DisplayName("Should pass validation when password contains special characters")
        public void shouldPassValidationWhenPasswordContainsSpecialCharacters() {
            // Given: A request with password containing various special characters
            final String specialCharPassword = "P@ssw0rd!#$%";
            final PasswordRequest request = new PasswordRequest()
                .setNewPassword(specialCharPassword)
                .setConfirmPassword(specialCharPassword);

            // And: A validation result
            final ValidationResult result = new ValidationResult();

            // And: The password validator returns a valid result
            final RuleResult passwordRuleResult = new RuleResult(true);
            when(customPasswordValidator.validatePassword(specialCharPassword))
                .thenReturn(passwordRuleResult);

            // When: Validating the password change request
            changePasswordValidator.validate(request, result);

            // Then: The validation should pass
            assertThat(result.hasErrors(), is(false));
        }

        @Test
        @DisplayName("Should throw ValidationException when password has trailing whitespace")
        public void shouldThrowValidationExceptionWhenPasswordHasTrailingWhitespace() {
            // Given: A request with password containing trailing whitespace
            final String passwordWithSpace = "Test123!@# ";
            final PasswordRequest request = new PasswordRequest()
                .setNewPassword(passwordWithSpace)
                .setConfirmPassword(passwordWithSpace);

            // And: A validation result
            final ValidationResult result = new ValidationResult();

            // And: The password validator returns an invalid result due to whitespace
            final RuleResult passwordRuleResult = new RuleResult(false);
            when(customPasswordValidator.validatePassword(passwordWithSpace))
                .thenReturn(passwordRuleResult);

            // When & Then: Validating the request should throw ValidationException
            final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> changePasswordValidator.validate(request, result)
            );

            // And: The exception should contain the correct error message
            assertThat(exception.getValidationResult().hasErrors(), is(true));
            assertThat(
                exception.getValidationResult().getErrors().get(0).getCode(),
                is(equalTo(PASSWORD_IS_NOT_STRONG_ENOUGH))
            );
        }
    }
}
