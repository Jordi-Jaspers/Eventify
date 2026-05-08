package io.github.eventify.api.user.model.validator;

import io.github.eventify.api.user.model.request.UpdateRetentionRequest;
import io.github.jframe.exception.core.ValidationException;
import io.github.jframe.validation.ValidationResult;
import io.github.jframe.validation.Validator;

import java.util.Set;

import org.springframework.stereotype.Component;

import static java.util.Objects.isNull;

/**
 * Validator for retention settings requests.
 */
@Component
public class RetentionValidator implements Validator<UpdateRetentionRequest> {

    // Error messages
    public static final String RETENTION_DAYS_INVALID = "Retention days must be one of: 90, 180, 365, 730, 1095, 1825";
    public static final String RETENTION_DAYS_REQUIRED = "Retention days is required";

    // Fields
    public static final String RETENTION_DAYS = "retentionDays";

    // Allowed values
    private static final Set<Integer> ALLOWED_VALUES = Set.of(90, 180, 365, 730, 1095, 1825);

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(final UpdateRetentionRequest request, final ValidationResult result) {
        if (isNull(request)) {
            result.reject("Request body is missing");
            throw new ValidationException(result);
        }

        result.rejectField(RETENTION_DAYS, request.getRetentionDays())
            .whenNull(RETENTION_DAYS_REQUIRED)
            .orWhen(days -> !ALLOWED_VALUES.contains(days), RETENTION_DAYS_INVALID);

        if (result.hasErrors()) {
            throw new ValidationException(result);
        }
    }
}
