package io.github.eventify.api.channel.model.validator;

import io.github.eventify.api.channel.model.request.CreateChannelRequest;
import io.github.eventify.api.channel.model.request.UpdateChannelRequest;
import io.github.jframe.exception.core.ValidationException;
import io.github.jframe.validation.ValidationResult;
import io.github.jframe.validation.Validator;

import org.springframework.stereotype.Component;

import static java.util.Objects.isNull;

/**
 * Validator for channel requests.
 */
@Component
public class ChannelValidator implements Validator<Object> {

    // Error messages
    public static final String BODY_IS_MISSING = "Request body is missing";
    public static final String NAME_REQUIRED = "Name is required";
    public static final String NAME_TOO_LONG = "Name must not exceed 100 characters";
    public static final String SLUG_REQUIRED = "Slug is required";
    public static final String SLUG_TOO_LONG = "Slug must not exceed 100 characters";
    public static final String SLUG_INVALID_FORMAT =
        "Slug must contain only lowercase letters, numbers, and dots. Cannot start or end with dots, or have consecutive dots";
    public static final String DESCRIPTION_TOO_LONG = "Description must not exceed 500 characters";

    // Fields
    public static final String NAME = "name";
    public static final String SLUG = "slug";
    public static final String DESCRIPTION = "description";

    // Slug pattern: lowercase letters, numbers, dots (not at start/end, no consecutive dots)
    private static final String SLUG_PATTERN = "^[a-z0-9]+(\\.[a-z0-9]+)*$";

    /**
     * Validates a CreateChannelRequest.
     *
     * @param request the create request
     * @param result  the validation result
     */
    public void validate(final CreateChannelRequest request, final ValidationResult result) {
        if (isNull(request)) {
            result.reject(BODY_IS_MISSING);
            throw new ValidationException(result);
        }

        result.rejectField(NAME, request.getName())
            .whenNull(NAME_REQUIRED)
            .orWhen(String::isEmpty, NAME_REQUIRED)
            .orWhen(String::isBlank, NAME_REQUIRED)
            .orWhen(name -> name.length() > 100, NAME_TOO_LONG);

        result.rejectField(SLUG, request.getSlug())
            .whenNull(SLUG_REQUIRED)
            .orWhen(String::isEmpty, SLUG_REQUIRED)
            .orWhen(String::isBlank, SLUG_REQUIRED)
            .orWhen(slug -> slug.length() > 100, SLUG_TOO_LONG)
            .orWhen(slug -> !slug.matches(SLUG_PATTERN), SLUG_INVALID_FORMAT);

        result.rejectField(DESCRIPTION, request.getDescription())
            .when(desc -> desc != null && desc.length() > 500, DESCRIPTION_TOO_LONG);

        if (result.hasErrors()) {
            throw new ValidationException(result);
        }
    }

    /**
     * Validates an UpdateChannelRequest.
     *
     * @param request the update request
     * @param result  the validation result
     */
    public void validate(final UpdateChannelRequest request, final ValidationResult result) {
        if (isNull(request)) {
            result.reject(BODY_IS_MISSING);
            throw new ValidationException(result);
        }

        result.rejectField(NAME, request.getName())
            .whenNull(NAME_REQUIRED)
            .orWhen(String::isEmpty, NAME_REQUIRED)
            .orWhen(String::isBlank, NAME_REQUIRED)
            .orWhen(name -> name.length() > 100, NAME_TOO_LONG);

        result.rejectField(DESCRIPTION, request.getDescription())
            .when(desc -> desc != null && desc.length() > 500, DESCRIPTION_TOO_LONG);

        if (result.hasErrors()) {
            throw new ValidationException(result);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(final Object request, final ValidationResult result) {
        if (request instanceof CreateChannelRequest) {
            validate((CreateChannelRequest) request, result);
        } else if (request instanceof UpdateChannelRequest) {
            validate((UpdateChannelRequest) request, result);
        } else {
            result.reject("Unsupported request type");
            throw new ValidationException(result);
        }
    }
}
