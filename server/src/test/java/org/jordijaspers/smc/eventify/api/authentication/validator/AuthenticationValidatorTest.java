package org.jordijaspers.smc.eventify.api.authentication.validator;

import org.hawaiiframework.validation.ValidationException;
import org.jordijaspers.eventify.api.authentication.model.request.LoginRequest;
import org.jordijaspers.eventify.api.authentication.model.request.RegisterUserRequest;
import org.jordijaspers.eventify.api.authentication.model.validator.AuthenticationValidator;
import org.jordijaspers.eventify.api.authentication.model.validator.CustomPasswordValidator;
import org.jordijaspers.smc.eventify.support.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.passay.RuleResult;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@DisplayName("ChangePasswordValidator")
public class AuthenticationValidatorTest extends UnitTest {

    private static final String VALID_EMAIL = "test@example.com";
    private static final String VALID_PASSWORD = "ValidPass1!";
    private static final String VALID_FIRST_NAME = "John";
    private static final String VALID_LAST_NAME = "Doe";

    @Mock
    private CustomPasswordValidator passwordValidator;

    private AuthenticationValidator validator;

    @BeforeEach
    void setUp() {
        validator = new AuthenticationValidator(passwordValidator);
    }

    @Nested
    @DisplayName("Register User Request Validation")
    class RegisterUserValidationTests {

        @Nested
        @DisplayName("Basic Field Validation")
        class BasicFieldValidationTests {

            @Test
            @DisplayName("Should validate successfully when all fields are valid")
            void shouldValidateSuccessfullyWhenAllFieldsAreValid() {
                when(passwordValidator.validatePassword(VALID_PASSWORD))
                    .thenReturn(new RuleResult(true));

                assertThatNoException()
                    .isThrownBy(() -> validator.validateUserRegistration(aRegisterUserRequest()));
            }

            @Test
            @DisplayName("Should throw ValidationException when request is null")
            void shouldThrowValidationExceptionWhenRequestIsNull() {
                assertThatThrownBy(() -> validator.validateUserRegistration(null))
                    .isInstanceOf(ValidationException.class)
                    .satisfies(thrown -> {
                        ValidationException ex = (ValidationException) thrown;
                        assertThat(ex.getValidationResult().getErrors())
                            .anyMatch(error -> error.getCode().equals(AuthenticationValidator.BODY_IS_MISSING));
                    });
            }
        }


        @Nested
        @DisplayName("Email Validation")
        class EmailValidationTests {

            @Test
            @DisplayName("Should throw ValidationException when email is invalid")
            void shouldThrowValidationExceptionWhenEmailIsInvalid() {
                RegisterUserRequest request = aRegisterUserRequest()
                    .setEmail("invalid-email");

                assertThatThrownBy(() -> validator.validateUserRegistration(request))
                    .isInstanceOf(ValidationException.class)
                    .satisfies(thrown -> {
                        ValidationException ex = (ValidationException) thrown;
                        assertThat(ex.getValidationResult().getErrors())
                            .anyMatch(error -> error.getCode().equals(AuthenticationValidator.EMAIL_MUST_CONTAIN_AN_AT));
                    });
            }

            @Test
            @DisplayName("Should throw ValidationException when email is null")
            void shouldThrowValidationExceptionWhenEmailIsNull() {
                RegisterUserRequest request = aRegisterUserRequest()
                    .setEmail(null);

                assertThatThrownBy(() -> validator.validateUserRegistration(request))
                    .isInstanceOf(ValidationException.class)
                    .satisfies(thrown -> {
                        ValidationException ex = (ValidationException) thrown;
                        assertThat(ex.getValidationResult().getErrors())
                            .anyMatch(error -> error.getCode().equals(AuthenticationValidator.EMAIL_MUST_NOT_BE_EMPTY));
                    });
            }
        }


        @Nested
        @DisplayName("Password Validation")
        class PasswordValidationTests {

            @Test
            @DisplayName("Should throw ValidationException when passwords don't match")
            void shouldThrowValidationExceptionWhenPasswordsDontMatch() {
                RegisterUserRequest request = aRegisterUserRequest()
                    .setPasswordConfirmation("DifferentPassword123!");

                assertThatThrownBy(() -> validator.validateUserRegistration(request))
                    .isInstanceOf(ValidationException.class)
                    .satisfies(thrown -> {
                        ValidationException ex = (ValidationException) thrown;
                        assertThat(ex.getValidationResult().getErrors())
                            .anyMatch(error -> error.getCode().equals(AuthenticationValidator.PASSWORD_DOES_NOT_MATCH_THE_CONFIRMATION));
                    });
            }

