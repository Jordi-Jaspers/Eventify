package io.github.eventify.api.event.model.request;

import io.github.eventify.api.event.model.Severity;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Request for creating an event.
 */
@Getter
@NoArgsConstructor
public class CreateEventRequest {

    private Long channelId;
    private Severity severity;
    private String title;
    private String message;
    private Map<String, Object> metadata;

    /**
     * Sets the channel ID.
     *
     * @param channelId the channel ID
     * @return this request for chaining
     */
    public CreateEventRequest setChannelId(final Long channelId) {
        this.channelId = channelId;
        return this;
    }

    /**
     * Sets the severity.
     *
     * @param severity the severity (OK, WARNING, CRITICAL)
     * @return this request for chaining
     */
    public CreateEventRequest setSeverity(final Severity severity) {
        this.severity = severity;
        return this;
    }

    /**
     * Sets the title.
     *
     * @param title the event title
     * @return this request for chaining
     */
    public CreateEventRequest setTitle(final String title) {
        this.title = title;
        return this;
    }

    /**
     * Sets the message.
     *
     * @param message the event message (optional)
     * @return this request for chaining
     */
    public CreateEventRequest setMessage(final String message) {
        this.message = message;
        return this;
    }

    /**
     * Sets the metadata.
     *
     * @param metadata the event metadata (optional)
     * @return this request for chaining
     */
    public CreateEventRequest setMetadata(final Map<String, Object> metadata) {
        this.metadata = metadata;
        return this;
    }
}
