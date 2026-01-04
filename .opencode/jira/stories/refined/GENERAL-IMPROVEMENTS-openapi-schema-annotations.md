# OpenAPI Schema Annotations for API Models

**Epic**: General Improvements
**Status**: Ready for Dev
**Estimate**: M (Medium)
**Created Date**: 2026-01-04

## 1. User Story
**As a** developer integrating with the Eventify API
**I want** comprehensive OpenAPI documentation with descriptions and examples for all request/response fields
**So that** I can understand the API contract without reading source code

## 2. Business Context & Value
The current API models lack `@Schema` annotations on fields, resulting in auto-generated OpenAPI docs with minimal context. This impacts:
- Developer onboarding time (must read source code to understand fields)
- API consumer experience (no examples in Swagger UI)
- Client code generation quality (missing field descriptions)

This story adds proper OpenAPI annotations to all request and response DTOs, improving API discoverability and self-documentation.

## 3. Acceptance Criteria
*   [ ] **Scenario 1**: All request model fields have @Schema annotations
    *   Given I view the OpenAPI spec (`/v3/api-docs`)
    *   When I inspect any request body schema
    *   Then each field has a `description` property
    *   And each field has an `example` value

*   [ ] **Scenario 2**: All response model fields have @Schema annotations
    *   Given I view the OpenAPI spec
    *   When I inspect any response schema
    *   Then each field has a `description` property
    *   And each field has an `example` value

*   [ ] **Scenario 3**: Javadoc on fields is removed
    *   Given I open any request/response model Java file
    *   When I inspect field declarations
    *   Then there are no Javadoc comments on individual fields
    *   And the field documentation exists only in `@Schema` annotations

*   [ ] **Scenario 4**: Class-level Javadoc is preserved
    *   Given I open any request/response model Java file
    *   When I inspect the class declaration
    *   Then the class-level Javadoc comment is still present (if it existed before)

*   [ ] **Scenario 5**: OpenAPI spec regenerates correctly
    *   Given I run the application
    *   When I access `/v3/api-docs`
    *   Then the JSON spec includes all new descriptions and examples
    *   And the spec is valid OpenAPI 3.0

## 4. Technical Requirements
*   **Annotation Pattern**:
    ```java
    @Schema(
        description = "Human-readable description of the field",
        example = "generic_placeholder_value"
    )
    private String fieldName;
    ```

*   **Required Mode**: Use `requiredMode = Schema.RequiredMode.REQUIRED` only for fields that are truly mandatory. Do NOT add validation annotations.

*   **Files to Update** (14 Request classes):
    | Package | Class |
    |---------|-------|
    | `api.authentication.model.request` | `LoginRequest` |
    | `api.authentication.model.request` | `RefreshTokenRequest` |
    | `api.authentication.model.request` | `RegisterUserRequest` |
    | `api.user.model.request` | `ForgotPasswordRequest` |
    | `api.user.model.request` | `PasswordRequest` |
    | `api.user.model.request` | `UpdatePasswordRequest` |
    | `api.user.model.request` | `UpdateRoleRequest` |
    | `api.user.model.request` | `UpdateUserDetailsRequest` |
    | `api.organization.model.request` | `AddMemberRequest` |
    | `api.organization.model.request` | `ProvisionOrganizationRequest` |
    | `api.organization.model.request` | `TransferOwnershipRequest` |
    | `api.organization.model.request` | `UpdateMemberRoleRequest` |
    | `api.admin.model.request` | `AssignOwnerRequest` |

*   **Files to Update** (11 Response classes):
    | Package | Class |
    |---------|-------|
    | `api.authentication.model.response` | `AuthenticationResponse` |
    | `api.authentication.model.response` | `RegisterResponse` |
    | `api.authentication.model.response` | `RoleResponse` |
    | `api.token.model.response` | `AuthenticationTokenResponse` |
    | `api.user.model.response` | `UserResponse` |
    | `api.user.model.response` | `UserDetailsResponse` |
    | `api.organization.model.response` | `OrganizationResponse` |
    | `api.organization.model.response` | `OrganizationMembershipResponse` |
    | `api.organization.model.response` | `UserOrganizationResponse` |
    | `api.admin.model.response` | `AdminStatsResponse` |
    | `api.bootstrap.model.response` | `DevCredentialsResponse` |

*   **Example Values** (use generic placeholders):
    | Field Type | Example Pattern |
    |------------|-----------------|
    | Email | `"user@example.com"` |
    | First/Last Name | `"John"`, `"Doe"` |
    | Password | `"********"` |
    | ID (Long) | `12345` |
    | Token | `"eyJhbGciOiJIUzI1NiIs..."` |
    | DateTime | `"2026-01-15T10:30:00Z"` |
    | Boolean | `true` or `false` |
    | Enum | First value of enum |
    | Role | `"USER"` or `"ADMIN"` |

*   **Import**: Add `import io.swagger.v3.oas.annotations.media.Schema;` to each file

## 5. Design & UI/UX
*   N/A - This is a backend documentation task

## 6. Implementation Notes / Research
*   **Existing Pattern**: See `ApiErrorCode.java`, `Role.java` for existing `@Schema` usage on enums
*   **OpenAPI Dependency**: Already present via `springdoc-openapi` (see existing `@Operation`, `@Tag` annotations in controllers)
*   **Field Description Sources**:
    *   Use existing Javadoc content if present (then remove the Javadoc)
    *   Write concise descriptions (1 sentence) for fields without Javadoc
*   **Regenerate OpenAPI JSON**: After implementation, run `./gradlew clean bootRun` and verify `/v3/api-docs` output
*   **Example Files**:
    *   Request without annotations: `LoginRequest.java` - has no `@Schema`
    *   Response without annotations: `AuthenticationResponse.java` - has no `@Schema`
*   **Estimated Effort**: ~25 classes × ~5 fields avg = ~125 field annotations
*   **Quality Check**: Run application and verify Swagger UI (`/swagger-ui.html` if configured) shows descriptions and examples
