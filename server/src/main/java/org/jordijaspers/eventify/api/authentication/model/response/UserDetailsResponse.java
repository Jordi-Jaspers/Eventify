package org.jordijaspers.eventify.api.authentication.model.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jordijaspers.eventify.api.authentication.model.Permission;
import org.jordijaspers.eventify.api.team.model.response.TeamResponse;

/**
 * A response model for user details.
 */
@Data
@NoArgsConstructor
public class UserDetailsResponse {

    private Long id;

    private String email;

    private String firstName;

    private String lastName;

    private ZonedDateTime lastLogin;

    private ZonedDateTime created;

    private String authority;

    private Set<Permission> permissions = new HashSet<>();

    private List<TeamResponse> teams = new ArrayList<>();

    private boolean enabled;

    private boolean validated;

}
