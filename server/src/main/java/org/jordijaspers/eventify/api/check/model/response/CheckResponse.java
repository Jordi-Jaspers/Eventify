package org.jordijaspers.eventify.api.check.model.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
public class CheckResponse {

    private Long id;

    private String name;

    private ZonedDateTime created;

}
