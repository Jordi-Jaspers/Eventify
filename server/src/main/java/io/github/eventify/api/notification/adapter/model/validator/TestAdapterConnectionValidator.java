package io.github.eventify.api.notification.adapter.model.validator;

import io.github.eventify.api.notification.adapter.model.request.TestAdapterConnectionRequest;
import io.github.jframe.exception.core.ValidationException;
import io.github.jframe.validation.ValidationResult;
import io.github.jframe.validation.Validator;

import org.springframework.stereotype.Component;

/**
 * Validator for TestAdapterConnectionRequest.
 */
@Component
public class TestAdapterConnectionValidator implements Validator<TestAdapterConnectionRequest> {

    public static final String ADAPTER_TYPE_REQUIRED = "Adapter type is required.";
    public static final String WEBHOOK_URL_REQUIRED = "Webhook URL is required.";

    public static final String FIELD_ADAPTER_TYPE = "adapterType";
    public static final String FIELD_WEBHOOK_URL = "webhookUrl";

    @Override
    public void validate(final TestAdapterConnectionRequest request, final ValidationResult result) {
        result.rejectField(FIELD_ADAPTER_TYPE, request.getAdapterType())
            .whenNull(ADAPTER_TYPE_REQUIRED);

        result.rejectField(FIELD_WEBHOOK_URL, request.getWebhookUrl())
            .whenNull(WEBHOOK_URL_REQUIRED)
            .orWhen(String::isBlank, WEBHOOK_URL_REQUIRED);
    }

    @Override
    public void validateAndThrow(final TestAdapterConnectionRequest request) {
        final ValidationResult result = new ValidationResult();
        validate(request, result);
        if (result.hasErrors()) {
            throw new ValidationException(result);
        }
    }
}
