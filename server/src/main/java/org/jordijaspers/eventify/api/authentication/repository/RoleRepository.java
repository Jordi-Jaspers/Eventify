package org.jordijaspers.eventify.api.authentication.repository;

import org.jordijaspers.eventify.api.authentication.model.Authority;
import org.jordijaspers.eventify.api.authentication.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * The repository for the roles.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByAuthority(@NonNull Authority authority);

    boolean existsByAuthority(@NonNull Authority authority);
}
