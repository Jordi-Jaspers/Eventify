package io.github.eventify.api.organization.model.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Response containing owner details.
 */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class OwnerResponse {

    private Long id;

    private String email;

    private String firstName;

    private String lastName;
}
