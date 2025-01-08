package org.jordijaspers.eventify.api.authentication.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.util.Set;
import jakarta.persistence.*;

import org.springframework.security.core.GrantedAuthority;

import static org.jordijaspers.eventify.Application.SERIAL_VERSION_UID;


/**
 * The role entity.
 */
@Data
@Entity
@NoArgsConstructor
@Table(name = "\"role\"")
public class Role implements GrantedAuthority {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(
        name = "authority",
        unique = true,
        nullable = false,
        updatable = false
    )
    private Authority authority;

    @Column(name = "description")
    private String description;

    /**
     * Creates a non-persisted role with the given authority and description.
     */
    public Role(final Authority authority) {
        this.authority = authority;
        this.description = authority.getDescription();
    }

    /**
     * Retrieves the permissions of the {@link Authority}.
     *
     * @return The permissions of the {@link Authority}.
     */
    public Set<Permission> getPermissions() {
        return this.authority.getPermissions();
    }

    /**
     * Retrieves the name of the role as a string.
     *
     * @return The name of the role.
     */
    @Override
    public String getAuthority() {
        return this.authority.name();
    }

    /**
     * Sets the role of the user.
     *
     * @param authority The role of the user.
     */
    public void setAuthority(final String authority) {
        this.authority = Authority.valueOf(authority);
    }
}
