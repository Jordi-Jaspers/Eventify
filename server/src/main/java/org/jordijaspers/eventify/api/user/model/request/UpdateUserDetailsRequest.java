package org.jordijaspers.eventify.api.user.model.request;

import lombok.Data;

/**
 * The user details update request.
 */
@Data
public class UpdateUserDetailsRequest {

    private String firstName;

    private String lastName;

}
