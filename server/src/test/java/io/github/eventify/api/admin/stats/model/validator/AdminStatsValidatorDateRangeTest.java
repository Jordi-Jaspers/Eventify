package io.github.eventify.api.admin.stats.model.validator;

import io.github.eventify.common.model.request.StatsRequest;
import io.github.eventify.common.model.validator.StatsRequestValidator;
import io.github.eventify.support.UnitTest;
import io.github.jframe.exception.core.ValidationException;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Unit Test - Admin Stats Validator (Date Range)")
public class AdminStatsValidatorDateRangeTest extends UnitTest {

    private StatsRequestValidator validator;

    @BeforeEach
    public void setUp() {
        validator = new StatsRequestValidator();
    }

    // ==================== Valid: days-only mode ====================

    @Test
    @DisplayName("Should pass when only days is provided")
    public void shouldPassWhenOnlyDaysProvided() {
        // Given: a request with only days set
        final StatsRequest request = new StatsRequest()
            .setDays(30);

        // When / Then: no exception thrown
        assertDoesNotThrow(() -> validator.validateAndThrow(request));
    }

    @Test
    @DisplayName("Should pass when days is 1 (lower boundary)")
    public void shouldPassWhenDaysIsLowerBoundary() {
        // Given: days = 1
        final StatsRequest request = new StatsRequest()
            .setDays(1);

        // When / Then: no exception thrown
        assertDoesNotThrow(() -> validator.validateAndThrow(request));
    }

    @Test
    @DisplayName("Should pass when days is 365 (upper boundary)")
    public void shouldPassWhenDaysIsUpperBoundary() {
        // Given: days = 365
        final StatsRequest request = new StatsRequest()
            .setDays(365);

        // When / Then: no exception thrown
        assertDoesNotThrow(() -> validator.validateAndThrow(request));
    }

    // ==================== Valid: date range mode ====================

    @Test
    @DisplayName("Should pass when valid startDate and endDate provided without days")
    public void shouldPassWhenValidDateRangeProvided() {
        // Given: a valid date range request
        final StatsRequest request = new StatsRequest()
            .setStartDate(LocalDate.now().minusDays(30))
            .setEndDate(LocalDate.now());

        // When / Then: no exception thrown
        assertDoesNotThrow(() -> validator.validateAndThrow(request));
    }

    @Test
    @DisplayName("Should pass when startDate equals endDate (single day range)")
    public void shouldPassWhenStartDateEqualsEndDate() {
        // Given: startDate == endDate
        final LocalDate sameDay = LocalDate.now().minusDays(1);
        final StatsRequest request = new StatsRequest()
            .setStartDate(sameDay)
            .setEndDate(sameDay);

        // When / Then: no exception thrown
        assertDoesNotThrow(() -> validator.validateAndThrow(request));
    }

    @Test
    @DisplayName("Should pass when endDate is today")
    public void shouldPassWhenEndDateIsToday() {
        // Given: endDate is exactly today
        final StatsRequest request = new StatsRequest()
            .setStartDate(LocalDate.now().minusDays(7))
            .setEndDate(LocalDate.now());

        // When / Then: no exception thrown
        assertDoesNotThrow(() -> validator.validateAndThrow(request));
    }

    @Test
    @DisplayName("Should pass when startDate is in the far past")
    public void shouldPassWhenStartDateIsInFarPast() {
        // Given: startDate is years ago
        final StatsRequest request = new StatsRequest()
            .setStartDate(LocalDate.of(2000, 1, 1))
            .setEndDate(LocalDate.now());

        // When / Then: no exception thrown
        assertDoesNotThrow(() -> validator.validateAndThrow(request));
    }

    // ==================== Invalid: mutual exclusivity ====================

    @Test
    @DisplayName("Should reject when both days and date range are provided")
    public void shouldRejectWhenBothDaysAndDateRangeProvided() {
        // Given: both modes supplied
        final StatsRequest request = new StatsRequest()
            .setDays(30)
            .setStartDate(LocalDate.now().minusDays(10))
            .setEndDate(LocalDate.now());

        // When / Then: validation exception thrown
        assertThrows(ValidationException.class, () -> validator.validateAndThrow(request));
    }

