package io.github.eventify.api.monitor.validator;

import io.github.eventify.api.monitor.model.TimeRange;
import io.github.eventify.api.monitor.model.request.MonitorRequest;
import io.github.eventify.support.UnitTest;
import io.github.jframe.exception.core.ValidationException;
import io.github.jframe.validation.ValidationResult;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for MonitorValidator.
 */
@DisplayName("Unit Test - Monitor Validator")
class MonitorValidatorTest extends UnitTest {

    private MonitorValidator validator;

    @BeforeEach
    void setUp() {
        validator = new MonitorValidator();
    }

    // ========================= WATCHLIST ID VALIDATION =========================

    @Test
    @DisplayName("Should pass when watchlistId is provided")
    void shouldPassWhenWatchlistIdIsProvided() {
        // Given: Valid request with watchlistId
        final MonitorRequest request = new MonitorRequest();
        request.setWatchlistId(1L);
        request.setFilters(
            io.github.eventify.api.monitor.model.MonitorFilters.builder()
                .timeRange(TimeRange.LAST_24H)
                .build()
        );

        // When/Then: Should not throw exception
        assertDoesNotThrow(() -> validator.validateAndThrow(request));
    }

    @Test
    @DisplayName("Should fail when watchlistId is null")
    void shouldFailWhenWatchlistIdIsNull() {
        // Given: Request without watchlistId
        final MonitorRequest request = new MonitorRequest();
        request.setFilters(
            io.github.eventify.api.monitor.model.MonitorFilters.builder()
                .timeRange(TimeRange.LAST_24H)
                .build()
        );

        // When: Validating request
        final ValidationException exception = assertThrows(ValidationException.class, () -> {
            validator.validateAndThrow(request);
        });

        // Then: Should have watchlistId field error
        final ValidationResult result = exception.getValidationResult();
        assertThat(
            result.getErrors().stream()
                .anyMatch(error -> error.getField().equals("watchlistId")),
            is(true)
        );
    }

    @Test
    @DisplayName("Should fail when request body is null")
    void shouldFailWhenRequestBodyIsNull() {
        // Given: Null request
        final MonitorRequest request = null;

        // When/Then: Should throw ValidationException
        assertThrows(ValidationException.class, () -> {
            validator.validateAndThrow(request);
        });
    }

    // ========================= TIME RANGE VALIDATION =========================

    @Test
    @DisplayName("Should pass when using preset time range")
    void shouldPassWhenUsingPresetTimeRange() {
        // Given: Request with preset time range
        final MonitorRequest request = new MonitorRequest();
        request.setWatchlistId(1L);
        request.setFilters(
            io.github.eventify.api.monitor.model.MonitorFilters.builder()
                .timeRange(TimeRange.LAST_7D)
                .build()
        );

        // When/Then: Should not throw exception
        assertDoesNotThrow(() -> validator.validateAndThrow(request));
    }

    @Test
    @DisplayName("Should pass when using valid custom range")
    void shouldPassWhenUsingValidCustomRange() {
        // Given: Request with valid custom date range
        final MonitorRequest request = new MonitorRequest();
        request.setWatchlistId(1L);
        request.setFilters(
            io.github.eventify.api.monitor.model.MonitorFilters.builder()
                .startTime(OffsetDateTime.now().minusDays(5))
                .endTime(OffsetDateTime.now())
                .build()
        );

        // When/Then: Should not throw exception
        assertDoesNotThrow(() -> validator.validateAndThrow(request));
    }

    @Test
    @DisplayName("Should fail when both timeRange and custom range provided")
    void shouldFailWhenBothTimeRangeAndCustomRangeProvided() {
        // Given: Request with both timeRange and custom range
        final MonitorRequest request = new MonitorRequest();
        request.setWatchlistId(1L);
        request.setFilters(
            io.github.eventify.api.monitor.model.MonitorFilters.builder()
                .timeRange(TimeRange.LAST_24H)
                .startTime(OffsetDateTime.now().minusDays(1))
                .endTime(OffsetDateTime.now())
                .build()
        );

        // When: Validating request
        final ValidationException exception = assertThrows(ValidationException.class, () -> {
            validator.validateAndThrow(request);
        });

        // Then: Should have error about mutual exclusivity
        final ValidationResult result = exception.getValidationResult();
        assertThat(result.hasErrors(), is(true));
    }

    @Test
    @DisplayName("Should fail when only startTime provided")
    void shouldFailWhenOnlyStartTimeProvided() {
        // Given: Request with only startTime
        final MonitorRequest request = new MonitorRequest();
        request.setWatchlistId(1L);
        request.setFilters(
            io.github.eventify.api.monitor.model.MonitorFilters.builder()
                .startTime(OffsetDateTime.now().minusDays(1))
                .build()
        );

        // When: Validating request
        final ValidationException exception = assertThrows(ValidationException.class, () -> {
            validator.validateAndThrow(request);
        });

        // Then: Should have error about missing date
        final ValidationResult result = exception.getValidationResult();
        assertThat(result.hasErrors(), is(true));
    }

