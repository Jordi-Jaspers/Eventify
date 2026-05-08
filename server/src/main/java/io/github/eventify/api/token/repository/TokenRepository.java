package io.github.eventify.api.token.repository;


import io.github.eventify.api.token.model.Token;
import io.github.eventify.api.token.model.TokenType;
import io.github.eventify.api.user.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


/**
 * The repository for the {@link Token} entity.
 */
@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {

    @Modifying(
        clearAutomatically = true,
        flushAutomatically = true
    )
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Query("DELETE Token t WHERE t.type IN (:types) AND t.user = :user")
    void invalidateTokensWithTypeForUser(@Param("types") List<TokenType> types, @Param("user") User user);

    @Modifying(
        clearAutomatically = true,
        flushAutomatically = true
    )
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Query("DELETE FROM Token t WHERE t.expiresAt <= CURRENT_TIMESTAMP")
    int deleteExpiredTokens();

    @Query(
        """
            FROM Token t
                LEFT JOIN FETCH t.user user
                WHERE t.expiresAt > CURRENT_TIMESTAMP AND t.valueHash = :hash
            """
    )
    Optional<Token> findByValueHash(String hash);

    @Query(
        """
            FROM Token t
                LEFT JOIN FETCH t.user user
                WHERE t.expiresAt > CURRENT_TIMESTAMP AND user.id = :userId AND t.familyId = :familyId
            """
    )
    Optional<Token> findByUserIdAndFamilyId(@Param("userId") Long userId, @Param("familyId") UUID familyId);

    @Query(
        """
            FROM Token t
                LEFT JOIN FETCH t.user user
                WHERE t.expiresAt > CURRENT_TIMESTAMP AND user.email = :email
            """
    )
    List<Token> findByEmail(@NonNull String email);

    @Modifying(
        clearAutomatically = true,
        flushAutomatically = true
    )
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Query("DELETE FROM Token t WHERE t.user.id IN :userIds")
    void deleteAllByUserIdIn(@Param("userIds") List<Long> userIds);
}
