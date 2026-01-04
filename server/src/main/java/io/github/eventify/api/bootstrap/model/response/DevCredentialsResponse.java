package io.github.eventify.api.bootstrap.model.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Response containing development credentials.
 */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class DevCredentialsResponse {

    private String email;

    private String password;

}
