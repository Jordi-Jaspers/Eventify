package io.github.eventify.api.channel.model.validator;

import io.github.eventify.api.channel.model.request.ChannelBatchRequest;
import io.github.jframe.exception.core.ValidationException;
import io.github.jframe.validation.ValidationResult;
import io.github.jframe.validation.Validator;

import java.util.List;

import org.springframework.stereotype.Component;

import static java.util.Objects.isNull;

/**
 * Validator for batch channel requests.
 */
@Component
public class ChannelBatchValidator implements Validator<ChannelBatchRequest> {

    public static final String BODY_IS_MISSING = "Request body is missing";
    public static final String CHANNEL_IDS_REQUIRED = "channelIds must not be empty";
    public static final String CHANNEL_IDS = "channelIds";

    @Override
    public void validate(final ChannelBatchRequest request, final ValidationResult result) {
        if (isNull(request)) {
            result.reject(BODY_IS_MISSING);
            throw new ValidationException(result);
        }

        result.rejectField(CHANNEL_IDS, request.getChannelIds())
            .whenNull(CHANNEL_IDS_REQUIRED)
            .orWhen(List::isEmpty, CHANNEL_IDS_REQUIRED);

        if (result.hasErrors()) {
            throw new ValidationException(result);
        }
    }
}
