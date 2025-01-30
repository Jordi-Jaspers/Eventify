package org.jordijaspers.eventify.api.monitoring.model.validator;

import lombok.RequiredArgsConstructor;

import java.time.Duration;

import org.hawaiiframework.validation.ValidationResult;
import org.hawaiiframework.validation.Validator;
import org.springframework.stereotype.Component;


/**
 * A custom dashboard validator.
 */
@Component
@RequiredArgsConstructor
public class WindowValidator implements Validator<Duration> {

    // Error messages
    public static final String WINDOW_MUST_BE_IN_HOURS = "Window must be specified in hours (e.g. 'PT24H')";
    public static final String WINDOW_MUST_BE_AT_LEAST_ONE_HOUR = "Window must be at least 1 hour";
    public static final String WINDOW_MUST_BE_VALID_DURATION = "Window must be a valid duration";

    // Field names
    public static final String WINDOW = "window";

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(final Duration window, final ValidationResult result) {
        result.rejectField(WINDOW, window)
            .when(w -> !w.toString().endsWith("H"), WINDOW_MUST_BE_IN_HOURS)
            .when(w -> !w.toString().startsWith("PT"), WINDOW_MUST_BE_VALID_DURATION)
            .when(w -> w.toHours() < 1, WINDOW_MUST_BE_AT_LEAST_ONE_HOUR);
    }
}
