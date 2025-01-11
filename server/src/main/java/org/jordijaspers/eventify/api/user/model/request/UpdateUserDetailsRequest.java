package org.jordijaspers.eventify.api.user.model.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * The user details update request.
 */
@Data
@ToString
@NoArgsConstructor
@Accessors(chain = true)
public class UpdateUserDetailsRequest {

    private String firstName;

    private String lastName;

}
