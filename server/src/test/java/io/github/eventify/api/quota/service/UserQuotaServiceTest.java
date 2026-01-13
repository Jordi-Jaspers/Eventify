package io.github.eventify.api.quota.service;

import io.github.eventify.api.quota.model.UserEventQuota;
import io.github.eventify.api.quota.model.response.UserQuotaResponse;
import io.github.eventify.api.quota.repository.UserEventQuotaRepository;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.user.service.UserService;
import io.github.eventify.support.UnitTest;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static java.time.ZoneOffset.UTC;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
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

    @Test
    @DisplayName("Should return true when user is under quota")
    void shouldReturnTrueWhenUserUnderQuota() {
        // Given: User has 500 events (under 1000 limit)
        final UserEventQuota quota = aQuotaWithEventCount(UNDER_QUOTA_COUNT);
        given(quotaRepository.findByUserId(USER_ID)).willReturn(Optional.of(quota));

        // When: Checking if user can send event
        final boolean result = userQuotaService.canSendEvent(USER_ID);

        // Then: Should return true
        assertThat(result, is(true));
    }

    @Test
    @DisplayName("Should return false when user is at quota")
    void shouldReturnFalseWhenUserAtQuota() {
        // Given: User has exactly 1000 events (at limit)
        final UserEventQuota quota = aQuotaWithEventCount(AT_QUOTA_COUNT);
        given(quotaRepository.findByUserId(USER_ID)).willReturn(Optional.of(quota));

        // When: Checking if user can send event
        final boolean result = userQuotaService.canSendEvent(USER_ID);

        // Then: Should return false
        assertThat(result, is(false));
    }

    @Test
    @DisplayName("Should increment event count")
    void shouldIncrementEventCount() {
        // Given: User has existing quota
        final UserEventQuota quota = aQuotaWithEventCount(UNDER_QUOTA_COUNT);
        given(quotaRepository.findByUserId(USER_ID)).willReturn(Optional.of(quota));
        given(quotaRepository.save(any(UserEventQuota.class))).willAnswer(invocation -> invocation.getArgument(0));

        // When: Incrementing usage
        userQuotaService.incrementUsage(USER_ID);

        // Then: Event count should be incremented
        verify(quotaRepository).save(any(UserEventQuota.class));
        assertThat(quota.getEventCount(), is(UNDER_QUOTA_COUNT + 1));
    }

    @Test
    @DisplayName("Should create quota record if not exists")
    void shouldCreateQuotaRecordIfNotExists() {
        // Given: No existing quota record
        given(quotaRepository.findByUserId(USER_ID)).willReturn(Optional.empty());
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
        given(quotaRepository.findByUserId(USER_ID)).willReturn(Optional.of(quota));

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
        given(quotaRepository.findByUserId(USER_ID)).willReturn(Optional.of(quota));

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
        given(quotaRepository.findByUserId(USER_ID)).willReturn(Optional.of(quota));

        // When: Getting quota status
        final UserQuotaResponse response = userQuotaService.getQuotaStatus(USER_ID);

        // Then: Percent used should be capped at 100.0
        assertThat(response.getPercentUsed(), is(lessThanOrEqualTo(100.0)));
        assertThat(response.getPercentUsed(), is(100.0));
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
