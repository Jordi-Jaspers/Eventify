package org.jordijaspers.eventify.api.source.model.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
public class ApiKeyResponse {

    private String key;

    private String createdBy;

    private ZonedDateTime created;

    private ZonedDateTime expiresAt;

    private ZonedDateTime lastUsed;

    private boolean enabled;

}
