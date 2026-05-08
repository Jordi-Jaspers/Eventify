package io.github.eventify.api.channel.model;

/**
 * Status of a channel.
 */
public enum ChannelStatus {
    /**
     * Channel is active and accepting events.
     */
    ACTIVE,

    /**
     * Channel is temporarily paused.
     */
    PAUSED,

    /**
     * Channel is pending deletion.
     */
    PENDING_DELETION
}
