package org.jordijaspers.eventify.api.monitoring.validator;

import java.time.Duration;
import java.util.List;

import org.hawaiiframework.validation.ValidationError;
import org.hawaiiframework.validation.ValidationResult;
import org.jordijaspers.eventify.api.monitoring.model.validator.WindowValidator;
import org.jordijaspers.eventify.support.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jordijaspers.eventify.api.monitoring.model.validator.WindowValidator.WINDOW;
import static org.jordijaspers.eventify.api.monitoring.model.validator.WindowValidator.WINDOW_MUST_BE_IN_HOURS;

@DisplayName("WindowValidator Unit Tests")
public class WindowValidatorTest extends UnitTest {

    private final WindowValidator validator = new WindowValidator();

    private final ValidationResult validationResult = new ValidationResult();


    @Nested
    @DisplayName("When validating duration format")
    public class FormatValidation {

        @Test
        @DisplayName("Should accept valid hour-based duration")
        void shouldAcceptValidHourDuration() {
            final Duration window = Duration.ofHours(24);
            validator.validate(window, validationResult);
            assertThat(validationResult.hasErrors()).isFalse();
        }

        @Test
        @DisplayName("Should reject non-hour-based duration")
        void shouldRejectNonHourDuration() {
            final Duration window = Duration.ofMinutes(120);
            validator.validate(window, validationResult);

            final List<ValidationError> errors = validationResult.getErrors();
            errors.stream()
                .filter(error -> error.getField().equals(WINDOW))
                .findFirst()
                .ifPresent(error -> {
                    assertThat(error.getCode()).isEqualTo(WINDOW_MUST_BE_IN_HOURS);
                });
        }
    }


    @Nested
    @DisplayName("When validating duration size")
    public class SizeValidation {

        @Test
        @DisplayName("Should accept duration of at least 1 hour")
        void shouldAcceptDurationOfAtLeastOneHour() {
            final Duration window = Duration.ofHours(1);
            validator.validate(window, validationResult);
            assertThat(validationResult.hasErrors()).isFalse();
        }

        @Test
        @DisplayName("Should reject duration of less than 1 hour")
        void shouldRejectDurationOfLessThanOneHour() {
            final Duration window = Duration.ofHours(0);
            validator.validate(window, validationResult);

            final List<ValidationError> errors = validationResult.getErrors();
            assertThat(errors).isNotEmpty();
        }

        @Test
        @DisplayName("Should not have a limit on the maximum duration")
        void shouldNotHaveLimitOnMaximumDuration() {
            final Duration window = Duration.ofHours(1000000000);
            validator.validate(window, validationResult);
            assertThat(validationResult.hasErrors()).isFalse();
        }
    }
}
