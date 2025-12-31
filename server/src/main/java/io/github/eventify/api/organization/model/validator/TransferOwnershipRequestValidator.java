package io.github.eventify.api.organization.model.validator;

import io.github.eventify.api.organization.model.request.TransferOwnershipRequest;
import io.github.jframe.exception.core.ValidationException;
import io.github.jframe.validation.ValidationResult;
import io.github.jframe.validation.Validator;

import org.springframework.stereotype.Component;

import static java.util.Objects.isNull;

/**
 * Validator for transfer ownership requests.
 */
@Component
public class TransferOwnershipRequestValidator implements Validator<TransferOwnershipRequest> {

    // Error messages
    public static final String BODY_IS_MISSING = "Request body is missing, please provide a request body with the correct configuration";
    public static final String NEW_OWNER_USER_ID_REQUIRED = "New owner user ID is required";
    public static final String NEW_OWNER_USER_ID_MUST_BE_POSITIVE = "New owner user ID must be positive";

    // Fields
    public static final String NEW_OWNER_USER_ID = "newOwnerUserId";

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(final TransferOwnershipRequest request, final ValidationResult result) {
        if (isNull(request)) {
            result.reject(BODY_IS_MISSING);
            throw new ValidationException(result);
        }

        result.rejectField(NEW_OWNER_USER_ID, request.getNewOwnerUserId())
            .whenNull(NEW_OWNER_USER_ID_REQUIRED)
            .orWhen(id -> id <= 0, NEW_OWNER_USER_ID_MUST_BE_POSITIVE);

        if (result.hasErrors()) {
            throw new ValidationException(result);
        }
    }
}
