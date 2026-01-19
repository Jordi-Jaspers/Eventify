package io.github.eventify.api.event.model.validator;

import io.github.eventify.api.event.model.request.CreateEventRequest;
import io.github.jframe.exception.core.ValidationException;
import io.github.jframe.validation.ValidationResult;
import io.github.jframe.validation.Validator;

import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import static java.util.Objects.isNull;

/**
 * Validator for event requests.
 */
@Component
public class CreateEventValidator implements Validator<CreateEventRequest> {

    // Error messages
    public static final String BODY_IS_MISSING = "Request body is missing";
    public static final String CHANNEL_ID_REQUIRED = "Channel ID is required";
    public static final String SEVERITY_REQUIRED = "Severity is required";
    public static final String TITLE_REQUIRED = "Title is required";
    public static final String TITLE_TOO_LONG = "Title must not exceed 255 characters";
    public static final String MESSAGE_TOO_LARGE = "Message must not exceed 10KB";
    public static final String METADATA_TOO_LARGE = "Metadata must not exceed 10KB";

    // Fields
    public static final String CHANNEL_ID = "channelId";
    public static final String SEVERITY = "severity";
    public static final String TITLE = "title";
    public static final String MESSAGE = "message";
    public static final String METADATA = "metadata";

    private static final int MAX_TITLE_LENGTH = 255;
    /** Maximum size in bytes (10KB). */
    private static final int MAX_BYTES = 10_240;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(final CreateEventRequest request, final ValidationResult result) {
        if (isNull(request)) {
            result.reject(BODY_IS_MISSING);
            throw new ValidationException(result);
        }

        validateChannelId(request, result);
        validateSeverity(request, result);
        validateTitle(request, result);
        validateMessage(request, result);
        validateMetadata(request, result);

        if (result.hasErrors()) {
            throw new ValidationException(result);
        }
    }

    private void validateChannelId(final CreateEventRequest request, final ValidationResult result) {
        result.rejectField(CHANNEL_ID, request.getChannelId())
            .whenNull(CHANNEL_ID_REQUIRED);
    }

    private void validateSeverity(final CreateEventRequest request, final ValidationResult result) {
        result.rejectField(SEVERITY, request.getSeverity())
            .whenNull(SEVERITY_REQUIRED);
    }

    private void validateTitle(final CreateEventRequest request, final ValidationResult result) {
        result.rejectField(TITLE, request.getTitle())
            .whenNull(TITLE_REQUIRED)
            .orWhen(String::isEmpty, TITLE_REQUIRED)
            .orWhen(String::isBlank, TITLE_REQUIRED)
            .orWhen(title -> title.length() > MAX_TITLE_LENGTH, TITLE_TOO_LONG);
    }

    private void validateMessage(final CreateEventRequest request, final ValidationResult result) {
        result.rejectField(MESSAGE, request.getMessage())
            .when(
                message -> message != null && message.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES,
                MESSAGE_TOO_LARGE
            );
    }

    private void validateMetadata(final CreateEventRequest request, final ValidationResult result) {
        if (request.getMetadata() != null) {
            try {
                final String json = objectMapper.writeValueAsString(request.getMetadata());
                final int byteSize = json.getBytes(StandardCharsets.UTF_8).length;
                result.rejectField(METADATA, request.getMetadata())
                    .when(metadata -> byteSize > MAX_BYTES, METADATA_TOO_LARGE);
            } catch (final JsonProcessingException e) {
                result.rejectField(METADATA, request.getMetadata())
                    .when(metadata -> true, "Invalid metadata format");
            }
        }
    }
}
