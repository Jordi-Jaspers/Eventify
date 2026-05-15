package io.github.eventify.common.audit.model;

import io.github.jframe.datasource.search.model.PageableItem;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import static io.github.eventify.Main.SERIAL_VERSION_UID;

/**
 * JPA entity representing an audit log entry for admin actions.
 */
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

    @Column(
        name = "actor_id",
        nullable = false
    )
    private Long actorId;

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

    /**
     * Business constructor for creating audit log entries.
     */
    public AuditLog(
                    final Long actorId,
                    final String method,
                    final String path,
                    final Short statusCode,
                    final String requestBody,
                    final String ipAddress,
                    final OffsetDateTime createdAt) {
        this.actorId = actorId;
        this.method = method;
        this.path = path;
        this.statusCode = statusCode;
        this.requestBody = requestBody;
        this.ipAddress = ipAddress;
        this.createdAt = createdAt;
    }
}
