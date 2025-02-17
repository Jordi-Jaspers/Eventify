package org.jordijaspers.eventify.support;

import java.time.ZonedDateTime;

import org.jordijaspers.eventify.api.event.model.Status;
import org.jordijaspers.eventify.api.event.model.request.EventRequest;
import org.jordijaspers.eventify.api.monitoring.model.response.TimelineDurationResponse;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class UnitTest {

    protected static final ZonedDateTime BASE_TIME = ZonedDateTime.now(UTC).withHour(9).withMinute(0).withSecond(0).withNano(0);

    protected EventRequest createEvent(final long minutes, final Status status) {
        final EventRequest event = new EventRequest();
        event.setTimestamp(BASE_TIME.plusMinutes(minutes));
        event.setStatus(status);
        return event;
    }

    protected static void assertDuration(
        final TimelineDurationResponse duration,
        final ZonedDateTime expectedStart,
        final ZonedDateTime expectedEnd,
        final Status expectedStatus) {
        assertThat(duration)
            .satisfies(d -> {
                assertThat(d.getStartTime()).isEqualTo(expectedStart);
                assertThat(d.getEndTime()).isEqualTo(expectedEnd);
                assertThat(d.getStatus()).isEqualTo(expectedStatus);
            });
    }
}
