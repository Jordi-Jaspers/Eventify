package io.github.eventify.api.admin.apikeys.model;

import io.github.eventify.api.apikey.model.response.ApiKeyResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/** Domain class holding admin API key statistics. */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiKeyStats {

    private Long totalKeys;
    private Long userKeys;
    private Long organizationKeys;
    private Long createdThisWeek;
    private Long createdThisMonth;
    private Long revokedThisMonth;
    private Long expiringNext30Days;
    private Long neverUsedKeys;
    private List<ApiKeyResponse> topKeysByUsage;
}
