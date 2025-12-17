package io.github.eventify.api.user.repository;

import io.github.eventify.api.admin.model.projection.DailyGrowthData;
import io.github.eventify.api.authentication.model.Role;
import io.github.eventify.api.user.model.User;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    @Query(
        """
            FROM User u
            WHERE u.email = :email
            """
    )
    Optional<User> findByEmail(@NonNull String email);

    /**
     * Find all users by email containing a certain string.
     *
     * @param testEmail the string to search for.
     * @return the users.
     */
    @Query(
        """
            FROM User u
            WHERE u.email LIKE %:testEmail%
            """
    )
    List<User> findAllByEmailContaining(@NonNull String testEmail);

    /**
     * Check if a user with the given role exists.
     *
     * @param role the role to check.
     * @return true if a user with the given role exists, false otherwise.
     */
    @Query(
        """
            SELECT COUNT(u) > 0
            FROM User u
            WHERE u.role = :role
            """
    )
    boolean existsByRole(@NonNull Role role);

    /**
     * Delete unvalidated accounts that are older than 1 month.
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM User u WHERE u.validated = false AND u.createdAt <= :limit")
    void deleteUnvalidatedAccounts(@NonNull OffsetDateTime limit);

    /**
     * Count validated users.
     *
     * @return the count of validated users.
     */
    long countByValidatedTrue();

    /**
     * Find the count of new users created in the given date range.
     *
     * @param start the start date of the range.
     * @param end   the end date of the range.
     * @return the count of new users.
     */
    @Query(
        value = """
            SELECT
                gs.date::date AS date,
                (SELECT COUNT(*) FROM "user" WHERE created_at < gs.date + INTERVAL '1 day') AS total,
                (SELECT COUNT(*) FROM "user" WHERE created_at >= gs.date AND created_at < gs.date + INTERVAL '1 day') AS new
            FROM generate_series(
                CAST(:start AS timestamp),
                CAST(:end AS timestamp),
                CAST('1 day' AS interval)
            ) AS gs(date)
            ORDER BY gs.date ASC
            """,
        nativeQuery = true
    )
    List<DailyGrowthData> findDailyGrowthCounts(
        @Param("start") OffsetDateTime start,
        @Param("end") OffsetDateTime end
    );

}
