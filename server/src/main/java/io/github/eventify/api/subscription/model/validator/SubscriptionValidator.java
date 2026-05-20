package io.github.eventify.api.subscription.model.validator;

import io.github.eventify.api.subscription.model.request.SubscribeRequest;
import io.github.jframe.exception.core.ValidationException;
import io.github.jframe.validation.ValidationResult;
import io.github.jframe.validation.Validator;

import java.util.List;

import org.springframework.stereotype.Component;

import static java.util.Objects.isNull;

/**
 * Validator for subscription requests.
 */
@Component
public class SubscriptionValidator implements Validator<SubscribeRequest> {

    // Error messages
    public static final String TARGET_SEVERITIES_REQUIRED = "Target severities are required";
    public static final String TARGET_SEVERITIES_NO_DATA_NOT_ALLOWED = "NO_DATA is not allowed in target severities";
    public static final String TARGET_SEVERITIES_INVALID = "Invalid severity value";
    public static final String ADAPTERS_REQUIRED = "Adapters are required";
    public static final String ADAPTERS_IN_APP_REQUIRED = "IN_APP adapter is required";

    // Fields
    public static final String TARGET_SEVERITIES = "targetSeverities";
    public static final String ADAPTERS = "adapters";

    // Constants
    private static final String NO_DATA = "NO_DATA";
    private static final List<String> ALLOWED_SEVERITIES = List.of("CRITICAL", "WARNING", "OK");

    @Override
    public void validate(final SubscribeRequest request, final ValidationResult result) {
        if (isNull(request)) {
            result.reject("Request body is missing");
            throw new ValidationException(result);
        }

        validateTargetSeverities(request, result);
        validateAdapters(request, result);

        if (result.hasErrors()) {
            throw new ValidationException(result);
        }
    }

    private void validateTargetSeverities(final SubscribeRequest request, final ValidationResult result) {
        final List<String> severities = request.getTargetSeverities();

        if (isNull(severities) || severities.isEmpty()) {
            result.rejectField(TARGET_SEVERITIES, severities)
                .whenNull(TARGET_SEVERITIES_REQUIRED)
                .orWhen(List::isEmpty, TARGET_SEVERITIES_REQUIRED);
            throw new ValidationException(result);
        }

        if (severities.contains(NO_DATA)) {
            result.rejectField(TARGET_SEVERITIES, NO_DATA)
                .when(v -> true, TARGET_SEVERITIES_NO_DATA_NOT_ALLOWED);
            throw new ValidationException(result);
        }

        final boolean hasInvalid = severities.stream().anyMatch(s -> !ALLOWED_SEVERITIES.contains(s));
        if (hasInvalid) {
            result.rejectField(TARGET_SEVERITIES, "invalid")
                .when(v -> true, TARGET_SEVERITIES_INVALID);
            throw new ValidationException(result);
        }
    }

    private void validateAdapters(final SubscribeRequest request, final ValidationResult result) {
        final List<String> adapters = request.getAdapters();

        if (isNull(adapters) || adapters.isEmpty()) {
            result.rejectField(ADAPTERS, adapters)
                .whenNull(ADAPTERS_REQUIRED)
                .orWhen(List::isEmpty, ADAPTERS_REQUIRED);
            throw new ValidationException(result);
        }

        if (!adapters.contains("IN_APP")) {
            result.rejectField(ADAPTERS, "missing-in-app")
                .when(v -> true, ADAPTERS_IN_APP_REQUIRED);
            throw new ValidationException(result);
        }
    }
}
