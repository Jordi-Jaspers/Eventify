package io.github.eventify.api.organization.model.validator;

import io.github.eventify.api.organization.model.OrganizationalRole;
import io.github.eventify.api.organization.model.request.AddMemberRequest;
import io.github.jframe.exception.core.ValidationException;
import io.github.jframe.validation.ValidationResult;
import io.github.jframe.validation.Validator;

import org.springframework.stereotype.Component;

import static java.util.Objects.isNull;

/**
 * Validator for add member requests.
 */
@Component
public class AddMemberRequestValidator implements Validator<AddMemberRequest> {

    // Error messages
    public static final String BODY_IS_MISSING = "Request body is missing, please provide a request body with the correct configuration";
    public static final String EMAIL_REQUIRED = "Email is required";
    public static final String EMAIL_BLANK = "Email cannot be blank";
    public static final String EMAIL_INVALID_FORMAT = "Email must be a valid email address";
    public static final String ROLE_REQUIRED = "Role is required";
    public static final String ROLE_CANNOT_BE_OWNER = "Cannot directly add a member with OWNER role. Use transfer ownership instead";

    // Fields
    public static final String EMAIL = "email";
    public static final String ROLE = "role";

    // Constraints
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(final AddMemberRequest request, final ValidationResult result) {
        if (isNull(request)) {
            result.reject(BODY_IS_MISSING);
            throw new ValidationException(result);
        }

        result.rejectField(EMAIL, request.getEmail())
            .whenNull(EMAIL_REQUIRED)
            .orWhen(String::isEmpty, EMAIL_BLANK)
            .orWhen(String::isBlank, EMAIL_BLANK)
            .orWhen(email -> !email.matches(EMAIL_REGEX), EMAIL_INVALID_FORMAT);

        result.rejectField(ROLE, request.getRole())
            .whenNull(ROLE_REQUIRED)
            .orWhen(role -> role == OrganizationalRole.OWNER, ROLE_CANNOT_BE_OWNER);

        if (result.hasErrors()) {
            throw new ValidationException(result);
        }
    }
}
