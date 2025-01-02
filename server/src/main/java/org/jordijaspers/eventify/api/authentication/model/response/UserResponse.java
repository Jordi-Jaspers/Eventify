package org.jordijaspers.eventify.api.authentication.model.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.jordijaspers.eventify.api.team.model.response.TeamResponse;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * The response of a user.
 */
@Data
@NoArgsConstructor
public class UserResponse {

    private String email;

    private List<TeamResponse> teams;

    private String authority;

    private ZonedDateTime lastLogin;

    private String accessToken;

    private String refreshToken;

    private ZonedDateTime expiresAt;

    private boolean enabled;

    private boolean validated;

}
