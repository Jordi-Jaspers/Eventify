package io.github.eventify.api.notification.adapter.model.validator;

import io.github.eventify.api.notification.adapter.model.AdapterType;
import io.github.eventify.api.notification.adapter.model.request.CreateAdapterConfigRequest;
import io.github.jframe.exception.core.ValidationException;
import io.github.jframe.validation.ValidationResult;
import io.github.jframe.validation.Validator;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import static java.util.Objects.isNull;

/** Validator for {@link CreateAdapterConfigRequest}. */
@Component
public class AdapterConfigValidator implements Validator<CreateAdapterConfigRequest> {

    // Field constants
    public static final String FIELD_ADAPTER_TYPE = "adapterType";
    public static final String FIELD_LABEL = "label";
    public static final String FIELD_CONFIG = "config";

    // Message constants
    public static final String ADAPTER_TYPE_REQUIRED = "Adapter type is required";
    public static final String LABEL_REQUIRED = "Label is required";
    public static final String CONFIG_REQUIRED = "Config must not be null";
    public static final String WEBHOOK_URL_REQUIRED = "webhookUrl is required in config for adapter type ";
    public static final String SYSTEM_MANAGED_TYPE =
        "In-app and email adapter configurations are managed by the system and cannot be created manually.";

    private static final List<AdapterType> WEBHOOK_REQUIRED_TYPES = List.of(AdapterType.MATTERMOST, AdapterType.SLACK);
    private static final List<AdapterType> SYSTEM_MANAGED_TYPES = List.of(AdapterType.IN_APP, AdapterType.EMAIL);

    @Override
    public void validate(final CreateAdapterConfigRequest request, final ValidationResult result) {
        if (isNull(request)) {
            result.rejectField(FIELD_ADAPTER_TYPE, (String) null)
                .whenNull(ADAPTER_TYPE_REQUIRED);
            throw new ValidationException(result);
        }

        validateAdapterType(request, result);
        validateLabel(request, result);
        validateConfig(request, result);
        validateWebhookUrl(request, result);
    }

    @Override
    public void validateAndThrow(final CreateAdapterConfigRequest request) {
        final ValidationResult result = new ValidationResult();
        validate(request, result);
    }

    private void validateAdapterType(final CreateAdapterConfigRequest request, final ValidationResult result) {
        result.rejectField(FIELD_ADAPTER_TYPE, request.getAdapterType())
            .whenNull(ADAPTER_TYPE_REQUIRED)
            .orWhen(SYSTEM_MANAGED_TYPES::contains, SYSTEM_MANAGED_TYPE);

        if (result.hasErrors()) {
            throw new ValidationException(result);
        }
    }

    private void validateLabel(final CreateAdapterConfigRequest request, final ValidationResult result) {
        result.rejectField(FIELD_LABEL, request.getLabel())
            .whenNull(LABEL_REQUIRED)
            .orWhen(String::isBlank, LABEL_REQUIRED);

        if (result.hasErrors()) {
            throw new ValidationException(result);
        }
    }

    private void validateConfig(final CreateAdapterConfigRequest request, final ValidationResult result) {
        result.rejectField(FIELD_CONFIG, request.getConfig())
            .whenNull(CONFIG_REQUIRED);

        if (result.hasErrors()) {
            throw new ValidationException(result);
        }
    }

    private void validateWebhookUrl(final CreateAdapterConfigRequest request, final ValidationResult result) {
        if (!WEBHOOK_REQUIRED_TYPES.contains(request.getAdapterType())) {
            return;
        }

        final Map<String, Object> config = request.getConfig();
        final Object webhookUrl = config.get("webhookUrl");
        final boolean webhookMissing = webhookUrl == null || (webhookUrl instanceof String s && s.isBlank());

        if (webhookMissing) {
            result.rejectField(FIELD_CONFIG, (Object) null)
                .whenNull(WEBHOOK_URL_REQUIRED + request.getAdapterType().name());
            throw new ValidationException(result);
        }
    }
}
