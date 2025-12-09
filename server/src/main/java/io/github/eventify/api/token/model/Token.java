package io.github.eventify.api.token.model;

import io.github.eventify.api.user.model.User;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;
import jakarta.persistence.*;

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
}
