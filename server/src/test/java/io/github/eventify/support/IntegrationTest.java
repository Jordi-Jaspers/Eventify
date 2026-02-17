package io.github.eventify.support;

import io.github.eventify.api.apikey.model.ApiKey;
import io.github.eventify.api.apikey.model.ApiKeyScope;
import io.github.eventify.api.authentication.model.Role;
import io.github.eventify.api.authentication.model.request.RegisterUserRequest;
import io.github.eventify.api.channel.model.Channel;
import io.github.eventify.api.channel.model.ChannelStatus;
import io.github.eventify.api.event.model.Event;
import io.github.eventify.api.event.model.Severity;
import io.github.eventify.api.organization.model.Organization;
import io.github.eventify.api.organization.model.OrganizationMembership;
import io.github.eventify.api.organization.model.OrganizationalRole;
import io.github.eventify.api.organization.model.request.ProvisionOrganizationRequest;
import io.github.eventify.api.quota.model.UserEventQuota;
import io.github.eventify.api.token.model.Token;
import io.github.eventify.api.token.model.TokenType;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.user.model.request.ForgotPasswordRequest;
import io.github.eventify.api.user.model.request.UpdatePasswordRequest;
import io.github.eventify.api.user.model.request.UpdateRoleRequest;
import io.github.eventify.api.user.model.request.UpdateUserDetailsRequest;
import io.github.eventify.common.security.principal.JwtUserPrincipalAuthenticationToken;
import io.github.eventify.common.security.principal.UserTokenPrincipal;
import io.github.eventify.common.util.TimeProvider;
import io.github.eventify.support.util.WebMvcConfigurator;
import io.github.jframe.exception.core.DataNotFoundException;
import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
 * Base class for integration tests. This class provides helper methods for creating test data and ensures proper cleanup between tests.
 *
 * <p>All test data is created with identifiable patterns to enable targeted cleanup:</p>
 * <ul>
 * <li>Users: emails contain {@link #TEST_EMAIL}</li>
 * <li>Organizations: names contain {@link #INTEGRATION_PREFIX}</li>
 * <li>Channels, Events, API Keys, Quotas: linked to test users</li>
 * </ul>
 *
 * <p>Cleanup uses efficient bulk SQL deletes in a single transaction.</p>
 */
@Slf4j
public class IntegrationTest extends WebMvcConfigurator {

    protected static final String FIRST_NAME = "John";
    protected static final String LAST_NAME = "Doe";
    protected static final String TEST_EMAIL = "user@integration.test";
    protected static final String TEST_PASSWORD = "Test123!@#";
    protected static final String INTEGRATION_PREFIX = "[Integration Test] - ";

    protected static final String ORGANIZATION_NAME = "Test Organization";

    protected static final String NEW_PASSWORD = "NewTest123!@#";
    protected static final String NEW_PASSWORD_CONFIRMATION = "NewTest123!@#";

    protected User admin;

    @BeforeEach
    public void setUpTest() {
        cleanUpTestData();

        admin = aValidatedUserWithRole(Role.ADMIN);
        final JwtUserPrincipalAuthenticationToken authentication = new JwtUserPrincipalAuthenticationToken(
            new UserTokenPrincipal(admin, admin.getAccessToken().getValue()),
            admin.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @AfterEach
    public void tearDownTest() {
        SecurityContextHolder.clearContext();
    }

    /**
     * Cleans up all test data using efficient bulk SQL in a single transaction.
     */
    private void cleanUpTestData() {
        final List<User> testUsers = userRepository.findAllByEmailContaining(TEST_EMAIL);
        final List<Long> testUserIds = testUsers.stream().map(User::getId).toList();
        final List<String> testEmails = testUsers.stream().map(User::getEmail).toList();

        testDataCleanupService.cleanUpTestData(testUserIds, testEmails, INTEGRATION_PREFIX);
    }

    // ========================= USER FACTORY METHODS =========================

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
        return authenticationService.register(userDetailsMapper.toUser(registerRequest), registerRequest.getPassword());
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

    protected User getUserDetails(final String email) {
        return userService.loadUserByUsername(email);
    }

    // ========================= ORGANIZATION FACTORY METHODS =========================

    protected ProvisionOrganizationRequest aProvisionOrganizationRequest() {
        final String suffix = UUID.randomUUID().toString().substring(0, 5);
        final User owner = aValidatedUser();
        return new ProvisionOrganizationRequest()
            .setName(INTEGRATION_PREFIX + ORGANIZATION_NAME + "-" + suffix)
            .setOwner(owner.getEmail());
    }

    protected ProvisionOrganizationRequest aProvisionOrganizationRequestWithOwner(final String ownerEmail) {
        final String suffix = UUID.randomUUID().toString().substring(0, 5);
        return new ProvisionOrganizationRequest()
            .setName(INTEGRATION_PREFIX + ORGANIZATION_NAME + "-" + suffix)
            .setOwner(ownerEmail);
    }

    protected Organization anOrganisationWithOwner(final User owner) {
        final Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();

        try {
            final JwtUserPrincipalAuthenticationToken ownerAuth = new JwtUserPrincipalAuthenticationToken(
                new UserTokenPrincipal(owner, owner.getAccessToken().getValue()),
                owner.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(ownerAuth);

            final String suffix = UUID.randomUUID().toString().substring(0, 5);
            final Organization org = new Organization(
                INTEGRATION_PREFIX + ORGANIZATION_NAME + "-" + suffix,
                "test-org-" + suffix
            );
            org.setCreatedBy(owner.getId());
            final Organization savedOrg = organizationRepository.save(org);

            final OrganizationMembership ownerMembership = new OrganizationMembership(
                savedOrg,
                owner,
                OrganizationalRole.OWNER
            );
            organizationMembershipRepository.save(ownerMembership);

            return savedOrg;
        } finally {
            SecurityContextHolder.getContext().setAuthentication(currentAuth);
        }
    }

    protected void addMemberToOrganization(final Organization organization, final User user,
        final OrganizationalRole role) {
        final OrganizationMembership membership = new OrganizationMembership(organization, user, role);
        organizationMembershipRepository.save(membership);
    }

    // ========================= CHANNEL FACTORY METHODS =========================

    protected Channel aChannelForUser(final User user, final String name) {
        final String slug = generateSlugFromName(name);
        final Channel channel = new Channel();
        channel.setName(name);
        channel.setSlug(slug);
        channel.setUser(user);
        channel.setOrganization(null);
        channel.setStatus(ChannelStatus.ACTIVE);
        channel.setIsStale(false);
        return channelRepository.save(channel);
    }

    protected Channel aChannelForOrganisation(final User user, final Organization org, final String name) {
        final String slug = generateSlugFromName(name);
        final Channel channel = new Channel();
        channel.setName(name);
        channel.setSlug(slug);
        channel.setUser(user);
        channel.setOrganization(org);
        channel.setStatus(ChannelStatus.ACTIVE);
        channel.setIsStale(false);
        return channelRepository.save(channel);
    }

    private String generateSlugFromName(final String name) {
        final String baseSlug = name.toLowerCase()
            .replaceAll("[^a-z0-9]+", ".")
            .replaceAll("^\\.+|\\.+$", "")
            .replaceAll("\\.{2,}", ".");
        return baseSlug + "." + UUID.randomUUID().toString().substring(0, 8);
    }

    protected void pauseChannel(final Channel channel) {
        channel.setStatus(ChannelStatus.PAUSED);
        channelRepository.save(channel);
    }

    /**
     * Updates a channel's created_at timestamp using direct SQL.
     * Required because the Channel entity has @CreationTimestamp and updatable=false on created_at,
     * which prevents JPA from updating this field after initial persist.
     *
     * @param channel   the channel to update
     * @param createdAt the new created_at timestamp
     */
    protected void updateChannelCreatedAt(final Channel channel, final OffsetDateTime createdAt) {
        final OffsetDateTime truncated = TimeProvider.truncateToMicros(createdAt);
        jdbcTemplate.update(
            "UPDATE channel SET created_at = ? WHERE id = ?",
            java.sql.Timestamp.from(truncated.toInstant()),
            channel.getId()
        );
        // Refresh entity to reflect the change
        channelRepository.findById(channel.getId()).ifPresent(c -> channel.setCreatedAt(c.getCreatedAt()));
    }

    // ========================= API KEY FACTORY METHODS =========================

    protected ApiKey anApiKeyForUser(final User user, final String name) {
        return anApiKeyForUserWithExpiry(user, name, null);
    }

    protected ApiKey anExpiredApiKeyForUser(final User user, final String name) {
        return anApiKeyForUserWithExpiry(user, name, TimeProvider.now().minusDays(1));
    }

    protected ApiKey anApiKeyForUserWithExpiry(final User user, final String name, final OffsetDateTime expiresAt) {
        final String randomPart = String.format("%032d", System.nanoTime()).substring(0, 32);
        final String rawKey = "evt_" + randomPart;
        final ApiKey apiKey = new ApiKey();
        apiKey.setName(name);
        apiKey.setUser(user);
        apiKey.setOrganization(null);
        apiKey.setScope(ApiKeyScope.USER);
        apiKey.setHashedKey(passwordEncoder.encode(rawKey));
        apiKey.setSuffix(rawKey.substring(rawKey.length() - 4));
        apiKey.setExpiresAt(expiresAt);
        apiKey.setKey(rawKey);
        return apiKeyRepository.save(apiKey);
    }

    protected ApiKey anApiKeyForOrganisation(final User user, final Organization org, final String name) {
        final String randomPart = String.format("%032d", System.nanoTime()).substring(0, 32);
        final String rawKey = "org_" + randomPart;
        final ApiKey apiKey = new ApiKey();
        apiKey.setName(name);
        apiKey.setUser(user);
        apiKey.setOrganization(org);
        apiKey.setScope(ApiKeyScope.ORGANIZATION);
        apiKey.setHashedKey(passwordEncoder.encode(rawKey));
        apiKey.setSuffix(rawKey.substring(rawKey.length() - 4));
        apiKey.setExpiresAt(null);
        apiKey.setKey(rawKey);
        return apiKeyRepository.save(apiKey);
    }

    // ========================= EVENT FACTORY METHODS =========================

    protected Event anEventForChannel(final Channel channel, final int daysAgo) {
        final Event event = new Event();
        event.setChannel(channel);
        event.setSeverity(Severity.OK);
        event.setTitle("Test Event");
        event.setMessage("Test message");
        event.setTimestamp(TimeProvider.now().minusDays(daysAgo));
        return event;
    }

    protected Event anEventForChannel(final Channel channel, final Severity severity, final OffsetDateTime timestamp) {
        final Event event = new Event();
        event.setChannel(channel);
        event.setSeverity(severity);
        event.setTitle("Test Event - " + severity);
        event.setMessage("Test message");
        event.setTimestamp(TimeProvider.truncateToMicros(timestamp));
        return eventRepository.save(event);
    }

    // ========================= WATCHLIST FACTORY METHODS =========================

    protected io.github.eventify.api.watchlist.model.Watchlist aWatchlistForUser(final User user, final String name) {
        final io.github.eventify.api.watchlist.model.Watchlist watchlist = new io.github.eventify.api.watchlist.model.Watchlist(
            name,
            user,
            null
        );
        return watchlistRepository.save(watchlist);
    }

    protected io.github.eventify.api.watchlist.model.Watchlist aWatchlistForOrganization(final User user, final Organization org,
        final String name) {
        final io.github.eventify.api.watchlist.model.Watchlist watchlist = new io.github.eventify.api.watchlist.model.Watchlist(
            name,
            user,
            org
        );
        return watchlistRepository.save(watchlist);
    }

    protected void addChannelToWatchlist(final io.github.eventify.api.watchlist.model.Watchlist watchlist, final Channel channel) {
        final io.github.eventify.api.watchlist.model.WatchlistConfiguration config = watchlist.getConfiguration();
        config.getChannels().add(channel);
        watchlist.setConfiguration(config);
        watchlistRepository.save(watchlist);
    }

    // ========================= QUOTA HELPER METHODS =========================

    protected void seedUserEventQuota(final User user, final int eventCount) {
        final UserEventQuota quota = userEventQuotaRepository.findByUserId(user.getId())
            .orElseGet(() -> {
                final UserEventQuota newQuota = new UserEventQuota(user);
                return userEventQuotaRepository.save(newQuota);
            });
        quota.setEventCount(eventCount);
        userEventQuotaRepository.save(quota);
    }

    protected int getUserQuotaEventCount(final User user) {
        return userEventQuotaRepository.findByUserId(user.getId())
            .map(UserEventQuota::getEventCount)
            .orElse(0);
    }

    // ========================= REQUEST FACTORY METHODS =========================

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

    // ========================= TOKEN HELPER METHODS =========================

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

    // ========================= OAUTH HELPER METHODS =========================

    protected OAuth2User aGoogleOAuth2User(final boolean emailVerified) {
        final String prefix = UUID.randomUUID().toString().substring(0, 5);
        final String email = prefix + "." + TEST_EMAIL;
        return aGoogleOAuth2User(email, emailVerified);
    }

    protected OAuth2User aGoogleOAuth2User(final String email, final boolean emailVerified) {
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

    protected OAuth2User aGithubOAuthUser(final boolean emailVerified) {
        final String prefix = UUID.randomUUID().toString().substring(0, 5);
        final String email = prefix + "." + TEST_EMAIL;
        return aGithubOAuthUser(email, emailVerified);
    }

    protected OAuth2User aGithubOAuthUser(final String email, final boolean emailVerified) {
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

    protected OAuth2UserRequest anOAuthRequestVia(final String registrationId) {
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

}
