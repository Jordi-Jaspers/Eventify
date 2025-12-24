package io.github.eventify.api.admin.model.validator;

import io.github.jframe.exception.core.ValidationException;
import io.github.jframe.validation.ValidationResult;
import io.github.jframe.validation.Validator;

import org.springframework.stereotype.Component;


/**
 * Validator for user search queries.
 */
@Component
public class UserSearchValidator implements Validator<String> {

    // Error messages
    public static final String QUERY_REQUIRED = "Search query is required";
    public static final String QUERY_TOO_SHORT = "Search query must be at least 3 characters";

    // Fields
    public static final String QUERY = "query";

    // Constraints
    private static final int MIN_QUERY_LENGTH = 3;

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(final String query, final ValidationResult result) {
        result.rejectField(QUERY, query)
            .whenNull(QUERY_REQUIRED)
            .orWhen(String::isEmpty, QUERY_REQUIRED)
            .orWhen(q -> q.length() < MIN_QUERY_LENGTH, QUERY_TOO_SHORT);

        if (result.hasErrors()) {
            throw new ValidationException(result);
        }
    }
}
