package org.jordijaspers.eventify.api.source.model.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
public class SourceResponse {

    private Long id;

    private String name;

    private String description;

    private ZonedDateTime created;

}
