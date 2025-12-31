package io.github.eventify.api.organization.model.validator;

import io.github.eventify.api.organization.model.OrganizationalRole;
import io.github.eventify.api.organization.model.request.UpdateMemberRoleRequest;
import io.github.eventify.support.UnitTest;
import io.github.jframe.exception.core.ValidationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.github.eventify.api.organization.model.validator.UpdateMemberRoleRequestValidator.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit Test - Update Member Role Request Validator.
 */
@DisplayName("Unit Test - Update Member Role Request Validator")
public class UpdateMemberRoleRequestValidatorTest extends UnitTest {

    private UpdateMemberRoleRequestValidator validator;

    @BeforeEach
    public void setUp() {
        validator = new UpdateMemberRoleRequestValidator();
    }

    @Test
    @DisplayName("Should pass validation with MEMBER role")
    public void shouldPassValidationWithMemberRole() {
        // Given: Valid request with MEMBER role
        final UpdateMemberRoleRequest request = new UpdateMemberRoleRequest()
            .setRole(OrganizationalRole.MEMBER);

        // When & Then: No exception thrown
        assertDoesNotThrow(() -> validator.validateAndThrow(request));
    }

    @Test
    @DisplayName("Should pass validation with ADMIN role")
    public void shouldPassValidationWithAdminRole() {
        // Given: Valid request with ADMIN role
        final UpdateMemberRoleRequest request = new UpdateMemberRoleRequest()
            .setRole(OrganizationalRole.ADMIN);

        // When & Then: No exception thrown
        assertDoesNotThrow(() -> validator.validateAndThrow(request));
    }

    @Test
    @DisplayName("Should reject null role")
    public void shouldRejectNullRole() {
        // Given: Request with null role
        final UpdateMemberRoleRequest request = new UpdateMemberRoleRequest()
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
        // Given: Request with OWNER role (use transfer ownership instead)
        final UpdateMemberRoleRequest request = new UpdateMemberRoleRequest()
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
