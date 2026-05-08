package io.github.eventify.api.watchlist.model.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import static io.github.eventify.Main.SERIAL_VERSION_UID;

/**
 * Request DTO for a channel group within a watchlist configuration.
 *
 * <p>Note: Nested groups (groups within groups) are not allowed.
 * Only channels can be members of a group.
 */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class ChannelGroupRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    /**
     * Unique identifier for this group.
     * If null, a new UUID will be generated.
     */
    private UUID id;

    /**
     * Display name for the group.
     */
    private String name;

    /**
     * Ordered list of channel IDs belonging to this group.
     */
    private List<Long> channelIds;
}
