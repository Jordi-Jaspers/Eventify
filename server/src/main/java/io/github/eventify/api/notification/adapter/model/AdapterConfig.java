package io.github.eventify.api.notification.adapter.model;

import io.github.eventify.api.user.model.User;
import io.github.jframe.datasource.search.model.PageableItem;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Map;
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

import org.apache.logging.log4j.util.Strings;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import static io.github.eventify.Main.SERIAL_VERSION_UID;

/**
 * Entity representing an adapter configuration for a user or organization.
 */
@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "adapter_config")
public class AdapterConfig implements PageableItem, Serializable {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(
        name = "id",
        updatable = false,
        nullable = false
    )
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "user_id",
        nullable = false
    )
    private User user;

    @Column(name = "organization_id")
    private Long organizationId;

    @Enumerated(EnumType.STRING)
    @Column(
        name = "adapter_type",
        nullable = false,
        length = 50
    )
    private AdapterType adapterType;

    @Column(
        name = "label",
        nullable = false,
        length = 100
    )
    private String label;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(
        name = "config",
        columnDefinition = "jsonb",
        nullable = false
    )
    private Map<String, Object> config;

    @Column(
        name = "enabled",
        nullable = false
    )
    private boolean enabled;

    @CreationTimestamp
    @Column(
        name = "created_at",
        nullable = false,
        updatable = false
    )
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(
        name = "updated_at",
        nullable = false
    )
    private OffsetDateTime updatedAt;

    /**
     * Returns a masked version of the webhookUrl from config.
     * Shows first 8 chars and last 4 chars, with 6 asterisks in between.
     *
     * @return masked webhook URL, or "********" if absent/too short
     */
    public String getMaskedWebhookUrl() {
        final String url = getWebhookUrlFromConfig();
        if (url == null || url.length() <= 8) {
            return "********";
        }
        return url.substring(0, 8) + Strings.repeat("*", 6) + url.substring(url.length() - 4);
    }

    private String getWebhookUrlFromConfig() {
        if (config == null) {
            return null;
        }
        final Object raw = config.get("webhookUrl");
        return raw instanceof String s ? s : null;
    }
}
