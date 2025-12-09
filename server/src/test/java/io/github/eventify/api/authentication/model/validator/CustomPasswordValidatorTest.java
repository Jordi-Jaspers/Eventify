package io.github.eventify.api.authentication.model.validator;

import io.github.eventify.support.UnitTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.passay.RuleResult;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Unit Test - Custom Password Validator")
public class CustomPasswordValidatorTest extends UnitTest {

    private CustomPasswordValidator customPasswordValidator;

    @BeforeEach
    public void setUp() {
        customPasswordValidator = new CustomPasswordValidator();
    }

    @Nested
    @DisplayName("Valid Password Validation")
    public class ValidPasswordTests {

        @Test
        @DisplayName("Should return valid result when password meets all requirements")
        public void shouldReturnValidResultWhenPasswordMeetsAllRequirements() {
            // Given: A password that meets all requirements
            final String validPassword = VALID_PASSWORD;

            // When: Validating the password
            final RuleResult result = customPasswordValidator.validatePassword(validPassword);

            // Then: The validation should pass
            assertThat(result.isValid(), is(true));

            // And: isWeakPassword should return false
            assertThat(customPasswordValidator.isWeakPassword(validPassword), is(false));
        }

        @Test
        @DisplayName("Should return valid result when password has minimum length with all character types")
        public void shouldReturnValidResultWhenPasswordHasMinimumLengthWithAllCharacterTypes() {
            // Given: A password with exactly 8 characters including all required types
            final String minLengthPassword = VALID_PASSWORD_MIN_LENGTH;

            // When: Validating the password
            final RuleResult result = customPasswordValidator.validatePassword(minLengthPassword);

            // Then: The validation should pass
            assertThat(result.isValid(), is(true));
            assertThat(customPasswordValidator.isWeakPassword(minLengthPassword), is(false));
        }

        @Test
        @DisplayName("Should return valid result when password contains multiple uppercase letters")
        public void shouldReturnValidResultWhenPasswordContainsMultipleUppercaseLetters() {
            // Given: A password with multiple uppercase letters
            final String password = "TESTPass123!";

            // When: Validating the password
            final RuleResult result = customPasswordValidator.validatePassword(password);

            // Then: The validation should pass
            assertThat(result.isValid(), is(true));
            assertThat(customPasswordValidator.isWeakPassword(password), is(false));
        }

        @Test
        @DisplayName("Should return valid result when password contains multiple lowercase letters")
        public void shouldReturnValidResultWhenPasswordContainsMultipleLowercaseLetters() {
            // Given: A password with multiple lowercase letters
            final String password = "Testpass123!";

            // When: Validating the password
            final RuleResult result = customPasswordValidator.validatePassword(password);

            // Then: The validation should pass
            assertThat(result.isValid(), is(true));
            assertThat(customPasswordValidator.isWeakPassword(password), is(false));
        }

        @Test
        @DisplayName("Should return valid result when password contains multiple digits")
        public void shouldReturnValidResultWhenPasswordContainsMultipleDigits() {
            // Given: A password with multiple digits
            final String password = "Test12345!";

            // When: Validating the password
            final RuleResult result = customPasswordValidator.validatePassword(password);

            // Then: The validation should pass
            assertThat(result.isValid(), is(true));
            assertThat(customPasswordValidator.isWeakPassword(password), is(false));
        }

        @Test
        @DisplayName("Should return valid result when password contains multiple special characters")
        public void shouldReturnValidResultWhenPasswordContainsMultipleSpecialCharacters() {
            // Given: A password with multiple special characters
            final String password = "Test123!@#$%";

            // When: Validating the password
            final RuleResult result = customPasswordValidator.validatePassword(password);

            // Then: The validation should pass
            assertThat(result.isValid(), is(true));
            assertThat(customPasswordValidator.isWeakPassword(password), is(false));
        }

        @Test
        @DisplayName("Should return valid result when password is at maximum practical length")
        public void shouldReturnValidResultWhenPasswordIsAtMaximumPracticalLength() {
            // Given: A very long password with all required character types
            final String longPassword = VALID_PASSWORD_MIN_LENGTH + "a".repeat(92);

            // When: Validating the password
            final RuleResult result = customPasswordValidator.validatePassword(longPassword);

            // Then: The validation should pass
            assertThat(result.isValid(), is(true));
            assertThat(customPasswordValidator.isWeakPassword(longPassword), is(false));
        }

