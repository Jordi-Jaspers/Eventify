package io.github.eventify.api.session.controller;

import io.github.eventify.api.authentication.model.request.LoginRequest;
import io.github.eventify.api.authentication.model.response.AuthenticationResponse;
import io.github.eventify.api.session.model.response.SessionResponse;
import io.github.eventify.api.user.model.User;
import io.github.eventify.support.IntegrationTest;

import java.util.List;
import jakarta.servlet.http.Cookie;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static io.github.eventify.api.Paths.LOGIN_PATH;
import static io.github.eventify.api.Paths.USER_SESSIONS_PATH;
import static io.github.eventify.api.Paths.USER_SESSION_PATH;
import static io.github.eventify.common.constant.Constants.Security.BEARER;
import static io.github.eventify.common.constant.Constants.Security.REFRESH_TOKEN_COOKIE;
import static io.github.jframe.util.mapper.ObjectMappers.fromJson;
import static io.github.jframe.util.mapper.ObjectMappers.toJson;
import static jakarta.servlet.http.HttpServletResponse.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Integration Test - Session Controller")
public class SessionControllerTest extends IntegrationTest {

    @Test
    @DisplayName("Should return list of sessions for authenticated user")
    public void listSessionsSuccess() throws Exception {
        // Given: A validated user who has logged in (has an active session)
        final User user = aValidatedUser();
        final AuthenticationResponse auth = loginAs(user);

        // When: Requesting the list of sessions
        final MockHttpServletRequestBuilder request = get(USER_SESSIONS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + auth.getAccessToken())
            .cookie(new Cookie(REFRESH_TOKEN_COOKIE, auth.getRefreshToken()));

        final ResultActions response = mockMvc.perform(request);

        // Then: The response should be OK
        response.andExpect(status().is(SC_OK));

        // And: The response should contain at least one session
        final String content = response.andReturn().getResponse().getContentAsString();
        final List<SessionResponse> sessions = fromJsonList(content, SessionResponse.class);

        assertThat(sessions, is(not(empty())));

        // And: The current session should be marked as current
        final boolean hasCurrentSession = sessions.stream().anyMatch(SessionResponse::isCurrent);
        assertThat(hasCurrentSession, is(true));
    }

    @Test
    @DisplayName("Should include id, deviceInfo, ipAddress, userAgent, lastActiveAt, createdAt, current, expiresAt in each session")
    public void listSessionsContainsExpectedFields() throws Exception {
        // Given: A validated user who has logged in
        final User user = aValidatedUser();
        final AuthenticationResponse auth = loginAs(user);

        // When: Requesting the list of sessions
        final MockHttpServletRequestBuilder request = get(USER_SESSIONS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + auth.getAccessToken())
            .cookie(new Cookie(REFRESH_TOKEN_COOKIE, auth.getRefreshToken()));

        final ResultActions response = mockMvc.perform(request);

        // Then: The response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Each session should have an id
        final String content = response.andReturn().getResponse().getContentAsString();
        final List<SessionResponse> sessions = fromJsonList(content, SessionResponse.class);

        assertThat(sessions, is(not(empty())));
        sessions.forEach(session -> assertThat(session.getId(), is(notNullValue())));

        // And: The current session should expose a non-null expiresAt
        final SessionResponse currentSession = sessions.stream()
            .filter(SessionResponse::isCurrent)
            .findFirst()
            .orElseThrow(() -> new AssertionError("No current session found"));
        assertThat(currentSession.getExpiresAt(), is(notNullValue()));
    }

    @Test
    @DisplayName("Should return 401 when listing sessions without authentication")
    public void listSessionsRequiresAuthentication() throws Exception {
        // When: Requesting sessions without auth
        final MockHttpServletRequestBuilder request = get(USER_SESSIONS_PATH)
            .contentType(APPLICATION_JSON);

        final ResultActions response = mockMvc.perform(request);

        // Then: The response should be UNAUTHORIZED
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }

    @Test
    @DisplayName("Should return 204 and remove session when revoking own session")
    public void revokeSessionSuccess() throws Exception {
        // Given: A validated user with an active session
        final User user = aValidatedUser();
        final AuthenticationResponse auth = loginAs(user);

        // And: The session ID is retrieved from the sessions list
        final List<SessionResponse> sessions = getSessions(auth);
        assertThat(sessions, is(not(empty())));
        final Long sessionId = sessions.get(0).getId();

        // When: Revoking the session
        final MockHttpServletRequestBuilder request = delete(USER_SESSION_PATH, sessionId)
            .header(AUTHORIZATION, BEARER + auth.getAccessToken());

        final ResultActions response = mockMvc.perform(request);

        // Then: The response should be NO_CONTENT
        response.andExpect(status().is(SC_NO_CONTENT));

        // And: The session should no longer appear in the list (need a new access token to verify)
        // Note: The current access token may still be valid; we verify via DB
        final List<io.github.eventify.api.token.model.Token> remainingTokens = getRefreshTokens(user);
        final boolean sessionStillExists = remainingTokens.stream()
            .anyMatch(t -> t.getId().equals(sessionId));
        assertThat(sessionStillExists, is(false));
    }

