package org.jordijaspers.eventify.api.team.model.request;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
public class TeamMemberRequest {

    private Set<Long> userIds = new HashSet<>();

}
