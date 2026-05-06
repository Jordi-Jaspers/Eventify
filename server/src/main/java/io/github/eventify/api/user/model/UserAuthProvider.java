package io.github.eventify.api.user.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
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
import jakarta.persistence.UniqueConstraint;

/**
 * Entity representing a linked authentication provider for a user.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
    name = "user_auth_provider",
    uniqueConstraints = {
        @UniqueConstraint(
            columnNames = {
                "user_id",
                "provider"
            }
        )
    }
)
public class UserAuthProvider implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "user_id",
        nullable = false
    )
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(
        name = "provider",
        nullable = false
    )
    private AuthProvider provider;

    @Column(
        name = "provider_email",
        nullable = false
    )
    private String providerEmail;

    @Column(
        name = "linked_at",
        nullable = false
    )
    private Instant linkedAt;

    /**
     * Convenience constructor.
     *
     * @param user          the user
     * @param provider      the auth provider
     * @param providerEmail the email from the provider
     */
    public UserAuthProvider(final User user, final AuthProvider provider, final String providerEmail) {
        this.user = user;
        this.provider = provider;
        this.providerEmail = providerEmail;
        this.linkedAt = Instant.now();
    }
}
