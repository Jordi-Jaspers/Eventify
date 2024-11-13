package org.jordijaspers.eventify.api.user.validator;

import lombok.RequiredArgsConstructor;
import org.hawaiiframework.validation.ValidationException;
import org.hawaiiframework.validation.ValidationResult;
import org.hawaiiframework.validation.Validator;
import org.jordijaspers.eventify.api.authentication.validator.CustomPasswordValidator;
import org.jordijaspers.eventify.api.user.model.request.PasswordRequest;
import org.passay.RuleResult;
import org.springframework.stereotype.Component;

import static java.util.Objects.isNull;

/**
 * {@inheritDoc}.
 */
@Component
@RequiredArgsConstructor
public class ChangePasswordValidator implements Validator<PasswordRequest> {

    // Error messages
    private static final String BODY_IS_MISSING = "Request body is missing, please provide a request body with the correct configuration";
    private static final String PASSWORD_MUST_NOT_BE_EMPTY = "password cannot not be empty";
    private static final String PASSWORD_DOES_NOT_MATCH_THE_CONFIRMATION = "Password does not match the confirmation";
    private static final String PASSWORD_IS_NOT_STRONG_ENOUGH = "Password is not strong enough";

    // Fields
    private static final String PASSWORD = "password";
    private static final String NEW_PASSWORD = "newPassword";
    private static final String CONFIRM_PASSWORD = "confirmPassword";

    // Constraints
    private final CustomPasswordValidator passwordValidator;

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(final PasswordRequest request, final ValidationResult result) {
        if (isNull(request)) {
            result.reject(BODY_IS_MISSING);
            throw new ValidationException(result);
        }

        result.rejectField(NEW_PASSWORD, request.getNewPassword())
            .whenNull(PASSWORD_MUST_NOT_BE_EMPTY)
            .orWhen(String::isEmpty, PASSWORD_MUST_NOT_BE_EMPTY);

        result.rejectField(CONFIRM_PASSWORD, request.getConfirmPassword())
            .whenNull(PASSWORD_DOES_NOT_MATCH_THE_CONFIRMATION)
            .orWhen(String::isEmpty, PASSWORD_DOES_NOT_MATCH_THE_CONFIRMATION)
            .orWhen(confirmation -> !confirmation.equals(request.getNewPassword()), PASSWORD_DOES_NOT_MATCH_THE_CONFIRMATION);

        if (result.hasErrors()) {
            throw new ValidationException(result);
        }

        final RuleResult passwordValidationResult = passwordValidator.validatePassword(request.getNewPassword());
        result.rejectField(PASSWORD, request.getNewPassword())
            .when(password -> !passwordValidationResult.isValid(), PASSWORD_IS_NOT_STRONG_ENOUGH);

        if (result.hasErrors()) {
            throw new ValidationException(result);
        }
    }
}
