package io.github.eventify.api.authentication.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * The request to register a user.
 */
@Data
@ToString
@NoArgsConstructor
@Accessors(chain = true)
public class RegisterUserRequest {

    @Schema(
        description = "User's first name",
        example = "John",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String firstName;

    @Schema(
        description = "User's last name",
        example = "Doe",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String lastName;

    @Schema(
        description = "User's email address",
        example = "user@example.com",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String email;

    @Schema(
        description = "User's password",
        example = "********",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String password;

    @Schema(
        description = "Password confirmation must match password",
        example = "********",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String passwordConfirmation;

    /**
     * Get the email in lowercase.
     *
     * @return The email in lowercase.
     */
    public String getEmail() {
        return email.toLowerCase();
    }
}