    @Test
    @DisplayName("Should reject when neither days nor date range are provided")
    public void shouldRejectWhenNeitherModeProvided() {
        // Given: empty request
        final StatsRequest request = new StatsRequest();

        // When / Then: validation exception thrown
        assertThrows(ValidationException.class, () -> validator.validateAndThrow(request));
    }

    // ==================== Invalid: partial date range ====================

    @Test
    @DisplayName("Should reject when only startDate is provided without endDate")
    public void shouldRejectWhenOnlyStartDateProvided() {
        // Given: startDate only, no days, no endDate
        final StatsRequest request = new StatsRequest()
            .setStartDate(LocalDate.now().minusDays(7));

        // When / Then: validation exception thrown
        assertThrows(ValidationException.class, () -> validator.validateAndThrow(request));
    }

    @Test
    @DisplayName("Should reject when only endDate is provided without startDate")
    public void shouldRejectWhenOnlyEndDateProvided() {
        // Given: endDate only, no days, no startDate
        final StatsRequest request = new StatsRequest()
            .setEndDate(LocalDate.now());

        // When / Then: validation exception thrown
        assertThrows(ValidationException.class, () -> validator.validateAndThrow(request));
    }

    // ==================== Invalid: date range ordering and future ====================

    @Test
    @DisplayName("Should reject when startDate is after endDate")
    public void shouldRejectWhenStartDateIsAfterEndDate() {
        // Given: inverted date range
        final StatsRequest request = new StatsRequest()
            .setStartDate(LocalDate.now())
            .setEndDate(LocalDate.now().minusDays(1));

        // When / Then: validation exception thrown
        assertThrows(ValidationException.class, () -> validator.validateAndThrow(request));
    }

    @Test
    @DisplayName("Should reject when endDate is in the future")
    public void shouldRejectWhenEndDateIsInFuture() {
        // Given: endDate is tomorrow
        final StatsRequest request = new StatsRequest()
            .setStartDate(LocalDate.now().minusDays(7))
            .setEndDate(LocalDate.now().plusDays(1));

        // When / Then: validation exception thrown
        assertThrows(ValidationException.class, () -> validator.validateAndThrow(request));
    }

    @Test
    @DisplayName("Should reject when startDate is in the future")
    public void shouldRejectWhenStartDateIsInFuture() {
        // Given: both dates in the future
        final StatsRequest request = new StatsRequest()
            .setStartDate(LocalDate.now().plusDays(1))
            .setEndDate(LocalDate.now().plusDays(7));

        // When / Then: validation exception thrown
        assertThrows(ValidationException.class, () -> validator.validateAndThrow(request));
    }

    // ==================== Invalid: days out of range ====================

    @Test
    @DisplayName("Should reject when days is 0")
    public void shouldRejectWhenDaysIsZero() {
        // Given: days = 0 (below lower boundary)
        final StatsRequest request = new StatsRequest()
            .setDays(0);

        // When / Then: validation exception thrown
        assertThrows(ValidationException.class, () -> validator.validateAndThrow(request));
    }

    @Test
    @DisplayName("Should reject when days is negative")
    public void shouldRejectWhenDaysIsNegative() {
        // Given: days is negative
        final StatsRequest request = new StatsRequest()
            .setDays(-7);

        // When / Then: validation exception thrown
        assertThrows(ValidationException.class, () -> validator.validateAndThrow(request));
    }

    @Test
    @DisplayName("Should reject when days exceeds 365")
    public void shouldRejectWhenDaysExceeds365() {
        // Given: days = 366 (above upper boundary)
        final StatsRequest request = new StatsRequest()
            .setDays(366);

        // When / Then: validation exception thrown
        assertThrows(ValidationException.class, () -> validator.validateAndThrow(request));
    }
}
