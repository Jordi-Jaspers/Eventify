package io.github.eventify.api.notification.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
public class NotificationBroadcast {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(
        name = "id",
        nullable = false,
        updatable = false
    )
    private Long id;

    @Column(
        name = "audience_type",
        nullable = false,
        length = 40
    )
    private String audienceType;

    @Column(
        name = "category",
        nullable = false,
        length = 40
    )
    private String category;

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
