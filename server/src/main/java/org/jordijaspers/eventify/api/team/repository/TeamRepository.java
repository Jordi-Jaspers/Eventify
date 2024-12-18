package org.jordijaspers.eventify.api.team.repository;

import org.jordijaspers.eventify.api.team.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    @Query("""
            FROM Team t
            LEFT JOIN FETCH t.members
            WHERE t.id = :id
        """)
    Optional<Team> findByIdWithMembers(@Param("id") Long id);

    @Override
    @NonNull
    @Query("FROM Team t LEFT JOIN FETCH t.members")
    List<Team> findAll();

    @Override
    @Modifying
    void delete(@NonNull Team team);

    boolean existsByNameIgnoreCase(String name);
}
