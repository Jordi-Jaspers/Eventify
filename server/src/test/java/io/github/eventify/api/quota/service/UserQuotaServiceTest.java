package io.github.eventify.api.quota.service;

import io.github.eventify.api.quota.model.UserEventQuota;
import io.github.eventify.api.quota.model.response.UserQuotaResponse;
import io.github.eventify.api.quota.repository.UserEventQuotaRepository;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.user.service.UserService;
import io.github.eventify.common.exception.QuotaExceededException;
import io.github.eventify.support.UnitTest;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import static java.time.ZoneOffset.UTC;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@DisplayName("Unit Test - User Quota Service")
public class UserQuotaServiceTest extends UnitTest {

    private static final Long USER_ID = 1L;
    private static final int QUOTA_LIMIT = 1000;
    private static final int UNDER_QUOTA_COUNT = 500;
    private static final int AT_QUOTA_COUNT = 1000;
    private static final int OVER_QUOTA_COUNT = 1050;

    @Mock
    private UserEventQuotaRepository quotaRepository;

    @Mock
    private UserService userService;

    private UserQuotaService userQuotaService;
    private User user;

    @BeforeEach
    void setUp() {
        userQuotaService = new UserQuotaService(quotaRepository, userService);
        user = aValidUser();
        user.setId(USER_ID);
        lenient().when(userService.findById(USER_ID)).thenReturn(user);
    }

    @Nested
    @DisplayName("checkAndIncrementOrThrow")
    class CheckAndIncrementOrThrow {

        @Test
        @DisplayName("Should accept event when user is under quota")
        void shouldAcceptEventWhenUserUnderQuota() {
            // Given: User has 500 events (under 1000 limit)
            final UserEventQuota quota = aQuotaWithEventCount(UNDER_QUOTA_COUNT);
            given(quotaRepository.findByUserIdWithLock(USER_ID)).willReturn(Optional.of(quota));
            given(quotaRepository.save(any(UserEventQuota.class))).willAnswer(invocation -> invocation.getArgument(0));

            // When: Checking and incrementing for 1 event
            assertDoesNotThrow(() -> userQuotaService.checkAndIncrementOrThrow(USER_ID, 1));

            // Then: Event count should be incremented
            assertThat(quota.getEventCount(), is(UNDER_QUOTA_COUNT + 1));
            verify(quotaRepository).save(quota);
        }

