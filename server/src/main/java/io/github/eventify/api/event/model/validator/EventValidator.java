package io.github.eventify.api.event.model.validator;

import io.github.eventify.api.event.model.request.BatchEventRequest;
import io.github.eventify.api.event.model.request.CreateEventRequest;
import io.github.jframe.exception.core.ValidationException;
import io.github.jframe.validation.ValidationResult;
import io.github.jframe.validation.Validator;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import static java.util.Objects.isNull;

/**
 * Validator for event requests (single and batch).
 * Handles data validation only - security checks are done via @PreAuthorize.
 */
@Component
public class EventValidator implements Validator<CreateEventRequest> {

    // Error messages - shared
    public static final String BODY_IS_MISSING = "Request body is missing";
    public static final String CHANNEL_ID_REQUIRED = "Channel ID is required";
    public static final String SEVERITY_REQUIRED = "Severity is required";
    public static final String TITLE_REQUIRED = "Title is required";
    public static final String TITLE_TOO_LONG = "Title must not exceed 255 characters";
    public static final String MESSAGE_TOO_LARGE = "Message must not exceed 10KB";
    public static final String METADATA_TOO_LARGE = "Metadata must not exceed 10KB";
    public static final String METADATA_INVALID = "Invalid metadata format";

    // Error messages - batch specific
    public static final String BATCH_EMPTY_MESSAGE = "Batch must contain at least one event";
    public static final String BATCH_TOO_LARGE_MESSAGE = "Batch size exceeds maximum of 100 events";
    public static final String TIMESTAMP_REQUIRED_MESSAGE = "Timestamp is required for batch events";
    public static final String TIMESTAMP_IN_FUTURE_MESSAGE = "Event timestamp cannot be in the future";

    // Fields
    public static final String CHANNEL_ID = "channelId";
    public static final String SEVERITY = "severity";
    public static final String TITLE = "title";
    public static final String MESSAGE = "message";
    public static final String METADATA = "metadata";
    public static final String TIMESTAMP_FIELD = "timestamp";

    // Constants
    private static final int MAX_TITLE_LENGTH = 255;
    private static final int MAX_BYTES = 10_240;
    private static final int MAX_BATCH_SIZE = 100;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ========== Single Event Validation (implements Validator interface) ==========

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(final CreateEventRequest request, final ValidationResult result) {
        if (isNull(request)) {
            result.reject(BODY_IS_MISSING);
            throw new ValidationException(result);
        }

        validateEventFields(request, "", result);

        if (result.hasErrors()) {
            throw new ValidationException(result);
        }
    }

    // ========== Batch Validation ==========

    /**
     * Validates a batch event request without throwing.
     * Accumulates errors in the result.
     *
     * @param request the batch event request
     * @param result  the validation result to accumulate errors
     */
    public void validate(final BatchEventRequest request, final ValidationResult result) {
        final String validationError = getBasicBatchValidationError(request);
        if (validationError != null) {
            result.reject(validationError);
        } else {
            validateBatchEvents(request.getEvents(), result);
        }
    }

    private String getBasicBatchValidationError(final BatchEventRequest request) {
        if (isNull(request) || isNull(request.getEvents()) || request.getEvents().isEmpty()) {
            return BATCH_EMPTY_MESSAGE;
        }
        return request.getEvents().size() > MAX_BATCH_SIZE ? BATCH_TOO_LARGE_MESSAGE : null;
    }

    /**
     * Validates a batch event request and throws if errors.
     *
     * @param request the batch event request
     * @throws ValidationException if validation fails
     */
    public void validateAndThrow(final BatchEventRequest request) {
        final ValidationResult result = new ValidationResult();
        validate(request, result);

        if (result.hasErrors()) {
            throw new ValidationException(result);
        }
    }

    /**
     * Validates a batch event request and throws if errors.
     *
     * @param request the batch event request
     * @param result  the validation result
     * @throws ValidationException if validation fails
     */
    public void validateAndThrow(final BatchEventRequest request, final ValidationResult result) {
        validate(request, result);

        if (result.hasErrors()) {
            throw new ValidationException(result);
        }
    }

    // ========== Private Validation Logic ==========

    private void validateEventFields(final CreateEventRequest request, final String fieldPrefix,
        final ValidationResult result) {

        result.rejectField(fieldPrefix + CHANNEL_ID, request.getChannelId())
            .whenNull(CHANNEL_ID_REQUIRED);

        result.rejectField(fieldPrefix + SEVERITY, request.getSeverity())
            .whenNull(SEVERITY_REQUIRED);

        result.rejectField(fieldPrefix + TITLE, request.getTitle())
            .whenNull(TITLE_REQUIRED)
            .orWhen(String::isEmpty, TITLE_REQUIRED)
            .orWhen(String::isBlank, TITLE_REQUIRED)
            .orWhen(title -> title.length() > MAX_TITLE_LENGTH, TITLE_TOO_LONG);

        result.rejectField(fieldPrefix + MESSAGE, request.getMessage())
            .when(this::exceedsMaxBytes, MESSAGE_TOO_LARGE);

        validateMetadata(request.getMetadata(), fieldPrefix, result);
    }

    private boolean exceedsMaxBytes(final String value) {
        return value != null && value.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES;
    }

    private void validateMetadata(final Map<String, Object> metadata, final String fieldPrefix,
        final ValidationResult result) {
        if (metadata == null) {
            return;
        }

        try {
            final String json = objectMapper.writeValueAsString(metadata);
            if (json.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES) {
                result.rejectField(fieldPrefix + METADATA, metadata)
                    .when(m -> true, METADATA_TOO_LARGE);
            }
        } catch (final JsonProcessingException e) {
            result.rejectField(fieldPrefix + METADATA, metadata)
                .when(m -> true, METADATA_INVALID);
        }
    }

    private void validateBatchEvents(final List<CreateEventRequest> events, final ValidationResult result) {
        final OffsetDateTime now = OffsetDateTime.now();

        IntStream.range(0, events.size())
            .forEach(index -> validateBatchEvent(events.get(index), index, now, result));
    }

    private void validateBatchEvent(final CreateEventRequest event, final int index,
        final OffsetDateTime now, final ValidationResult result) {
        final String prefix = formatFieldPrefix(index);

        validateEventFields(event, prefix, result);
        validateBatchTimestamp(event, prefix, now, result);
    }

    private void validateBatchTimestamp(final CreateEventRequest event, final String prefix,
        final OffsetDateTime now, final ValidationResult result) {

        result.rejectField(prefix + TIMESTAMP_FIELD, event.getTimestamp())
            .whenNull(TIMESTAMP_REQUIRED_MESSAGE);

        if (event.getTimestamp() != null && event.getTimestamp().isAfter(now)) {
            result.rejectField(prefix + TIMESTAMP_FIELD, event.getTimestamp())
                .when(ts -> true, TIMESTAMP_IN_FUTURE_MESSAGE);
        }
    }

    private String formatFieldPrefix(final int index) {
        return "events[" + index + "].";
    }
}
