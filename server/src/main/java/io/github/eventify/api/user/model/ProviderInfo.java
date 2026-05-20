package io.github.eventify.api.user.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Domain class representing a provider connection status for a user. */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProviderInfo {

    private String provider;
    private boolean connected;
    private String providerEmail;
}
