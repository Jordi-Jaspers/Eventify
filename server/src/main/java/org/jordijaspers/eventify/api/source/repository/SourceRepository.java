package org.jordijaspers.eventify.api.source.repository;

import java.util.List;
import java.util.Optional;

import org.jordijaspers.eventify.api.source.model.Source;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


/**
 * The data access interface for {@link Source}.
 */
@Repository
@Transactional
public interface SourceRepository extends JpaRepository<Source, Long> {

    @Query("FROM Source s WHERE s.name LIKE %:name%")
    List<Source> findAllByNameContaining(@Param("name") String name);

    @Query(
        """
            FROM Source s
            LEFT JOIN FETCH s.apiKey token
            WHERE token.key = :token
            """
    )
    Optional<Source> findByToken(@Param("token") String token);
}
