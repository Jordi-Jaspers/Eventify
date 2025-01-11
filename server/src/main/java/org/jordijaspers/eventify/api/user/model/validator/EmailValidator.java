package org.jordijaspers.eventify.api.user.model.validator;

import lombok.RequiredArgsConstructor;

import org.hawaiiframework.validation.ValidationResult;
import org.hawaiiframework.validation.Validator;
import org.jordijaspers.eventify.api.user.model.request.UpdateEmailRequest;
import org.springframework.stereotype.Component;

import static org.jordijaspers.eventify.common.constants.Constants.Email.OWASP_EMAIL_REGEX;

/**
 * A custom email validator.
 */
@Component
@RequiredArgsConstructor
public class EmailValidator implements Validator<UpdateEmailRequest> {

    // Error messages
    public static final String EMAIL_MUST_NOT_BE_EMPTY = "email cannot not be empty";
    public static final String EMAIL_MUST_CONTAIN_AN_AT = "email must contain an @";
    public static final String EMAIL_MUST_BE_A_VALID_EMAIL_ADDRESS = "email is not a valid email address";

    // Fields
    public static final String EMAIL = "email";

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(final UpdateEmailRequest request, final ValidationResult result) {
        result.rejectField(EMAIL, request.getEmail())
            .whenNull(EMAIL_MUST_NOT_BE_EMPTY)
            .orWhen(String::isEmpty, EMAIL_MUST_NOT_BE_EMPTY)
            .orWhen(email -> !email.contains("@"), EMAIL_MUST_CONTAIN_AN_AT)
            .orWhen(email -> !email.matches(OWASP_EMAIL_REGEX), EMAIL_MUST_BE_A_VALID_EMAIL_ADDRESS);
    }
}
