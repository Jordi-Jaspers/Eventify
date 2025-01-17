package org.jordijaspers.eventify.api.source.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;
import jakarta.persistence.*;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import static java.util.Objects.nonNull;
import static org.jordijaspers.eventify.Application.SERIAL_VERSION_UID;
import static org.jordijaspers.eventify.common.util.SecurityUtil.getLoggedInUsername;

@Data
@Entity
@Table(name = "api_key")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ApiKey implements Serializable {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
        name = "key",
        updatable = false,
        nullable = false
    )
    private String key;

    @Column(
        name = "created_by",
        updatable = false,
        nullable = false
    )
    private String createdBy;

    @CreationTimestamp
    @Column(
        name = "created",
        updatable = false,
        nullable = false
    )
    private LocalDateTime created;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @UpdateTimestamp
    @Column(name = "last_used")
    private LocalDateTime lastUsed;

    @Column(name = "enabled")
    private boolean enabled;

    /**
     * A constructor to create a new API key.
     */
    public ApiKey() {
        initialize();
    }

    private void initialize() {
        if (nonNull(this.id)) {
            return;
        }

        this.key = UUID.randomUUID().toString();
        this.createdBy = getLoggedInUsername();
        this.expiresAt = LocalDateTime.now().plusYears(1);
        this.lastUsed = LocalDateTime.now();
        this.enabled = true;
    }
}
