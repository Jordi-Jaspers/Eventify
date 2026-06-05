package io.github.eventify.api.subscription.model.validator;

import io.github.eventify.api.event.model.Severity;
import io.github.eventify.api.subscription.model.request.CreateSubscriptionRequest;
import io.github.eventify.api.subscription.model.request.UpdateSubscriptionRequest;
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
public class SubscriptionValidator implements Validator<CreateSubscriptionRequest> {

    public static final String TARGET_SEVERITIES = "targetSeverities";
    public static final String ADAPTER_CONFIG_IDS = "adapterConfigIds";

    public static final String TARGET_SEVERITIES_REQUIRED = "At least one severity level must be selected.";
    public static final String TARGET_SEVERITIES_NO_DATA_NOT_ALLOWED = "The 'No Data' severity cannot be used as a notification trigger.";
    public static final String ADAPTER_CONFIG_IDS_REQUIRED = "At least one adapter config is required";
    private static final String REQUEST_BODY_MISSING = "Request body is missing";

    @Override
    public void validate(final CreateSubscriptionRequest request, final ValidationResult result) {
        if (isNull(request)) {
            result.reject(REQUEST_BODY_MISSING);
            throw new ValidationException(result);
        }
        validateCore(request.getTargetSeverities(), request.getAdapterConfigIds(), result);
    }

    /**
     * Validates and throws for update requests.
     *
     * @param request the update request
     */
    public void validateAndThrow(final UpdateSubscriptionRequest request) {
        final ValidationResult result = new ValidationResult();
        if (isNull(request)) {
            result.reject(REQUEST_BODY_MISSING);
            throw new ValidationException(result);
        }
        validateCore(request.getTargetSeverities(), request.getAdapterConfigIds(), result);
    }

    private void validateCore(final List<Severity> severities, final List<Long> adapterConfigIds, final ValidationResult result) {
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
        if (isNull(adapterConfigIds) || adapterConfigIds.isEmpty()) {
            result.rejectField(ADAPTER_CONFIG_IDS, adapterConfigIds)
                .whenNull(ADAPTER_CONFIG_IDS_REQUIRED)
                .orWhen(List::isEmpty, ADAPTER_CONFIG_IDS_REQUIRED);
            throw new ValidationException(result);
        }
    }
}
