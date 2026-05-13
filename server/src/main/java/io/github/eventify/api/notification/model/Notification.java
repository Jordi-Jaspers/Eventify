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
 * Entity representing a notification for a user.
 */
@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "notification")
public class Notification implements PageableItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(
        name = "id",
        nullable = false,
        updatable = false
    )
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "user_id",
        nullable = false
    )
    private User user;

    @Column(
        name = "category",
        nullable = false,
        length = 40
    )
    @Enumerated(EnumType.STRING)
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
        name = "urgent",
        nullable = false
    )
    private boolean urgent;

    @CreationTimestamp
    @Column(
        name = "created_at",
        nullable = false,
        updatable = false
    )
    private OffsetDateTime createdAt;

    @Column(name = "read_at")
    private OffsetDateTime readAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "broadcast_id")
    private NotificationBroadcast broadcast;

    /**
     * Business constructor for creating a notification.
     *
     * @param user        the recipient user
     * @param category    the notification category
     * @param title       the notification title
     * @param message     the notification message
     * @param actionUrl   optional action URL
     * @param actionLabel optional action label
     * @param urgent      whether the notification is urgent
     */
    public Notification(
                        final User user,
                        final NotificationCategory category,
                        final String title,
                        final String message,
                        final String actionUrl,
                        final String actionLabel,
                        final boolean urgent) {
        this.user = user;
        this.category = category;
        this.title = title;
        this.message = message;
        this.actionUrl = actionUrl;
        this.actionLabel = actionLabel;
        this.urgent = urgent;
    }
}
