package org.jordijaspers.eventify.api.team.repository;

import java.util.List;
import java.util.Optional;

import org.jordijaspers.eventify.api.team.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    @NonNull
    @Override
    @Query("FROM Team t LEFT JOIN FETCH t.members")
    List<Team> findAll();

    @Query(
        """
            FROM Team t
            LEFT JOIN FETCH t.members
            WHERE t.id = :id
            """
    )
    Optional<Team> findByIdWithMembers(@Param("id") Long id);

    @Query(
        """
            FROM Team t
            LEFT JOIN FETCH t.members
            WHERE t.name LIKE %:name%
            """
    )
    List<Team> findAllByNameContaining(@NonNull String name);

    @Override
    @Modifying
    void delete(@NonNull Team team);

    boolean existsByNameIgnoreCase(String name);
}
