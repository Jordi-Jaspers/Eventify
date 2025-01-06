package org.jordijaspers.eventify.api.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hawaiiframework.repository.DataNotFoundException;
import org.jordijaspers.eventify.api.authentication.model.Authority;
import org.jordijaspers.eventify.api.authentication.repository.RoleRepository;
import org.jordijaspers.eventify.api.token.model.TokenType;
import org.jordijaspers.eventify.api.token.service.TokenService;
import org.jordijaspers.eventify.api.user.model.User;
import org.jordijaspers.eventify.api.user.model.request.UpdateUserDetailsRequest;
import org.jordijaspers.eventify.api.user.repository.UserRepository;
import org.jordijaspers.eventify.common.exception.AuthorizationException;
import org.jordijaspers.eventify.common.exception.UserAlreadyExistsException;
import org.jordijaspers.eventify.email.service.sender.EmailService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.jordijaspers.eventify.api.authentication.model.Authority.USER;
import static org.jordijaspers.eventify.common.constants.Constants.Time.*;
import static org.jordijaspers.eventify.common.exception.ApiErrorCode.INVALID_CREDENTIALS;
import static org.jordijaspers.eventify.common.exception.ApiErrorCode.USER_NOT_FOUND_ERROR;

/**
 * A service to manage users, their registration and authentication. It also implements the {@link UserDetailsService} to load users by
 * their username. So, Spring Security can use this service to authenticate users.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final PasswordEncoder passwordEncoder;

    private final RoleRepository roleRepository;

    private final TokenService tokenService;

    private final UserRepository userRepository;

    private final EmailService emailService;

    /**
     * Loads the user by the given username. If the user is not found, an exception is thrown.
     *
     * @param username the username of the user
     * @return the user
     */
    @Override
    public User loadUserByUsername(final String username) {
        return userRepository.findByEmail(username)
            .orElseThrow(() -> new AuthorizationException(INVALID_CREDENTIALS));
    }

    /**
     * Retrieve all the users from the database.
     *
     * @return a list of all the users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Delete all unvalidated accounts that are older than a month. This method is scheduled to run every hour.
     */
    @Scheduled(fixedDelay = 24 * MINUTES_PER_HOUR * SECONDS_PER_MINUTE * MILLIS_PER_SECOND)
    public void deleteUnvalidatedAccounts() {
        userRepository.deleteUnvalidatedAccounts(LocalDateTime.now().minusMonths(1));
    }

    /**
     * Locks or unlocks the user with the given id.
     *
     * @param id       the id of the user
     * @param lockUser true to lock the user, false to unlock the user
     */
    public User lockUser(final Long id, final boolean lockUser) {
        final User user = userRepository.findById(id).orElseThrow();
        user.setEnabled(!lockUser);
        return userRepository.save(user);
    }

    /**
     * Update the authority of the user with the given id.
     */
    public User updateAuthority(final Long id, final Authority authority) {
        final User user = userRepository.findById(id)
            .orElseThrow(() -> new DataNotFoundException(USER_NOT_FOUND_ERROR));
        user.setRole(roleRepository.findByAuthority(authority).orElseThrow());
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
     * Updates the email of the user. If the email is already in use, an exception is thrown.
     *
     * @param user  the user to update the email for
     * @param email the new email
     * @return the updated user
     */
    public User updateEmail(final User user, final String email) {
        log.info("Attempting to update email for '{}'", user.getEmail());
        if (isEmailInAlreadyUse(email)) {
            throw new UserAlreadyExistsException();
        }

        user.setEmail(email);
        user.setValidated(false);
        user.setCreated(LocalDateTime.now());
        tokenService.invalidateTokensForUser(user, TokenType.values());

        log.info("Email has been updated to '{}', sending email to validate account.", email);
        emailService.sendUserValidationEmail(user);
        return userRepository.save(user);
    }

    /**
     * Checks if the email is already in use.
     *
     * @param email the email to check
     * @return true if the email is already in use, false otherwise
     */
    public boolean isEmailInAlreadyUse(final String email) {
        return userRepository.findByEmail(email).isPresent();
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
        newUser.setRole(roleRepository.findByAuthority(USER).orElseThrow());
        newUser.setEnabled(true);
        newUser.setValidated(false);
        return userRepository.save(newUser);
    }
}
