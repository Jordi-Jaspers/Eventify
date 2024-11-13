package org.jordijaspers.eventify.api.token.model;

import lombok.*;
import org.jordijaspers.eventify.api.user.model.User;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import jakarta.persistence.*;

import static org.jordijaspers.eventify.Application.SERIAL_VERSION_UID;


/**
 * The token model which represents a token in the database.
 */
@Data
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "user")
@Table(name = "token")
public class Token implements Serializable {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(
        name = "value",
        unique = true
    )
    private String value;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private TokenType type;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

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
    public Token(final String value, final LocalDateTime expiresAt, final TokenType type, final User user) {
        this.value = value;
        this.type = type;
        this.expiresAt = expiresAt;
        this.user = user;
    }
}
