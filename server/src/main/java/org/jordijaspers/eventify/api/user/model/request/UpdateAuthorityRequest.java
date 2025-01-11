package org.jordijaspers.eventify.api.user.model.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.jordijaspers.eventify.api.authentication.model.Authority;

/**
 * The user details update request.
 */
@Data
@ToString
@NoArgsConstructor
@Accessors(chain = true)
public class UpdateAuthorityRequest {

    private Authority authority;

}
