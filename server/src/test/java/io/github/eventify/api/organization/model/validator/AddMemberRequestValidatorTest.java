package io.github.eventify.api.organization.model.validator;

import io.github.eventify.api.organization.model.OrganizationalRole;
import io.github.eventify.api.organization.model.request.AddMemberRequest;
import io.github.eventify.support.UnitTest;
import io.github.jframe.exception.core.ValidationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.github.eventify.api.organization.model.validator.AddMemberRequestValidator.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit Test - Add Member Request Validator.
 */
@DisplayName("Unit Test - Add Member Request Validator")
public class AddMemberRequestValidatorTest extends UnitTest {

    private AddMemberRequestValidator validator;

    @BeforeEach
    public void setUp() {
        validator = new AddMemberRequestValidator();
    }

    @Test
    @DisplayName("Should pass validation with valid email and MEMBER role")
    public void shouldPassValidationWithValidEmailAndMemberRole() {
        // Given: Valid request with MEMBER role
        final AddMemberRequest request = new AddMemberRequest()
            .setEmail(VALID_EMAIL)
            .setRole(OrganizationalRole.MEMBER);

        // When & Then: No exception thrown
        assertDoesNotThrow(() -> validator.validateAndThrow(request));
    }

    @Test
    @DisplayName("Should pass validation with valid email and ADMIN role")
    public void shouldPassValidationWithValidEmailAndAdminRole() {
        // Given: Valid request with ADMIN role
        final AddMemberRequest request = new AddMemberRequest()
            .setEmail(VALID_EMAIL)
            .setRole(OrganizationalRole.ADMIN);

        // When & Then: No exception thrown
        assertDoesNotThrow(() -> validator.validateAndThrow(request));
    }

    @Test
    @DisplayName("Should reject null email")
    public void shouldRejectNullEmail() {
        // Given: Request with null email
        final AddMemberRequest request = new AddMemberRequest()
            .setEmail(null)
            .setRole(OrganizationalRole.MEMBER);

        // When & Then: Throws validation exception
        final ValidationException exception = assertThrows(
            ValidationException.class,
            () -> validator.validateAndThrow(request)
        );

        assertThat(exception.getValidationResult().hasErrors(), is(true));
        assertThat(
            exception.getValidationResult().getErrors().stream()
                .anyMatch(error -> error.getField().equals(EMAIL) && error.getCode().equals(EMAIL_REQUIRED)),
            is(true)
        );
    }

    @Test
    @DisplayName("Should reject blank email")
    public void shouldRejectBlankEmail() {
        // Given: Request with blank email
        final AddMemberRequest request = new AddMemberRequest()
            .setEmail("   ")
            .setRole(OrganizationalRole.MEMBER);

        // When & Then: Throws validation exception
        final ValidationException exception = assertThrows(
            ValidationException.class,
            () -> validator.validateAndThrow(request)
        );

        assertThat(exception.getValidationResult().hasErrors(), is(true));
        assertThat(
            exception.getValidationResult().getErrors().stream()
                .anyMatch(error -> error.getField().equals(EMAIL) && error.getCode().equals(EMAIL_BLANK)),
            is(true)
        );
    }

    @Test
    @DisplayName("Should reject empty email")
    public void shouldRejectEmptyEmail() {
        // Given: Request with empty email
        final AddMemberRequest request = new AddMemberRequest()
            .setEmail("")
            .setRole(OrganizationalRole.MEMBER);

        // When & Then: Throws validation exception
        final ValidationException exception = assertThrows(
            ValidationException.class,
            () -> validator.validateAndThrow(request)
        );

        assertThat(exception.getValidationResult().hasErrors(), is(true));
        assertThat(
            exception.getValidationResult().getErrors().stream()
                .anyMatch(error -> error.getField().equals(EMAIL) && error.getCode().equals(EMAIL_BLANK)),
            is(true)
        );
    }

    @Test
    @DisplayName("Should reject invalid email format")
    public void shouldRejectInvalidEmailFormat() {
        // Given: Request with invalid email format
        final AddMemberRequest request = new AddMemberRequest()
            .setEmail("not-an-email")
            .setRole(OrganizationalRole.MEMBER);

        // When & Then: Throws validation exception
        final ValidationException exception = assertThrows(
            ValidationException.class,
            () -> validator.validateAndThrow(request)
        );

        assertThat(exception.getValidationResult().hasErrors(), is(true));
        assertThat(
            exception.getValidationResult().getErrors().stream()
                .anyMatch(error -> error.getField().equals(EMAIL) && error.getCode().equals(EMAIL_INVALID_FORMAT)),
            is(true)
        );
    }

    @Test
    @DisplayName("Should reject null role")
    public void shouldRejectNullRole() {
        // Given: Request with null role
        final AddMemberRequest request = new AddMemberRequest()
            .setEmail(VALID_EMAIL)
            .setRole(null);

        // When & Then: Throws validation exception
        final ValidationException exception = assertThrows(
            ValidationException.class,
            () -> validator.validateAndThrow(request)
        );

        assertThat(exception.getValidationResult().hasErrors(), is(true));
        assertThat(
            exception.getValidationResult().getErrors().stream()
                .anyMatch(error -> error.getField().equals(ROLE) && error.getCode().equals(ROLE_REQUIRED)),
            is(true)
        );
    }

    @Test
    @DisplayName("Should reject OWNER role")
    public void shouldRejectOwnerRole() {
        // Given: Request with OWNER role (cannot directly add owner)
        final AddMemberRequest request = new AddMemberRequest()
            .setEmail(VALID_EMAIL)
            .setRole(OrganizationalRole.OWNER);

        // When & Then: Throws validation exception
        final ValidationException exception = assertThrows(
            ValidationException.class,
            () -> validator.validateAndThrow(request)
        );

        assertThat(exception.getValidationResult().hasErrors(), is(true));
        assertThat(
            exception.getValidationResult().getErrors().stream()
                .anyMatch(error -> error.getField().equals(ROLE) && error.getCode().equals(ROLE_CANNOT_BE_OWNER)),
            is(true)
        );
    }
}
