package io.github.eventify.api.user.service;

import io.github.eventify.api.authentication.model.Role;
import io.github.eventify.api.notification.service.NotificationDispatchService;
import io.github.eventify.api.user.model.AuthProvider;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.user.model.UserAuthProvider;
import io.github.eventify.api.user.model.UserMetaData;
import io.github.eventify.api.user.model.request.UpdateUserDetailsRequest;
import io.github.eventify.api.user.repository.UserAuthProviderRepository;
import io.github.eventify.api.user.repository.UserRepository;
import io.github.eventify.common.email.service.sender.EmailService;
import io.github.eventify.common.exception.AuthorizationException;
import io.github.eventify.common.exception.DemoteLastAdminException;
import io.github.eventify.common.exception.SelfLockingException;
import io.github.eventify.common.exception.UserAlreadyExistsException;
import io.github.jframe.datasource.search.JpaSearchSpecification;
import io.github.jframe.datasource.search.model.SearchCriterium;
import io.github.jframe.datasource.search.model.input.SortablePageInput;
import io.github.jframe.exception.core.DataNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;

import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static io.github.eventify.api.authentication.model.Role.ADMIN;
import static io.github.eventify.api.authentication.model.Role.USER;
import static io.github.eventify.common.exception.ApiErrorCode.INVALID_CREDENTIALS;
import static io.github.eventify.common.exception.ApiErrorCode.USER_NOT_FOUND_ERROR;
import static io.github.eventify.common.security.SecurityUtil.getLoggedInUser;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * Core service for user management operations including registration, lookup, role management,
 * account locking, and user detail updates. Also implements {@link UserDetailsService} for
 * Spring Security authentication.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings(
    {
        "PMD.ExcessiveImports",
        "checkstyle:ClassFanOutComplexity"
    }
)
public class UserService implements UserDetailsService {

    private final PasswordEncoder passwordEncoder;

    private final UserRepository userRepository;

    private final UserAuthProviderRepository userAuthProviderRepository;

    private final UserMetaData userMetaData;

    private final EmailService emailService;

    private final NotificationDispatchService notificationDispatchService;

    /**
     * Loads a user by their email address (username) for Spring Security authentication.
     *
     * @param username the email address of the user
     * @return the {@link User} matching the given email
     * @throws AuthorizationException if no user with the given email exists
     */
    @NonNull
    @Override
    public User loadUserByUsername(@NonNull final String username) {
        return userRepository.findByEmail(username)
            .orElseThrow(() -> new AuthorizationException(INVALID_CREDENTIALS));
    }

