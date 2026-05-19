package io.github.eventify.api.admin.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Domain model for a top channel entry by event volume. */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopChannelInfo {

    private Long channelId;

    private String channelName;

    private String ownerName;

    private Long eventCount;

    private Double percentage;
}
