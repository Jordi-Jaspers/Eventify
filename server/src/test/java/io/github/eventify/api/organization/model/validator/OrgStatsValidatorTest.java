package io.github.eventify.api.organization.model.validator;

import io.github.eventify.support.UnitTest;
import io.github.jframe.exception.core.ValidationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Unit Test - OrgStats Validator")
public class OrgStatsValidatorTest extends UnitTest {

    private OrgStatsValidator validator;

    @BeforeEach
    public void setUp() {
        validator = new OrgStatsValidator();
    }

    @Test
    @DisplayName("Should pass when days is 30 (default)")
    public void shouldPassWhenDaysIs30() {
        // Given: Default days value
        final int days = 30;

        // When/Then: No exception thrown
        assertDoesNotThrow(() -> validator.validateAndThrow(days));
    }

    @Test
    @DisplayName("Should pass when days is 1 (minimum)")
    public void shouldPassWhenDaysIs1() {
        // Given: Minimum valid days
        final int days = 1;

        // When/Then: No exception thrown
        assertDoesNotThrow(() -> validator.validateAndThrow(days));
    }

    @Test
    @DisplayName("Should pass when days is 365 (maximum)")
    public void shouldPassWhenDaysIs365() {
        // Given: Maximum valid days
        final int days = 365;

        // When/Then: No exception thrown
        assertDoesNotThrow(() -> validator.validateAndThrow(days));
    }

    @Test
    @DisplayName("Should fail when days is 0")
    public void shouldFailWhenDaysIsZero() {
        // Given: Zero days
        final int days = 0;

        // When/Then: Validation exception thrown
        assertThrows(
            ValidationException.class,
            () -> validator.validateAndThrow(days)
        );
    }

    @Test
    @DisplayName("Should fail when days is negative")
    public void shouldFailWhenDaysIsNegative() {
        // Given: Negative days
        final int days = -1;

        // When/Then: Validation exception thrown
        assertThrows(
            ValidationException.class,
            () -> validator.validateAndThrow(days)
        );
    }

    @Test
    @DisplayName("Should fail when days is 366 (above maximum)")
    public void shouldFailWhenDaysIs366() {
        // Given: Days above maximum
        final int days = 366;

        // When/Then: Validation exception thrown
        assertThrows(
            ValidationException.class,
            () -> validator.validateAndThrow(days)
        );
    }

    @Test
    @DisplayName("Should fail when days is 999")
    public void shouldFailWhenDaysIs999() {
        // Given: Days far above maximum
        final int days = 999;

        // When/Then: Validation exception thrown
        assertThrows(
            ValidationException.class,
            () -> validator.validateAndThrow(days)
        );
    }
}