        @Test
        @DisplayName("Should return valid result when password uses different special characters")
        public void shouldReturnValidResultWhenPasswordUsesDifferentSpecialCharacters() {
            // Given: Passwords with different special characters
            final String password1 = "Test123!";
            final String password2 = "Test123@";
            final String password3 = "Test123#";
            final String password4 = "Test123$";
            final String password5 = "Test123%";

            // When: Validating the passwords
            final RuleResult result1 = customPasswordValidator.validatePassword(password1);
            final RuleResult result2 = customPasswordValidator.validatePassword(password2);
            final RuleResult result3 = customPasswordValidator.validatePassword(password3);
            final RuleResult result4 = customPasswordValidator.validatePassword(password4);
            final RuleResult result5 = customPasswordValidator.validatePassword(password5);

            // Then: All validations should pass
            assertThat(result1.isValid(), is(true));
            assertThat(result2.isValid(), is(true));
            assertThat(result3.isValid(), is(true));
            assertThat(result4.isValid(), is(true));
            assertThat(result5.isValid(), is(true));
        }
    }


    @Nested
    @DisplayName("Invalid Password - Length Requirements")
    public class InvalidPasswordLengthTests {

        @Test
        @DisplayName("Should return invalid result when password is too short")
        public void shouldReturnInvalidResultWhenPasswordIsTooShort() {
            // Given: A password with only 7 characters
            final String shortPassword = PASSWORD_TOO_SHORT;

            // When: Validating the password
            final RuleResult result = customPasswordValidator.validatePassword(shortPassword);

            // Then: The validation should fail
            assertThat(result.isValid(), is(false));

            // And: isWeakPassword should return true
            assertThat(customPasswordValidator.isWeakPassword(shortPassword), is(true));

            // And: The result should contain error details
            assertThat(result.getDetails(), is(not(empty())));
        }

        @Test
        @DisplayName("Should return invalid result when password is empty")
        public void shouldReturnInvalidResultWhenPasswordIsEmpty() {
            // Given: An empty password
            final String emptyPassword = "";

            // When: Validating the password
            final RuleResult result = customPasswordValidator.validatePassword(emptyPassword);

            // Then: The validation should fail
            assertThat(result.isValid(), is(false));
            assertThat(customPasswordValidator.isWeakPassword(emptyPassword), is(true));
        }

        @Test
        @DisplayName("Should return invalid result when password has only 1 character")
        public void shouldReturnInvalidResultWhenPasswordHasOnly1Character() {
            // Given: A password with only 1 character
            final String singleCharPassword = "A";

            // When: Validating the password
            final RuleResult result = customPasswordValidator.validatePassword(singleCharPassword);

            // Then: The validation should fail
            assertThat(result.isValid(), is(false));
            assertThat(customPasswordValidator.isWeakPassword(singleCharPassword), is(true));
        }
    }


    @Nested
    @DisplayName("Invalid Password - Missing Character Types")
    public class InvalidPasswordCharacterTypeTests {

        @Test
        @DisplayName("Should return invalid result when password has no uppercase letters")
        public void shouldReturnInvalidResultWhenPasswordHasNoUppercaseLetters() {
            // Given: A password without uppercase letters
            final String noUppercasePassword = PASSWORD_NO_UPPERCASE;

            // When: Validating the password
            final RuleResult result = customPasswordValidator.validatePassword(noUppercasePassword);

            // Then: The validation should fail
            assertThat(result.isValid(), is(false));
            assertThat(customPasswordValidator.isWeakPassword(noUppercasePassword), is(true));
            assertThat(result.getDetails(), is(not(empty())));
        }

        @Test
        @DisplayName("Should return invalid result when password has no lowercase letters")
        public void shouldReturnInvalidResultWhenPasswordHasNoLowercaseLetters() {
            // Given: A password without lowercase letters
            final String noLowercasePassword = PASSWORD_NO_LOWERCASE;

            // When: Validating the password
            final RuleResult result = customPasswordValidator.validatePassword(noLowercasePassword);

            // Then: The validation should fail
            assertThat(result.isValid(), is(false));
            assertThat(customPasswordValidator.isWeakPassword(noLowercasePassword), is(true));
            assertThat(result.getDetails(), is(not(empty())));
        }

