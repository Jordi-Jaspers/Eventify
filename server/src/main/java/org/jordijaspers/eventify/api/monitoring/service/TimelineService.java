package org.jordijaspers.eventify.api.monitoring.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Set;

import org.jordijaspers.eventify.api.monitoring.model.mapper.TimelineDurationMapper;
import org.jordijaspers.eventify.api.monitoring.model.response.TimelineResponse;
import org.jordijaspers.eventify.api.monitoring.repository.TimelineDurationRepository;
import org.springframework.stereotype.Service;

import static java.time.ZoneOffset.UTC;
import static java.util.stream.Collectors.*;
import static org.jordijaspers.eventify.api.event.model.Status.UNKNOWN;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimelineService {

    private final TimelineDurationRepository timelineDurationRepository;

    private final TimelineDurationMapper timelineDurationMapper;

    /**
     * Get all timelines for the given checks within the given window. The checks are grouped by check ID. If the check does not have any
     * events within the given window, a duration will be created with the UNKNOWN status.
     *
     * @param checkIds The check IDs to get the timelines for
     * @param window   The window to get the timelines for in minutes
     * @return the timelines for the given checks, guaranteed to have at least one duration for each check
     */
    public Map<Long, TimelineResponse> getTimelinesForChecks(final Set<Long> checkIds, final Long window) {
        final OffsetDateTime startTime = OffsetDateTime.now(UTC).minusMinutes(window);
        final ZonedDateTime now = ZonedDateTime.now(UTC);

        final Map<Long, TimelineResponse> timelineMap = timelineDurationRepository
            .findDurationsForChecks(checkIds, startTime)
            .parallelStream()
            .collect(
                groupingBy(
                    duration -> duration.getId().getCheckId(),
                    collectingAndThen(
                        mapping(timelineDurationMapper::toTimelineDurationResponse, toList()),
                        TimelineResponse::new
                    )
                )
            );

        return checkIds.parallelStream()
            .collect(
                toMap(
                    checkId -> checkId,
                    checkId -> timelineMap.getOrDefault(checkId, new TimelineResponse(startTime.toZonedDateTime(), now, UNKNOWN))
                )
            );
    }
}
