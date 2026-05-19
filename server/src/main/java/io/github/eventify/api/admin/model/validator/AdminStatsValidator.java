package io.github.eventify.api.admin.model.validator;

import io.github.jframe.validation.ValidationResult;
import io.github.jframe.validation.Validator;

import java.util.Set;

import org.springframework.stereotype.Component;

/** Validates that a days parameter is one of the allowed values: 7, 30, 90, or 180. */
@Component
public class AdminStatsValidator implements Validator<Object> {

    public static final String INVALID_DAYS = "days must be one of: 7, 30, 90, 180";

    private static final Set<Integer> ALLOWED_DAYS = Set.of(7, 30, 90, 180);

    @Override
    public void validate(final Object request, final ValidationResult result) {
        if (!(request instanceof Integer days) || !ALLOWED_DAYS.contains(days)) {
            result.reject(INVALID_DAYS);
        }
    }
}