            @Test
            @DisplayName("Should throw ValidationException when password is too short")
            void shouldThrowValidationExceptionWhenPasswordIsTooShort() {
                String shortPassword = "Aa1!";
                RegisterUserRequest request = aRegisterUserRequest()
                    .setPassword(shortPassword)
                    .setPasswordConfirmation(shortPassword);

                when(passwordValidator.validatePassword(shortPassword))
                    .thenReturn(new RuleResult(false));

                assertThatThrownBy(() -> validator.validateUserRegistration(request))
                    .isInstanceOf(ValidationException.class)
                    .satisfies(thrown -> {
                        ValidationException ex = (ValidationException) thrown;
                        assertThat(ex.getValidationResult().getErrors())
                            .anyMatch(error -> error.getCode().contains("Password is not strong enough"));
                    });
            }
        }
    }


    @Nested
    @DisplayName("Login Request Validation")
    class LoginValidationTests {

        @Nested
        @DisplayName("Basic Field Validation")
        class BasicFieldValidationTests {

            @Test
            @DisplayName("Should validate successfully when all fields are valid")
            void shouldValidateSuccessfullyWhenAllFieldsAreValid() {
                assertThatNoException()
                    .isThrownBy(() -> validator.validateLoginRequest(aLoginRequest()));
            }

            @Test
            @DisplayName("Should throw ValidationException when request is null")
            void shouldThrowValidationExceptionWhenRequestIsNull() {
                assertThatThrownBy(() -> validator.validateLoginRequest(null))
                    .isInstanceOf(ValidationException.class)
                    .satisfies(thrown -> {
                        ValidationException ex = (ValidationException) thrown;
                        assertThat(ex.getValidationResult().getErrors())
                            .anyMatch(error -> error.getCode().equals(AuthenticationValidator.BODY_IS_MISSING));
                    });
            }
        }


        @Nested
        @DisplayName("Email Validation")
        class EmailValidationTests {

            @Test
            @DisplayName("Should throw ValidationException when email is empty")
            void shouldThrowValidationExceptionWhenEmailIsEmpty() {
                LoginRequest request = aLoginRequest()
                    .setEmail("");

                assertThatThrownBy(() -> validator.validateLoginRequest(request))
                    .isInstanceOf(ValidationException.class)
                    .satisfies(thrown -> {
                        ValidationException ex = (ValidationException) thrown;
                        assertThat(ex.getValidationResult().getErrors())
                            .anyMatch(error -> error.getCode().equals(AuthenticationValidator.EMAIL_MUST_NOT_BE_EMPTY));
                    });
            }

            @Test
            @DisplayName("Should throw ValidationException when email is null")
            void shouldThrowValidationExceptionWhenEmailIsNull() {
                LoginRequest request = aLoginRequest()
                    .setEmail(null);

                assertThatThrownBy(() -> validator.validateLoginRequest(request))
                    .isInstanceOf(ValidationException.class)
                    .satisfies(thrown -> {
                        ValidationException ex = (ValidationException) thrown;
                        assertThat(ex.getValidationResult().getErrors())
                            .anyMatch(error -> error.getCode().equals(AuthenticationValidator.EMAIL_MUST_NOT_BE_EMPTY));
                    });
            }
        }


        @Nested
        @DisplayName("Password Validation")
        class PasswordValidationTests {

            @Test
            @DisplayName("Should throw ValidationException when password is empty")
            void shouldThrowValidationExceptionWhenPasswordIsEmpty() {
                LoginRequest request = aLoginRequest()
                    .setPassword("");

                assertThatThrownBy(() -> validator.validateLoginRequest(request))
                    .isInstanceOf(ValidationException.class)
                    .satisfies(thrown -> {
                        ValidationException ex = (ValidationException) thrown;
                        assertThat(ex.getValidationResult().getErrors())
                            .anyMatch(error -> error.getCode().equals(AuthenticationValidator.PASSWORD_MUST_NOT_BE_EMPTY));
                    });
            }

            @Test
            @DisplayName("Should throw ValidationException when password is null")
            void shouldThrowValidationExceptionWhenPasswordIsNull() {
                LoginRequest request = aLoginRequest()
                    .setPassword(null);

                assertThatThrownBy(() -> validator.validateLoginRequest(request))
                    .isInstanceOf(ValidationException.class)
                    .satisfies(thrown -> {
                        ValidationException ex = (ValidationException) thrown;
                        assertThat(ex.getValidationResult().getErrors())
                            .anyMatch(error -> error.getCode().equals(AuthenticationValidator.PASSWORD_MUST_NOT_BE_EMPTY));
                    });
            }
        }
    }

    private static RegisterUserRequest aRegisterUserRequest() {
        return new RegisterUserRequest()
            .setEmail(VALID_EMAIL)
            .setPassword(VALID_PASSWORD)
            .setPasswordConfirmation(VALID_PASSWORD)
            .setFirstName(VALID_FIRST_NAME)
            .setLastName(VALID_LAST_NAME);
    }

    private static LoginRequest aLoginRequest() {
        return new LoginRequest()
            .setEmail(VALID_EMAIL)
            .setPassword(VALID_PASSWORD);
    }
}
