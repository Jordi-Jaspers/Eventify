package io.github.eventify.api.user.model.response;

import io.github.jframe.datasource.search.model.resource.PageableItemResource;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Response DTO for user search results.
 */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class UserResponse implements PageableItemResource {

    private Long id;

    private String email;

    private String firstName;

    private String lastName;
}
