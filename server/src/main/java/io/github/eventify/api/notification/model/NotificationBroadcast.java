package io.github.eventify.api.notification.model;

import io.github.eventify.api.user.model.User;
import io.github.jframe.datasource.search.model.PageableItem;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;

/**
 * Entity representing a broadcast notification sent to multiple users.
 */
@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "notification_broadcast")
public class NotificationBroadcast implements PageableItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(
        name = "id",
        nullable = false,
        updatable = false
    )
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sent_by")
    private User sentBy;

    @Enumerated(EnumType.STRING)
    @Column(
        name = "audience_type",
        nullable = false,
        length = 40
    )
    private NotificationAudienceType audienceType;

    @Column(name = "audience_target_id")
    private Long audienceTargetId;

    @Column(
        name = "audience_role",
        length = 40
    )
    private String audienceRole;

    @Enumerated(EnumType.STRING)
    @Column(
        name = "category",
        nullable = false,
        length = 40
    )
    private NotificationCategory category;

    @Column(
        name = "title",
        nullable = false,
        length = 120
    )
    private String title;

    @Column(
        name = "message",
        nullable = false,
        length = 500
    )
    private String message;

    @Column(
        name = "action_url",
        length = 500
    )
    private String actionUrl;

    @Column(
        name = "action_label",
        length = 100
    )
    private String actionLabel;

    @Column(
        name = "recipient_count",
        nullable = false
    )
    private int recipientCount;

    @CreationTimestamp
    @Column(
        name = "created_at",
        nullable = false,
        updatable = false
    )
    private OffsetDateTime createdAt;
}
