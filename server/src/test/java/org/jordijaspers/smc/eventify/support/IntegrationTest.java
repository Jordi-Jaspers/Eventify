package org.jordijaspers.smc.eventify.support;

import java.util.List;
import java.util.UUID;

import org.hawaiiframework.repository.DataNotFoundException;
import org.jordijaspers.eventify.api.authentication.model.request.RegisterUserRequest;
import org.jordijaspers.eventify.api.token.model.Token;
import org.jordijaspers.eventify.api.user.model.User;
import org.junit.jupiter.api.AfterEach;

import static org.jordijaspers.eventify.api.token.model.TokenType.REFRESH_TOKEN;
import static org.jordijaspers.eventify.api.token.model.TokenType.USER_VALIDATION_TOKEN;
import static org.jordijaspers.eventify.common.exception.ApiErrorCode.TOKEN_NOT_FOUND_ERROR;

/**
 * Base class for integration tests. This class is used to create helper methods for integration tests.
 */
public class IntegrationTest extends WebMvcConfigurator {

    protected static final String FIRST_NAME = "Eventify";
    protected static final String LAST_NAME = "User";
    protected static final String TEST_EMAIL = "eventify.user@integration.test";
    protected static final String TEST_PASSWORD = "Test123!@#";

    @AfterEach
    public void tearDown() {
        deleteAllTestUsers();
    }

    protected User anUnvalidatedUser() {
        final RegisterUserRequest registerRequest = aRegisterRequest();
        return authenticationService.register(userMapper.toUser(registerRequest), registerRequest.getPassword());
    }

    protected User aValidatedUser() {
        final User user = anUnvalidatedUser();
        final Token token = getValidationToken(user);
        return authenticationService.verifyEmail(token.getValue());
    }

    protected User aLockedUser() {
        final User user = aValidatedUser();
        return userService.lockUser(user.getId(), true);
    }

    protected static RegisterUserRequest aRegisterRequest() {
        return new RegisterUserRequest()
            .setEmail(UUID.randomUUID() + "-" + TEST_EMAIL)
            .setPassword(TEST_PASSWORD)
            .setPasswordConfirmation(TEST_PASSWORD)
            .setFirstName(FIRST_NAME)
            .setLastName(LAST_NAME);
    }

    protected Token getValidationToken(final User user) {
        return tokenRepository.findByEmail(user.getEmail())
            .filter(entry -> entry.getType().equals(USER_VALIDATION_TOKEN))
            .orElseThrow(() -> new DataNotFoundException(TOKEN_NOT_FOUND_ERROR));
    }

    protected Token getRefreshToken(final User user) {
        return tokenRepository.findByEmail(user.getEmail())
            .filter(entry -> entry.getType().equals(REFRESH_TOKEN))
            .orElseThrow(() -> new DataNotFoundException(TOKEN_NOT_FOUND_ERROR));
    }

    protected User getUserDetails(final String email) {
        return userService.loadUserByUsername(email);
    }

    private void deleteAllTestUsers() {
        final List<User> users = userRepository.findAllByEmailContaining(TEST_EMAIL);
        userRepository.deleteAll(users);
    }
}
