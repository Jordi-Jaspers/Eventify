package io.github.eventify.api.organization.model.validator;

import io.github.eventify.api.organization.model.request.ProvisionOrganizationRequest;
import io.github.jframe.exception.core.ValidationException;
import io.github.jframe.validation.ValidationResult;
import io.github.jframe.validation.Validator;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import static java.util.Objects.isNull;

/**
 * Validator for organization provisioning requests.
 */
@Component
@RequiredArgsConstructor
public class OrganizationValidator implements Validator<ProvisionOrganizationRequest> {

    // Error messages
    public static final String BODY_IS_MISSING = "Request body is missing, please provide a request body with the correct configuration";
    public static final String NAME_REQUIRED = "Organization name is required";
    public static final String NAME_BLANK = "Organization name cannot be blank";
    public static final String NAME_TOO_SHORT = "Organization name must be at least 3 characters";
    public static final String NAME_TOO_LONG = "Organization name must not exceed 100 characters";

    // Fields
    public static final String NAME = "name";

    // Constraints
    private static final int MIN_NAME_LENGTH = 3;
    private static final int MAX_NAME_LENGTH = 100;

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(final ProvisionOrganizationRequest request, final ValidationResult result) {
        if (isNull(request)) {
            result.reject(BODY_IS_MISSING);
            throw new ValidationException(result);
        }

        result.rejectField(NAME, request.getName())
            .whenNull(NAME_REQUIRED)
            .orWhen(String::isEmpty, NAME_BLANK)
            .orWhen(String::isBlank, NAME_BLANK)
            .orWhen(name -> name.trim().length() < MIN_NAME_LENGTH, NAME_TOO_SHORT)
            .orWhen(name -> name.trim().length() > MAX_NAME_LENGTH, NAME_TOO_LONG);

        if (result.hasErrors()) {
            throw new ValidationException(result);
        }
    }
}
