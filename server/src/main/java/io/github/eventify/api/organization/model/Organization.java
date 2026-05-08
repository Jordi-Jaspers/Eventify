package io.github.eventify.api.organization.model;

import io.github.eventify.api.user.model.User;
import io.github.jframe.datasource.search.model.PageableItem;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;
import jakarta.persistence.*;

import static io.github.eventify.Main.SERIAL_VERSION_UID;
import static io.github.eventify.common.security.SecurityUtil.getLoggedInUser;
import static java.time.ZoneOffset.UTC;

/**
 * Entity representing an organization.
 */
@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "organization")
public class Organization implements PageableItem, Serializable {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

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
        name = "member_count",
        insertable = false,
        updatable = false
    )
    private Integer memberCount;

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

    @Column(
        name = "retention_days",
        nullable = false
    )
    private Integer retentionDays = 90;

    @Transient
    private User owner;

    /**
     * Constructor to create a new organization with the specified name and slug.
     *
     * @param name the name of the organization
     * @param slug the slug of the organization
     */
    public Organization(final String name, final String slug) {
        this.name = name;
        this.slug = slug;
        this.status = OrganizationStatus.TRIAL;
        this.createdBy = getLoggedInUser().getId();
        this.createdAt = OffsetDateTime.now(UTC);
    }

}
