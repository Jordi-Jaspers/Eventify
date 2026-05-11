package io.github.eventify.api.notification.service;

import io.github.eventify.api.notification.model.NotificationAudience;
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

    /**
     * Resolves the audience to a list of users.
     *
     * @param audience the notification audience
     * @return list of resolved users
     */
    public List<User> resolve(final NotificationAudience audience) {
        final Optional<User> user = userRepository.findById(audience.getUserId());
        return user.map(List::of).orElse(List.of());
    }
}
