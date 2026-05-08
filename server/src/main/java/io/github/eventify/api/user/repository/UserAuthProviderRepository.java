package io.github.eventify.api.user.repository;

import io.github.eventify.api.user.model.AuthProvider;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.user.model.UserAuthProvider;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for {@link UserAuthProvider} entities.
 */
public interface UserAuthProviderRepository extends JpaRepository<UserAuthProvider, Long> {

    /**
     * Find all auth providers for a given user.
     *
     * @param user the user
     * @return list of auth providers
     */
    List<UserAuthProvider> findAllByUser(User user);

    /**
     * Find a specific auth provider for a user.
     *
     * @param user     the user
     * @param provider the provider type
     * @return optional auth provider
     */
    Optional<UserAuthProvider> findByUserAndProvider(User user, AuthProvider provider);

    /**
     * Check if a user has a specific provider linked.
     *
     * @param user     the user
     * @param provider the provider type
     * @return true if the provider is linked
     */
    boolean existsByUserAndProvider(User user, AuthProvider provider);

    /**
     * Find a UserAuthProvider by provider type and provider email.
     *
     * @param provider      the provider type
     * @param providerEmail the email from the provider
     * @return optional auth provider
     */
    Optional<UserAuthProvider> findByProviderAndProviderEmail(AuthProvider provider, String providerEmail);

}
