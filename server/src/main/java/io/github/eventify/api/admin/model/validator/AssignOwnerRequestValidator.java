package io.github.eventify.api.admin.model.validator;

import io.github.eventify.api.admin.model.request.AssignOwnerRequest;
import io.github.jframe.exception.core.ValidationException;
import io.github.jframe.validation.ValidationResult;
import io.github.jframe.validation.Validator;

import org.springframework.stereotype.Component;

import static io.github.eventify.common.constant.Constants.Email.OWASP_EMAIL_REGEX;
import static java.util.Objects.isNull;

/**
 * Validator for assign owner requests.
 */
@Component
public class AssignOwnerRequestValidator implements Validator<AssignOwnerRequest> {

    // Error messages
    public static final String BODY_IS_MISSING = "Request body is missing, please provide a request body with the correct configuration";
    public static final String EMAIL_OR_USER_ID_REQUIRED = "Either email or userId must be provided";
    public static final String EMAIL_INVALID_FORMAT = "Email must be a valid email address";

    // Fields
    public static final String EMAIL = "email";
    public static final String USER_ID = "userId";

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(final AssignOwnerRequest request, final ValidationResult result) {
        if (isNull(request)) {
            result.reject(BODY_IS_MISSING);
            throw new ValidationException(result);
        }

        // Either email or userId must be provided
        if (isNull(request.getEmail()) && isNull(request.getUserId())) {
            result.reject(EMAIL_OR_USER_ID_REQUIRED);
        }

        // If email is provided, validate format
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            result.rejectField(EMAIL, request.getEmail())
                .when(email -> !email.matches(OWASP_EMAIL_REGEX), EMAIL_INVALID_FORMAT);
        }

        if (result.hasErrors()) {
            throw new ValidationException(result);
        }
    }
}
