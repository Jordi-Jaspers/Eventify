package org.jordijaspers.eventify.api.event.validator;

import java.time.ZonedDateTime;
import java.util.List;

import org.hawaiiframework.validation.ValidationError;
import org.hawaiiframework.validation.ValidationException;
import org.hawaiiframework.validation.ValidationResult;
import org.jordijaspers.eventify.api.event.model.Status;
import org.jordijaspers.eventify.api.event.model.request.EventRequest;
import org.jordijaspers.eventify.api.event.model.validator.EventValidator;
import org.jordijaspers.eventify.support.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.jordijaspers.eventify.api.event.model.validator.EventValidator.*;

@DisplayName("Event Validator Unit Tests")
public final class EventValidatorTest extends UnitTest {

    private final EventValidator validator = new EventValidator();

    private final ValidationResult validationResult = new ValidationResult();

    @Test
    public void shouldValidateSuccessfullyWhenAllFieldsAreValid() {
        final EventRequest request = aValidEventRequest();
        validator.validate(request, validationResult);
        assertThat(validationResult.hasErrors()).isFalse();
    }

    @Test
    public void shouldRejectWhenBodyIsMissing() {
        assertThatThrownBy(() -> validator.validate(null, validationResult))
            .isInstanceOf(ValidationException.class);

        final List<ValidationError> errors = validationResult.getErrors();
        assertThat(errors).hasSize(1);
        assertThat(errors.getFirst().getCode()).isEqualTo(BODY_IS_MISSING);
    }

    @Nested
    @DisplayName("Required Fields Validation")
    public final class RequiredFieldsValidation {

        @Test
        public void shouldRejectWhenCheckIdIsMissing() {
            final EventRequest request = aValidEventRequest();
            request.setCheckId(null);

            assertThatThrownBy(() -> validator.validate(request, validationResult))
                .isInstanceOf(ValidationException.class);

            final List<ValidationError> errors = validationResult.getErrors();
            errors.stream()
                .filter(error -> error.getField().equals(CHECK_ID))
                .findFirst()
                .ifPresent(error -> assertThat(error.getCode()).isEqualTo(CHECK_ID_IS_MISSING));
        }

        @Test
        public void shouldRejectWhenStatusIsMissing() {
            final EventRequest request = aValidEventRequest();
            request.setStatus(null);

            assertThatThrownBy(() -> validator.validate(request, validationResult))
                .isInstanceOf(ValidationException.class);

            final List<ValidationError> errors = validationResult.getErrors();
            errors.stream()
                .filter(error -> error.getField().equals(STATUS))
                .findFirst()
                .ifPresent(error -> assertThat(error.getCode()).isEqualTo(STATUS_IS_MISSING));
        }

        @Test
        public void shouldRejectWhenMessageIsMissing() {
            final EventRequest request = aValidEventRequest();
            request.setMessage(null);

            assertThatThrownBy(() -> validator.validate(request, validationResult))
                .isInstanceOf(ValidationException.class);

            final List<ValidationError> errors = validationResult.getErrors();
            errors.stream()
                .filter(error -> error.getField().equals(MESSAGE))
                .findFirst()
                .ifPresent(error -> assertThat(error.getCode()).isEqualTo(MESSAGE_IS_MISSING));
        }

        @Test
        public void shouldRejectWhenMessageIsBlank() {
            final EventRequest request = aValidEventRequest();
            request.setMessage("   ");

            assertThatThrownBy(() -> validator.validate(request, validationResult))
                .isInstanceOf(ValidationException.class);

            final List<ValidationError> errors = validationResult.getErrors();
            errors.stream()
                .filter(error -> error.getField().equals(MESSAGE))
                .findFirst()
                .ifPresent(error -> assertThat(error.getCode()).isEqualTo(MESSAGE_IS_MISSING));
        }

        @Test
        public void shouldRejectWhenMessageIsTooLong() {
            final EventRequest request = aValidEventRequest();
            request.setMessage("x".repeat(MAX_MESSAGE_LENGTH + 1));

            assertThatThrownBy(() -> validator.validate(request, validationResult))
                .isInstanceOf(ValidationException.class);

            final List<ValidationError> errors = validationResult.getErrors();
            errors.stream()
                .filter(error -> error.getField().equals(MESSAGE))
                .findFirst()
                .ifPresent(error -> assertThat(error.getCode()).isEqualTo(MESSAGE_IS_TOO_LONG));
        }
    }


    @Nested
    @DisplayName("Timestamp Validation")
    public final class TimestampValidation {

        @Test
        public void shouldRejectWhenTimestampIsMissing() {
            final EventRequest request = aValidEventRequest();
            request.setTimestamp(null);

            assertThatThrownBy(() -> validator.validate(request, validationResult))
                .isInstanceOf(ValidationException.class);

            final List<ValidationError> errors = validationResult.getErrors();
            errors.stream()
                .filter(error -> error.getField().equals(TIMESTAMP))
                .findFirst()
                .ifPresent(error -> assertThat(error.getCode()).isEqualTo(TIMESTAMP_IS_MISSING));
        }

        @Test
        public void shouldRejectWhenTimestampIsInFuture() {
            final EventRequest request = aValidEventRequest();
            request.setTimestamp(ZonedDateTime.now(UTC).plusMinutes(1));

            assertThatThrownBy(() -> validator.validate(request, validationResult))
                .isInstanceOf(ValidationException.class);

            final List<ValidationError> errors = validationResult.getErrors();
            errors.stream()
                .filter(error -> error.getField().equals(TIMESTAMP))
                .findFirst()
                .ifPresent(error -> assertThat(error.getCode()).isEqualTo(TIMESTAMP_IS_IN_THE_FUTURE));
        }

        @Test
        public void shouldRejectWhenTimestampIsTooOld() {
            final EventRequest request = aValidEventRequest();
            request.setTimestamp(ZonedDateTime.now(UTC).minus(MAX_TIMESTAMP_AGE).minusMinutes(1));

            assertThatThrownBy(() -> validator.validate(request, validationResult))
                .isInstanceOf(ValidationException.class);

            final List<ValidationError> errors = validationResult.getErrors();
            errors.stream()
                .filter(error -> error.getField().equals(TIMESTAMP))
                .findFirst()
                .ifPresent(error -> assertThat(error.getCode()).isEqualTo(TIMESTAMP_IS_TOO_OLD));
        }
    }

    private static EventRequest aValidEventRequest() {
        return new EventRequest()
            .setCheckId(1L)
            .setStatus(Status.OK)
            .setMessage("Valid event message")
            .setTimestamp(ZonedDateTime.now(UTC))
            .setCorrelationId("correlation-id");
    }
}
