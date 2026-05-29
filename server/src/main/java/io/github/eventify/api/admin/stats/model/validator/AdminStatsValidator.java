package io.github.eventify.api.admin.stats.model.validator;

import io.github.eventify.api.admin.stats.model.request.AdminStatsRequest;
import io.github.jframe.exception.core.ValidationException;
import io.github.jframe.validation.ValidationResult;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

/** Validates admin stats query parameters. */
@Component
public class AdminStatsValidator {

    public static final String INVALID_DAYS = "days must be between 1 and 365";
    public static final String DATE_RANGE_PARTIAL = "Both startDate and endDate must be provided together";
    public static final String DATE_RANGE_ORDER = "startDate must not be after endDate";
    public static final String DATE_RANGE_FUTURE = "endDate must not be in the future";
    public static final String MUTUAL_EXCLUSIVITY = "Provide either days or startDate+endDate, not both";
    public static final String NO_MODE = "Either days or startDate+endDate must be provided";

    /**
     * Validates an AdminStatsRequest — exactly one mode must be present:
     * either days OR (startDate + endDate).
     */
    public void validateAndThrow(final AdminStatsRequest request) {
        final boolean hasDays = request.getDays() != null;
        final boolean hasStart = request.getStartDate() != null;
        final boolean hasEnd = request.getEndDate() != null;

        if (hasDays && (hasStart || hasEnd)) {
            reject(MUTUAL_EXCLUSIVITY);
        }
        if (!hasDays && !hasStart && !hasEnd) {
            reject(NO_MODE);
        }

        if (hasDays) {
            validateDays(request.getDays());
        } else {
            validateDateRange(hasStart, hasEnd, request.getStartDate(), request.getEndDate());
        }
    }

    private void validateDays(final int days) {
        if (days < 1 || days > 365) {
            reject(INVALID_DAYS);
        }
    }

    private void validateDateRange(final boolean hasStart, final boolean hasEnd,
        final LocalDate startDate, final LocalDate endDate) {
        if (hasStart != hasEnd) {
            reject(DATE_RANGE_PARTIAL);
        }
        final ValidationResult result = new ValidationResult();
        if (endDate.isAfter(LocalDate.now())) {
            result.reject(DATE_RANGE_FUTURE);
        }
        if (startDate.isAfter(endDate)) {
            result.reject(DATE_RANGE_ORDER);
        }
        if (result.hasErrors()) {
            throw new ValidationException(result);
        }
    }

    private void reject(final String message) {
        final ValidationResult result = new ValidationResult();
        result.reject(message);
        throw new ValidationException(result);
    }
}
