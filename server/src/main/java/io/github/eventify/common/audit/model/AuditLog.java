package io.github.eventify.common.audit.model;

import io.github.eventify.api.user.model.User;
import io.github.jframe.datasource.search.model.PageableItem;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import static io.github.eventify.Main.SERIAL_VERSION_UID;

/** JPA entity representing an HTTP request audit log entry. */
@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "audit_log")
public class AuditLog implements PageableItem, Serializable {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private User actor;

    @Column(
        name = "method",
        nullable = false,
        length = 7
    )
    private String method;

    @Column(
        name = "path",
        nullable = false,
        length = 512
    )
    private String path;

    @Column(
        name = "status_code",
        nullable = false
    )
    private Short statusCode;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(
        name = "request_body",
        columnDefinition = "jsonb"
    )
    private String requestBody;

    @Column(
        name = "ip_address",
        nullable = false,
        length = 45
    )
    private String ipAddress;

    @Column(
        name = "created_at",
        nullable = false
    )
    private OffsetDateTime createdAt;

    /** Creates an AuditLog with all fields. */
    public AuditLog(
                    final User actor,
                    final String method,
                    final String path,
                    final Short statusCode,
                    final String requestBody,
                    final String ipAddress,
                    final OffsetDateTime createdAt) {
        this.actor = actor;
        this.method = method;
        this.path = path;
        this.statusCode = statusCode;
        this.requestBody = requestBody;
        this.ipAddress = ipAddress;
        this.createdAt = createdAt;
    }
}
