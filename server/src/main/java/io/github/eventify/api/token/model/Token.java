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
    private Long id;

    @Column(
        name = "value",
        unique = true
    )
    @EqualsAndHashCode.Include
    private String value;

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
     * The default non-id constructor.
     *
     * @param value     the value of the token
     * @param expiresAt the expiration date of the token
     * @param type      the type of the token
     * @param user      the user the token belongs to
     */
    public Token(final String value, final OffsetDateTime expiresAt, final TokenType type, final User user) {
        this.value = value;
        this.type = type;
        this.expiresAt = expiresAt;
        this.user = user;
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
