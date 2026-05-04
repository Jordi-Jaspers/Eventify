package io.github.eventify.api.token.service;

import io.github.eventify.api.token.model.Token;
import io.github.eventify.api.token.model.TokenType;
import io.github.eventify.api.token.repository.TokenRepository;
import io.github.eventify.api.user.model.User;
import io.github.eventify.support.UnitTest;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import static java.time.ZoneOffset.UTC;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("Unit Test - Token Service")
public class TokenServiceTest extends UnitTest {

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private HttpServletRequest httpServletRequest;

    private TokenService tokenService;

    @BeforeEach
    public void setUp() {
        tokenService = new TokenService(tokenRepository, jwtService);

        // Default stub: save returns the token passed in
        when(tokenRepository.save(any(Token.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @DisplayName("Should not invalidate prior refresh tokens when generating new authorization tokens")
    public void generateAuthorizationTokensDoesNotInvalidatePriorTokens() {
        // Given: A user with an existing refresh token in the DB
        final User user = aValidUser();
        final Token existingRefreshToken = Token.builder()
            .id(99L)
            .value("existing-refresh-token")
            .type(TokenType.REFRESH_TOKEN)
            .expiresAt(OffsetDateTime.now(UTC).plusDays(30))
            .user(user)
            .build();

        // And: The repository returns the existing token when queried
        when(tokenRepository.findByEmail(user.getEmail())).thenReturn(List.of(existingRefreshToken));

        // And: JwtService generates new tokens
        final Token newAccessToken = Token.builder()
            .value("new-access-token")
            .type(TokenType.ACCESS_TOKEN)
            .expiresAt(OffsetDateTime.now(UTC).plusMinutes(15))
            .user(user)
            .build();
        final Token newRefreshToken = Token.builder()
            .value("new-refresh-token")
            .type(TokenType.REFRESH_TOKEN)
            .expiresAt(OffsetDateTime.now(UTC).plusDays(30))
            .user(user)
            .build();
        when(jwtService.generateAccessToken(user)).thenReturn(newAccessToken);
        when(jwtService.generateRefreshToken(user)).thenReturn(newRefreshToken);

        // When: Generating authorization tokens with a request context
        tokenService.generateAuthorizationTokens(user, httpServletRequest);

        // Then: The existing refresh token should NOT be deleted (no invalidation of prior tokens)
        verify(tokenRepository, never()).invalidateTokensWithTypeForUser(
            argThat(types -> types.contains(TokenType.REFRESH_TOKEN)),
            eq(user)
        );

        // And: The new refresh token should be saved
        verify(tokenRepository, atLeastOnce()).save(any(Token.class));
    }

    @Test
    @DisplayName("Should capture device info from request when generating authorization tokens")
    public void generateAuthorizationTokensCapturesDeviceInfo() {
        // Given: A user
        final User user = aValidUser();

        // And: A request with User-Agent and remote address
        when(httpServletRequest.getHeader("User-Agent"))
            .thenReturn("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
        when(httpServletRequest.getRemoteAddr()).thenReturn("1.2.3.4");
        when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn(null);

        // And: JwtService generates tokens
        final Token newAccessToken = Token.builder()
            .value("access-token")
            .type(TokenType.ACCESS_TOKEN)
            .expiresAt(OffsetDateTime.now(UTC).plusMinutes(15))
            .user(user)
            .build();
        final Token newRefreshToken = Token.builder()
            .value("refresh-token")
            .type(TokenType.REFRESH_TOKEN)
            .expiresAt(OffsetDateTime.now(UTC).plusDays(30))
            .user(user)
            .build();
        when(jwtService.generateAccessToken(user)).thenReturn(newAccessToken);
        when(jwtService.generateRefreshToken(user)).thenReturn(newRefreshToken);

        // When: Generating authorization tokens
        tokenService.generateAuthorizationTokens(user, httpServletRequest);

        // Then: The saved refresh token should have device info populated
        final ArgumentCaptor<Token> tokenCaptor = ArgumentCaptor.forClass(Token.class);
        verify(tokenRepository, atLeastOnce()).save(tokenCaptor.capture());

        final Token savedRefreshToken = tokenCaptor.getAllValues().stream()
            .filter(t -> TokenType.REFRESH_TOKEN.equals(t.getType()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("No REFRESH_TOKEN saved"));

        assertThat(savedRefreshToken.getIpAddress(), is(equalTo("1.2.3.4")));
        assertThat(savedRefreshToken.getUserAgent(), containsString("Chrome"));
        assertThat(savedRefreshToken.getDeviceInfo(), is(notNullValue()));
        assertThat(savedRefreshToken.getLastActiveAt(), is(notNullValue()));
    }

    @Test
    @DisplayName("Should use X-Forwarded-For header as IP address when present")
    public void generateAuthorizationTokensUsesXForwardedForWhenPresent() {
        // Given: A user
        final User user = aValidUser();

        // And: A request with X-Forwarded-For header
        when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn("9.9.9.9, 10.0.0.1");
        when(httpServletRequest.getRemoteAddr()).thenReturn("192.168.1.1");
        when(httpServletRequest.getHeader("User-Agent")).thenReturn("TestAgent/1.0");

        // And: JwtService generates tokens
        final Token newAccessToken = Token.builder()
            .value("access-token")
            .type(TokenType.ACCESS_TOKEN)
            .expiresAt(OffsetDateTime.now(UTC).plusMinutes(15))
            .user(user)
            .build();
        final Token newRefreshToken = Token.builder()
            .value("refresh-token")
            .type(TokenType.REFRESH_TOKEN)
            .expiresAt(OffsetDateTime.now(UTC).plusDays(30))
            .user(user)
            .build();
        when(jwtService.generateAccessToken(user)).thenReturn(newAccessToken);
        when(jwtService.generateRefreshToken(user)).thenReturn(newRefreshToken);

        // When: Generating authorization tokens
        tokenService.generateAuthorizationTokens(user, httpServletRequest);

        // Then: The saved refresh token should use the X-Forwarded-For IP (first one)
        final ArgumentCaptor<Token> tokenCaptor = ArgumentCaptor.forClass(Token.class);
        verify(tokenRepository, atLeastOnce()).save(tokenCaptor.capture());

        final Token savedRefreshToken = tokenCaptor.getAllValues().stream()
            .filter(t -> TokenType.REFRESH_TOKEN.equals(t.getType()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("No REFRESH_TOKEN saved"));

        assertThat(savedRefreshToken.getIpAddress(), is(equalTo("9.9.9.9")));
    }

    @Test
    @DisplayName("Should capture device info when verifyEmail flow calls generateAuthorizationTokens with a request")
    public void verifyEmailAlsoCapturesDeviceInfo() {
        // Given: A user (as returned by the verifyEmail flow after validation)
        final User user = aValidUser();

        // And: A request with User-Agent and remote address (simulating the HTTP request passed through verifyEmail)
        when(httpServletRequest.getHeader("User-Agent")).thenReturn("Mozilla/5.0 (iPhone; CPU iPhone OS 17_0)");
        when(httpServletRequest.getRemoteAddr()).thenReturn("10.0.0.1");
        when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn(null);

        // And: JwtService generates tokens
        final Token newAccessToken = Token.builder()
            .value("access-token")
            .type(TokenType.ACCESS_TOKEN)
            .expiresAt(OffsetDateTime.now(UTC).plusMinutes(15))
            .user(user)
            .build();
        final Token newRefreshToken = Token.builder()
            .value("refresh-token")
            .type(TokenType.REFRESH_TOKEN)
            .expiresAt(OffsetDateTime.now(UTC).plusDays(30))
            .user(user)
            .build();
        when(jwtService.generateAccessToken(user)).thenReturn(newAccessToken);
        when(jwtService.generateRefreshToken(user)).thenReturn(newRefreshToken);

        // When: generateAuthorizationTokens is called with the request (as verifyEmail will do after refactor)
        tokenService.generateAuthorizationTokens(user, httpServletRequest);

        // Then: The saved refresh token should have device metadata populated
        final ArgumentCaptor<Token> tokenCaptor = ArgumentCaptor.forClass(Token.class);
        verify(tokenRepository, atLeastOnce()).save(tokenCaptor.capture());

        final Token savedRefreshToken = tokenCaptor.getAllValues().stream()
            .filter(t -> TokenType.REFRESH_TOKEN.equals(t.getType()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("No REFRESH_TOKEN saved"));

        assertThat(savedRefreshToken.getIpAddress(), is(equalTo("10.0.0.1")));
        assertThat(savedRefreshToken.getUserAgent(), containsString("iPhone"));
        assertThat(savedRefreshToken.getDeviceInfo(), is(notNullValue()));
        assertThat(savedRefreshToken.getLastActiveAt(), is(notNullValue()));
    }

    @Test
    @DisplayName("Should only rotate the used refresh token when refreshing, leaving other sessions untouched")
    public void refreshRotatesOnlyUsedRefreshToken() {
        // Given: A user with two active refresh tokens (sessions A and B)
        final User user = aValidUser();

        final Token sessionAToken = Token.builder()
            .id(1L)
            .value("session-a-refresh-token")
            .type(TokenType.REFRESH_TOKEN)
            .expiresAt(OffsetDateTime.now(UTC).plusDays(30))
            .user(user)
            .build();

        final Token sessionBToken = Token.builder()
            .id(2L)
            .value("session-b-refresh-token")
            .type(TokenType.REFRESH_TOKEN)
            .expiresAt(OffsetDateTime.now(UTC).plusDays(30))
            .user(user)
            .build();

        // And: The repository finds session A's token by value
        when(tokenRepository.findByValue("session-a-refresh-token")).thenReturn(Optional.of(sessionAToken));

        // And: JwtService generates new tokens for the rotation
        final Token newAccessToken = Token.builder()
            .value("new-access-token")
            .type(TokenType.ACCESS_TOKEN)
            .expiresAt(OffsetDateTime.now(UTC).plusMinutes(15))
            .user(user)
            .build();
        final Token newRefreshToken = Token.builder()
            .value("new-session-a-refresh-token")
            .type(TokenType.REFRESH_TOKEN)
            .expiresAt(OffsetDateTime.now(UTC).plusDays(30))
            .user(user)
            .build();
        when(jwtService.generateAccessToken(user)).thenReturn(newAccessToken);
        when(jwtService.generateRefreshToken(user)).thenReturn(newRefreshToken);

        // When: Refreshing using session A's token
        tokenService.refresh("session-a-refresh-token", httpServletRequest);

        // Then: Only session A's token should be deleted (by value, not by user-wide invalidation)
        verify(tokenRepository, times(1)).delete(sessionAToken);

        // And: Session B's token should NOT be deleted
        verify(tokenRepository, never()).delete(sessionBToken);

        // And: A new refresh token should be saved
        verify(tokenRepository, atLeastOnce()).save(any(Token.class));
    }

    @Test
    @DisplayName("Should preserve device info on the new token when rotating a refresh token")
    public void refreshPreservesDeviceInfoOnRotatedToken() {
        // Given: A user with a refresh token that has device info
        final User user = aValidUser();

        final Token existingToken = Token.builder()
            .id(1L)
            .value("old-refresh-token")
            .type(TokenType.REFRESH_TOKEN)
            .expiresAt(OffsetDateTime.now(UTC).plusDays(30))
            .user(user)
            .build();
        existingToken.setDeviceInfo("Chrome on macOS");
        existingToken.setUserAgent("Mozilla/5.0 Chrome/120.0.0.0");
        existingToken.setIpAddress("5.5.5.5");
        existingToken.setLastActiveAt(OffsetDateTime.now(UTC).minusHours(1));

        // And: The repository finds the token by value
        when(tokenRepository.findByValue("old-refresh-token")).thenReturn(Optional.of(existingToken));

        // And: JwtService generates new tokens
        final Token newAccessToken = Token.builder()
            .value("new-access-token")
            .type(TokenType.ACCESS_TOKEN)
            .expiresAt(OffsetDateTime.now(UTC).plusMinutes(15))
            .user(user)
            .build();
        final Token newRefreshToken = Token.builder()
            .value("new-refresh-token")
            .type(TokenType.REFRESH_TOKEN)
            .expiresAt(OffsetDateTime.now(UTC).plusDays(30))
            .user(user)
            .build();
        when(jwtService.generateAccessToken(user)).thenReturn(newAccessToken);
        when(jwtService.generateRefreshToken(user)).thenReturn(newRefreshToken);

        // When: Refreshing the token
        tokenService.refresh("old-refresh-token", httpServletRequest);

        // Then: The new refresh token should carry forward the device info
        final ArgumentCaptor<Token> tokenCaptor = ArgumentCaptor.forClass(Token.class);
        verify(tokenRepository, atLeastOnce()).save(tokenCaptor.capture());

        final Token savedRefreshToken = tokenCaptor.getAllValues().stream()
            .filter(t -> TokenType.REFRESH_TOKEN.equals(t.getType()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("No REFRESH_TOKEN saved"));

        assertThat(savedRefreshToken.getDeviceInfo(), is(equalTo("Chrome on macOS")));
        assertThat(savedRefreshToken.getUserAgent(), is(equalTo("Mozilla/5.0 Chrome/120.0.0.0")));
        assertThat(savedRefreshToken.getIpAddress(), is(equalTo("5.5.5.5")));

        // And: lastActiveAt should be updated (more recent than the old one)
        assertThat(savedRefreshToken.getLastActiveAt(), is(notNullValue()));
        assertThat(
            savedRefreshToken.getLastActiveAt().isAfter(existingToken.getLastActiveAt()),
            is(true)
        );
    }
}
