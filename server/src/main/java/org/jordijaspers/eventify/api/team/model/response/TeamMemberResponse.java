package org.jordijaspers.eventify.api.team.model.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TeamMemberResponse {

    private Long id;

    private String email;

    private String firstName;

    private String lastName;

}
