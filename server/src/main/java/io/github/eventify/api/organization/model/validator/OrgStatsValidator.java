package io.github.eventify.api.organization.model.validator;

import io.github.jframe.validation.ValidationResult;
import io.github.jframe.validation.Validator;

import org.springframework.stereotype.Component;

/** Validates org stats query parameters. */
@Component
public class OrgStatsValidator implements Validator<Integer> {

    public static final String DAYS_INVALID = "days must be between 1 and 365";

    @Override
    public void validate(final Integer days, final ValidationResult result) {
        if (days < 1 || days > 365) {
            result.reject(DAYS_INVALID);
        }
    }
}
