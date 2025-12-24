package io.github.eventify.api.admin.service;

import io.github.eventify.api.user.model.User;
import io.github.eventify.api.user.model.response.UserSearchResult;
import io.github.eventify.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Service for admin user management operations.
 */
@Service
@RequiredArgsConstructor
public class AdminUserService {

    private static final int MAX_RESULTS = 10;
    private static final int MIN_QUERY_LENGTH = 3;

    private final UserRepository userRepository;

    /**
     * Search for users by email, first name, or last name.
     *
     * @param query the search query
     * @return list of matching users
     */
    public List<UserSearchResult> searchUsers(final String query) {
        if (query == null || query.length() < MIN_QUERY_LENGTH) {
            throw new IllegalArgumentException("Search query must be at least 3 characters");
        }

        final Pageable pageable = PageRequest.of(0, MAX_RESULTS);
        final List<User> users = userRepository.searchUsers(query, pageable);

        return users.stream()
            .map(this::mapToUserSearchResult)
            .toList();
    }

    private UserSearchResult mapToUserSearchResult(final User user) {
        return new UserSearchResult()
            .setId(user.getId())
            .setEmail(user.getEmail())
            .setFirstName(user.getFirstName())
            .setLastName(user.getLastName());
    }
}
