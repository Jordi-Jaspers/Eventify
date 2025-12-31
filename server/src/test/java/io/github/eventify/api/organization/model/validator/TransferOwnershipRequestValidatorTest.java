package io.github.eventify.api.organization.model.validator;

import io.github.eventify.api.organization.model.request.TransferOwnershipRequest;
import io.github.eventify.support.UnitTest;
import io.github.jframe.exception.core.ValidationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.github.eventify.api.organization.model.validator.TransferOwnershipRequestValidator.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit Test - Transfer Ownership Request Validator.
 */
@DisplayName("Unit Test - Transfer Ownership Request Validator")
public class TransferOwnershipRequestValidatorTest extends UnitTest {

    private TransferOwnershipRequestValidator validator;

    @BeforeEach
    public void setUp() {
        validator = new TransferOwnershipRequestValidator();
    }

    @Test
    @DisplayName("Should pass validation with valid newOwnerUserId")
    public void shouldPassValidationWithValidNewOwnerUserId() {
        // Given: Valid request with newOwnerUserId
        final TransferOwnershipRequest request = new TransferOwnershipRequest()
            .setNewOwnerUserId(123L);

        // When & Then: No exception thrown
        assertDoesNotThrow(() -> validator.validateAndThrow(request));
    }

    @Test
    @DisplayName("Should reject null newOwnerUserId")
    public void shouldRejectNullNewOwnerUserId() {
        // Given: Request with null newOwnerUserId
        final TransferOwnershipRequest request = new TransferOwnershipRequest()
            .setNewOwnerUserId(null);

        // When & Then: Throws validation exception
        final ValidationException exception = assertThrows(
            ValidationException.class,
            () -> validator.validateAndThrow(request)
        );

        assertThat(exception.getValidationResult().hasErrors(), is(true));
        assertThat(
            exception.getValidationResult().getErrors().stream()
                .anyMatch(error -> error.getField().equals(NEW_OWNER_USER_ID) && error.getCode().equals(NEW_OWNER_USER_ID_REQUIRED)),
            is(true)
        );
    }

    @Test
    @DisplayName("Should reject zero newOwnerUserId")
    public void shouldRejectZeroNewOwnerUserId() {
        // Given: Request with zero newOwnerUserId
        final TransferOwnershipRequest request = new TransferOwnershipRequest()
            .setNewOwnerUserId(0L);

        // When & Then: Throws validation exception
        final ValidationException exception = assertThrows(
            ValidationException.class,
            () -> validator.validateAndThrow(request)
        );

        assertThat(exception.getValidationResult().hasErrors(), is(true));
        assertThat(
            exception.getValidationResult().getErrors().stream()
                .anyMatch(
                    error -> error.getField().equals(NEW_OWNER_USER_ID) && error.getCode().equals(NEW_OWNER_USER_ID_MUST_BE_POSITIVE)
                ),
            is(true)
        );
    }

    @Test
    @DisplayName("Should reject negative newOwnerUserId")
    public void shouldRejectNegativeNewOwnerUserId() {
        // Given: Request with negative newOwnerUserId
        final TransferOwnershipRequest request = new TransferOwnershipRequest()
            .setNewOwnerUserId(-1L);

        // When & Then: Throws validation exception
        final ValidationException exception = assertThrows(
            ValidationException.class,
            () -> validator.validateAndThrow(request)
        );

        assertThat(exception.getValidationResult().hasErrors(), is(true));
        assertThat(
            exception.getValidationResult().getErrors().stream()
                .anyMatch(
                    error -> error.getField().equals(NEW_OWNER_USER_ID) && error.getCode().equals(NEW_OWNER_USER_ID_MUST_BE_POSITIVE)
                ),
            is(true)
        );
    }
}
