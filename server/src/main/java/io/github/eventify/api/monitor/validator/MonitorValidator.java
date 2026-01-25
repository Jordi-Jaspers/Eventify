package io.github.eventify.api.monitor.validator;

import io.github.eventify.api.monitor.model.MonitorFilters;
import io.github.eventify.api.monitor.model.TimeRange;
import io.github.eventify.api.monitor.model.request.MonitorRequest;
import io.github.jframe.exception.core.ValidationException;
import io.github.jframe.validation.ValidationResult;
import io.github.jframe.validation.Validator;

import org.springframework.stereotype.Component;

import static java.util.Objects.isNull;

/**
 * Validator for MonitorRequest.
 */
@Component
public class MonitorValidator implements Validator<MonitorRequest> {

    public static final String WATCHLIST_ID_REQUIRED = "Watchlist ID is required";
    public static final String START_TIME_REQUIRED = "Start time is required when end time is provided";
    public static final String END_TIME_REQUIRED = "End time is required when start time is provided";
    public static final String END_TIME_MUST_BE_AFTER_START = "End time must be after start time";
    public static final String MUTUALLY_EXCLUSIVE = "Cannot provide both preset time range and custom date range";
    public static final String CUSTOM_REQUIRES_DATES = "CUSTOM time range requires both startTime and endTime";
    public static final String PRESET_CANNOT_HAVE_DATES = "Cannot specify both timeRange preset and custom dates";
    public static final String BOTH_DATES_REQUIRED = "Both startTime and endTime are required for custom range";
    public static final String START_MUST_DIFFER = "endTime must be different from startTime";
    public static final String RANGE_EXCEEDS_MAX = "Custom date range cannot exceed 30 days";

    private static final String FIELD_FILTERS = "filters";
    private static final String FIELD_FILTERS_END_TIME = "filters.endTime";
    private static final String FIELD_END_TIME = "endTime";
    private static final String FIELD_START_TIME = "startTime";
    private static final long MAX_RANGE_DAYS = 30;

    @Override
    public void validate(final MonitorRequest request, final ValidationResult result) {
        if (isNull(request)) {
            result.reject("Request body is missing");
            throw new ValidationException(result);
        }

        validateWatchlistId(request, result);
        validateTimeRange(request, result);

        if (result.hasErrors()) {
            throw new ValidationException(result);
        }
    }

    private void validateWatchlistId(final MonitorRequest request, final ValidationResult result) {
        result.rejectField("watchlistId", request.getWatchlistId())
            .whenNull(WATCHLIST_ID_REQUIRED);
    }

    private void validateTimeRange(final MonitorRequest request, final ValidationResult result) {
        final MonitorFilters filters = request.getFilters();
        if (filters == null) {
            return;
        }

        final TimeRange timeRange = filters.getTimeRange();
        final boolean hasCustomDates = filters.getStartTime() != null || filters.getEndTime() != null;

        if (timeRange == TimeRange.CUSTOM) {
            validateCustomTimeRange(filters, result);
        } else if (timeRange != null && hasCustomDates) {
            result.reject(PRESET_CANNOT_HAVE_DATES);
        } else if (hasCustomDates) {
            validateCustomDatesWithoutEnum(filters, result);
        }
    }

    private void validateCustomTimeRange(final MonitorFilters filters, final ValidationResult result) {
        if (filters.getStartTime() == null || filters.getEndTime() == null) {
            result.reject(CUSTOM_REQUIRES_DATES);
        } else {
            validateCustomRange(filters, result);
        }
    }

    private void validateCustomDatesWithoutEnum(final MonitorFilters filters, final ValidationResult result) {
        if (filters.getStartTime() == null || filters.getEndTime() == null) {
            result.reject(BOTH_DATES_REQUIRED);
        } else {
            validateCustomRange(filters, result);
        }
    }

    private void validateCustomRange(final MonitorFilters filters, final ValidationResult result) {
        if (filters.getStartTime().isAfter(filters.getEndTime())) {
            result.reject(END_TIME_MUST_BE_AFTER_START);
        } else if (filters.getStartTime().isEqual(filters.getEndTime())) {
            result.reject(START_MUST_DIFFER);
        } else {
            validateRangeDuration(filters, result);
        }
    }

    private void validateRangeDuration(final MonitorFilters filters, final ValidationResult result) {
        final long daysBetween = java.time.Duration.between(
            filters.getStartTime(),
            filters.getEndTime()
        ).toDays();
        if (daysBetween > MAX_RANGE_DAYS) {
            result.reject(RANGE_EXCEEDS_MAX);
        }
    }
}
