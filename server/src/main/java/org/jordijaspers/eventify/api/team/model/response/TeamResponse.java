package org.jordijaspers.eventify.api.team.model.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class TeamResponse {

    private String name;

    private String description;

    private LocalDateTime created;

}
