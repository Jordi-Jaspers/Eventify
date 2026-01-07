package io.github.eventify.api.apikey.model;

import io.github.eventify.api.organization.model.Organization;
import io.github.eventify.api.user.model.User;
import io.github.jframe.datasource.search.model.PageableItem;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;
import jakarta.persistence.*;

import org.apache.logging.log4j.util.Strings;
import org.hibernate.annotations.CreationTimestamp;

import static io.github.eventify.Main.SERIAL_VERSION_UID;

/**
 * Entity representing an API key.
 */
@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "api_key")
public class ApiKey implements PageableItem, Serializable {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
        name = "suffix",
        nullable = false,
        length = 4
    )
    private String suffix;

    @Column(
        name = "hashed_key",
        nullable = false
    )
    private String hashedKey;

    @Column(
        name = "name",
        nullable = false,
        length = 100
    )
    private String name;

    @Column(
        name = "scope",
        nullable = false
    )
    @Enumerated(EnumType.STRING)
    private ApiKeyScope scope;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "user_id",
        nullable = false
    )
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @CreationTimestamp
    @Column(
        name = "created_at",
        nullable = false,
        updatable = false
    )
    private OffsetDateTime createdAt;

    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;

    @Column(name = "last_used_at")
    private OffsetDateTime lastUsedAt;

    @Column(
        name = "total_requests",
        nullable = false
    )
    private Long totalRequests = 0L;

    @Transient
    private String key;

    /**
     * Get masked key in format: evt_******xxxx or org_******xxxx where xxxx is the last 4 characters of the key.
     *
     * @return masked API key string
     */
    public String getMaskedKey() {
        return scope.getPrefix() + Strings.repeat("*", 6) + suffix;
    }
}