        @Test
        @DisplayName("Should throw QuotaExceededException when user is at quota")
        void shouldThrowWhenUserAtQuota() {
            // Given: User has exactly 1000 events (at limit)
            final UserEventQuota quota = aQuotaWithEventCount(AT_QUOTA_COUNT);
            given(quotaRepository.findByUserIdWithLock(USER_ID)).willReturn(Optional.of(quota));

            // When & Then: Should throw QuotaExceededException
            final QuotaExceededException exception = assertThrows(
                QuotaExceededException.class,
                () -> userQuotaService.checkAndIncrementOrThrow(USER_ID, 1)
            );

            // And: Exception should contain rate limit info
            assertThat(exception.getLimit(), is(QUOTA_LIMIT));
            assertThat(exception.getRemaining(), is(0));
            assertThat(exception.getResetDate(), is(notNullValue()));

            // And: Event count should not be incremented
            assertThat(quota.getEventCount(), is(AT_QUOTA_COUNT));
            verify(quotaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw when batch would exceed quota")
        void shouldThrowWhenBatchWouldExceedQuota() {
            // Given: User has 995 events
            final int currentCount = 995;
            final UserEventQuota quota = aQuotaWithEventCount(currentCount);
            given(quotaRepository.findByUserIdWithLock(USER_ID)).willReturn(Optional.of(quota));

            // When & Then: Batch of 10 should be rejected
            final QuotaExceededException exception = assertThrows(
                QuotaExceededException.class,
                () -> userQuotaService.checkAndIncrementOrThrow(USER_ID, 10)
            );

            // And: Exception should contain correct remaining count
            assertThat(exception.getRemaining(), is(QUOTA_LIMIT - currentCount));

            // And: No events should be saved
            verify(quotaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should accept batch that exactly fills quota")
        void shouldAcceptBatchThatExactlyFillsQuota() {
            // Given: User has 995 events
            final int currentCount = 995;
            final UserEventQuota quota = aQuotaWithEventCount(currentCount);
            given(quotaRepository.findByUserIdWithLock(USER_ID)).willReturn(Optional.of(quota));
            given(quotaRepository.save(any(UserEventQuota.class))).willAnswer(invocation -> invocation.getArgument(0));

            // When: Batch of 5 should be accepted (exactly fills to 1000)
            assertDoesNotThrow(() -> userQuotaService.checkAndIncrementOrThrow(USER_ID, 5));

            // Then: Event count should be exactly at limit
            assertThat(quota.getEventCount(), is(QUOTA_LIMIT));
            verify(quotaRepository).save(quota);
        }

        @Test
        @DisplayName("Should create quota record on first event")
        void shouldCreateQuotaRecordOnFirstEvent() {
            // Given: No existing quota record
            given(quotaRepository.findByUserIdWithLock(USER_ID)).willReturn(Optional.empty());
            given(quotaRepository.save(any(UserEventQuota.class))).willAnswer(invocation -> {
                final UserEventQuota q = invocation.getArgument(0);
                q.setId(1L);
                return q;
            });

            // When: First event is sent
            assertDoesNotThrow(() -> userQuotaService.checkAndIncrementOrThrow(USER_ID, 1));

            // Then: Quota record should be saved (create + increment = 2 calls)
            final ArgumentCaptor<UserEventQuota> captor = ArgumentCaptor.forClass(UserEventQuota.class);
            verify(quotaRepository, times(2)).save(captor.capture());

            // And: Final saved quota should have count = 1
            final UserEventQuota savedQuota = captor.getAllValues().getLast();
            assertThat(savedQuota.getEventCount(), is(1));
            assertThat(savedQuota.getUser(), is(user));
        }

        @Test
        @DisplayName("Should accept event when exactly one under limit")
        void shouldAcceptEventWhenExactlyOneUnderLimit() {
            // Given: User has 999 events
            final UserEventQuota quota = aQuotaWithEventCount(QUOTA_LIMIT - 1);
            given(quotaRepository.findByUserIdWithLock(USER_ID)).willReturn(Optional.of(quota));
            given(quotaRepository.save(any(UserEventQuota.class))).willAnswer(invocation -> invocation.getArgument(0));

            // When: Sending 1 event
            assertDoesNotThrow(() -> userQuotaService.checkAndIncrementOrThrow(USER_ID, 1));

            // Then: Event count should be at limit
            assertThat(quota.getEventCount(), is(QUOTA_LIMIT));
        }
    }


    @Nested
    @DisplayName("getQuotaStatus")
    class GetQuotaStatus {

        @Test
        @DisplayName("Should create quota record if not exists")
        void shouldCreateQuotaRecordIfNotExists() {
            // Given: No existing quota record
            given(quotaRepository.findByUserIdWithLock(USER_ID)).willReturn(Optional.empty());
            given(quotaRepository.save(any(UserEventQuota.class))).willAnswer(invocation -> invocation.getArgument(0));

            // When: Getting quota status
            final UserQuotaResponse response = userQuotaService.getQuotaStatus(USER_ID);

            // Then: New quota record should be created with 0 usage
            assertThat(response.getUsed(), is(0));
            assertThat(response.getLimit(), is(QUOTA_LIMIT));
            assertThat(response.getRemaining(), is(QUOTA_LIMIT));
            verify(quotaRepository).save(any(UserEventQuota.class));
        }

        @Test
        @DisplayName("Should return correct quota status")
        void shouldReturnCorrectQuotaStatus() {
            // Given: User has 342 events
            final int eventCount = 342;
            final UserEventQuota quota = aQuotaWithEventCount(eventCount);
            given(quotaRepository.findByUserIdWithLock(USER_ID)).willReturn(Optional.of(quota));

            // When: Getting quota status
            final UserQuotaResponse response = userQuotaService.getQuotaStatus(USER_ID);

            // Then: Response should contain correct values
            assertThat(response.getUsed(), is(eventCount));
            assertThat(response.getLimit(), is(QUOTA_LIMIT));
            assertThat(response.getRemaining(), is(QUOTA_LIMIT - eventCount));
            assertThat(response.getPeriodStart(), is(notNullValue()));
            assertThat(response.getPeriodEnd(), is(notNullValue()));
        }

        @Test
        @DisplayName("Should calculate percent used correctly")
        void shouldCalculatePercentUsedCorrectly() {
            // Given: User has 342 events (34.2%)
            final int eventCount = 342;
            final UserEventQuota quota = aQuotaWithEventCount(eventCount);
            given(quotaRepository.findByUserIdWithLock(USER_ID)).willReturn(Optional.of(quota));

            // When: Getting quota status
            final UserQuotaResponse response = userQuotaService.getQuotaStatus(USER_ID);

            // Then: Percent used should be 34.2
            assertThat(response.getPercentUsed(), is(closeTo(34.2, 0.01)));
        }

        @Test
        @DisplayName("Should not exceed 100 percent used")
        void shouldNotExceed100PercentUsed() {
            // Given: User has 1050 events (over limit)
            final UserEventQuota quota = aQuotaWithEventCount(OVER_QUOTA_COUNT);
            given(quotaRepository.findByUserIdWithLock(USER_ID)).willReturn(Optional.of(quota));

            // When: Getting quota status
            final UserQuotaResponse response = userQuotaService.getQuotaStatus(USER_ID);

            // Then: Percent used should be capped at 100.0
            assertThat(response.getPercentUsed(), is(lessThanOrEqualTo(100.0)));
            assertThat(response.getPercentUsed(), is(100.0));
        }
    }


    @Nested
    @DisplayName("getNextResetDate")
    class GetNextResetDate {

        @Test
        @DisplayName("Should return first day of next month")
        void shouldReturnFirstDayOfNextMonth() {
            // When: Getting next reset date
            final OffsetDateTime resetDate = userQuotaService.getNextResetDate();

            // Then: Should be first day of next month at midnight
            final OffsetDateTime now = OffsetDateTime.now(UTC);
            final OffsetDateTime expectedMonth = now.plusMonths(1);
            assertThat(resetDate.getYear(), is(expectedMonth.getYear()));
            assertThat(resetDate.getMonthValue(), is(expectedMonth.getMonthValue()));
            assertThat(resetDate.getDayOfMonth(), is(1));
            assertThat(resetDate.getHour(), is(0));
            assertThat(resetDate.getMinute(), is(0));
            assertThat(resetDate.getSecond(), is(0));
        }
    }

    private UserEventQuota aQuotaWithEventCount(final int eventCount) {
        final UserEventQuota quota = new UserEventQuota();
        quota.setId(1L);
        quota.setUser(user);
        quota.setEventCount(eventCount);
        quota.setPeriodStart(OffsetDateTime.now(UTC).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0));
        quota.setUpdatedAt(OffsetDateTime.now(UTC));
        return quota;
    }
}
