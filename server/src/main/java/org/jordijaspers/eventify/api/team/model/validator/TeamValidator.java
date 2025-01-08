package org.jordijaspers.eventify.api.team.model.validator;

import lombok.RequiredArgsConstructor;

import org.hawaiiframework.validation.ValidationResult;
import org.hawaiiframework.validation.Validator;
import org.jordijaspers.eventify.api.team.model.request.TeamRequest;
import org.springframework.stereotype.Component;

/**
 * {@inheritDoc}.
 */
@Component
@RequiredArgsConstructor
public class TeamValidator implements Validator<TeamRequest> {

    // Error messages
    private static final String NAME_MUST_NOT_BE_EMPTY = "Name cannot be empty, because it is required.";
    private static final String MAX_CHARACTERS_NAME = "The maximum number of characters is 255.";
    private static final String DESCRIPTION_MUST_NOT_BE_EMPTY = "Description cannot be empty, because it is required.";
    private static final String MAX_CHARACTERS_DESCRIPTION = "The description cannot exceed 4000 characters.";

    // Constraints
    private static final int MAX_NAME_LENGTH = 255;
    private static final int MAX_DESCRIPTION_LENGTH = 4000;

    // Fields
    private static final String NAME = "name";
    private static final String DESCRIPTION = "description";

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(final TeamRequest request, final ValidationResult result) {
        result.rejectField(NAME, request.getName())
            .whenNull(NAME_MUST_NOT_BE_EMPTY)
            .orWhen(String::isBlank, NAME_MUST_NOT_BE_EMPTY)
            .orWhen(name -> name.length() > MAX_NAME_LENGTH, MAX_CHARACTERS_NAME);

        result.rejectField(DESCRIPTION, request.getDescription())
            .whenNull(DESCRIPTION_MUST_NOT_BE_EMPTY)
            .orWhen(String::isBlank, DESCRIPTION_MUST_NOT_BE_EMPTY)
            .orWhen(description -> description.length() > MAX_DESCRIPTION_LENGTH, MAX_CHARACTERS_DESCRIPTION);
    }
}
