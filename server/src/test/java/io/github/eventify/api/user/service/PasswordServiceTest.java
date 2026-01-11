package io.github.eventify.api.user.service;

import io.github.eventify.api.token.model.TokenType;
import io.github.eventify.api.token.service.TokenService;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.user.repository.UserRepository;
import io.github.eventify.common.email.service.sender.EmailService;
import io.github.eventify.support.UnitTest;
import io.github.jframe.exception.core.DataNotFoundException;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("Unit Test - Password Service - Force Password Reset")
public class PasswordServiceTest extends UnitTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenService tokenService;

    @Mock
    private EmailService emailService;

    private PasswordService passwordService;

    @BeforeEach
    public void setUp() {
        passwordService = new PasswordService(passwordEncoder, userRepository, tokenService, emailService);
        lenient().when(passwordEncoder.encode(anyString())).thenReturn("encoded-random-password");
    }

    @Test
    @DisplayName("Should invalidate password, tokens, and send reset email when user exists")
    public void shouldInvalidatePasswordTokensAndSendEmailWhenUserExists() {
        // Given: A valid user in the repository
        final Long userId = 1L;
        final User user = aValidUser();
        final String originalPassword = user.getPassword();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When: Force password reset is requested
        passwordService.forcePasswordReset(userId);

        // Then: User should be fetched from repository
        verify(userRepository, times(1)).findById(userId);

        // And: Password should be changed to random value
        verify(passwordEncoder, times(1)).encode(anyString());
        verify(userRepository, times(1)).save(user);

        // And: All tokens should be invalidated (security measure)
        verify(tokenService, times(1)).invalidateTokensForUser(user, TokenType.values());

        // And: Password reset email should be sent
        verify(emailService, times(1)).sendPasswordResetEmail(user);
    }

    @Test
    @DisplayName("Should set password to encoded random value")
    public void shouldSetPasswordToEncodedRandomValue() {
        // Given: A valid user in the repository
        final Long userId = 1L;
        final User user = aValidUser();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(anyString())).thenReturn("new-encoded-password");

        // When: Force password reset is requested
        passwordService.forcePasswordReset(userId);

        // Then: User password should be updated
        final ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPassword(), is("new-encoded-password"));
    }

    @Test
    @DisplayName("Should throw DataNotFoundException when user ID does not exist")
    public void shouldThrowDataNotFoundExceptionWhenUserIdDoesNotExist() {
        // Given: No user exists with the given ID
        final Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then: Force password reset should throw DataNotFoundException
        assertThrows(
            DataNotFoundException.class,
            () -> passwordService.forcePasswordReset(userId)
        );

        // And: No password change or email should occur
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(tokenService, never()).invalidateTokensForUser(any(), any());
        verify(emailService, never()).sendPasswordResetEmail(any(User.class));
    }

    @Test
    @DisplayName("Should force reset for locked user")
    public void shouldForceResetForLockedUser() {
        // Given: A locked user in the repository
        final Long userId = 1L;
        final User lockedUser = aValidUser();
        lockedUser.setEnabled(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(lockedUser));

        // When: Force password reset is requested for locked user
        passwordService.forcePasswordReset(userId);

        // Then: All operations should still occur
        verify(passwordEncoder, times(1)).encode(anyString());
        verify(userRepository, times(1)).save(lockedUser);
        verify(tokenService, times(1)).invalidateTokensForUser(lockedUser, TokenType.values());
        verify(emailService, times(1)).sendPasswordResetEmail(lockedUser);
    }

    @Test
    @DisplayName("Should force reset for user with unvalidated email")
    public void shouldForceResetForUserWithUnvalidatedEmail() {
        // Given: A user with unvalidated email in the repository
        final Long userId = 1L;
        final User unvalidatedUser = aValidUser();
        unvalidatedUser.setValidated(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(unvalidatedUser));

        // When: Force password reset is requested for unvalidated user
        passwordService.forcePasswordReset(userId);

        // Then: All operations should still occur
        verify(passwordEncoder, times(1)).encode(anyString());
        verify(userRepository, times(1)).save(unvalidatedUser);
        verify(tokenService, times(1)).invalidateTokensForUser(unvalidatedUser, TokenType.values());
        verify(emailService, times(1)).sendPasswordResetEmail(unvalidatedUser);
    }

    @Test
    @DisplayName("Should throw DataNotFoundException when user ID is null")
    public void shouldThrowDataNotFoundExceptionWhenUserIdIsNull() {
        // Given: A null user ID
        final Long userId = null;
        when(userRepository.findById(null)).thenReturn(Optional.empty());

        // When & Then: Should throw DataNotFoundException
        assertThrows(
            DataNotFoundException.class,
            () -> passwordService.forcePasswordReset(userId)
        );

        // And: No operations should occur
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(tokenService, never()).invalidateTokensForUser(any(), any());
        verify(emailService, never()).sendPasswordResetEmail(any(User.class));
    }
}
