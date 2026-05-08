package io.github.eventify.api.user.model.response;

import io.github.eventify.api.authentication.model.Permission;
import io.github.eventify.api.authentication.model.Role;
import io.github.eventify.api.organization.model.response.UserOrganizationResponse;
import io.github.jframe.datasource.search.model.resource.PageableItemResource;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * A response model for user details.
 */
@Data
@NoArgsConstructor
public class UserDetailsResponse implements PageableItemResource {

    @Schema(
        description = "Unique user identifier",
        example = "12345",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Long id;

    @Schema(
        description = "User's email address",
        example = "user@example.com",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String email;

    @Schema(
        description = "User's first name",
        example = "John",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String firstName;

    @Schema(
        description = "User's last name",
        example = "Doe",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String lastName;

    @Schema(
        description = "Timestamp of the user's last login",
        example = "2026-01-15T10:30:00Z",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private ZonedDateTime lastLogin;

    @Schema(
        description = "Timestamp when the user account was created",
        example = "2026-01-15T10:30:00Z",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private ZonedDateTime createdAt;

    @Schema(
        description = "User's system role",
        example = "USER",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Role role;

    @Schema(
        description = "Set of permissions granted to the user",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Set<Permission> permissions = EnumSet.noneOf(Permission.class);

    @Schema(
        description = "Whether the user account is enabled",
        example = "true",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private boolean enabled;

    @Schema(
        description = "Whether the user's email has been validated",
        example = "true",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private boolean validated;

    @Schema(
        description = "List of organizations the user belongs to",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private List<UserOrganizationResponse> organizations = new ArrayList<>();

}
