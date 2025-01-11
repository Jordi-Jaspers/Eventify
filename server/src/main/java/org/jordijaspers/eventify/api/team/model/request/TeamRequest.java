package org.jordijaspers.eventify.api.team.model.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@ToString
@NoArgsConstructor
@Accessors(chain = true)
public class TeamRequest {

    private String name;

    private String description;

}
