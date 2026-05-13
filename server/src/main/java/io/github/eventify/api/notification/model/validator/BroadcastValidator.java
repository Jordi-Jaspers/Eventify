package io.github.eventify.api.notification.model.validator;

import io.github.eventify.api.notification.model.request.AudienceRequest;
import io.github.eventify.api.notification.model.request.CreateBroadcastRequest;
import io.github.jframe.exception.core.ValidationException;
import io.github.jframe.validation.ValidationResult;
import io.github.jframe.validation.Validator;

import org.springframework.stereotype.Component;

import static java.util.Objects.isNull;

/**
 * Validator for broadcast-related requests.
 */
@Component
public class BroadcastValidator implements Validator<CreateBroadcastRequest> {

    public static final String TITLE_REQUIRED = "Title is required";
    public static final String TITLE_TOO_LONG = "Title must not exceed 120 characters";
    public static final String MESSAGE_REQUIRED = "Message is required";
    public static final String MESSAGE_TOO_LONG = "Message must not exceed 500 characters";
    public static final String AUDIENCE_TYPE_REQUIRED = "Audience type is required";
    public static final String ACTION_BOTH_OR_NEITHER = "actionUrl and actionLabel must both be provided or both be absent";

    @Override
    public void validate(final CreateBroadcastRequest request, final ValidationResult result) {
        if (isNull(request)) {
            result.reject("Request body is missing");
            throw new ValidationException(result);
        }

        result.rejectField("title", request.getTitle())
            .whenNull(TITLE_REQUIRED)
            .orWhen(String::isEmpty, TITLE_REQUIRED)
            .orWhen(t -> t.length() > 120, TITLE_TOO_LONG);

        result.rejectField("message", request.getMessage())
            .whenNull(MESSAGE_REQUIRED)
            .orWhen(String::isEmpty, MESSAGE_REQUIRED)
            .orWhen(m -> m.length() > 500, MESSAGE_TOO_LONG);

        final AudienceRequest audience = request.getAudience();
        if (isNull(audience) || audience.getType() == null) {
            result.rejectField("audience.type", null).whenNull(AUDIENCE_TYPE_REQUIRED);
        }

        final boolean hasUrl = !isNullOrEmpty(request.getActionUrl());
        final boolean hasLabel = !isNullOrEmpty(request.getActionLabel());
        if (hasUrl != hasLabel) {
            result.reject(ACTION_BOTH_OR_NEITHER);
        }

        if (result.hasErrors()) {
            throw new ValidationException(result);
        }
    }

    /**
     * Validates an audience request (for preview endpoint).
     *
     * @param audienceRequest the audience request
     */
    public void validateAudience(final AudienceRequest audienceRequest) {
        final ValidationResult result = new ValidationResult();

        if (isNull(audienceRequest) || audienceRequest.getType() == null) {
            result.rejectField("type", null).whenNull(AUDIENCE_TYPE_REQUIRED);
        }

        if (result.hasErrors()) {
            throw new ValidationException(result);
        }
    }

    private static boolean isNullOrEmpty(final String value) {
        return isNull(value) || value.isEmpty();
    }
}
