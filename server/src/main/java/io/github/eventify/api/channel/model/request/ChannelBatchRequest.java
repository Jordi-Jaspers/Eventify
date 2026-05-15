package io.github.eventify.api.channel.model.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Request DTO for batch channel operations.
 */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class ChannelBatchRequest {

    private List<Long> channelIds;
}
