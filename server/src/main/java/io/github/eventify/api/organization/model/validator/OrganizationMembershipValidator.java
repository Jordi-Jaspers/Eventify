package io.github.eventify.api.organization.model.validator;

import io.github.eventify.api.admin.model.request.AssignOwnerRequest;
import io.github.eventify.api.organization.model.OrganizationalRole;
import io.github.eventify.api.organization.model.request.AddMemberRequest;
import io.github.eventify.api.organization.model.request.TransferOwnershipRequest;
import io.github.eventify.api.organization.model.request.UpdateMemberRoleRequest;
import io.github.jframe.exception.core.ValidationException;
import io.github.jframe.validation.ValidationResult;
import io.github.jframe.validation.Validator;

import org.springframework.stereotype.Component;

import static io.github.eventify.common.constant.Constants.Email.OWASP_EMAIL_REGEX;
import static java.util.Objects.isNull;

/**
 * Consolidated validator for organization membership operations.
 * <p>
 * Handles validation for:
 * <ul>
 * <li>Adding members to an organization</li>
 * <li>Updating member roles</li>
 * <li>Transferring ownership</li>
 * <li>Assigning owner (admin operation)</li>
 * </ul>
 */
@Component
public class OrganizationMembershipValidator implements Validator<Object> {

    // Common error messages
    public static final String BODY_IS_MISSING = "Request body is missing, please provide a request body with the correct configuration";

    // Add member error messages
    public static final String EMAIL_REQUIRED = "Email is required";
    public static final String EMAIL_BLANK = "Email cannot be blank";
    public static final String EMAIL_INVALID_FORMAT = "Email must be a valid email address";
    public static final String ROLE_REQUIRED = "Role is required";
    public static final String ROLE_CANNOT_BE_OWNER = "Cannot directly add a member with OWNER role. Use transfer ownership instead";

    // Update role error messages
    public static final String UPDATE_ROLE_CANNOT_BE_OWNER = "Cannot set role to OWNER. Use transfer ownership instead";

    // Transfer ownership error messages
    public static final String NEW_OWNER_USER_ID_REQUIRED = "New owner user ID is required";
    public static final String NEW_OWNER_USER_ID_MUST_BE_POSITIVE = "New owner user ID must be positive";
    public static final String CURRENT_OWNER_USER_ID_REQUIRED = "Current owner user ID is required";
    public static final String CURRENT_OWNER_USER_ID_MUST_BE_POSITIVE = "Current owner user ID must be positive";

    // Assign owner error messages
    public static final String EMAIL_OR_USER_ID_REQUIRED = "Either email or userId must be provided";

    // Field names
    private static final String EMAIL = "email";
    private static final String ROLE = "role";
    private static final String NEW_OWNER_USER_ID = "newOwnerUserId";
    private static final String CURRENT_OWNER_USER_ID = "currentOwnerUserId";

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
     * Validates an add member request.
     *
     * @param request The add member request to validate.
     */
    public void validateAddMember(final AddMemberRequest request) {
        final ValidationResult result = new ValidationResult();

        if (isNull(request)) {
            result.reject(BODY_IS_MISSING);
            throw new ValidationException(result);
        }

        result.rejectField(EMAIL, request.getEmail())
            .whenNull(EMAIL_REQUIRED)
            .orWhen(String::isEmpty, EMAIL_BLANK)
            .orWhen(String::isBlank, EMAIL_BLANK)
            .orWhen(email -> !email.matches(OWASP_EMAIL_REGEX), EMAIL_INVALID_FORMAT);

        result.rejectField(ROLE, request.getRole())
            .whenNull(ROLE_REQUIRED)
            .orWhen(role -> role == OrganizationalRole.OWNER, ROLE_CANNOT_BE_OWNER);

        if (result.hasErrors()) {
            throw new ValidationException(result);
        }
    }

    /**
     * Validates an update member role request.
     *
     * @param request The update role request to validate.
     */
    public void validateUpdateRole(final UpdateMemberRoleRequest request) {
        final ValidationResult result = new ValidationResult();

        if (isNull(request)) {
            result.reject(BODY_IS_MISSING);
            throw new ValidationException(result);
        }

        result.rejectField(ROLE, request.getRole())
            .whenNull(ROLE_REQUIRED)
            .orWhen(role -> role == OrganizationalRole.OWNER, UPDATE_ROLE_CANNOT_BE_OWNER);

        if (result.hasErrors()) {
            throw new ValidationException(result);
        }
    }

    /**
     * Validates a transfer ownership request.
     *
     * @param request The transfer ownership request to validate.
     */
    public void validateTransferOwnership(final TransferOwnershipRequest request) {
        final ValidationResult result = new ValidationResult();

        if (isNull(request)) {
            result.reject(BODY_IS_MISSING);
            throw new ValidationException(result);
        }

        result.rejectField(CURRENT_OWNER_USER_ID, request.getCurrentOwnerUserId())
            .whenNull(CURRENT_OWNER_USER_ID_REQUIRED)
            .orWhen(id -> id <= 0, CURRENT_OWNER_USER_ID_MUST_BE_POSITIVE);

        result.rejectField(NEW_OWNER_USER_ID, request.getNewOwnerUserId())
            .whenNull(NEW_OWNER_USER_ID_REQUIRED)
            .orWhen(id -> id <= 0, NEW_OWNER_USER_ID_MUST_BE_POSITIVE);

        if (result.hasErrors()) {
            throw new ValidationException(result);
        }
    }

    /**
     * Validates an assign owner request (admin operation).
     *
     * @param request The assign owner request to validate.
     */
    public void validateAssignOwner(final AssignOwnerRequest request) {
        final ValidationResult result = new ValidationResult();

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
