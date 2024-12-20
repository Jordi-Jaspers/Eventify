package org.jordijaspers.eventify.api.authentication.model.validator;

import lombok.RequiredArgsConstructor;
import org.hawaiiframework.validation.ValidationException;
import org.hawaiiframework.validation.ValidationResult;
import org.hawaiiframework.validation.Validator;
import org.jordijaspers.eventify.api.authentication.model.request.LoginRequest;
import org.jordijaspers.eventify.api.authentication.model.request.RegisterUserRequest;
import org.passay.RuleResult;
import org.springframework.stereotype.Component;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static org.jordijaspers.eventify.common.constants.Constants.Email.OWASP_EMAIL_REGEX;

/**
 * {@inheritDoc}.
 */
@Component
@RequiredArgsConstructor
public class AuthenticationValidator implements Validator<Object> {

    // Error messages
    private static final String BODY_IS_MISSING = "Request body is missing, please provide a request body with the correct configuration";
    private static final String EMAIL_MUST_NOT_BE_EMPTY = "email must not be empty";
    private static final String EMAIL_MUST_CONTAIN_AN_AT = "email must contain an @";
    private static final String EMAIL_MUST_BE_A_VALID_EMAIL_ADDRESS = "email is not a valid email address";
    private static final String PASSWORD_MUST_NOT_BE_EMPTY = "password cannot be empty";
    private static final String PASSWORD_DOES_NOT_MATCH_THE_CONFIRMATION = "Password does not match the confirmation";

    // Fields
    private static final String EMAIL = "email";
    private static final String PASSWORD = "password";
    private static final String PASSWORD_CONFIRMATION = "passwordConfirmation";

    // Constraints
    private final CustomPasswordValidator passwordValidator;

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(final Object input, final ValidationResult result) {
        if (isNull(input)) {
            result.reject(BODY_IS_MISSING);
            throw new ValidationException(result);
        }
    }

    /**
     * Validates the user registration request.
     *
     * @param request The request to validate.
     */
    public void validateUserRegistration(final RegisterUserRequest request) {
        final ValidationResult result = new ValidationResult();
        if (isNull(request)) {
            result.reject(BODY_IS_MISSING);
            throw new ValidationException(result);
        }

        result.rejectField(EMAIL, request.getEmail())
            .whenNull(EMAIL_MUST_NOT_BE_EMPTY)
            .orWhen(String::isEmpty, EMAIL_MUST_NOT_BE_EMPTY)
            .orWhen(email -> !email.contains("@"), EMAIL_MUST_CONTAIN_AN_AT)
            .orWhen(email -> !email.matches(OWASP_EMAIL_REGEX), EMAIL_MUST_BE_A_VALID_EMAIL_ADDRESS);

        result.rejectField(PASSWORD, request.getPassword())
            .whenNull(PASSWORD_MUST_NOT_BE_EMPTY)
            .orWhen(String::isEmpty, PASSWORD_MUST_NOT_BE_EMPTY);

        result.rejectField(PASSWORD_CONFIRMATION, request.getPasswordConfirmation())
            .whenNull(PASSWORD_DOES_NOT_MATCH_THE_CONFIRMATION)
            .orWhen(String::isEmpty, PASSWORD_DOES_NOT_MATCH_THE_CONFIRMATION)
            .orWhen(confirmation -> !confirmation.equals(request.getPassword()), PASSWORD_DOES_NOT_MATCH_THE_CONFIRMATION);

        if (result.hasErrors()) {
            throw new ValidationException(result);
        }

        final RuleResult passwordValidationResult = passwordValidator.validatePassword(request.getPassword());
        result.rejectField(PASSWORD, request.getPassword())
            .when(
                password -> !passwordValidationResult.isValid(),
                format("Password is not strong enough: %s", passwordValidationResult.getDetails())
            );

        if (result.hasErrors()) {
            throw new ValidationException(result);
        }
    }

    /**
     * Validates the login request.
     *
     * @param request The request to validate.
     */
    public void validateLoginRequest(final LoginRequest request) {
        final ValidationResult result = new ValidationResult();
        if (isNull(request)) {
            result.reject(BODY_IS_MISSING);
            throw new ValidationException(result);
        }

        result.rejectField(EMAIL, request.getEmail())
            .whenNull(EMAIL_MUST_NOT_BE_EMPTY)
            .orWhen(String::isEmpty, EMAIL_MUST_NOT_BE_EMPTY);

        result.rejectField(PASSWORD, request.getPassword())
            .whenNull(PASSWORD_MUST_NOT_BE_EMPTY)
            .orWhen(String::isEmpty, PASSWORD_MUST_NOT_BE_EMPTY);

        if (result.hasErrors()) {
            throw new ValidationException(result);
        }
    }
}
