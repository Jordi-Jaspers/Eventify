package io.github.eventify.api.authentication.model.response;

import io.github.eventify.api.authentication.model.Role;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

/**
 * The response of login and registration endpoints.
 */
@Data
@NoArgsConstructor
public class AuthenticationResponse {

    private String email;

    private Role role;

    private String accessToken;

    private String refreshToken;

    private ZonedDateTime expiresAt;

    private boolean enabled;

    private boolean validated;

}