        @Test
        @DisplayName("Should return invalid result when password has no digits")
        public void shouldReturnInvalidResultWhenPasswordHasNoDigits() {
            // Given: A password without digits
            final String noDigitsPassword = PASSWORD_NO_DIGIT;

            // When: Validating the password
            final RuleResult result = customPasswordValidator.validatePassword(noDigitsPassword);

            // Then: The validation should fail
            assertThat(result.isValid(), is(false));
            assertThat(customPasswordValidator.isWeakPassword(noDigitsPassword), is(true));
            assertThat(result.getDetails(), is(not(empty())));
        }

        @Test
        @DisplayName("Should return invalid result when password has no special characters")
        public void shouldReturnInvalidResultWhenPasswordHasNoSpecialCharacters() {
            // Given: A password without special characters
            final String noSpecialCharsPassword = PASSWORD_NO_SPECIAL;

            // When: Validating the password
            final RuleResult result = customPasswordValidator.validatePassword(noSpecialCharsPassword);

            // Then: The validation should fail
            assertThat(result.isValid(), is(false));
            assertThat(customPasswordValidator.isWeakPassword(noSpecialCharsPassword), is(true));
            assertThat(result.getDetails(), is(not(empty())));
        }

        @Test
        @DisplayName("Should return invalid result when password has only uppercase letters")
        public void shouldReturnInvalidResultWhenPasswordHasOnlyUppercaseLetters() {
            // Given: A password with only uppercase letters
            final String onlyUppercasePassword = "TESTPASSWORD";

            // When: Validating the password
            final RuleResult result = customPasswordValidator.validatePassword(onlyUppercasePassword);

            // Then: The validation should fail
            assertThat(result.isValid(), is(false));
            assertThat(customPasswordValidator.isWeakPassword(onlyUppercasePassword), is(true));
        }

        @Test
        @DisplayName("Should return invalid result when password has only lowercase letters")
        public void shouldReturnInvalidResultWhenPasswordHasOnlyLowercaseLetters() {
            // Given: A password with only lowercase letters
            final String onlyLowercasePassword = "testpassword";

            // When: Validating the password
            final RuleResult result = customPasswordValidator.validatePassword(onlyLowercasePassword);

            // Then: The validation should fail
            assertThat(result.isValid(), is(false));
            assertThat(customPasswordValidator.isWeakPassword(onlyLowercasePassword), is(true));
        }

        @Test
        @DisplayName("Should return invalid result when password has only digits")
        public void shouldReturnInvalidResultWhenPasswordHasOnlyDigits() {
            // Given: A password with only digits
            final String onlyDigitsPassword = "12345678";

            // When: Validating the password
            final RuleResult result = customPasswordValidator.validatePassword(onlyDigitsPassword);

            // Then: The validation should fail
            assertThat(result.isValid(), is(false));
            assertThat(customPasswordValidator.isWeakPassword(onlyDigitsPassword), is(true));
        }

        @Test
        @DisplayName("Should return invalid result when password has only special characters")
        public void shouldReturnInvalidResultWhenPasswordHasOnlySpecialCharacters() {
            // Given: A password with only special characters
            final String onlySpecialCharsPassword = "!@#$%^&*";

            // When: Validating the password
            final RuleResult result = customPasswordValidator.validatePassword(onlySpecialCharsPassword);

            // Then: The validation should fail
            assertThat(result.isValid(), is(false));
            assertThat(customPasswordValidator.isWeakPassword(onlySpecialCharsPassword), is(true));
        }
    }


    @Nested
    @DisplayName("Invalid Password - Whitespace")
    public class InvalidPasswordWhitespaceTests {

        @Test
        @DisplayName("Should return invalid result when password contains whitespace")
        public void shouldReturnInvalidResultWhenPasswordContainsWhitespace() {
            // Given: A password with whitespace in the middle
            final String passwordWithSpace = "Test 123!";

            // When: Validating the password
            final RuleResult result = customPasswordValidator.validatePassword(passwordWithSpace);

            // Then: The validation should fail
            assertThat(result.isValid(), is(false));
            assertThat(customPasswordValidator.isWeakPassword(passwordWithSpace), is(true));
            assertThat(result.getDetails(), is(not(empty())));
        }

