package io.github.eventify.api.organization.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import jakarta.persistence.*;

/**
 * Entity representing an organization.
 */
@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "organization")
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
        name = "name",
        nullable = false
    )
    private String name;

    @Column(
        name = "slug",
        nullable = false
    )
    private String slug;

    @Column(
        name = "status",
        nullable = false
    )
    @Enumerated(EnumType.STRING)
    private OrganizationStatus status;

    @Column(
        name = "created_by",
        nullable = false
    )
    private Long createdBy;

    @Column(
        name = "created_at",
        nullable = false,
        updatable = false
    )
    private OffsetDateTime createdAt;

    @Column(name = "deleted_by")
    private Long deletedBy;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;
}
