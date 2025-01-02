package org.jordijaspers.eventify.api.check.repository;

import org.jordijaspers.eventify.api.check.model.Check;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The repository for the Check entity.
 */
@Repository
public interface CheckRepository extends JpaRepository<Check, Long> {

    @Query(
        value = """
            SELECT * FROM "check"
            WHERE SIMILARITY("name", :searchTerm) > :threshold
            ORDER BY SIMILARITY("name", :searchTerm) DESC, "name"
            LIMIT :limit OFFSET :offset
            """,
        nativeQuery = true
    )
    List<Check> findByNameFuzzy(
        @Param("searchTerm") String searchTerm,
        @Param("threshold") double threshold,
        @Param("limit") int limit,
        @Param("offset") int offset
    );

    // Count for pagination
    @Query(
        value = """
            SELECT COUNT(*) FROM "check"
            WHERE SIMILARITY(name, :searchTerm) > :threshold
            """,
        nativeQuery = true
    )
    long countByNameFuzzy(
        @Param("searchTerm") String searchTerm,
        @Param("threshold") double threshold
    );
}
