package io.github.eventify.api.organization.model.validator;

import io.github.eventify.api.organization.model.request.ProvisionOrganizationRequest;
import io.github.eventify.support.UnitTest;
import io.github.jframe.exception.core.ValidationException;
import io.github.jframe.validation.ValidationResult;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.github.eventify.api.organization.model.validator.OrganizationValidator.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit Test - Provision Organization Validator.
 */
@DisplayName("Unit Test - Provision Organization Validator")
public class OrganizationValidatorTest extends UnitTest {

    private OrganizationValidator validator;

    @BeforeEach
    public void setUp() {
        validator = new OrganizationValidator();
    }

    @Test
    @DisplayName("Should accept valid organization name")
    public void shouldAcceptValidOrganizationName() {
        // Given: Valid organization request
        final ProvisionOrganizationRequest request = new ProvisionOrganizationRequest();
        request.setName("Acme Corp");
        request.setOwner("owner@example.com");
        final ValidationResult result = new ValidationResult();

        // When: Validating request
        validator.validate(request, result);

        // Then: Validation should pass
        assertThat(result.hasErrors(), is(false));
    }

    @Test
    @DisplayName("Should reject null request body")
    public void shouldRejectNullRequestBody() {
        // Given: Null request
        final ProvisionOrganizationRequest request = null;
        final ValidationResult result = new ValidationResult();

        // When & Then: Should throw ValidationException
        final ValidationException exception = assertThrows(
            ValidationException.class,
            () -> validator.validate(request, result)
        );

        assertThat(exception.getValidationResult().getErrors().size(), is(1));
        assertThat(
            exception.getValidationResult().getErrors().get(0).getCode(),
            is(equalTo(BODY_IS_MISSING))
        );
    }

    @Test
    @DisplayName("Should reject null name")
    public void shouldRejectNullName() {
        // Given: Request with null name
        final ProvisionOrganizationRequest request = new ProvisionOrganizationRequest();
        request.setName(null);
        final ValidationResult result = new ValidationResult();

        // When & Then: Should throw ValidationException
        final ValidationException exception = assertThrows(
            ValidationException.class,
            () -> validator.validate(request, result)
        );

        assertThat(exception.getValidationResult().hasErrors(), is(true));
        assertThat(
            exception.getValidationResult().getErrors().stream()
                .anyMatch(
                    error -> error.getField().equals(NAME) &&
                        error.getCode().equals(NAME_REQUIRED)
                ),
            is(true)
        );
    }

    @Test
    @DisplayName("Should reject blank name")
    public void shouldRejectBlankName() {
        // Given: Request with blank name
        final ProvisionOrganizationRequest request = new ProvisionOrganizationRequest();
        request.setName("   ");
        final ValidationResult result = new ValidationResult();

        // When & Then: Should throw ValidationException
        final ValidationException exception = assertThrows(
            ValidationException.class,
            () -> validator.validate(request, result)
        );

        assertThat(exception.getValidationResult().hasErrors(), is(true));
        assertThat(
            exception.getValidationResult().getErrors().stream()
                .anyMatch(
                    error -> error.getField().equals(NAME) &&
                        error.getCode().equals(NAME_BLANK)
                ),
            is(true)
        );
    }

    @Test
    @DisplayName("Should reject empty name")
    public void shouldRejectEmptyName() {
        // Given: Request with empty name
        final ProvisionOrganizationRequest request = new ProvisionOrganizationRequest();
        request.setName("");
        final ValidationResult result = new ValidationResult();

        // When & Then: Should throw ValidationException
        final ValidationException exception = assertThrows(
            ValidationException.class,
            () -> validator.validate(request, result)
        );

        assertThat(exception.getValidationResult().hasErrors(), is(true));
        assertThat(
            exception.getValidationResult().getErrors().stream()
                .anyMatch(
                    error -> error.getField().equals(NAME) &&
                        error.getCode().equals(NAME_BLANK)
                ),
            is(true)
        );
    }

    @Test
    @DisplayName("Should reject name too short")
    public void shouldRejectNameTooShort() {
        // Given: Request with name less than 3 characters
        final ProvisionOrganizationRequest request = new ProvisionOrganizationRequest();
        request.setName("AB");
        final ValidationResult result = new ValidationResult();

        // When & Then: Should throw ValidationException
        final ValidationException exception = assertThrows(
            ValidationException.class,
            () -> validator.validate(request, result)
        );

        assertThat(exception.getValidationResult().hasErrors(), is(true));
        assertThat(
            exception.getValidationResult().getErrors().stream()
                .anyMatch(
                    error -> error.getField().equals(NAME) &&
                        error.getCode().equals(NAME_TOO_SHORT)
                ),
            is(true)
        );
    }

    @Test
    @DisplayName("Should reject name too long")
    public void shouldRejectNameTooLong() {
        // Given: Request with name exceeding 100 characters
        final ProvisionOrganizationRequest request = new ProvisionOrganizationRequest();
        request.setName("A".repeat(101));
        final ValidationResult result = new ValidationResult();

        // When & Then: Should throw ValidationException
        final ValidationException exception = assertThrows(
            ValidationException.class,
            () -> validator.validate(request, result)
        );

        assertThat(exception.getValidationResult().hasErrors(), is(true));
        assertThat(
            exception.getValidationResult().getErrors().stream()
                .anyMatch(
                    error -> error.getField().equals(NAME) &&
                        error.getCode().equals(NAME_TOO_LONG)
                ),
            is(true)
        );
    }

    @Test
    @DisplayName("Should accept name with exactly 3 characters")
    public void shouldAcceptNameWithExactly3Characters() {
        // Given: Request with name of exactly 3 characters
        final ProvisionOrganizationRequest request = new ProvisionOrganizationRequest();
        request.setName("ABC");
        request.setOwner("owner@example.com");
        final ValidationResult result = new ValidationResult();

        // When: Validating request
        validator.validate(request, result);

        // Then: Validation should pass
        assertThat(result.hasErrors(), is(false));
    }

    @Test
    @DisplayName("Should accept name with exactly 100 characters")
    public void shouldAcceptNameWithExactly100Characters() {
        // Given: Request with name of exactly 100 characters
        final ProvisionOrganizationRequest request = new ProvisionOrganizationRequest();
        request.setName("A".repeat(100));
        request.setOwner("owner@example.com");
        final ValidationResult result = new ValidationResult();

        // When: Validating request
        validator.validate(request, result);

        // Then: Validation should pass
        assertThat(result.hasErrors(), is(false));
    }
}
