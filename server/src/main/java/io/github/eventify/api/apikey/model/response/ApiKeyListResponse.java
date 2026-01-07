package io.github.eventify.api.apikey.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Response DTO for listing API keys.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ApiKeyListResponse {

    private List<ApiKeyResponse> keys;

    private Integer limit;

}