    /**
     * Finds a user by their email address.
     *
     * @param email the email address to look up
     * @return the matching {@link User}
     * @throws DataNotFoundException if no user with the given email exists
     */
    public User findByEmail(final String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new DataNotFoundException(USER_NOT_FOUND_ERROR));
    }

    /**
     * Finds a user by their ID.
     *
     * @param id the user ID
     * @return the matching {@link User}
     * @throws DataNotFoundException if no user with the given ID exists
     */
    public User findById(final Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new DataNotFoundException(USER_NOT_FOUND_ERROR));
    }

    /**
     * Searches users with pagination, sorting, and filtering based on the provided input.
     *
     * @param input the search input containing filters, sort order, and pagination parameters
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
     * Locks or unlocks a user account. An admin cannot lock their own account.
     *
     * @param id       the ID of the user to lock or unlock
     * @param lockUser {@code true} to lock the account, {@code false} to unlock
     * @return the updated {@link User}
     * @throws SelfLockingException if the currently authenticated user attempts to lock themselves
     */
    public User lockUser(final Long id, final boolean lockUser) {
        final User user = findById(id);
        if (Objects.equals(user.getId(), getLoggedInUser().getId())) {
            throw new SelfLockingException();
        }
        user.setEnabled(!lockUser);
        return userRepository.save(user);
    }

    /**
     * Updates the global role of a user. Prevents demoting the last system administrator.
     *
     * @param id   the ID of the user whose role should be updated
     * @param role the new role to assign
     * @return the updated {@link User}
     * @throws DemoteLastAdminException if the operation would remove the last admin
     */
    public User updateAuthority(final Long id, final Role role) {
        final User user = findById(id);
        if (isDemotingLastAdmin(user, role)) {
            throw new DemoteLastAdminException();
        }
        user.setRole(role);
        return userRepository.save(user);
    }

    /**
     * Updates the first and last name of a user from the given request.
     *
     * @param user    the user to update
     * @param request the request containing the new first and last name
     * @return the updated {@link User}
     */
    public User updateUserDetails(final User user, final UpdateUserDetailsRequest request) {
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        return updateUserDetails(user);
    }

    /**
     * Persists updated user details to the database.
     *
     * @param user the user entity to save
     * @return the saved {@link User}
     */
    public User updateUserDetails(final User user) {
        return userRepository.save(user);
    }

    /**
     * Retrieves a user along with their organization memberships eagerly loaded.
     *
     * @param id the user ID
     * @return the {@link User} with organizations loaded
     * @throws DataNotFoundException if no user with the given ID exists
     */
    public User getUserWithOrganizations(final Long id) {
        return userRepository.findByIdWithOrganizations(id)
            .orElseThrow(() -> new DataNotFoundException(USER_NOT_FOUND_ERROR));
    }

    /**
     * Resends the account validation email to the given email address. Silently does nothing
     * if the user does not exist or is already validated.
     *
     * @param email the email address to resend the validation email to
     */
    public void resendVerificationEmail(final String email) {
        log.debug("Attempting to resend validation email to '{}'", email);
        final User user = userRepository.findByEmail(email).orElse(null);
        if (isNull(user) || user.isValidated()) {
            log.error("User '{}' not found or already validated", email);
            return;
        }
        emailService.sendUserValidationEmail(user);
    }

    /**
     * Registers a new user and sends a validation email. If a user with the same email already
     * exists and is not yet validated, the validation email is resent. If the user is already
     * validated, a {@link UserAlreadyExistsException} is thrown.
     *
     * @param newUser  the user entity to register
     * @param password the plain-text password to encode and assign
     * @return the registered (or existing) {@link User}
     * @throws UserAlreadyExistsException if a validated user with the same email already exists
     */
    public User registerAndNotify(final User newUser, final String password) {
        log.debug("Attempting to register new user '{}'", newUser.getEmail());
        final User existingUser = userRepository.findByEmail(newUser.getEmail()).orElse(null);
        if (nonNull(existingUser)) {
            return handleExistingUserOnRegister(existingUser);
        }
        final User user = register(newUser, password);
        log.info("User has been registered, sending email to validate account.");
        emailService.sendUserValidationEmail(user);
        notificationDispatchService.dispatchWelcomeNotification(user);
        return user;
    }

    /**
     * Updates the event retention period (in days) for the given user.
     *
     * @param user          the user to update
     * @param retentionDays the number of days to retain events
     * @return the updated {@link User}
     */
    public User updateRetentionDays(final User user, final Integer retentionDays) {
        user.setRetentionDays(retentionDays);
        return userRepository.save(user);
    }

    private User handleExistingUserOnRegister(final User existingUser) {
        if (existingUser.isValidated()) {
            resendVerificationEmail(existingUser.getEmail());
            return existingUser;
        }
        log.error("User '{}' already exists", existingUser.getEmail());
        throw new UserAlreadyExistsException();
    }

    private boolean isDemotingLastAdmin(final User user, final Role role) {
        return user.getRole() == ADMIN && role != ADMIN && userRepository.countByRole(ADMIN) <= 1;
    }

    private User register(final User newUser, final String password) {
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setRole(USER);
        newUser.setEnabled(true);
        newUser.setValidated(false);
        final User savedUser = userRepository.save(newUser);
        userAuthProviderRepository.save(new UserAuthProvider(savedUser, AuthProvider.LOCAL, savedUser.getEmail()));
        return savedUser;
    }
}
