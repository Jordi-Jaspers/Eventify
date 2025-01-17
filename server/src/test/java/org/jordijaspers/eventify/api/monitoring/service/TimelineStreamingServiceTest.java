package org.jordijaspers.eventify.api.monitoring.service;

import java.time.Duration;
import java.util.Optional;

import org.jordijaspers.eventify.api.dashboard.model.Dashboard;
import org.jordijaspers.eventify.api.dashboard.service.DashboardService;
import org.jordijaspers.eventify.api.monitoring.model.DashboardSubscription;
import org.jordijaspers.eventify.api.monitoring.model.SubscriptionKey;
import org.jordijaspers.eventify.support.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("TimelineStreamingService Unit Tests")
public class TimelineStreamingServiceTest extends UnitTest {

    private static final Long DASHBOARD_ID = 1L;

    private static final Duration DEFAULT_WINDOW = Duration.ofHours(1);

    @Mock
    private DashboardService dashboardService;

    @Mock
    private TimelineInitializer timelineInitializer;

    @InjectMocks
    private TimelineStreamingService timelineStreamingService;


    @Nested
    @DisplayName("Subscribe Tests")
    public class SubscribeTests {

        @Mock
        private Dashboard dashboard;

        @BeforeEach
        public void setUp() {
            when(dashboardService.getDashboardConfiguration(DASHBOARD_ID)).thenReturn(dashboard);
            when(dashboard.getId()).thenReturn(DASHBOARD_ID);
        }

        @Test
        @DisplayName("Should reuse existing subscription for same dashboard and window")
        public void shouldReuseExistingSubscriptionForSameDashboardAndWindow() {
            // Given: Initial subscription exists
            final SseEmitter firstEmitter = timelineStreamingService.subscribe(DASHBOARD_ID, Optional.of(DEFAULT_WINDOW));

            // When: Creating second subscription with same parameters
            final SseEmitter secondEmitter = timelineStreamingService.subscribe(DASHBOARD_ID, Optional.of(DEFAULT_WINDOW));

            // Then: Same emitter should be returned
            assertThat(secondEmitter).isSameAs(firstEmitter);

            // And: Dashboard service should only be called once
            verify(dashboardService, times(1)).getDashboardConfiguration(DASHBOARD_ID);

            // And: Timeline initializer should only be called once
            verify(timelineInitializer, times(1)).initializeTimelines(any(DashboardSubscription.class));

            // And: There is only one active subscription
            assertThat(timelineStreamingService.getSubscriptions()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should create new subscription for different window")
        public void shouldCreateNewSubscriptionForDifferentWindow() {
            // Given: Initial subscription exists
            final SseEmitter firstEmitter = timelineStreamingService.subscribe(DASHBOARD_ID, Optional.of(DEFAULT_WINDOW));

            // When: Creating subscription with different window
            final Duration differentWindow = Duration.ofHours(2);
            final SseEmitter secondEmitter = timelineStreamingService.subscribe(DASHBOARD_ID, Optional.of(differentWindow));

            // Then: Different emitter should be returned
            assertThat(secondEmitter).isNotSameAs(firstEmitter);

            // And: Dashboard service should be called twice
            verify(dashboardService, times(2)).getDashboardConfiguration(DASHBOARD_ID);

            // And: Timeline initializer should be called twice
            verify(timelineInitializer, times(2)).initializeTimelines(any(DashboardSubscription.class));

            // And: There are two active subscriptions
            assertThat(timelineStreamingService.getSubscriptions()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should cleanup subscription on completion")
        public void shouldCleanupSubscriptionOnCompletion() {
            // Given: A subscription exists
            timelineStreamingService.subscribe(DASHBOARD_ID, Optional.of(DEFAULT_WINDOW));

            // And: The emitter is active
            assertThat(timelineStreamingService.getSubscriptions()).isEqualTo(1);

            // When: Emitter completes
            timelineStreamingService.removeSubscription(new SubscriptionKey(DASHBOARD_ID, DEFAULT_WINDOW));

            // Then: the cleanup method should be called
            assertThat(timelineStreamingService.getSubscriptions()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should use default window when none provided")
        public void shouldUseDefaultWindowWhenNoneProvided() {
            // When: Creating subscription without window
            timelineStreamingService.subscribe(DASHBOARD_ID, Optional.empty());

            // Then: Default window should be used (72 hours)
            verify(timelineInitializer).initializeTimelines(
                argThat(
                    subscription -> subscription.getWindow().equals(Duration.ofHours(72))
                )
            );
        }
    }
}
