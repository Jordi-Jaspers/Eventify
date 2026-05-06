package io.github.eventify.api.user.model;

import io.github.eventify.api.authentication.model.Permission;
import io.github.eventify.api.authentication.model.Role;
import io.github.eventify.api.organization.model.OrganizationMembership;
import io.github.eventify.api.token.model.Token;
import io.github.eventify.common.security.oauth2.provider.OAuth2UserInfo;
import io.github.jframe.datasource.search.model.PageableItem;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import jakarta.persistence.*;

import org.apache.logging.log4j.util.Strings;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import static io.github.eventify.Main.SERIAL_VERSION_UID;


/**
 * The user entity.
 */
@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "\"user\"")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User implements UserDetails, PageableItem {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
        name = "email",
        unique = true
    )
    private String email;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "password")
    private String password;

    @Column(name = "enabled")
    private boolean enabled;

    @Column(name = "validated")
    private boolean validated;

    @Column(
        name = "has_password",
        nullable = false
    )
    private boolean hasPassword;

    @UpdateTimestamp
    @Column(name = "last_login")
    private OffsetDateTime lastLogin;

    @CreationTimestamp
    @Column(
        name = "created_at",
        updatable = false
    )
    private OffsetDateTime createdAt;

    @Column(name = "\"role\"")
    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(
        mappedBy = "user",
        cascade = CascadeType.ALL,
        fetch = FetchType.LAZY,
        orphanRemoval = true
    )
    private List<Token> tokens = new ArrayList<>();

    @OneToMany(
        mappedBy = "user",
        cascade = CascadeType.ALL,
        fetch = FetchType.LAZY,
        orphanRemoval = true
    )
    private List<UserAuthProvider> authProviders = new ArrayList<>();

    @OneToMany(
        mappedBy = "user",
        cascade = CascadeType.ALL,
        fetch = FetchType.LAZY,
        orphanRemoval = true
    )
    private List<OrganizationMembership> organizations = new ArrayList<>();

    @Column(
        name = "retention_days",
        nullable = false
    )
    private Integer retentionDays = 90;

    @Transient
    private Token accessToken;

    @Transient
    private Token refreshToken;

    /**
     * Constructor to create a User from OAuth2 user info.
     *
     * @param oAuth2UserInfo The OAuth2 user info.
     */
    public User(final OAuth2UserInfo oAuth2UserInfo) {
        this.email = oAuth2UserInfo.getEmail();
        this.firstName = oAuth2UserInfo.getFirstName();
        this.lastName = oAuth2UserInfo.getLastName();
        this.enabled = true;
        this.validated = true;
        this.role = Role.USER;
        this.hasPassword = false;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @NonNull
    @Override
    public String getUsername() {
        return email == null ? Strings.EMPTY : email.toLowerCase();
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return role.getPermissions()
            .stream()
            .map(permission -> new SimpleGrantedAuthority(permission.name()))
            .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAccountNonLocked() {
        return enabled;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Get the email in lowercase.
     *
     * @return The email in lowercase, or null if email is not set.
     */
    public String getEmail() {
        return email == null ? null : email.toLowerCase();
    }

    /**
     * Checks if the user has a certain permission.
     *
     * @param permission The permission to check.
     */
    public boolean hasPermission(final Permission permission) {
        return role.getPermissions().contains(permission);
    }
}
