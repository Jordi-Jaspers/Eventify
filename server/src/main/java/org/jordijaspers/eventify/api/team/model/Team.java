package org.jordijaspers.eventify.api.team.model;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.*;

import org.hibernate.annotations.CreationTimestamp;
import org.jordijaspers.eventify.api.user.model.User;

import static org.jordijaspers.eventify.Application.SERIAL_VERSION_UID;

@Data
@Entity
@NoArgsConstructor
@Table(name = "team")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Team implements Serializable {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @CreationTimestamp
    @Column(
        name = "created",
        updatable = false
    )
    private LocalDateTime created;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_team",
        joinColumns = @JoinColumn(name = "team_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> members = new HashSet<>();

    /**
     * A constructor to create a new team.
     *
     * @param name        The name of the team.
     * @param description The description of the team.
     */
    public Team(final String name, final String description) {
        this.name = name;
        this.description = description;
    }

    /**
     * Add a member to the team.
     *
     * @param user The user to add to the team.
     */
    public void addMember(final User user) {
        members.add(user);
        user.getTeams().add(this);
    }

    /**
     * Remove a member from the team.
     *
     * @param user The user to remove from the team.
     */
    public void removeMember(final User user) {
        members.remove(user);
        user.getTeams().remove(this);
    }
}
