package io.github.eventify.api.apikey.model;

import io.github.eventify.api.user.model.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;
import jakarta.persistence.*;

import static io.github.eventify.Main.SERIAL_VERSION_UID;

/**
 * Entity representing an audit record for revoked API keys.
 */
@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "api_key_audit")
public class ApiKeyAudit implements Serializable {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
        name = "key_suffix",
        nullable = false,
        length = 4
    )
    private String keySuffix;

    @Column(
        name = "key_name",
        nullable = false,
        length = 100
    )
    private String keyName;

    @Column(
        name = "scope",
        nullable = false
    )
    @Enumerated(EnumType.STRING)
    private ApiKeyScope scope;

    @Column(name = "owner_user_id")
    private Long ownerUserId;

    @Column(name = "organization_id")
    private Long organizationId;

    @Column(
        name = "created_by",
        nullable = false
    )
    private Long createdBy;

    @Column(
        name = "created_at",
        nullable = false
    )
    private OffsetDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "revoked_by")
    private User revokedBy;

    @Column(
        name = "revoked_at",
        nullable = false
    )
    private OffsetDateTime revokedAt;

    @Column(
        name = "total_requests",
        nullable = false
    )
    private Long totalRequests;

    /**
     * Factory method to create an audit record from a revoked API key.
     *
     * @param apiKey  the API key being revoked
     * @param revoker the user revoking the key
     * @return a new ApiKeyAudit instance
     */
    public static ApiKeyAudit fromRevokedKey(final ApiKey apiKey, final User revoker) {
        final ApiKeyAudit audit = new ApiKeyAudit();
        audit.setKeySuffix(apiKey.getSuffix());
        audit.setKeyName(apiKey.getName());
        audit.setScope(apiKey.getScope());
        audit.setOwnerUserId(apiKey.getUser().getId());
        audit.setOrganizationId(apiKey.getOrganization() != null ? apiKey.getOrganization().getId() : null);
        audit.setCreatedBy(apiKey.getUser().getId());
        audit.setCreatedAt(apiKey.getCreatedAt());
        audit.setRevokedBy(revoker);
        audit.setRevokedAt(OffsetDateTime.now());
        audit.setTotalRequests(apiKey.getTotalRequests());
        return audit;
    }
}
