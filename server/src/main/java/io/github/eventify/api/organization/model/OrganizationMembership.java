package io.github.eventify.api.organization.model;

import io.github.eventify.api.user.model.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import jakarta.persistence.*;

import static java.time.ZoneOffset.UTC;

/**
 * Entity representing organization membership.
 */
@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "organization_membership")
public class OrganizationMembership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "user_id",
        nullable = false
    )
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "organization_id",
        nullable = false
    )
    private Organization organization;

    @Column(
        name = "role",
        nullable = false
    )
    @Enumerated(EnumType.STRING)
    private OrganizationalRole role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_by")
    private User invitedBy;

    @Column(
        name = "created_at",
        nullable = false,
        updatable = false
    )
    private OffsetDateTime createdAt;

    /**
     * Constructs a new OrganizationMembership with the specified organization, user, and role.
     *
     * @param organization the organization
     * @param owner        the user
     * @param role         the organizational role
     */
    public OrganizationMembership(final Organization organization, final User owner, final OrganizationalRole role) {
        this.organization = organization;
        this.user = owner;
        this.role = role;
        this.createdAt = OffsetDateTime.now(UTC);
    }
}
