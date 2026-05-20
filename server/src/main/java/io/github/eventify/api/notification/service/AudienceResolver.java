package io.github.eventify.api.notification.service;

import io.github.eventify.api.notification.model.NotificationAudience;
import io.github.eventify.api.notification.model.NotificationAudienceType;
import io.github.eventify.api.organization.model.OrganizationMembership;
import io.github.eventify.api.organization.repository.OrganizationMembershipRepository;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

/**
 * Resolves a {@link NotificationAudience} to a list of target users.
 */
@Service
@RequiredArgsConstructor
public class AudienceResolver {

    private final UserRepository userRepository;

    private final OrganizationMembershipRepository organizationMembershipRepository;

    /**
     * Resolves the audience to a list of users.
     *
     * @param audience the notification audience
     * @return list of resolved users
     */
    public List<User> resolve(final NotificationAudience audience) {
        return switch (audience.getType()) {
            case NotificationAudienceType.USER -> {
                final Optional<User> user = userRepository.findById(audience.getUserId());
                yield user.map(List::of).orElse(List.of());
            }
            case NotificationAudienceType.ALL_USERS -> userRepository.findAll();
            case NotificationAudienceType.ORGANIZATION -> organizationMembershipRepository
                .findAllByOrganizationIdWithUser(audience.getOrganizationId())
                .stream()
                .map(OrganizationMembership::getUser)
                .toList();
            case NotificationAudienceType.ALL_ORGANIZATION_OWNERS -> organizationMembershipRepository.findAllOwnersDistinct();
            case NotificationAudienceType.GLOBAL_ROLE -> userRepository.findAllByRole(audience.getRole());
        };
    }

    /**
     * Counts the number of users in the audience without loading them.
     *
     * @param audience the notification audience
     * @return count of resolved users
     */
    public long count(final NotificationAudience audience) {
        return switch (audience.getType()) {
            case NotificationAudienceType.USER -> userRepository.existsById(audience.getUserId()) ? 1L : 0L;
            case NotificationAudienceType.ALL_USERS -> userRepository.count();
            case NotificationAudienceType.ORGANIZATION -> organizationMembershipRepository.countByOrganizationId(
                audience.getOrganizationId()
            );
            case NotificationAudienceType.ALL_ORGANIZATION_OWNERS -> organizationMembershipRepository.countDistinctOwners();
            case NotificationAudienceType.GLOBAL_ROLE -> userRepository.countByRole(audience.getRole());
        };
    }
}
