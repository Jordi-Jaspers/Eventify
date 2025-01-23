package org.jordijaspers.eventify.api.event.model.validator;

import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.time.ZonedDateTime;

import org.hawaiiframework.validation.ValidationException;
import org.hawaiiframework.validation.ValidationResult;
import org.hawaiiframework.validation.Validator;
import org.jordijaspers.eventify.api.event.model.request.EventRequest;
import org.springframework.stereotype.Component;

import static java.time.ZoneOffset.UTC;
import static java.util.Objects.isNull;

/**
 * A custom dashboard validator.
 */
@Component
@RequiredArgsConstructor
public class EventValidator implements Validator<EventRequest> {

    // Error messages
    public static final String BODY_IS_MISSING = "Request body is missing, please provide a request body with the correct configuration";
    public static final String CHECK_ID_IS_MISSING = "Check ID is required";
    public static final String STATUS_IS_MISSING = "Status is required";

    public static final String MESSAGE_IS_MISSING = "Message is required";
    public static final String MESSAGE_IS_TOO_LONG = "Message is too long, maximum length is 500 characters";

    public static final String TIMESTAMP_IS_MISSING = "Timestamp is required";
    public static final String TIMESTAMP_IS_IN_THE_FUTURE = "Timestamp is in the future";
    public static final String TIMESTAMP_IS_TOO_OLD = "Timestamp is too old, maximum age is 15 minutes";

    // Fields
    public static final String CHECK_ID = "checkId";
    public static final String TIMESTAMP = "timestamp";
    public static final String STATUS = "status";
    public static final String MESSAGE = "message";
    public static final String CORRELATION_ID = "correlationId";

    // Constraints
    public static final int MAX_MESSAGE_LENGTH = 500;
    public static final Duration MAX_TIMESTAMP_AGE = Duration.ofMinutes(15);

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(final EventRequest request, final ValidationResult result) {
        if (isNull(request)) {
            result.reject(BODY_IS_MISSING);
            throw new ValidationException(result);
        }

        validateRequired(request, result);
        validateTimestamp(request, result);
        validateMessage(request, result);
    }

    private void validateRequired(final EventRequest request, final ValidationResult result) {
        result.rejectField(CHECK_ID, request.getCheckId())
            .whenNull(CHECK_ID_IS_MISSING);

        result.rejectField(STATUS, request.getStatus())
            .whenNull(STATUS_IS_MISSING);

        result.rejectField(MESSAGE, request.getMessage())
            .whenNull(MESSAGE_IS_MISSING)
            .orWhen(String::isBlank, MESSAGE_IS_MISSING);

        result.rejectField(TIMESTAMP, request.getTimestamp())
            .whenNull(TIMESTAMP_IS_MISSING);

        if (result.hasErrors()) {
            throw new ValidationException(result);
        }
    }

    private void validateTimestamp(final EventRequest request, final ValidationResult result) {
        final ZonedDateTime now = ZonedDateTime.now(UTC);
        if (request.getTimestamp().isAfter(now)) {
            result.rejectField(TIMESTAMP, request.getTimestamp())
                .when(timestamp -> timestamp.isAfter(now), TIMESTAMP_IS_IN_THE_FUTURE);
        }

        if (request.getTimestamp().isBefore(now.minus(MAX_TIMESTAMP_AGE))) {
            result.rejectField(TIMESTAMP, request.getTimestamp())
                .when(timestamp -> timestamp.isBefore(now.minus(MAX_TIMESTAMP_AGE)), TIMESTAMP_IS_TOO_OLD);
        }

        if (result.hasErrors()) {
            throw new ValidationException(result);
        }
    }

    private void validateMessage(final EventRequest request, final ValidationResult result) {
        if (request.getMessage().length() > MAX_MESSAGE_LENGTH) {
            result.rejectField(MESSAGE, request.getMessage())
                .when(message -> message.length() > MAX_MESSAGE_LENGTH, MESSAGE_IS_TOO_LONG);
        }

        if (result.hasErrors()) {
            throw new ValidationException(result);
        }
    }
}
