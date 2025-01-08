package org.jordijaspers.eventify.api.user.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import org.jordijaspers.eventify.api.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * The data access interface for {@link User}.
 */
@Repository
@Transactional
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find all users by their ids.
     *
     * @param ids the ids of the users.
     * @return the users.
     */
    Set<User> findAllByIdIn(Collection<Long> ids);

    /**
     * Find a user by email.
     *
     * @param email the email.
     * @return the user.
     */
    @Query("""
        FROM User u
        LEFT JOIN FETCH u.role
        LEFT JOIN FETCH u.teams
        WHERE u.email = :email
        """)
    Optional<User> findByEmail(@NonNull String email);

    /**
     * Delete unvalidated accounts that are older than 1 month.
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM User u WHERE u.validated = false AND u.created <= :limit")
    void deleteUnvalidatedAccounts(@NonNull LocalDateTime limit);
}
