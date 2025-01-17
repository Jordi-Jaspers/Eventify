package org.jordijaspers.eventify.api.monitoring.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

import org.jordijaspers.eventify.api.monitoring.model.mapper.TimelineDurationMapper;
import org.jordijaspers.eventify.api.monitoring.model.response.TimelineResponse;
import org.jordijaspers.eventify.api.monitoring.repository.TimelineDurationRepository;
import org.springframework.stereotype.Service;

import static java.util.stream.Collectors.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimelineService {

    private final TimelineDurationRepository timelineDurationRepository;

    private final TimelineDurationMapper timelineDurationMapper;

    /**
     * Get all timelines for the given checks within the given window. The checks are grouped by check ID.
     *
     * @param checkIds The check IDs to get the timelines for
     * @param window   The window to get the timelines for
     * @return the timelines for the given checks
     */
    public Map<Long, TimelineResponse> getTimelinesForChecks(final Set<Long> checkIds, final Duration window) {
        final LocalDateTime startTime = LocalDateTime.now().minus(window);

        return timelineDurationRepository
            .findDurationsForChecks(checkIds, startTime)
            .stream()
            .collect(
                groupingBy(
                    timelineDuration -> timelineDuration.getId().getCheckId(),
                    mapping(
                        timelineDurationMapper::toTimelineDurationResponse,
                        collectingAndThen(
                            toList(),
                            TimelineResponse::new
                        )
                    )
                )
            );
    }
}