    @Test
    @DisplayName("Should fail when only endTime provided")
    void shouldFailWhenOnlyEndTimeProvided() {
        // Given: Request with only endTime
        final MonitorRequest request = new MonitorRequest();
        request.setWatchlistId(1L);
        request.setFilters(
            io.github.eventify.api.monitor.model.MonitorFilters.builder()
                .endTime(OffsetDateTime.now())
                .build()
        );

        // When: Validating request
        final ValidationException exception = assertThrows(ValidationException.class, () -> {
            validator.validateAndThrow(request);
        });

        // Then: Should have error about missing date
        final ValidationResult result = exception.getValidationResult();
        assertThat(result.hasErrors(), is(true));
    }

    @Test
    @DisplayName("Should fail when endTime is before startTime")
    void shouldFailWhenEndTimeIsBeforeStartTime() {
        // Given: Request with endTime before startTime
        final MonitorRequest request = new MonitorRequest();
        request.setWatchlistId(1L);
        request.setFilters(
            io.github.eventify.api.monitor.model.MonitorFilters.builder()
                .startTime(OffsetDateTime.now())
                .endTime(OffsetDateTime.now().minusDays(1))
                .build()
        );

        // When: Validating request
        final ValidationException exception = assertThrows(ValidationException.class, () -> {
            validator.validateAndThrow(request);
        });

        // Then: Should have error about endTime
        final ValidationResult result = exception.getValidationResult();
        assertThat(result.hasErrors(), is(true));
    }

    @Test
    @DisplayName("Should fail when custom range exceeds 30 days")
    void shouldFailWhenCustomRangeExceeds30Days() {
        // Given: Request with range > 30 days
        final MonitorRequest request = new MonitorRequest();
        request.setWatchlistId(1L);
        request.setFilters(
            io.github.eventify.api.monitor.model.MonitorFilters.builder()
                .startTime(OffsetDateTime.now().minusDays(31))
                .endTime(OffsetDateTime.now())
                .build()
        );

        // When: Validating request
        final ValidationException exception = assertThrows(ValidationException.class, () -> {
            validator.validateAndThrow(request);
        });

        // Then: Should have error about range duration
        final ValidationResult result = exception.getValidationResult();
        assertThat(result.hasErrors(), is(true));
    }

    @Test
    @DisplayName("Should pass when custom range exactly 30 days")
    void shouldPassWhenCustomRangeExactly30Days() {
        // Given: Request with exactly 30 days range
        final MonitorRequest request = new MonitorRequest();
        request.setWatchlistId(1L);
        request.setFilters(
            io.github.eventify.api.monitor.model.MonitorFilters.builder()
                .startTime(OffsetDateTime.now().minusDays(30))
                .endTime(OffsetDateTime.now())
                .build()
        );

        // When/Then: Should not throw exception
        assertDoesNotThrow(() -> validator.validateAndThrow(request));
    }

    // ========================= FILTER VALIDATION =========================

    @Test
    @DisplayName("Should pass when onlyCritical is null")
    void shouldPassWhenOnlyCriticalIsNull() {
        // Given: Request with null onlyCritical filter
        final MonitorRequest request = new MonitorRequest();
        request.setWatchlistId(1L);
        request.setFilters(
            io.github.eventify.api.monitor.model.MonitorFilters.builder()
                .timeRange(TimeRange.LAST_24H)
                .onlyCritical(null)
                .build()
        );

        // When/Then: Should not throw exception
        assertDoesNotThrow(() -> validator.validateAndThrow(request));
    }

    @Test
    @DisplayName("Should pass when sortBySeverity is null")
    void shouldPassWhenSortBySeverityIsNull() {
        // Given: Request with null sortBySeverity filter
        final MonitorRequest request = new MonitorRequest();
        request.setWatchlistId(1L);
        request.setFilters(
            io.github.eventify.api.monitor.model.MonitorFilters.builder()
                .timeRange(TimeRange.LAST_24H)
                .sortBySeverity(null)
                .build()
        );

        // When/Then: Should not throw exception
        assertDoesNotThrow(() -> validator.validateAndThrow(request));
    }

    @Test
    @DisplayName("Should pass when all filters are provided")
    void shouldPassWhenAllFiltersAreProvided() {
        // Given: Request with all filters
        final MonitorRequest request = new MonitorRequest();
        request.setWatchlistId(1L);
        request.setFilters(
            io.github.eventify.api.monitor.model.MonitorFilters.builder()
                .timeRange(TimeRange.LAST_24H)
                .onlyCritical(true)
                .sortBySeverity(false)
                .build()
        );

        // When/Then: Should not throw exception
        assertDoesNotThrow(() -> validator.validateAndThrow(request));
    }

