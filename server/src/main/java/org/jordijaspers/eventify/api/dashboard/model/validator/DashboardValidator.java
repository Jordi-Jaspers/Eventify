package org.jordijaspers.eventify.api.dashboard.model.validator;

import lombok.RequiredArgsConstructor;

import org.hawaiiframework.validation.ValidationException;
import org.hawaiiframework.validation.ValidationResult;
import org.hawaiiframework.validation.Validator;
import org.jordijaspers.eventify.api.dashboard.model.request.CreateDashboardRequest;
import org.jordijaspers.eventify.api.dashboard.model.request.UpdateDashboardDetailsRequest;
import org.springframework.stereotype.Component;

import static java.util.Objects.isNull;

/**
 * A custom dashboard validator.
 */
@Component
@RequiredArgsConstructor
public class DashboardValidator implements Validator<Object> {

    // Error messages
    public static final String BODY_IS_MISSING = "Request body is missing, please provide a request body with the correct configuration";
    public static final String NAME_MUST_NOT_BE_EMPTY = "Name must not be empty";
    public static final String NAME_TOO_LONG = "Name is too long, maximum length is 255 characters";
    public static final String DESCRIPTION_MUST_NOT_BE_EMPTY = "Description must not be empty";
    public static final String DESCRIPTION_TOO_LONG = "Description is too long, maximum length is 500 characters";

    // Fields
    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";
    public static final String GLOBAL = "global";
    public static final String CONFIGURATION = "configuration";

    // Constraints
    public static final int MAX_NAME_LENGTH = 255;
    public static final int MAX_DESCRIPTION_LENGTH = 500;

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
    public void validateCreateDashboardRequest(final CreateDashboardRequest request) {
        final ValidationResult result = new ValidationResult();
        if (isNull(request)) {
            result.reject(BODY_IS_MISSING);
            throw new ValidationException(result);
        }

        result.rejectField(NAME, request.getName())
            .whenNull(NAME_MUST_NOT_BE_EMPTY)
            .orWhen(String::isEmpty, NAME_MUST_NOT_BE_EMPTY);

        result.rejectField(DESCRIPTION, request.getDescription())
            .whenNull(DESCRIPTION_MUST_NOT_BE_EMPTY)
            .orWhen(String::isEmpty, DESCRIPTION_MUST_NOT_BE_EMPTY);

        if (result.hasErrors()) {
            throw new ValidationException(result);
        }

        if (request.getName().length() > MAX_NAME_LENGTH) {
            result.rejectField(NAME, request.getName())
                .when(name -> name.length() > MAX_NAME_LENGTH, NAME_TOO_LONG);

            throw new ValidationException(result);
        }

        if (request.getDescription().length() > MAX_DESCRIPTION_LENGTH) {
            result.rejectField(DESCRIPTION, request.getDescription())
                .when(description -> description.length() > MAX_DESCRIPTION_LENGTH, DESCRIPTION_TOO_LONG);

            throw new ValidationException(result);
        }
    }

    /**
     * Validates the update dashboard details request.
     *
     * @param request The request to validate.
     */
    public void validateUpdateDashboardDetailsRequest(final UpdateDashboardDetailsRequest request) {
        final ValidationResult result = new ValidationResult();
        if (isNull(request)) {
            result.reject(BODY_IS_MISSING);
            throw new ValidationException(result);
        }

        result.rejectField(NAME, request.getName())
            .whenNull(NAME_MUST_NOT_BE_EMPTY)
            .orWhen(String::isEmpty, NAME_MUST_NOT_BE_EMPTY);

        result.rejectField(DESCRIPTION, request.getDescription())
            .whenNull(DESCRIPTION_MUST_NOT_BE_EMPTY)
            .orWhen(String::isEmpty, DESCRIPTION_MUST_NOT_BE_EMPTY);

        if (result.hasErrors()) {
            throw new ValidationException(result);
        }

        if (request.getName().length() > MAX_NAME_LENGTH) {
            result.rejectField(NAME, request.getName())
                .when(name -> name.length() > MAX_NAME_LENGTH, NAME_TOO_LONG);

            throw new ValidationException(result);
        }

        if (request.getDescription().length() > MAX_DESCRIPTION_LENGTH) {
            result.rejectField(DESCRIPTION, request.getDescription())
                .when(description -> description.length() > MAX_DESCRIPTION_LENGTH, DESCRIPTION_TOO_LONG);

            throw new ValidationException(result);
        }
    }
}
