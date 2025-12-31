package io.github.eventify.api.organization.model.validator;

import io.github.eventify.api.organization.model.OrganizationalRole;
import io.github.eventify.api.organization.model.request.UpdateMemberRoleRequest;
import io.github.jframe.exception.core.ValidationException;
import io.github.jframe.validation.ValidationResult;
import io.github.jframe.validation.Validator;

import org.springframework.stereotype.Component;

import static java.util.Objects.isNull;

/**
 * Validator for update member role requests.
 */
@Component
public class UpdateMemberRoleRequestValidator implements Validator<UpdateMemberRoleRequest> {

    // Error messages
    public static final String BODY_IS_MISSING = "Request body is missing, please provide a request body with the correct configuration";
    public static final String ROLE_REQUIRED = "Role is required";
    public static final String ROLE_CANNOT_BE_OWNER = "Cannot set role to OWNER. Use transfer ownership instead";

    // Fields
    public static final String ROLE = "role";

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(final UpdateMemberRoleRequest request, final ValidationResult result) {
        if (isNull(request)) {
            result.reject(BODY_IS_MISSING);
            throw new ValidationException(result);
        }

        result.rejectField(ROLE, request.getRole())
            .whenNull(ROLE_REQUIRED)
            .orWhen(role -> role == OrganizationalRole.OWNER, ROLE_CANNOT_BE_OWNER);

        if (result.hasErrors()) {
            throw new ValidationException(result);
        }
    }
}
