package org.jordijaspers.eventify.api.token.repository;


import java.util.List;
import java.util.Optional;

import org.jordijaspers.eventify.api.token.model.Token;
import org.jordijaspers.eventify.api.token.model.TokenType;
import org.jordijaspers.eventify.api.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
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
    void deleteExpiredTokens();

    @Query(
        """
            FROM Token t
                LEFT JOIN FETCH t.user user
                LEFT JOIN FETCH user.teams teams
                WHERE t.expiresAt > CURRENT_TIMESTAMP AND t.value = :token
            """
    )
    Optional<Token> findByValue(String token);

    @Query(
        """
            FROM Token t
                LEFT JOIN FETCH t.user user
                LEFT JOIN FETCH user.teams teams
                WHERE t.expiresAt > CURRENT_TIMESTAMP AND user.email = :email
            """
    )
    List<Token> findByEmail(@NonNull String email);
}
