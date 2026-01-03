package io.github.eventify.api.organization.model.validator;

import io.github.eventify.api.admin.model.request.AssignOwnerRequest;
import io.github.eventify.api.organization.model.OrganizationalRole;
import io.github.eventify.api.organization.model.request.AddMemberRequest;
import io.github.eventify.api.organization.model.request.TransferOwnershipRequest;
import io.github.eventify.api.organization.model.request.UpdateMemberRoleRequest;
import io.github.eventify.support.UnitTest;
import io.github.jframe.exception.core.ValidationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.github.eventify.api.organization.model.validator.OrganizationMembershipValidator.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit Test - Organization Membership Validator.
 */
@DisplayName("Unit Test - Organization Membership Validator")
public class OrganizationMembershipValidatorTest extends UnitTest {

    private OrganizationMembershipValidator validator;

    @BeforeEach
    public void setUp() {
        validator = new OrganizationMembershipValidator();
    }

    @Nested
    @DisplayName("Add Member Validation")
    class AddMemberValidationTests {

        @Test
        @DisplayName("Should pass validation with valid email and MEMBER role")
        public void shouldPassValidationWithValidEmailAndMemberRole() {
            // Given: Valid request with MEMBER role
            final AddMemberRequest request = new AddMemberRequest()
                .setEmail(VALID_EMAIL)
                .setRole(OrganizationalRole.MEMBER);

            // When & Then: No exception thrown
            assertDoesNotThrow(() -> validator.validateAddMember(request));
        }

        @Test
        @DisplayName("Should pass validation with valid email and ADMIN role")
        public void shouldPassValidationWithValidEmailAndAdminRole() {
            // Given: Valid request with ADMIN role
            final AddMemberRequest request = new AddMemberRequest()
                .setEmail(VALID_EMAIL)
                .setRole(OrganizationalRole.ADMIN);

            // When & Then: No exception thrown
            assertDoesNotThrow(() -> validator.validateAddMember(request));
        }

        @Test
        @DisplayName("Should reject null request")
        public void shouldRejectNullRequest() {
            // When & Then: Throws validation exception
            final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> validator.validateAddMember(null)
            );

            assertThat(exception.getValidationResult().hasErrors(), is(true));
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
                () -> validator.validateAddMember(request)
            );

            assertThat(exception.getValidationResult().hasErrors(), is(true));
            assertThat(
                exception.getValidationResult().getErrors().stream()
                    .anyMatch(error -> error.getCode().equals(EMAIL_REQUIRED)),
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
                () -> validator.validateAddMember(request)
            );

            assertThat(exception.getValidationResult().hasErrors(), is(true));
            assertThat(
                exception.getValidationResult().getErrors().stream()
                    .anyMatch(error -> error.getCode().equals(EMAIL_BLANK)),
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
                () -> validator.validateAddMember(request)
            );

            assertThat(exception.getValidationResult().hasErrors(), is(true));
            assertThat(
                exception.getValidationResult().getErrors().stream()
                    .anyMatch(error -> error.getCode().equals(EMAIL_BLANK)),
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
                () -> validator.validateAddMember(request)
            );

            assertThat(exception.getValidationResult().hasErrors(), is(true));
            assertThat(
                exception.getValidationResult().getErrors().stream()
                    .anyMatch(error -> error.getCode().equals(EMAIL_INVALID_FORMAT)),
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
                () -> validator.validateAddMember(request)
            );

            assertThat(exception.getValidationResult().hasErrors(), is(true));
            assertThat(
                exception.getValidationResult().getErrors().stream()
                    .anyMatch(error -> error.getCode().equals(ROLE_REQUIRED)),
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
                () -> validator.validateAddMember(request)
            );

