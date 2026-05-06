package io.github.eventify.api.user.service;

import io.github.eventify.api.user.model.AuthProvider;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.user.model.UserAuthProvider;
import io.github.eventify.api.user.model.response.ProviderResponse;
import io.github.eventify.api.user.repository.UserAuthProviderRepository;
import io.github.eventify.api.user.repository.UserRepository;
import io.github.eventify.common.exception.LastAuthMethodException;
import io.github.eventify.common.exception.LinkOAuth2Exception;
import io.github.eventify.common.exception.LocalProviderUnlinkException;
import io.github.jframe.exception.core.DataNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static io.github.eventify.common.exception.ApiErrorCode.EMAIL_IN_USE_ERROR;
import static io.github.eventify.common.exception.ApiErrorCode.PROVIDER_ALREADY_LINKED_ERROR;
import static io.github.eventify.common.exception.ApiErrorCode.PROVIDER_LINKED_ELSEWHERE_ERROR;
import static io.github.eventify.common.exception.ApiErrorCode.PROVIDER_NOT_FOUND_ERROR;

/**
 * Service for managing user authentication providers.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserAuthProviderService {

    private final UserAuthProviderRepository userAuthProviderRepository;

    private final UserRepository userRepository;

    /**
     * Returns all three providers (LOCAL, GOOGLE, GITHUB) with their connection status for the given user.
     *
     * @param user the user
     * @return list of provider responses
     */
    @Transactional(readOnly = true)
    public List<ProviderResponse> listProvidersForUser(final User user) {
        final List<UserAuthProvider> linked = userAuthProviderRepository.findAllByUser(user);
        final Map<AuthProvider, UserAuthProvider> linkedMap = linked.stream()
            .collect(Collectors.toMap(UserAuthProvider::getProvider, Function.identity()));

        return Arrays.stream(AuthProvider.values())
            .map(provider -> toProviderResponse(provider, linkedMap.get(provider)))
            .toList();
    }

    /**
     * Unlinks an authentication provider from the user.
     *
     * @param user       the authenticated user
     * @param providerId the ID of the provider record to unlink
     */
    public void unlinkProvider(final User user, final Long providerId) {
        final UserAuthProvider provider = userAuthProviderRepository.findById(providerId)
            .orElseThrow(() -> new DataNotFoundException(PROVIDER_NOT_FOUND_ERROR));

        if (!provider.getUser().getId().equals(user.getId())) {
            throw new DataNotFoundException(PROVIDER_NOT_FOUND_ERROR);
        }

        if (!canUnlink(user, provider)) {
            if (AuthProvider.LOCAL.equals(provider.getProvider())) {
                throw new LocalProviderUnlinkException();
            }
            throw new LastAuthMethodException();
        }

        if (AuthProvider.LOCAL.equals(provider.getProvider())) {
            throw new LocalProviderUnlinkException();
        }

        userAuthProviderRepository.delete(provider);
        log.info("Unlinked provider '{}' for user '{}'", provider.getProvider(), user.getEmail());
    }

    /**
     * Creates or updates a UserAuthProvider record for the given user and provider.
     *
     * @param user          the user
     * @param provider      the auth provider
     * @param providerEmail the email from the provider
     */
    public void upsertProvider(final User user, final AuthProvider provider, final String providerEmail) {
        final Optional<UserAuthProvider> existing = userAuthProviderRepository.findByUserAndProvider(user, provider);
        if (existing.isPresent()) {
            final UserAuthProvider record = existing.get();
            record.setProviderEmail(providerEmail);
            userAuthProviderRepository.save(record);
        } else {
            userAuthProviderRepository.save(new UserAuthProvider(user, provider, providerEmail));
        }
    }

    /**
     * Links an OAuth2 provider to the given user after performing cross-user safety checks.
     * <p>
     * Checks (in order):
     * <ol>
     * <li>K5: Same provider already linked to this user → PROVIDER_ALREADY_LINKED_ERROR</li>
     * <li>K4: (provider, providerEmail) already linked to another user → PROVIDER_LINKED_ELSEWHERE_ERROR</li>
     * <li>K3: providerEmail matches another user's primary email → EMAIL_IN_USE_ERROR (self-email is allowed)</li>
     * </ol>
     *
     * @param user          the current authenticated user
     * @param provider      the OAuth2 provider
     * @param providerEmail the email from the provider
     */
    public void linkProviderForUser(final User user, final AuthProvider provider, final String providerEmail) {
        if (userAuthProviderRepository.existsByUserAndProvider(user, provider)) {
            throw new LinkOAuth2Exception(PROVIDER_ALREADY_LINKED_ERROR);
        }

        final Optional<UserAuthProvider> existingProviderRecord =
            userAuthProviderRepository.findByProviderAndProviderEmail(provider, providerEmail);
        if (existingProviderRecord.isPresent() && !existingProviderRecord.get().getUser().getId().equals(user.getId())) {
            throw new LinkOAuth2Exception(PROVIDER_LINKED_ELSEWHERE_ERROR);
        }

        final Optional<User> emailOwner = userRepository.findByEmail(providerEmail);
        if (emailOwner.isPresent() && !emailOwner.get().getId().equals(user.getId())) {
            throw new LinkOAuth2Exception(EMAIL_IN_USE_ERROR);
        }

        userAuthProviderRepository.save(new UserAuthProvider(user, provider, providerEmail));
        log.info("Linked provider '{}' with email '{}' for user '{}'", provider, providerEmail, user.getEmail());
    }

    /**
     * Finds a UserAuthProvider by provider type and provider email.
     *
     * @param provider      the provider type
     * @param providerEmail the email from the provider
     * @return optional auth provider
     */
    @Transactional(readOnly = true)
    public Optional<UserAuthProvider> findByProviderAndProviderEmail(final AuthProvider provider, final String providerEmail) {
        return userAuthProviderRepository.findByProviderAndProviderEmail(provider, providerEmail);
    }

    private boolean canUnlink(final User user, final UserAuthProvider providerToRemove) {
        final List<UserAuthProvider> allProviders = userAuthProviderRepository.findAllByUser(user);
        final long remainingCount = allProviders.stream()
            .filter(p -> !p.getId().equals(providerToRemove.getId()))
            .count();
        return remainingCount > 0;
    }

    private ProviderResponse toProviderResponse(final AuthProvider provider, final UserAuthProvider linked) {
        final ProviderResponse response = new ProviderResponse();
        response.setProvider(provider);
        if (linked != null) {
            response.setId(linked.getId());
            response.setConnected(true);
            response.setProviderEmail(linked.getProviderEmail());
        } else {
            response.setConnected(false);
        }
        return response;
    }
}
