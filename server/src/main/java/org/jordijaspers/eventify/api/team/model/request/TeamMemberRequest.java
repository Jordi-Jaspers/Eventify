package org.jordijaspers.eventify.api.team.model.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.HashSet;
import java.util.Set;

@Data
@ToString
@NoArgsConstructor
@Accessors(chain = true)
public class TeamMemberRequest {

    private Set<Long> userIds = new HashSet<>();

}
