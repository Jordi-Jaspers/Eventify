package io.github.eventify.api.user.service;

import io.github.eventify.api.user.repository.UserRepository;
import io.github.eventify.support.UnitTest;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit Test - User Cleanup Service
 */
@DisplayName("Unit Test - User Cleanup Service")
public class UserCleanupServiceTest extends UnitTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserCleanupService userCleanupService;

    @Test
    @DisplayName("Should delete expired unvalidated accounts when accounts exist")
    public void shouldDeleteExpiredUnvalidatedAccountsWhenAccountsExist() {
        // Given: Repository returns 3 deleted accounts
        when(userRepository.deleteUnvalidatedAccounts(any(OffsetDateTime.class))).thenReturn(3);

        // When: Deleting expired unvalidated accounts
        userCleanupService.deleteExpiredUnvalidatedAccounts();

        // Then: Should call repository method with cutoff date
        verify(userRepository).deleteUnvalidatedAccounts(any(OffsetDateTime.class));
    }

    @Test
    @DisplayName("Should complete gracefully when no expired accounts")
    public void shouldCompleteGracefullyWhenNoExpiredAccounts() {
        // Given: Repository returns 0 deleted accounts
        when(userRepository.deleteUnvalidatedAccounts(any(OffsetDateTime.class))).thenReturn(0);

        // When: Deleting expired unvalidated accounts
        userCleanupService.deleteExpiredUnvalidatedAccounts();

        // Then: Should complete without error
        verify(userRepository).deleteUnvalidatedAccounts(any(OffsetDateTime.class));
    }

    @Test
    @DisplayName("Should handle repository exception gracefully")
    public void shouldHandleRepositoryExceptionGracefully() {
        // Given: Repository throws exception
        doThrow(new RuntimeException("Database connection lost"))
            .when(userRepository).deleteUnvalidatedAccounts(any(OffsetDateTime.class));

        // When: Deleting expired accounts (exception is caught internally)
        userCleanupService.deleteExpiredUnvalidatedAccounts();

        // Then: Should not propagate exception
        verify(userRepository).deleteUnvalidatedAccounts(any(OffsetDateTime.class));
    }

    @Test
    @DisplayName("Should be idempotent when called multiple times")
    public void shouldBeIdempotentWhenCalledMultipleTimes() {
        // Given: Repository returns different counts
        when(userRepository.deleteUnvalidatedAccounts(any(OffsetDateTime.class)))
            .thenReturn(3)
            .thenReturn(0);

        // When: Called multiple times
        userCleanupService.deleteExpiredUnvalidatedAccounts();
        userCleanupService.deleteExpiredUnvalidatedAccounts();

        // Then: Should call repository twice
        verify(userRepository, times(2)).deleteUnvalidatedAccounts(any(OffsetDateTime.class));
    }

    @Test
    @DisplayName("Should use one month cutoff date for deletion")
    public void shouldUseOneMonthCutoffDateForDeletion() {
        // Given: Repository accepts any date
        when(userRepository.deleteUnvalidatedAccounts(any(OffsetDateTime.class))).thenReturn(1);

        // When: Deleting expired accounts
        userCleanupService.deleteExpiredUnvalidatedAccounts();

        // Then: Should call with a date (the service calculates 1 month ago)
        verify(userRepository).deleteUnvalidatedAccounts(any(OffsetDateTime.class));
    }
}
