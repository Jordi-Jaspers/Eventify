package org.jordijaspers.eventify.api.team.model.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class TeamResponse {

    private Long id;

    private String name;

    private String description;

    private ZonedDateTime created;

    private List<TeamMemberResponse> members = new ArrayList<>();

}
