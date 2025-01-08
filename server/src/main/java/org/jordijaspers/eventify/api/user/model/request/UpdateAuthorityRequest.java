package org.jordijaspers.eventify.api.user.model.request;

import lombok.Data;

import org.jordijaspers.eventify.api.authentication.model.Authority;

/**
 * The user details update request.
 */
@Data
public class UpdateAuthorityRequest {

    private Authority authority;

}
