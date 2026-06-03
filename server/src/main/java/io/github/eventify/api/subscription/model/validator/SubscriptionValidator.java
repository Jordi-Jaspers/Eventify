package io.github.eventify.api.subscription.model.validator;

import io.github.eventify.api.event.model.Severity;
import io.github.eventify.api.notification.adapter.model.AdapterType;
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
    public static final String TARGET_SEVERITIES_REQUIRED = "At least one severity level must be selected.";
    public static final String TARGET_SEVERITIES_NO_DATA_NOT_ALLOWED = "The 'No Data' severity cannot be used as a notification trigger.";
    public static final String TARGET_SEVERITIES_INVALID = "One or more severity values are not recognized.";
    public static final String ADAPTERS_REQUIRED = "At least one notification channel must be selected.";
    public static final String ADAPTERS_IN_APP_REQUIRED = "In-app notifications must always be enabled.";

    // Fields
    public static final String TARGET_SEVERITIES = "targetSeverities";
    public static final String ADAPTERS = "adapters";

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
        final List<Severity> severities = request.getTargetSeverities();

        if (isNull(severities) || severities.isEmpty()) {
            result.rejectField(TARGET_SEVERITIES, severities)
                .whenNull(TARGET_SEVERITIES_REQUIRED)
                .orWhen(List::isEmpty, TARGET_SEVERITIES_REQUIRED);
            throw new ValidationException(result);
        }

        if (severities.contains(Severity.NO_DATA)) {
            result.rejectField(TARGET_SEVERITIES, Severity.NO_DATA)
                .when(v -> true, TARGET_SEVERITIES_NO_DATA_NOT_ALLOWED);
            throw new ValidationException(result);
        }
    }

    private void validateAdapters(final SubscribeRequest request, final ValidationResult result) {
        final List<AdapterType> adapters = request.getAdapters();

        if (isNull(adapters) || adapters.isEmpty()) {
            result.rejectField(ADAPTERS, adapters)
                .whenNull(ADAPTERS_REQUIRED)
                .orWhen(List::isEmpty, ADAPTERS_REQUIRED);
            throw new ValidationException(result);
        }

        if (!adapters.contains(AdapterType.IN_APP)) {
            result.rejectField(ADAPTERS, "missing-in-app")
                .when(v -> true, ADAPTERS_IN_APP_REQUIRED);
            throw new ValidationException(result);
        }
    }
}
