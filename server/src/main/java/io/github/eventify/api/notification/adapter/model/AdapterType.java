package io.github.eventify.api.notification.adapter.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Enum representing the supported notification adapter types.
 */
@Getter
@AllArgsConstructor
@Schema(description = "AdapterType")
public enum AdapterType {

    IN_APP,
    MATTERMOST,
    SLACK,
    EMAIL;
}
