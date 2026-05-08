package io.github.eventify.api.apikey.model.validator;

import io.github.eventify.api.apikey.model.request.CreateApiKeyRequest;
import io.github.jframe.exception.core.ValidationException;
import io.github.jframe.validation.ValidationResult;
import io.github.jframe.validation.Validator;
import lombok.RequiredArgsConstructor;

import java.time.OffsetDateTime;

import org.springframework.stereotype.Component;

import static java.util.Objects.isNull;

/**
 * Validator for creating API keys.
 */
@Component
@RequiredArgsConstructor
public class CreateApiKeyValidator implements Validator<CreateApiKeyRequest> {

    // Error messages
    public static final String BODY_IS_MISSING = "Request body is missing";
    public static final String NAME_MUST_NOT_BE_EMPTY = "Name is required";
    public static final String NAME_TOO_LONG = "Name must not exceed 100 characters";
    public static final String EXPIRATION_MUST_BE_FUTURE = "Expiration date must be in the future";

    // Fields
    public static final String NAME = "name";
    public static final String EXPIRES_AT = "expiresAt";

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(final CreateApiKeyRequest request, final ValidationResult result) {
        if (isNull(request)) {
            result.reject(BODY_IS_MISSING);
            throw new ValidationException(result);
        }

        result.rejectField(NAME, request.getName())
            .whenNull(NAME_MUST_NOT_BE_EMPTY)
            .orWhen(String::isEmpty, NAME_MUST_NOT_BE_EMPTY)
            .orWhen(String::isBlank, NAME_MUST_NOT_BE_EMPTY)
            .orWhen(name -> name.length() > 100, NAME_TOO_LONG);

        result.rejectField(EXPIRES_AT, request.getExpiresAt())
            .when(expiresAt -> expiresAt != null && expiresAt.isBefore(OffsetDateTime.now()), EXPIRATION_MUST_BE_FUTURE);

        if (result.hasErrors()) {
            throw new ValidationException(result);
        }
    }
}
