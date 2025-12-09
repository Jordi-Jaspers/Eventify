package io.github.eventify.api.authentication.model.validator;

import io.github.eventify.api.authentication.model.request.LoginRequest;
import io.github.eventify.api.authentication.model.request.RegisterUserRequest;
import io.github.eventify.support.UnitTest;
import io.github.jframe.exception.core.ValidationException;
import io.github.jframe.validation.ValidationResult;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.passay.RuleResult;

import static io.github.eventify.api.authentication.model.validator.AuthenticationValidator.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@DisplayName("Unit Test - Authentication Validator")
public class AuthenticationValidatorTest extends UnitTest {

    @Mock
    private CustomPasswordValidator customPasswordValidator;

    private AuthenticationValidator authenticationValidator;

    @BeforeEach
    public void setUp() {
        authenticationValidator = new AuthenticationValidator(customPasswordValidator);
    }

    @Nested
    @DisplayName("Base Validate Method Tests")
    public class BaseValidateTests {

        @Test
        @DisplayName("Should throw ValidationException when input is null")
        public void shouldThrowValidationExceptionWhenInputIsNull() {
            // Given: A null input
            final Object input = null;

            // And: A validation result
            final ValidationResult result = new ValidationResult();

            // When & Then: Validating null input should throw ValidationException
            final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> authenticationValidator.validate(input, result)
            );

            // And: The exception should contain the correct error message
            assertThat(exception.getValidationResult().getErrors().size(), is(1));
            assertThat(
                exception.getValidationResult().getErrors().get(0).getCode(),
                is(equalTo(BODY_IS_MISSING))
            );
        }