    // ========================= EDGE CASES =========================

    @Test
    @DisplayName("Should pass when CUSTOM range with both dates")
    void shouldPassWhenCustomRangeWithBothDates() {
        // Given: Request with CUSTOM timeRange and both dates
        final MonitorRequest request = new MonitorRequest();
        request.setWatchlistId(1L);
        request.setFilters(
            io.github.eventify.api.monitor.model.MonitorFilters.builder()
                .timeRange(TimeRange.CUSTOM)
                .startTime(OffsetDateTime.now().minusDays(5))
                .endTime(OffsetDateTime.now())
                .build()
        );

        // When/Then: Should not throw exception
        assertDoesNotThrow(() -> validator.validateAndThrow(request));
    }

    @Test
    @DisplayName("Should fail when CUSTOM range without startTime")
    void shouldFailWhenCustomRangeWithoutStartTime() {
        // Given: Request with CUSTOM timeRange but missing startTime
        final MonitorRequest request = new MonitorRequest();
        request.setWatchlistId(1L);
        request.setFilters(
            io.github.eventify.api.monitor.model.MonitorFilters.builder()
                .timeRange(TimeRange.CUSTOM)
                .endTime(OffsetDateTime.now())
                .build()
        );

        // When: Validating request
        final ValidationException exception = assertThrows(ValidationException.class, () -> {
            validator.validateAndThrow(request);
        });

        // Then: Should have error about CUSTOM requiring dates
        final ValidationResult result = exception.getValidationResult();
        assertThat(result.hasErrors(), is(true));
        assertThat(
            result.getErrors().stream()
                .anyMatch(error -> error.getCode().contains("CUSTOM")),
            is(true)
        );
    }

    @Test
    @DisplayName("Should fail when CUSTOM range without endTime")
    void shouldFailWhenCustomRangeWithoutEndTime() {
        // Given: Request with CUSTOM timeRange but missing endTime
        final MonitorRequest request = new MonitorRequest();
        request.setWatchlistId(1L);
        request.setFilters(
            io.github.eventify.api.monitor.model.MonitorFilters.builder()
                .timeRange(TimeRange.CUSTOM)
                .startTime(OffsetDateTime.now().minusDays(5))
                .build()
        );

        // When: Validating request
        final ValidationException exception = assertThrows(ValidationException.class, () -> {
            validator.validateAndThrow(request);
        });

        // Then: Should have error about CUSTOM requiring dates
        final ValidationResult result = exception.getValidationResult();
        assertThat(result.hasErrors(), is(true));
        assertThat(
            result.getErrors().stream()
                .anyMatch(error -> error.getCode().contains("CUSTOM")),
            is(true)
        );
    }

    @Test
    @DisplayName("Should fail when CUSTOM range without any dates")
    void shouldFailWhenCustomRangeWithoutAnyDates() {
        // Given: Request with CUSTOM timeRange but no dates
        final MonitorRequest request = new MonitorRequest();
        request.setWatchlistId(1L);
        request.setFilters(
            io.github.eventify.api.monitor.model.MonitorFilters.builder()
                .timeRange(TimeRange.CUSTOM)
                .build()
        );

        // When: Validating request
        final ValidationException exception = assertThrows(ValidationException.class, () -> {
            validator.validateAndThrow(request);
        });

        // Then: Should have error about CUSTOM requiring dates
        final ValidationResult result = exception.getValidationResult();
        assertThat(result.hasErrors(), is(true));
        assertThat(
            result.getErrors().stream()
                .anyMatch(error -> error.getCode().contains("CUSTOM")),
            is(true)
        );
    }

    @Test
    @DisplayName("Should fail when startTime equals endTime")
    void shouldFailWhenStartTimeEqualsEndTime() {
        // Given: Request with identical start and end times
        final OffsetDateTime now = OffsetDateTime.now();
        final MonitorRequest request = new MonitorRequest();
        request.setWatchlistId(1L);
        request.setFilters(
            io.github.eventify.api.monitor.model.MonitorFilters.builder()
                .startTime(now)
                .endTime(now)
                .build()
        );

        // When: Validating request
        final ValidationException exception = assertThrows(ValidationException.class, () -> {
            validator.validateAndThrow(request);
        });

        // Then: Should have error about endTime
        final ValidationResult result = exception.getValidationResult();
        assertThat(result.hasErrors(), is(true));
    }

    @Test
    @DisplayName("Should pass when no time parameters provided")
    void shouldPassWhenNoTimeParametersProvided() {
        // Given: Request with only watchlistId (will use watchlist defaults)
        final MonitorRequest request = new MonitorRequest();
        request.setWatchlistId(1L);

        // When/Then: Should not throw exception
        assertDoesNotThrow(() -> validator.validateAndThrow(request));
    }
}
