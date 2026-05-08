package io.github.eventify.api.authentication.model.validator;

import java.util.List;

import org.passay.DefaultPasswordValidator;
import org.passay.PasswordData;
import org.passay.ValidationResult;
import org.passay.data.EnglishCharacterData;
import org.passay.rule.CharacterRule;
import org.passay.rule.LengthRule;
import org.passay.rule.WhitespaceRule;
import org.springframework.stereotype.Component;

/**
 * A custom password strength validator.
 */
@Component
public class CustomPasswordValidator {

    private final DefaultPasswordValidator passwordValidator;

    /**
     * Creates a new password validator with custom rules.
     */
    public CustomPasswordValidator() {
        this.passwordValidator = new DefaultPasswordValidator(
            List.of(
                // length at least 8 characters
                new LengthRule(8, 100),
                // at least one upper-case character
                new CharacterRule(EnglishCharacterData.UpperCase, 1),
                // at least one lower-case character
                new CharacterRule(EnglishCharacterData.LowerCase, 1),
                // at least one digit character
                new CharacterRule(EnglishCharacterData.Digit, 1),
                // at least one symbol (special character)
                new CharacterRule(EnglishCharacterData.Special, 1),
                // no whitespace
                new WhitespaceRule()
            )
        );
    }

    /**
     * Check if the password is weak.
     *
     * @param password The password to check.
     * @return True if the password is weak, false otherwise.
     */
    public boolean isWeakPassword(final String password) {
        final ValidationResult result = passwordValidator.validate(new PasswordData(password));
        return !result.isValid();
    }

    /**
     * Validate the password using the configured rules.
     *
     * @param password The password to validate.
     * @return The result of the validation.
     */
    public ValidationResult validatePassword(final String password) {
        return passwordValidator.validate(new PasswordData(password));
    }
}
