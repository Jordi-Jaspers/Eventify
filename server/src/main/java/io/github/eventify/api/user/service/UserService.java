package io.github.eventify.api.user.service;

import io.github.eventify.api.authentication.model.Role;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.user.model.UserMetaData;
import io.github.eventify.api.user.model.request.UpdateUserDetailsRequest;
import io.github.eventify.api.user.repository.UserRepository;
import io.github.eventify.common.email.service.sender.EmailService;
import io.github.eventify.common.exception.AuthorizationException;
import io.github.eventify.common.exception.UserAlreadyExistsException;
import io.github.jframe.datasource.search.model.JpaSearchSpecification;
import io.github.jframe.datasource.search.model.SearchCriterium;
import io.github.jframe.datasource.search.model.input.SortablePageInput;
import io.github.jframe.exception.core.DataNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static io.github.eventify.api.authentication.model.Role.USER;
import static io.github.eventify.common.exception.ApiErrorCode.INVALID_CREDENTIALS;
import static io.github.eventify.common.exception.ApiErrorCode.USER_NOT_FOUND_ERROR;
import static java.time.ZoneOffset.UTC;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * A service to manage users, their registration and authentication. It also implements the {@link UserDetailsService} to load users by
 * their username. So, Spring Security can use this service to authenticate users.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final PasswordEncoder passwordEncoder;

    private final UserRepository userRepository;

    private final UserMetaData userMetaData;

    private final EmailService emailService;

    /**
     * Loads the user by the given username. If the user is not found, an exception is thrown.
     *
     * @param username the username of the user
     * @return the user
     */
    @NonNull
    @Override
    public User loadUserByUsername(@NonNull final String username) {
        return userRepository.findByEmail(username)
            .orElseThrow(() -> new AuthorizationException(INVALID_CREDENTIALS));
    }

    /**
     * Search for users with pagination and sorting.
     *
     * @param input the pagination and sorting input
     * @return a page of users matching the search criteria
     */
    public Page<User> searchUsers(final SortablePageInput input) {
        final Sort sort = userMetaData.toSort(input.getSortOrder());
        final Pageable pageable = PageRequest.of(input.getPageNumber(), input.getPageSize(), sort);

        final List<SearchCriterium> criteria = userMetaData.toSearchCriteria(input.getSearchInputs());
        final Specification<User> spec = new JpaSearchSpecification<>(criteria);
        return userRepository.findAll(spec, pageable);
    }

    /**
     * Delete all unvalidated accounts that are older than a month. This method is scheduled to run every hour.
     */
    @Scheduled(
        fixedDelay = 24,
        timeUnit = TimeUnit.HOURS
    )
    public void deleteUnvalidatedAccounts() {
        userRepository.deleteUnvalidatedAccounts(OffsetDateTime.now(UTC).minusMonths(1));
    }

    /**
     * Locks or unlocks the user with the given id.
     *
     * @param id       the id of the user
     * @param lockUser true to lock the user, false to unlock the user
     */
    public User lockUser(final Long id, final boolean lockUser) {
        final User user = userRepository.findById(id)
            .orElseThrow(() -> new DataNotFoundException(USER_NOT_FOUND_ERROR));

        user.setEnabled(!lockUser);
        return userRepository.save(user);
    }

    /**
     * Update the role of the user with the given id.
     */
    public User updateAuthority(final Long id, final Role role) {
        final User user = userRepository.findById(id).orElseThrow(() -> new DataNotFoundException(USER_NOT_FOUND_ERROR));
        user.setRole(role);
        return userRepository.save(user);
    }

    /**
     * Updates the user details.
     *
     * @param user    the user to update the details for
     * @param request the request containing the new details
     * @return the updated user
     */
    public User updateUserDetails(final User user, final UpdateUserDetailsRequest request) {
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        return updateUserDetails(user);
    }

    /**
     * Updates the user details.
     *
     * @param user the user to update
     * @return the updated user
     */
    public User updateUserDetails(final User user) {
        return userRepository.save(user);
    }

    /**
     * Resends the verification email to the user with the given email.
     *
     * @param email the email of the user
     */
    public void resendVerificationEmail(final String email) {
        log.info("Attempting to resend validation email to '{}'", email);
        final User user = userRepository.findByEmail(email).orElse(null);
        if (isNull(user) || user.isValidated()) {
            log.error("User '{}' not found or already validated", email);
        } else {
            emailService.sendUserValidationEmail(user);
        }
    }

    /**
     * Registers a new user and sends an email to validate the account.
     *
     * @param newUser  the new user to register
     * @param password the password of the new user
     * @return the registered user
     */
    public User registerAndNotify(final User newUser, final String password) {
        log.info("Attempting to register new user '{}'", newUser.getEmail());
        final User existingUser = userRepository.findByEmail(newUser.getEmail()).orElse(null);
        if (nonNull(existingUser)) {
            if (existingUser.isValidated()) {
                resendVerificationEmail(existingUser.getEmail());
                return existingUser;
            }
            log.error("User '{}' already exists", newUser.getEmail());
            throw new UserAlreadyExistsException();
        }

        final User user = register(newUser, password);
        log.info("User has been registered, sending email to validate account.");
        emailService.sendUserValidationEmail(user);
        return user;
    }

    private User register(final User newUser, final String password) {
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setRole(USER);
        newUser.setEnabled(true);
        newUser.setValidated(false);
        return userRepository.save(newUser);
    }
}
