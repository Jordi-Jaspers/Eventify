package org.jordijaspers.eventify.api.authentication.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

import java.io.Serial;
import jakarta.persistence.*;

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
        unique = true
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

    @Override
    public String getAuthority() {
        return authority.name();
    }

    public void setAuthority(final String authority) {
        this.authority = Authority.valueOf(authority);
    }
}
