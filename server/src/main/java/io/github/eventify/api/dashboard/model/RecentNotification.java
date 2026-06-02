package io.github.eventify.api.dashboard.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/** Recent notification summary. */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecentNotification {

    private Long id;
    private String title;
    private String message;
    private String category;
    private boolean urgent;
    private OffsetDateTime createdAt;
    private String actionUrl;
}