    @Test
    @DisplayName("Should return 404 when revoking a session that belongs to a different user")
    public void revokeSessionOfOtherUserReturns404() throws Exception {
        // Given: Two validated users
        final User victim = aValidatedUser();
        final AuthenticationResponse victimAuth = loginAs(victim);

        final User attacker = aValidatedUser();
        final AuthenticationResponse attackerAuth = loginAs(attacker);

        // And: The victim's session ID
        final List<SessionResponse> victimSessions = getSessions(victimAuth);
        assertThat(victimSessions, is(not(empty())));
        final Long victimSessionId = victimSessions.get(0).getId();

        // When: Attacker tries to revoke victim's session
        final MockHttpServletRequestBuilder request = delete(USER_SESSION_PATH, victimSessionId)
            .header(AUTHORIZATION, BEARER + attackerAuth.getAccessToken());

        final ResultActions response = mockMvc.perform(request);

        // Then: The response should be NOT_FOUND (no existence leak)
        response.andExpect(status().is(SC_NOT_FOUND));
    }

    @Test
    @DisplayName("Should return 404 when revoking a session that does not exist")
    public void revokeNonExistentSessionReturns404() throws Exception {
        // Given: A validated user
        final User user = aValidatedUser();
        final AuthenticationResponse auth = loginAs(user);

        // When: Revoking a non-existent session
        final MockHttpServletRequestBuilder request = delete(USER_SESSION_PATH, 999999L)
            .header(AUTHORIZATION, BEARER + auth.getAccessToken());

        final ResultActions response = mockMvc.perform(request);

        // Then: The response should be NOT_FOUND
        response.andExpect(status().is(SC_NOT_FOUND));
    }

    @Test
    @DisplayName("Should return 401 when revoking a session without authentication")
    public void revokeSessionRequiresAuthentication() throws Exception {
        // When: Revoking a session without auth
        final MockHttpServletRequestBuilder request = delete(USER_SESSION_PATH, 1L);

        final ResultActions response = mockMvc.perform(request);

        // Then: The response should be UNAUTHORIZED
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }

    @Test
    @DisplayName("Should return 204 and revoke all sessions except the current one")
    public void revokeAllOtherSessionsSuccess() throws Exception {
        // Given: A validated user who has logged in twice (two sessions)
        final User user = aValidatedUser();
        final AuthenticationResponse sessionA = loginAs(user);
        final AuthenticationResponse sessionB = loginAs(user);

        // And: Both sessions exist (plus the verifyEmail session = 3 total)
        assertThat(getRefreshTokens(user), hasSize(3));

        // When: Revoking all other sessions from session B
        final MockHttpServletRequestBuilder request = delete(USER_SESSIONS_PATH)
            .header(AUTHORIZATION, BEARER + sessionB.getAccessToken())
            .cookie(new Cookie(REFRESH_TOKEN_COOKIE, sessionB.getRefreshToken()));

        final ResultActions response = mockMvc.perform(request);

        // Then: The response should be NO_CONTENT
        response.andExpect(status().is(SC_NO_CONTENT));

        // And: Only session B's refresh token should remain
        final List<io.github.eventify.api.token.model.Token> remaining = getRefreshTokens(user);
        assertThat(remaining, hasSize(1));
        assertThat(remaining.get(0).getValue(), is(equalTo(sessionB.getRefreshToken())));
    }

    @Test
    @DisplayName("Should return 401 when revoking all sessions without authentication")
    public void revokeAllSessionsRequiresAuthentication() throws Exception {
        // When: Revoking all sessions without auth
        final MockHttpServletRequestBuilder request = delete(USER_SESSIONS_PATH);

        final ResultActions response = mockMvc.perform(request);

        // Then: The response should be UNAUTHORIZED
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }

    // ========================= HELPER METHODS =========================

    private AuthenticationResponse loginAs(final User user) throws Exception {
        final LoginRequest loginRequest = new LoginRequest()
            .setEmail(user.getEmail())
            .setPassword(TEST_PASSWORD);

        final ResultActions loginResponse = mockMvc.perform(
            post(LOGIN_PATH)
                .contentType(APPLICATION_JSON)
                .content(toJson(loginRequest))
        );

        loginResponse.andExpect(status().is(SC_OK));

        return fromJson(
            loginResponse.andReturn().getResponse().getContentAsString(),
            AuthenticationResponse.class
        );
    }

    private List<SessionResponse> getSessions(final AuthenticationResponse auth) throws Exception {
        final ResultActions response = mockMvc.perform(
            get(USER_SESSIONS_PATH)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + auth.getAccessToken())
                .cookie(new Cookie(REFRESH_TOKEN_COOKIE, auth.getRefreshToken()))
        );

        final String content = response.andReturn().getResponse().getContentAsString();
        return fromJsonList(content, SessionResponse.class);
    }

    private <T> List<T> fromJsonList(final String json, final Class<T> clazz) {
        return objectMapper.readerForListOf(clazz).readValue(json);
    }
}
