package io.github.eventify.api.token.service;

import io.github.eventify.api.token.repository.TokenRepository;
import io.github.eventify.support.UnitTest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit Test - Token Cleanup Service
 */
@DisplayName("Unit Test - Token Cleanup Service")
public class TokenCleanupServiceTest extends UnitTest {

    @Mock
    private TokenRepository tokenRepository;

    @InjectMocks
    private TokenCleanupService tokenCleanupService;

    @Test
    @DisplayName("Should delete expired tokens when tokens exist")
    public void shouldDeleteExpiredTokensWhenTokensExist() {
        // Given: Repository returns 5 deleted tokens
        when(tokenRepository.deleteExpiredTokens()).thenReturn(5);

        // When: Deleting expired tokens
        tokenCleanupService.deleteExpiredTokens();

        // Then: Should call repository method
        verify(tokenRepository).deleteExpiredTokens();
    }

    @Test
    @DisplayName("Should complete gracefully when no expired tokens")
    public void shouldCompleteGracefullyWhenNoExpiredTokens() {
        // Given: Repository returns 0 deleted tokens
        when(tokenRepository.deleteExpiredTokens()).thenReturn(0);

        // When: Deleting expired tokens
        tokenCleanupService.deleteExpiredTokens();

        // Then: Should complete without error
        verify(tokenRepository).deleteExpiredTokens();
    }

    @Test
    @DisplayName("Should handle repository exception gracefully")
    public void shouldHandleRepositoryExceptionGracefully() {
        // Given: Repository throws exception
        doThrow(new RuntimeException("Database connection lost"))
            .when(tokenRepository).deleteExpiredTokens();

        // When: Deleting expired tokens (exception is caught internally)
        tokenCleanupService.deleteExpiredTokens();

        // Then: Should not propagate exception
        verify(tokenRepository).deleteExpiredTokens();
    }

    @Test
    @DisplayName("Should be idempotent when called multiple times")
    public void shouldBeIdempotentWhenCalledMultipleTimes() {
        // Given: Repository returns different counts
        when(tokenRepository.deleteExpiredTokens())
            .thenReturn(5)
            .thenReturn(0);

        // When: Called multiple times
        tokenCleanupService.deleteExpiredTokens();
        tokenCleanupService.deleteExpiredTokens();

        // Then: Should call repository twice
        verify(tokenRepository, times(2)).deleteExpiredTokens();
    }

    @Test
    @DisplayName("Should delete large number of expired tokens")
    public void shouldDeleteLargeNumberOfExpiredTokens() {
        // Given: Repository returns large count
        when(tokenRepository.deleteExpiredTokens()).thenReturn(10000);

        // When: Deleting expired tokens
        tokenCleanupService.deleteExpiredTokens();

        // Then: Should complete successfully
        verify(tokenRepository).deleteExpiredTokens();
    }
}