        @Test
        @DisplayName("Should not throw exception when input is not null")
        public void shouldNotThrowExceptionWhenInputIsNotNull() {
            // Given: A non-null input
            final Object input = new Object();

            // And: A validation result
            final ValidationResult result = new ValidationResult();

            // When: Validating non-null input
            authenticationValidator.validate(input, result);

            // Then: No exception should be thrown and no errors should be added
            assertThat(result.hasErrors(), is(false));
        }
    }


    @Nested
    @DisplayName("User Registration Validation - Valid Cases")
    public class ValidUserRegistrationTests {

        @Test
        @DisplayName("Should pass validation when registration request is valid")
        public void shouldPassValidationWhenRegistrationRequestIsValid() {
            // Given: A valid registration request
            final RegisterUserRequest request = new RegisterUserRequest()
                .setEmail(VALID_EMAIL)
                .setPassword(VALID_PASSWORD)
                .setPasswordConfirmation(VALID_PASSWORD_CONFIRMATION);

            // And: The password validator returns a valid result
            final RuleResult passwordRuleResult = new RuleResult(true);
            when(customPasswordValidator.validatePassword(VALID_PASSWORD))
                .thenReturn(passwordRuleResult);

            // When: Validating the registration request
            authenticationValidator.validateUserRegistration(request);

            // Then: No exception should be thrown
        }

        @Test
        @DisplayName("Should pass validation when email contains dots in local part")
        public void shouldPassValidationWhenEmailContainsDotsInLocalPart() {
            // Given: A registration request with email containing dots
            final RegisterUserRequest request = new RegisterUserRequest()
                .setEmail("first.last@example.com")
                .setPassword(VALID_PASSWORD)
                .setPasswordConfirmation(VALID_PASSWORD_CONFIRMATION);

            // And: The password validator returns a valid result
            final RuleResult passwordRuleResult = new RuleResult(true);
            when(customPasswordValidator.validatePassword(VALID_PASSWORD))
                .thenReturn(passwordRuleResult);

            // When: Validating the registration request
            authenticationValidator.validateUserRegistration(request);

            // Then: No exception should be thrown
        }

        @Test
        @DisplayName("Should pass validation when email contains plus sign")
        public void shouldPassValidationWhenEmailContainsPlusSign() {
            // Given: A registration request with email containing plus sign
            final RegisterUserRequest request = new RegisterUserRequest()
                .setEmail("user+tag@example.com")
                .setPassword(VALID_PASSWORD)
                .setPasswordConfirmation(VALID_PASSWORD_CONFIRMATION);

            // And: The password validator returns a valid result
            final RuleResult passwordRuleResult = new RuleResult(true);
            when(customPasswordValidator.validatePassword(VALID_PASSWORD))
                .thenReturn(passwordRuleResult);

            // When: Validating the registration request
            authenticationValidator.validateUserRegistration(request);

            // Then: No exception should be thrown
        }
    }


    @Nested
    @DisplayName("User Registration Validation - Null Request")
    public class NullRegistrationRequestTests {

        @Test
        @DisplayName("Should throw ValidationException when registration request is null")
        public void shouldThrowValidationExceptionWhenRegistrationRequestIsNull() {
            // Given: A null registration request
            final RegisterUserRequest request = null;

            // When & Then: Validating null request should throw ValidationException
            final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> authenticationValidator.validateUserRegistration(request)
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
    @DisplayName("User Registration Validation - Email Validation")
    public class RegistrationEmailValidationTests {

        @Test
        @DisplayName("Should throw NullPointerException when email is null due to getEmail toLowerCase")
        public void shouldThrowNullPointerExceptionWhenEmailIsNullDueToGetEmailToLowerCase() {
            // Given: A registration request with null email
            final RegisterUserRequest request = new RegisterUserRequest()
                .setEmail(null)
                .setPassword(VALID_PASSWORD)
                .setPasswordConfirmation(VALID_PASSWORD_CONFIRMATION);

            // When & Then: Validating the request should throw NullPointerException
            // This is because RegisterUserRequest.getEmail() calls toLowerCase() on null
            assertThrows(
                NullPointerException.class,
                () -> authenticationValidator.validateUserRegistration(request)
            );
        }

        @Test
        @DisplayName("Should throw ValidationException when email is empty")
        public void shouldThrowValidationExceptionWhenEmailIsEmpty() {
            // Given: A registration request with empty email
            final RegisterUserRequest request = new RegisterUserRequest()
                .setEmail("")
                .setPassword(VALID_PASSWORD)
                .setPasswordConfirmation(VALID_PASSWORD_CONFIRMATION);

            // When & Then: Validating the request should throw ValidationException
            final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> authenticationValidator.validateUserRegistration(request)
            );

            // And: The exception should contain the correct error message
            assertThat(exception.getValidationResult().hasErrors(), is(true));
            assertThat(
                exception.getValidationResult().getErrors().stream()
                    .anyMatch(error -> error.getCode().equals(EMAIL_MUST_NOT_BE_EMPTY)),
                is(true)
            );
        }

        @Test
        @DisplayName("Should throw ValidationException when email does not contain at symbol")
        public void shouldThrowValidationExceptionWhenEmailDoesNotContainAtSymbol() {
            // Given: A registration request with email missing @ symbol
            final RegisterUserRequest request = new RegisterUserRequest()
                .setEmail("userexample.com")
                .setPassword(VALID_PASSWORD)
                .setPasswordConfirmation(VALID_PASSWORD_CONFIRMATION);

            // When & Then: Validating the request should throw ValidationException
            final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> authenticationValidator.validateUserRegistration(request)
            );

            // And: The exception should contain the correct error message
            assertThat(exception.getValidationResult().hasErrors(), is(true));
            assertThat(
                exception.getValidationResult().getErrors().stream()
                    .anyMatch(error -> error.getCode().equals(EMAIL_MUST_CONTAIN_AN_AT)),
                is(true)
            );
        }

        @Test
        @DisplayName("Should throw ValidationException when email is not a valid email address")
        public void shouldThrowValidationExceptionWhenEmailIsNotAValidEmailAddress() {
            // Given: A registration request with invalid email format
            final RegisterUserRequest request = new RegisterUserRequest()
                .setEmail("user@")
                .setPassword(VALID_PASSWORD)
                .setPasswordConfirmation(VALID_PASSWORD_CONFIRMATION);

            // When & Then: Validating the request should throw ValidationException
            final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> authenticationValidator.validateUserRegistration(request)
            );

            // And: The exception should contain the correct error message
            assertThat(exception.getValidationResult().hasErrors(), is(true));
            assertThat(
                exception.getValidationResult().getErrors().stream()
                    .anyMatch(error -> error.getCode().equals(EMAIL_MUST_BE_A_VALID_EMAIL_ADDRESS)),
                is(true)
            );
        }
    }


    @Nested
    @DisplayName("User Registration Validation - Password Validation")
    public class RegistrationPasswordValidationTests {

        @Test
        @DisplayName("Should throw ValidationException when password is null")
        public void shouldThrowValidationExceptionWhenPasswordIsNull() {
            // Given: A registration request with null password
            final RegisterUserRequest request = new RegisterUserRequest()
                .setEmail(VALID_EMAIL)
                .setPassword(null)
                .setPasswordConfirmation(VALID_PASSWORD_CONFIRMATION);

            // When & Then: Validating the request should throw ValidationException
            final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> authenticationValidator.validateUserRegistration(request)
            );

            // And: The exception should contain the correct error message
            assertThat(exception.getValidationResult().hasErrors(), is(true));
            assertThat(
                exception.getValidationResult().getErrors().stream()
                    .anyMatch(error -> error.getCode().equals(PASSWORD_MUST_NOT_BE_EMPTY)),
                is(true)
            );
        }

        @Test
        @DisplayName("Should throw ValidationException when password is empty")
        public void shouldThrowValidationExceptionWhenPasswordIsEmpty() {
            // Given: A registration request with empty password
            final RegisterUserRequest request = new RegisterUserRequest()
                .setEmail(VALID_EMAIL)
                .setPassword("")
                .setPasswordConfirmation(VALID_PASSWORD_CONFIRMATION);

            // When & Then: Validating the request should throw ValidationException
            final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> authenticationValidator.validateUserRegistration(request)
            );

            // And: The exception should contain the correct error message
            assertThat(exception.getValidationResult().hasErrors(), is(true));
            assertThat(
                exception.getValidationResult().getErrors().stream()
                    .anyMatch(error -> error.getCode().equals(PASSWORD_MUST_NOT_BE_EMPTY)),
                is(true)
            );
        }

        @Test
        @DisplayName("Should throw ValidationException when password is not strong enough")
        public void shouldThrowValidationExceptionWhenPasswordIsNotStrongEnough() {
            // Given: A registration request with weak password
            final RegisterUserRequest request = new RegisterUserRequest()
                .setEmail(VALID_EMAIL)
                .setPassword(WEAK_PASSWORD)
                .setPasswordConfirmation(WEAK_PASSWORD);

            // And: The password validator returns an invalid result
            final RuleResult passwordRuleResult = new RuleResult(false);
            when(customPasswordValidator.validatePassword(WEAK_PASSWORD))
                .thenReturn(passwordRuleResult);

            // When & Then: Validating the request should throw ValidationException
            final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> authenticationValidator.validateUserRegistration(request)
            );

            // And: The exception should contain password strength error
            assertThat(exception.getValidationResult().hasErrors(), is(true));
            assertThat(
                exception.getValidationResult().getErrors().stream()
                    .anyMatch(error -> error.getCode().contains("Password is not strong enough")),
                is(true)
            );
        }
    }


    @Nested
    @DisplayName("User Registration Validation - Password Confirmation")
    public class RegistrationPasswordConfirmationTests {

        @Test
        @DisplayName("Should throw ValidationException when password confirmation is null")
        public void shouldThrowValidationExceptionWhenPasswordConfirmationIsNull() {
            // Given: A registration request with null password confirmation
            final RegisterUserRequest request = new RegisterUserRequest()
                .setEmail(VALID_EMAIL)
                .setPassword(VALID_PASSWORD)
                .setPasswordConfirmation(null);

            // When & Then: Validating the request should throw ValidationException
            final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> authenticationValidator.validateUserRegistration(request)
            );

            // And: The exception should contain the correct error message
            assertThat(exception.getValidationResult().hasErrors(), is(true));
            assertThat(
                exception.getValidationResult().getErrors().stream()
                    .anyMatch(error -> error.getCode().equals(PASSWORD_DOES_NOT_MATCH_THE_CONFIRMATION)),
                is(true)
            );
        }

        @Test
        @DisplayName("Should throw ValidationException when password confirmation is empty")
        public void shouldThrowValidationExceptionWhenPasswordConfirmationIsEmpty() {
            // Given: A registration request with empty password confirmation
            final RegisterUserRequest request = new RegisterUserRequest()
                .setEmail(VALID_EMAIL)
                .setPassword(VALID_PASSWORD)
                .setPasswordConfirmation("");

            // When & Then: Validating the request should throw ValidationException
            final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> authenticationValidator.validateUserRegistration(request)
            );

            // And: The exception should contain the correct error message
            assertThat(exception.getValidationResult().hasErrors(), is(true));
            assertThat(
                exception.getValidationResult().getErrors().stream()
                    .anyMatch(error -> error.getCode().equals(PASSWORD_DOES_NOT_MATCH_THE_CONFIRMATION)),
                is(true)
            );
        }

        @Test
        @DisplayName("Should throw ValidationException when password confirmation does not match")
        public void shouldThrowValidationExceptionWhenPasswordConfirmationDoesNotMatch() {
            // Given: A registration request with non-matching passwords
            final RegisterUserRequest request = new RegisterUserRequest()
                .setEmail(VALID_EMAIL)
                .setPassword(VALID_PASSWORD)
                .setPasswordConfirmation("DifferentPassword123!@#");

            // When & Then: Validating the request should throw ValidationException
            final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> authenticationValidator.validateUserRegistration(request)
            );

            // And: The exception should contain the correct error message
            assertThat(exception.getValidationResult().hasErrors(), is(true));
            assertThat(
                exception.getValidationResult().getErrors().stream()
                    .anyMatch(error -> error.getCode().equals(PASSWORD_DOES_NOT_MATCH_THE_CONFIRMATION)),
                is(true)
            );
        }
    }


    @Nested
    @DisplayName("Login Request Validation - Valid Cases")
    public class ValidLoginRequestTests {

        @Test
        @DisplayName("Should pass validation when login request is valid")
        public void shouldPassValidationWhenLoginRequestIsValid() {
            // Given: A valid login request
            final LoginRequest request = new LoginRequest()
                .setEmail(VALID_EMAIL)
                .setPassword(VALID_PASSWORD);

            // When: Validating the login request
            authenticationValidator.validateLoginRequest(request);

            // Then: No exception should be thrown
        }

        @Test
        @DisplayName("Should pass validation when email contains special characters")
        public void shouldPassValidationWhenEmailContainsSpecialCharacters() {
            // Given: A login request with email containing special characters
            final LoginRequest request = new LoginRequest()
                .setEmail("user+test@example.com")
                .setPassword(VALID_PASSWORD);

            // When: Validating the login request
            authenticationValidator.validateLoginRequest(request);

            // Then: No exception should be thrown
        }
    }


    @Nested
    @DisplayName("Login Request Validation - Null Request")
    public class NullLoginRequestTests {

        @Test
        @DisplayName("Should throw ValidationException when login request is null")
        public void shouldThrowValidationExceptionWhenLoginRequestIsNull() {
            // Given: A null login request
            final LoginRequest request = null;

            // When & Then: Validating null request should throw ValidationException
            final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> authenticationValidator.validateLoginRequest(request)
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
    @DisplayName("Login Request Validation - Email Validation")
    public class LoginEmailValidationTests {

        @Test
        @DisplayName("Should throw ValidationException when email is null")
        public void shouldThrowValidationExceptionWhenEmailIsNull() {
            // Given: A login request with null email
            final LoginRequest request = new LoginRequest()
                .setEmail(null)
                .setPassword(VALID_PASSWORD);

            // When & Then: Validating the request should throw ValidationException
            final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> authenticationValidator.validateLoginRequest(request)
            );

            // And: The exception should contain the correct error message
            assertThat(exception.getValidationResult().hasErrors(), is(true));
            assertThat(exception.getValidationResult().getErrors().size(), is(1));
            assertThat(
                exception.getValidationResult().getErrors().get(0).getCode(),
                is(equalTo(EMAIL_MUST_NOT_BE_EMPTY))
            );
        }

        @Test
        @DisplayName("Should throw ValidationException when email is empty")
        public void shouldThrowValidationExceptionWhenEmailIsEmpty() {
            // Given: A login request with empty email
            final LoginRequest request = new LoginRequest()
                .setEmail("")
                .setPassword(VALID_PASSWORD);

            // When & Then: Validating the request should throw ValidationException
            final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> authenticationValidator.validateLoginRequest(request)
            );

            // And: The exception should contain the correct error message
            assertThat(exception.getValidationResult().hasErrors(), is(true));
            assertThat(exception.getValidationResult().getErrors().size(), is(1));
            assertThat(
                exception.getValidationResult().getErrors().get(0).getCode(),
                is(equalTo(EMAIL_MUST_NOT_BE_EMPTY))
            );
        }
    }


    @Nested
    @DisplayName("Login Request Validation - Password Validation")
    public class LoginPasswordValidationTests {

        @Test
        @DisplayName("Should throw ValidationException when password is null")
        public void shouldThrowValidationExceptionWhenPasswordIsNull() {
            // Given: A login request with null password
            final LoginRequest request = new LoginRequest()
                .setEmail(VALID_EMAIL)
                .setPassword(null);

            // When & Then: Validating the request should throw ValidationException
            final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> authenticationValidator.validateLoginRequest(request)
            );

            // And: The exception should contain the correct error message
            assertThat(exception.getValidationResult().hasErrors(), is(true));
            assertThat(exception.getValidationResult().getErrors().size(), is(1));
            assertThat(
                exception.getValidationResult().getErrors().get(0).getCode(),
                is(equalTo(PASSWORD_MUST_NOT_BE_EMPTY))
            );
        }

        @Test
        @DisplayName("Should throw ValidationException when password is empty")
        public void shouldThrowValidationExceptionWhenPasswordIsEmpty() {
            // Given: A login request with empty password
            final LoginRequest request = new LoginRequest()
                .setEmail(VALID_EMAIL)
                .setPassword("");

            // When & Then: Validating the request should throw ValidationException
            final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> authenticationValidator.validateLoginRequest(request)
            );

            // And: The exception should contain the correct error message
            assertThat(exception.getValidationResult().hasErrors(), is(true));
            assertThat(exception.getValidationResult().getErrors().size(), is(1));
            assertThat(
                exception.getValidationResult().getErrors().get(0).getCode(),
                is(equalTo(PASSWORD_MUST_NOT_BE_EMPTY))
            );
        }
    }


    @Nested
    @DisplayName("Login Request Validation - Multiple Errors")
    public class LoginMultipleErrorsTests {

        @Test
        @DisplayName("Should throw ValidationException with multiple errors when both fields are null")
        public void shouldThrowValidationExceptionWithMultipleErrorsWhenBothFieldsAreNull() {
            // Given: A login request with both fields null
            final LoginRequest request = new LoginRequest()
                .setEmail(null)
                .setPassword(null);

            // When & Then: Validating the request should throw ValidationException
            final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> authenticationValidator.validateLoginRequest(request)
            );

            // And: The exception should contain errors for both fields
            assertThat(exception.getValidationResult().hasErrors(), is(true));
            assertThat(exception.getValidationResult().getErrors().size(), is(2));
        }

        @Test
        @DisplayName("Should throw ValidationException with multiple errors when both fields are empty")
        public void shouldThrowValidationExceptionWithMultipleErrorsWhenBothFieldsAreEmpty() {
            // Given: A login request with both fields empty
            final LoginRequest request = new LoginRequest()
                .setEmail("")
                .setPassword("");

            // When & Then: Validating the request should throw ValidationException
            final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> authenticationValidator.validateLoginRequest(request)
            );

            // And: The exception should contain errors for both fields
            assertThat(exception.getValidationResult().hasErrors(), is(true));
            assertThat(exception.getValidationResult().getErrors().size(), is(2));
        }
    }


    @Nested
    @DisplayName("User Registration Validation - Multiple Errors")
    public class RegistrationMultipleErrorsTests {

        @Test
        @DisplayName("Should throw NullPointerException when all fields are null due to email toLowerCase")
        public void shouldThrowNullPointerExceptionWhenAllFieldsAreNullDueToEmailToLowerCase() {
            // Given: A registration request with all fields null
            final RegisterUserRequest request = new RegisterUserRequest()
                .setEmail(null)
                .setPassword(null)
                .setPasswordConfirmation(null);

            // When & Then: Validating the request should throw NullPointerException
            // This is because RegisterUserRequest.getEmail() calls toLowerCase() on null
            assertThrows(
                NullPointerException.class,
                () -> authenticationValidator.validateUserRegistration(request)
            );
        }

        @Test
        @DisplayName("Should throw ValidationException with multiple errors when all fields are empty")
        public void shouldThrowValidationExceptionWithMultipleErrorsWhenAllFieldsAreEmpty() {
            // Given: A registration request with all fields empty
            final RegisterUserRequest request = new RegisterUserRequest()
                .setEmail("")
                .setPassword("")
                .setPasswordConfirmation("");

            // When & Then: Validating the request should throw ValidationException
            final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> authenticationValidator.validateUserRegistration(request)
            );

            // And: The exception should contain errors for all fields
            assertThat(exception.getValidationResult().hasErrors(), is(true));
            assertThat(exception.getValidationResult().getErrors().size(), is(greaterThanOrEqualTo(3)));
        }
    }


    @Nested
    @DisplayName("Edge Cases")
    public class EdgeCaseTests {

        @Test
        @DisplayName("Should pass validation when login password is weak but not validated for strength")
        public void shouldPassValidationWhenLoginPasswordIsWeakButNotValidatedForStrength() {
            // Given: A login request with weak password (login doesn't validate strength)
            final LoginRequest request = new LoginRequest()
                .setEmail(VALID_EMAIL)
                .setPassword(WEAK_PASSWORD);

            // When: Validating the login request
            authenticationValidator.validateLoginRequest(request);

            // Then: No exception should be thrown (login doesn't check password strength)
        }

        @Test
        @DisplayName("Should throw ValidationException when registration has valid format but weak password")
        public void shouldThrowValidationExceptionWhenRegistrationHasValidFormatButWeakPassword() {
            // Given: A registration request with valid format but weak password
            final RegisterUserRequest request = new RegisterUserRequest()
                .setEmail(VALID_EMAIL)
                .setPassword(WEAK_PASSWORD)
                .setPasswordConfirmation(WEAK_PASSWORD);

            // And: The password validator returns an invalid result
            final RuleResult passwordRuleResult = new RuleResult(false);
            when(customPasswordValidator.validatePassword(WEAK_PASSWORD))
                .thenReturn(passwordRuleResult);

            // When & Then: Validating the request should throw ValidationException
            final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> authenticationValidator.validateUserRegistration(request)
            );

            // And: The exception should contain password strength error
            assertThat(exception.getValidationResult().hasErrors(), is(true));
        }

        @Test
        @DisplayName("Should pass validation when registration has uppercase email")
        public void shouldPassValidationWhenRegistrationHasUppercaseEmail() {
            // Given: A registration request with uppercase email
            final RegisterUserRequest request = new RegisterUserRequest()
                .setEmail("USER@EXAMPLE.COM")
                .setPassword(VALID_PASSWORD)
                .setPasswordConfirmation(VALID_PASSWORD_CONFIRMATION);

            // And: The password validator returns a valid result
            final RuleResult passwordRuleResult = new RuleResult(true);
            when(customPasswordValidator.validatePassword(VALID_PASSWORD))
                .thenReturn(passwordRuleResult);

            // When: Validating the registration request
            authenticationValidator.validateUserRegistration(request);

            // Then: No exception should be thrown
        }

        @Test
        @DisplayName("Should pass validation when login has uppercase email")
        public void shouldPassValidationWhenLoginHasUppercaseEmail() {
            // Given: A login request with uppercase email
            final LoginRequest request = new LoginRequest()
                .setEmail("USER@EXAMPLE.COM")
                .setPassword(VALID_PASSWORD);

            // When: Validating the login request
            authenticationValidator.validateLoginRequest(request);

            // Then: No exception should be thrown
        }
    }
}
