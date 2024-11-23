package org.jordijaspers.eventify.api.authentication.model.response;

import lombok.Data;
import org.jordijaspers.eventify.api.team.model.response.TeamResponse;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * A response model for user details.
 */
@Data
public class UserDetailsResponse {

    private String email;

    private String firstName;

    private String lastName;

    private ZonedDateTime lastLogin;

    private List<String> authorities;

    private List<TeamResponse> teams;

    private boolean enabled;

    private boolean validated;

}
