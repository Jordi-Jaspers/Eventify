package io.github.eventify.api.organization.repository;

import io.github.eventify.api.organization.model.Organization;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repository for {@link Organization} entities.
 */
@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    /**
     * Find organization by slug excluding soft-deleted ones.
     *
     * @param slug the slug to search for
     * @return Optional containing organization if found
     */
    Optional<Organization> findBySlug(String slug);

    /**
     * Check if organization with slug exists (excluding soft-deleted).
     *
     * @param slug the slug to check
     * @return true if exists, false otherwise
     */
    boolean existsBySlugAndDeletedAtIsNull(String slug);

    /**
     * Find all organizations by name containing a certain string.
     *
     * @param name the string to search for.
     * @return the users.
     */
    @Query(
        """
            FROM Organization o
            WHERE o.name LIKE %:name%
            """
    )
    List<Organization> findAllByNameContaining(@NonNull String name);

    /**
     * Delete all organizations created by the given list of user IDs.
     */
    @Modifying
    @Transactional
    @Query(
        """
            DELETE FROM Organization o
            WHERE o.createdBy IN :ids
            """
    )
    void deleteAllByCreatedBy(@Param("ids") List<Long> ids);

    /**
     * Hard Delete all organizations.
     */
    @Modifying
    @Transactional
    @Override
    void deleteAll();

}