            assertThat(exception.getValidationResult().hasErrors(), is(true));
            assertThat(
                exception.getValidationResult().getErrors().stream()
                    .anyMatch(error -> error.getCode().equals(ROLE_CANNOT_BE_OWNER)),
                is(true)
            );
        }
    }


    @Nested
    @DisplayName("Update Role Validation")
    class UpdateRoleValidationTests {

        @Test
        @DisplayName("Should pass validation with MEMBER role")
        public void shouldPassValidationWithMemberRole() {
            // Given: Valid request with MEMBER role
            final UpdateMemberRoleRequest request = new UpdateMemberRoleRequest()
                .setRole(OrganizationalRole.MEMBER);

            // When & Then: No exception thrown
            assertDoesNotThrow(() -> validator.validateUpdateRole(request));
        }

        @Test
        @DisplayName("Should pass validation with ADMIN role")
        public void shouldPassValidationWithAdminRole() {
            // Given: Valid request with ADMIN role
            final UpdateMemberRoleRequest request = new UpdateMemberRoleRequest()
                .setRole(OrganizationalRole.ADMIN);

            // When & Then: No exception thrown
            assertDoesNotThrow(() -> validator.validateUpdateRole(request));
        }

        @Test
        @DisplayName("Should reject null request")
        public void shouldRejectNullRequest() {
            // When & Then: Throws validation exception
            final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> validator.validateUpdateRole(null)
            );

            assertThat(exception.getValidationResult().hasErrors(), is(true));
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
                () -> validator.validateUpdateRole(request)
            );

            assertThat(exception.getValidationResult().hasErrors(), is(true));
            assertThat(
                exception.getValidationResult().getErrors().stream()
                    .anyMatch(error -> error.getCode().equals(ROLE_REQUIRED)),
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
                () -> validator.validateUpdateRole(request)
            );

            assertThat(exception.getValidationResult().hasErrors(), is(true));
            assertThat(
                exception.getValidationResult().getErrors().stream()
                    .anyMatch(error -> error.getCode().equals(UPDATE_ROLE_CANNOT_BE_OWNER)),
                is(true)
            );
        }
    }


    @Nested
    @DisplayName("Transfer Ownership Validation")
    class TransferOwnershipValidationTests {

        @Test
        @DisplayName("Should pass validation with valid current and new owner user IDs")
        public void shouldPassValidationWithValidUserIds() {
            // Given: Valid request with both currentOwnerUserId and newOwnerUserId
            final TransferOwnershipRequest request = new TransferOwnershipRequest()
                .setCurrentOwnerUserId(1L)
                .setNewOwnerUserId(2L);

            // When & Then: No exception thrown
            assertDoesNotThrow(() -> validator.validateTransferOwnership(request));
        }

        @Test
        @DisplayName("Should reject null request")
        public void shouldRejectNullRequest() {
            // When & Then: Throws validation exception
            final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> validator.validateTransferOwnership(null)
            );

            assertThat(exception.getValidationResult().hasErrors(), is(true));
        }

        @Test
        @DisplayName("Should reject null currentOwnerUserId")
        public void shouldRejectNullCurrentOwnerUserId() {
            // Given: Request with null currentOwnerUserId
            final TransferOwnershipRequest request = new TransferOwnershipRequest()
                .setCurrentOwnerUserId(null)
                .setNewOwnerUserId(2L);

            // When & Then: Throws validation exception
            final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> validator.validateTransferOwnership(request)
            );

            assertThat(exception.getValidationResult().hasErrors(), is(true));
            assertThat(
                exception.getValidationResult().getErrors().stream()
                    .anyMatch(error -> error.getCode().equals(CURRENT_OWNER_USER_ID_REQUIRED)),
                is(true)
            );
        }

        @Test
        @DisplayName("Should reject zero currentOwnerUserId")
        public void shouldRejectZeroCurrentOwnerUserId() {
            // Given: Request with zero currentOwnerUserId
            final TransferOwnershipRequest request = new TransferOwnershipRequest()
                .setCurrentOwnerUserId(0L)
                .setNewOwnerUserId(2L);

            // When & Then: Throws validation exception
            final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> validator.validateTransferOwnership(request)
            );

            assertThat(exception.getValidationResult().hasErrors(), is(true));
            assertThat(
                exception.getValidationResult().getErrors().stream()
                    .anyMatch(error -> error.getCode().equals(CURRENT_OWNER_USER_ID_MUST_BE_POSITIVE)),
                is(true)
            );
        }

        @Test
        @DisplayName("Should reject negative currentOwnerUserId")
        public void shouldRejectNegativeCurrentOwnerUserId() {
            // Given: Request with negative currentOwnerUserId
            final TransferOwnershipRequest request = new TransferOwnershipRequest()
                .setCurrentOwnerUserId(-1L)
                .setNewOwnerUserId(2L);

            // When & Then: Throws validation exception
            final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> validator.validateTransferOwnership(request)
            );

            assertThat(exception.getValidationResult().hasErrors(), is(true));
            assertThat(
                exception.getValidationResult().getErrors().stream()
                    .anyMatch(error -> error.getCode().equals(CURRENT_OWNER_USER_ID_MUST_BE_POSITIVE)),
                is(true)
            );
        }

        @Test
        @DisplayName("Should reject null newOwnerUserId")
        public void shouldRejectNullNewOwnerUserId() {
            // Given: Request with null newOwnerUserId
            final TransferOwnershipRequest request = new TransferOwnershipRequest()
                .setCurrentOwnerUserId(1L)
                .setNewOwnerUserId(null);

            // When & Then: Throws validation exception
            final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> validator.validateTransferOwnership(request)
            );

            assertThat(exception.getValidationResult().hasErrors(), is(true));
            assertThat(
                exception.getValidationResult().getErrors().stream()
                    .anyMatch(error -> error.getCode().equals(NEW_OWNER_USER_ID_REQUIRED)),
                is(true)
            );
        }

        @Test
        @DisplayName("Should reject zero newOwnerUserId")
        public void shouldRejectZeroNewOwnerUserId() {
            // Given: Request with zero newOwnerUserId
            final TransferOwnershipRequest request = new TransferOwnershipRequest()
                .setCurrentOwnerUserId(1L)
                .setNewOwnerUserId(0L);

            // When & Then: Throws validation exception
            final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> validator.validateTransferOwnership(request)
            );

            assertThat(exception.getValidationResult().hasErrors(), is(true));
            assertThat(
                exception.getValidationResult().getErrors().stream()
                    .anyMatch(error -> error.getCode().equals(NEW_OWNER_USER_ID_MUST_BE_POSITIVE)),
                is(true)
            );
        }

        @Test
        @DisplayName("Should reject negative newOwnerUserId")
        public void shouldRejectNegativeNewOwnerUserId() {
            // Given: Request with negative newOwnerUserId
            final TransferOwnershipRequest request = new TransferOwnershipRequest()
                .setCurrentOwnerUserId(1L)
                .setNewOwnerUserId(-1L);

            // When & Then: Throws validation exception
            final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> validator.validateTransferOwnership(request)
            );

            assertThat(exception.getValidationResult().hasErrors(), is(true));
            assertThat(
                exception.getValidationResult().getErrors().stream()
                    .anyMatch(error -> error.getCode().equals(NEW_OWNER_USER_ID_MUST_BE_POSITIVE)),
                is(true)
            );
        }
    }


    @Nested
    @DisplayName("Assign Owner Validation")
    class AssignOwnerValidationTests {

        @Test
        @DisplayName("Should pass validation with valid email")
        public void shouldPassValidationWithValidEmail() {
            // Given: Valid request with email
            final AssignOwnerRequest request = new AssignOwnerRequest()
                .setEmail(VALID_EMAIL);

            // When & Then: No exception thrown
            assertDoesNotThrow(() -> validator.validateAssignOwner(request));
        }

        @Test
        @DisplayName("Should pass validation with valid userId")
        public void shouldPassValidationWithValidUserId() {
            // Given: Valid request with userId
            final AssignOwnerRequest request = new AssignOwnerRequest()
                .setUserId(123L);

            // When & Then: No exception thrown
            assertDoesNotThrow(() -> validator.validateAssignOwner(request));
        }

        @Test
        @DisplayName("Should pass validation with both email and userId")
        public void shouldPassValidationWithBothEmailAndUserId() {
            // Given: Valid request with both email and userId
            final AssignOwnerRequest request = new AssignOwnerRequest()
                .setEmail(VALID_EMAIL)
                .setUserId(123L);

            // When & Then: No exception thrown
            assertDoesNotThrow(() -> validator.validateAssignOwner(request));
        }

        @Test
        @DisplayName("Should reject null request")
        public void shouldRejectNullRequest() {
            // When & Then: Throws validation exception
            final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> validator.validateAssignOwner(null)
            );

            assertThat(exception.getValidationResult().hasErrors(), is(true));
        }

        @Test
        @DisplayName("Should reject when both email and userId are null")
        public void shouldRejectWhenBothEmailAndUserIdAreNull() {
            // Given: Request with neither email nor userId
            final AssignOwnerRequest request = new AssignOwnerRequest();

            // When & Then: Throws validation exception
            final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> validator.validateAssignOwner(request)
            );

            assertThat(exception.getValidationResult().hasErrors(), is(true));
            assertThat(
                exception.getValidationResult().getErrors().stream()
                    .anyMatch(error -> error.getCode().equals(EMAIL_OR_USER_ID_REQUIRED)),
                is(true)
            );
        }

        @Test
        @DisplayName("Should reject invalid email format")
        public void shouldRejectInvalidEmailFormat() {
            // Given: Request with invalid email format
            final AssignOwnerRequest request = new AssignOwnerRequest()
                .setEmail("not-an-email");

            // When & Then: Throws validation exception
            final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> validator.validateAssignOwner(request)
            );

            assertThat(exception.getValidationResult().hasErrors(), is(true));
            assertThat(
                exception.getValidationResult().getErrors().stream()
                    .anyMatch(error -> error.getCode().equals(EMAIL_INVALID_FORMAT)),
                is(true)
            );
        }
    }
}
