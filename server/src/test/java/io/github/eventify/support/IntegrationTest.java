package io.github.eventify.support;

import io.github.eventify.api.authentication.model.Role;
import io.github.eventify.api.authentication.model.request.RegisterUserRequest;
import io.github.eventify.api.token.model.Token;
import io.github.eventify.api.token.model.TokenType;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.user.model.request.*;
import io.github.eventify.support.util.WebMvcConfigurator;
import io.github.jframe.exception.core.DataNotFoundException;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import static io.github.eventify.api.token.model.TokenType.REFRESH_TOKEN;
import static io.github.eventify.api.token.model.TokenType.USER_VALIDATION_TOKEN;
import static io.github.eventify.common.constant.Constants.OAuthAttributes.*;
import static io.github.eventify.common.exception.ApiErrorCode.TOKEN_NOT_FOUND_ERROR;

/**
 * Base class for integration tests. This class is used to create helper methods for integration tests.
 */
@Slf4j
public class IntegrationTest extends WebMvcConfigurator {

    protected static final String FIRST_NAME = "John";
    protected static final String LAST_NAME = "Doe";
    protected static final String TEST_EMAIL = "user@integration.test";
    protected static final String TEST_PASSWORD = "Test123!@#";
    protected static final String INTEGRATION_PREFIX = "[Integration Test] - ";

    protected static final String NEW_PASSWORD = "NewTest123!@#";
    protected static final String NEW_PASSWORD_CONFIRMATION = "NewTest123!@#";

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @BeforeEach
    public void cleanUp() {
        deleteAllTestUsers();
    }

    protected User aValidatedUserWithRole(final Role role) {
        final User user = aValidatedUser();
        updateUserRole(user, role);
        return authenticationService.refresh(user.getRefreshToken().getValue());
    }

    protected void updateUserRole(final User user, final Role role) {
        userService.updateAuthority(user.getId(), role);
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

    protected UpdateRoleRequest anUpdateRoleRequest(final Role role) {
        return new UpdateRoleRequest()
            .setRole(role);
    }

    protected static RegisterUserRequest aRegisterRequest() {
        final String prefix = UUID.randomUUID().toString().substring(0, 5);
        return new RegisterUserRequest()
            .setFirstName(FIRST_NAME)
            .setLastName(LAST_NAME)
            .setEmail(prefix + "." + TEST_EMAIL)
            .setPassword(TEST_PASSWORD)
            .setPasswordConfirmation(TEST_PASSWORD);
    }

    protected static ForgotPasswordRequest aForgotPasswordRequest() {
        final ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setNewPassword(NEW_PASSWORD);
        request.setConfirmPassword(NEW_PASSWORD_CONFIRMATION);
        request.setToken(UUID.randomUUID().toString());
        return request;
    }

    protected static UpdatePasswordRequest anUpdatePasswordRequest() {
        final UpdatePasswordRequest request = new UpdatePasswordRequest();
        request.setOldPassword(TEST_PASSWORD);
        request.setNewPassword(NEW_PASSWORD);
        request.setConfirmPassword(NEW_PASSWORD_CONFIRMATION);
        return request;
    }

    protected UpdateUserDetailsRequest anUpdateUserDetailsRequest() {
        return new UpdateUserDetailsRequest()
            .setFirstName("Updated")
            .setLastName("User");
    }

    protected Token getPasswordResetToken(final User user) {
        return tokenRepository.findByEmail(user.getEmail())
            .stream()
            .filter(token -> token.getType().equals(TokenType.RESET_PASSWORD_TOKEN))
            .findFirst()
            .orElseThrow(() -> new DataNotFoundException(TOKEN_NOT_FOUND_ERROR));
    }

    protected Token getValidationToken(final User user) {
        return tokenRepository.findByEmail(user.getEmail())
            .stream()
            .filter(entry -> entry.getType().equals(USER_VALIDATION_TOKEN))
            .findFirst()
            .orElseThrow(() -> new DataNotFoundException(TOKEN_NOT_FOUND_ERROR));
    }

    protected Token getRefreshToken(final User user) {
        return tokenRepository.findByEmail(user.getEmail())
            .stream()
            .filter(entry -> entry.getType().equals(REFRESH_TOKEN))
            .findFirst()
            .orElseThrow(() -> new DataNotFoundException(TOKEN_NOT_FOUND_ERROR));
    }

    protected User getUserDetails(final String email) {
        return userService.loadUserByUsername(email);
    }

    protected OAuth2User aValidGoogleOAuth2User(final boolean emailVerified) {
        final String prefix = UUID.randomUUID().toString().substring(0, 5);
        final String email = prefix + "." + TEST_EMAIL;
        return aValidGoogleOAuth2User(email, emailVerified);
    }

    protected OAuth2User aValidGoogleOAuth2User(final String email, final boolean emailVerified) {
        final Map<String, Object> attributes = new HashMap<>();
        attributes.put(SUB, "google-12345");
        attributes.put(EMAIL, email);
        attributes.put(GIVEN_NAME, FIRST_NAME);
        attributes.put(FAMILY_NAME, LAST_NAME);
        attributes.put(EMAIL_VERIFIED, emailVerified);

        return new DefaultOAuth2User(
            List.of(),
            attributes,
            EMAIL
        );
    }

    protected OAuth2User aValidGithubOAuthUser(final boolean emailVerified) {
        final String prefix = UUID.randomUUID().toString().substring(0, 5);
        final String email = prefix + "." + TEST_EMAIL;
        return aValidGithubOAuthUser(email, emailVerified);
    }

    protected OAuth2User aValidGithubOAuthUser(final String email, final boolean emailVerified) {
        final Map<String, Object> attributes = new HashMap<>();
        attributes.put(ID, "gh-67890");
        attributes.put(EMAIL, emailVerified ? email : null);
        attributes.put(NAME, FIRST_NAME + " " + LAST_NAME);

        return new DefaultOAuth2User(
            List.of(),
            attributes,
            ID
        );
    }

    protected OAuth2UserRequest aValidOAuthRequestVia(final String registrationId) {
        final ClientRegistration clientRegistration = ClientRegistration
            .withRegistrationId(registrationId)
            .clientId("test-client-id")
            .clientSecret("test-client-secret")
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri("http://localhost/login/oauth2/code/" + registrationId)
            .authorizationUri("http://localhost/oauth2/authorize")
            .tokenUri("http://localhost/oauth2/token")
            .userInfoUri("http://localhost/oauth2/userinfo")
            .userNameAttributeName("sub")
            .build();

        final OAuth2AccessToken accessToken = new OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER,
            "test-token",
            null,
            null
        );

        return new OAuth2UserRequest(clientRegistration, accessToken);
    }

    private void deleteAllTestUsers() {
        final List<User> users = userRepository.findAllByEmailContaining(TEST_EMAIL);
        userRepository.deleteAll(users);
    }
}