        @Test
        @DisplayName("Should return invalid result when password starts with whitespace")
        public void shouldReturnInvalidResultWhenPasswordStartsWithWhitespace() {
            // Given: A password starting with whitespace
            final String passwordWithLeadingSpace = " Test123!";

            // When: Validating the password
            final RuleResult result = customPasswordValidator.validatePassword(passwordWithLeadingSpace);

            // Then: The validation should fail
            assertThat(result.isValid(), is(false));
            assertThat(customPasswordValidator.isWeakPassword(passwordWithLeadingSpace), is(true));
        }

        @Test
        @DisplayName("Should return invalid result when password ends with whitespace")
        public void shouldReturnInvalidResultWhenPasswordEndsWithWhitespace() {
            // Given: A password ending with whitespace
            final String passwordWithTrailingSpace = "Test123! ";

            // When: Validating the password
            final RuleResult result = customPasswordValidator.validatePassword(passwordWithTrailingSpace);

            // Then: The validation should fail
            assertThat(result.isValid(), is(false));
            assertThat(customPasswordValidator.isWeakPassword(passwordWithTrailingSpace), is(true));
        }

        @Test
        @DisplayName("Should return invalid result when password contains tab character")
        public void shouldReturnInvalidResultWhenPasswordContainsTabCharacter() {
            // Given: A password with tab character
            final String passwordWithTab = "Test\t123!";

            // When: Validating the password
            final RuleResult result = customPasswordValidator.validatePassword(passwordWithTab);

            // Then: The validation should fail
            assertThat(result.isValid(), is(false));
            assertThat(customPasswordValidator.isWeakPassword(passwordWithTab), is(true));
        }

        @Test
        @DisplayName("Should return invalid result when password contains newline character")
        public void shouldReturnInvalidResultWhenPasswordContainsNewlineCharacter() {
            // Given: A password with newline character
            final String passwordWithNewline = "Test\n123!";

            // When: Validating the password
            final RuleResult result = customPasswordValidator.validatePassword(passwordWithNewline);

            // Then: The validation should fail
            assertThat(result.isValid(), is(false));
            assertThat(customPasswordValidator.isWeakPassword(passwordWithNewline), is(true));
        }
    }


    @Nested
    @DisplayName("Invalid Password - Multiple Missing Requirements")
    public class InvalidPasswordMultipleRequirementsTests {

        @Test
        @DisplayName("Should return invalid result when password is missing multiple character types")
        public void shouldReturnInvalidResultWhenPasswordIsMissingMultipleCharacterTypes() {
            // Given: A password missing uppercase, digits, and special characters
            final String weakPassword = "testpassword";

            // When: Validating the password
            final RuleResult result = customPasswordValidator.validatePassword(weakPassword);

            // Then: The validation should fail
            assertThat(result.isValid(), is(false));
            assertThat(customPasswordValidator.isWeakPassword(weakPassword), is(true));

            // And: The result should contain multiple error details
            assertThat(result.getDetails().size(), is(greaterThan(1)));
        }

        @Test
        @DisplayName("Should return invalid result when password is too short and missing character types")
        public void shouldReturnInvalidResultWhenPasswordIsTooShortAndMissingCharacterTypes() {
            // Given: A short password missing multiple requirements
            final String weakPassword = "test";

            // When: Validating the password
            final RuleResult result = customPasswordValidator.validatePassword(weakPassword);

            // Then: The validation should fail
            assertThat(result.isValid(), is(false));
            assertThat(customPasswordValidator.isWeakPassword(weakPassword), is(true));

            // And: The result should contain multiple error details
            assertThat(result.getDetails().size(), is(greaterThan(1)));
        }
    }


    @Nested
    @DisplayName("Edge Cases")
    public class EdgeCaseTests {

