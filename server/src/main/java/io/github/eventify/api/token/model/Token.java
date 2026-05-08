package io.github.eventify.api.token.model;

import io.github.eventify.api.user.model.User;
import io.github.eventify.common.util.DeviceInfoExtractor;
import io.github.eventify.common.util.TimeProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.servlet.http.HttpServletRequest;

import static io.github.eventify.Main.SERIAL_VERSION_UID;


/**
 * The token model which represents a token in the database.
 */
@Getter
@Setter
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "token")
public class Token implements Serializable {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(
        name = "value_hash",
        unique = true,
        nullable = false
    )
    private String valueHash;

    @Column(
        name = "family_id",
        nullable = false
    )
    private UUID familyId;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private TokenType type;

    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @Column(
        name = "device_info",
        length = 255
    )
    private String deviceInfo;

    @Column(
        name = "user_agent",
        length = 512
    )
    private String userAgent;

    @Column(
        name = "ip_address",
        length = 45
    )
    private String ipAddress;

    @Column(name = "last_active_at")
    private OffsetDateTime lastActiveAt;

    /**
     * The raw (unhashed) token value. Not persisted — used to pass the raw token to the cookie layer.
     */
    @Transient
    @Getter(lombok.AccessLevel.NONE)
    private String rawValue;

    /**
     * Backward-compatible alias for rawValue. Not persisted.
     * Kept so that existing builder usages (.value(...)) continue to compile.
     */
    @Transient
    @Getter(lombok.AccessLevel.NONE)
    private String value;

    /**
     * Returns the raw token value. Falls back to {@code value} then {@code valueHash} for backward compatibility.
     * Note: for non-refresh tokens, valueHash stores the raw value directly.
     */
    public String getRawValue() {
        final String resolved = rawValue != null ? rawValue : value;
        return resolved != null ? resolved : valueHash;
    }

    /**
     * Returns the raw token value. Delegates to {@link #getRawValue()} for backward compatibility.
     */
    public String getValue() {
        return getRawValue();
    }

    /**
     * Captures device metadata from the supplied HTTP request. No-op when the request is null.
     * Sets {@code deviceInfo}, {@code userAgent}, {@code ipAddress}, and {@code lastActiveAt} to the current time.
     *
     * @param request the HTTP request to extract device info from (may be {@code null})
     */
    public void captureDeviceMetadata(final HttpServletRequest request) {
        if (request == null) {
            return;
        }
        final String ua = DeviceInfoExtractor.extractUserAgent(request);
        this.deviceInfo = DeviceInfoExtractor.extractDeviceInfo(ua);
        this.userAgent = ua;
        this.ipAddress = DeviceInfoExtractor.extractIpAddress(request);
        this.lastActiveAt = TimeProvider.now();
    }

    /**
     * Copies device metadata ({@code deviceInfo}, {@code userAgent}, {@code ipAddress}) from another token.
     * {@code lastActiveAt} is NOT copied — caller should set it to reflect new activity.
     *
     * @param other the token to inherit device metadata from
     */
    public void inheritDeviceMetadataFrom(final Token other) {
        this.deviceInfo = other.deviceInfo;
        this.userAgent = other.userAgent;
        this.ipAddress = other.ipAddress;
    }
}
