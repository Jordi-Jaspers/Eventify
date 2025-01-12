package org.jordijaspers.eventify.api.user.validator;

import java.util.List;

import org.hawaiiframework.validation.ValidationError;
import org.hawaiiframework.validation.ValidationResult;
import org.jordijaspers.eventify.api.user.model.request.UpdateEmailRequest;
import org.jordijaspers.eventify.api.user.model.validator.EmailValidator;
import org.jordijaspers.eventify.support.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jordijaspers.eventify.api.user.model.validator.EmailValidator.*;

@DisplayName("EmailValidator")
public class EmailValidatorTest extends UnitTest {

    private final EmailValidator validator = new EmailValidator();
    private final ValidationResult validationResult = new ValidationResult();

    @Nested
    @DisplayName("Email Format Validation")
    public final class EmailFormatValidation {

        @Test
        public void shouldThrowValidationExceptionWhenEmailIsNull() {
            final UpdateEmailRequest request = anEmailRequest(null);
            validator.validate(request, validationResult);

            final List<ValidationError> errors = validationResult.getErrors();
            errors.stream()
                .filter(error -> error.getField().equals(EMAIL))
                .findFirst()
                .ifPresent(error -> {
                    assertThat(error.getCode()).isEqualTo(EMAIL_MUST_NOT_BE_EMPTY);
                });
        }

        @Test
        public void shouldThrowValidationExceptionWhenEmailIsEmpty() {
            final UpdateEmailRequest request = anEmailRequest("");
            validator.validate(request, validationResult);

            final List<ValidationError> errors = validationResult.getErrors();
            errors.stream()
                .filter(error -> error.getField().equals(EMAIL))
                .findFirst()
                .ifPresent(error -> {
                    assertThat(error.getCode()).isEqualTo(EMAIL_MUST_NOT_BE_EMPTY);
                });
        }

        @Test
        public void shouldThrowValidationExceptionWhenEmailDoesNotContainAt() {
            final UpdateEmailRequest request = anEmailRequest("invalidemail.com");
            validator.validate(request, validationResult);

            final List<ValidationError> errors = validationResult.getErrors();
            errors.stream()
                .filter(error -> error.getField().equals(EMAIL))
                .findFirst()
                .ifPresent(error -> {
                    assertThat(error.getCode()).isEqualTo(EMAIL_MUST_CONTAIN_AN_AT);
                });
        }

        @Test
        public void shouldThrowValidationExceptionWhenEmailIsInvalid() {
            final UpdateEmailRequest request = anEmailRequest("invalid@email@domain.com");
            validator.validate(request, validationResult);

            final List<ValidationError> errors = validationResult.getErrors();
            errors.stream()
                .filter(error -> error.getField().equals(EMAIL))
                .findFirst()
                .ifPresent(error -> {
                    assertThat(error.getCode()).isEqualTo(EMAIL_MUST_BE_A_VALID_EMAIL_ADDRESS);
                });
        }

        @Test
        public void shouldValidateSuccessfullyWhenEmailIsValid() {
            final UpdateEmailRequest request = aValidEmailRequest();
            validator.validate(request, validationResult);

            assertThat(validationResult.hasErrors()).isFalse();
        }
    }

    private static UpdateEmailRequest aValidEmailRequest() {
        return anEmailRequest("valid.email@domain.com");
    }

    private static UpdateEmailRequest anEmailRequest(final String email) {
        return new UpdateEmailRequest()
            .setEmail(email);
    }
}
