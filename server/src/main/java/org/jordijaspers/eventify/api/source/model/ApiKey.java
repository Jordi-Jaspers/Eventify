package org.jordijaspers.eventify.api.source.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import jakarta.persistence.*;

import org.hibernate.annotations.CreationTimestamp;

import static org.jordijaspers.eventify.Application.SERIAL_VERSION_UID;

@Data
@Entity
@NoArgsConstructor
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

    @Column(name = "last_used")
    private LocalDateTime lastUsed;

    @Column(name = "enabled")
    private boolean enabled;

}
