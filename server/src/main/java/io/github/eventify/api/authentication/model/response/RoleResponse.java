package io.github.eventify.api.authentication.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The authority of a user.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponse {

    private String key;

    private String value;

    private String description;

}