        @Test
        @DisplayName("Should return valid result when password contains all printable ASCII special characters")
        public void shouldReturnValidResultWhenPasswordContainsAllPrintableAsciiSpecialCharacters() {
            // Given: A password with various ASCII special characters
            final String password = "Test123!@#$%^&*()_+-=[]{}|;:',.<>?/~`";

            // When: Validating the password
            final RuleResult result = customPasswordValidator.validatePassword(password);

            // Then: The validation should pass
            assertThat(result.isValid(), is(true));
            assertThat(customPasswordValidator.isWeakPassword(password), is(false));
        }

        @Test
        @DisplayName("Should return valid result when password is exactly 100 characters")
        public void shouldReturnValidResultWhenPasswordIsExactly100Characters() {
            // Given: A password with exactly 100 characters
            final String exactPassword = "Test123!" + "a".repeat(92);

            // When: Validating the password
            final RuleResult result = customPasswordValidator.validatePassword(exactPassword);

            // Then: The validation should pass
            assertThat(result.isValid(), is(true));
            assertThat(customPasswordValidator.isWeakPassword(exactPassword), is(false));
        }

        @Test
        @DisplayName("Should return valid result when password uses minimum of each required character type")
        public void shouldReturnValidResultWhenPasswordUsesMinimumOfEachRequiredCharacterType() {
            // Given: A password with exactly one of each required character type
            final String minRequiredPassword = "Aa1!aaaa";

            // When: Validating the password
            final RuleResult result = customPasswordValidator.validatePassword(minRequiredPassword);

            // Then: The validation should pass
            assertThat(result.isValid(), is(true));
            assertThat(customPasswordValidator.isWeakPassword(minRequiredPassword), is(false));
        }

        @Test
        @DisplayName("Should return consistent results when validating same password multiple times")
        public void shouldReturnConsistentResultsWhenValidatingSamePasswordMultipleTimes() {
            // Given: A valid password
            final String password = "Test123!";

            // When: Validating the password multiple times
            final RuleResult result1 = customPasswordValidator.validatePassword(password);
            final RuleResult result2 = customPasswordValidator.validatePassword(password);
            final RuleResult result3 = customPasswordValidator.validatePassword(password);

            // Then: All results should be consistent
            assertThat(result1.isValid(), is(true));
            assertThat(result2.isValid(), is(true));
            assertThat(result3.isValid(), is(true));

            // And: isWeakPassword should also be consistent
            assertThat(customPasswordValidator.isWeakPassword(password), is(false));
        }

        @Test
        @DisplayName("Should return invalid result when password exceeds maximum length of 100 characters")
        public void shouldReturnInvalidResultWhenPasswordExceedsMaximumLengthOf100Characters() {
            // Given: A password with more than 100 characters
            final String tooLongPassword = "Test123!" + "a".repeat(93);

            // When: Validating the password
            final RuleResult result = customPasswordValidator.validatePassword(tooLongPassword);

            // Then: The validation should fail
            assertThat(result.isValid(), is(false));
            assertThat(customPasswordValidator.isWeakPassword(tooLongPassword), is(true));
        }
    }


    @Nested
    @DisplayName("isWeakPassword Method Tests")
    public class IsWeakPasswordMethodTests {

        @Test
        @DisplayName("Should return false when password is strong")
        public void shouldReturnFalseWhenPasswordIsStrong() {
            // Given: A strong password
            final String strongPassword = "Test123!@#";

            // When: Checking if password is weak
            final boolean isWeak = customPasswordValidator.isWeakPassword(strongPassword);

            // Then: Should return false
            assertThat(isWeak, is(false));
        }

        @Test
        @DisplayName("Should return true when password is weak")
        public void shouldReturnTrueWhenPasswordIsWeak() {
            // Given: A weak password
            final String weakPassword = "weak";

            // When: Checking if password is weak
            final boolean isWeak = customPasswordValidator.isWeakPassword(weakPassword);

            // Then: Should return true
            assertThat(isWeak, is(true));
        }

        @Test
        @DisplayName("Should throw NullPointerException when password is null")
        public void shouldThrowNullPointerExceptionWhenPasswordIsNull() {
            // Given: A null password
            final String nullPassword = null;

            // When & Then: Checking if password is weak should throw NullPointerException
            assertThrows(
                NullPointerException.class,
                () -> customPasswordValidator.isWeakPassword(nullPassword)
            );
        }
    }
}
