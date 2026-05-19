package io.github.eventify.api.admin.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Domain object holding platform-wide counts for organizations, users and channels. */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminCounts {

    private long totalOrganizations;

    private long totalUsers;

    private long activeUsers;

    private long totalChannels;

    private long activeChannels;

    private long pausedChannels;

    private long staleChannels;

    private long pendingDeletionChannels;
}
