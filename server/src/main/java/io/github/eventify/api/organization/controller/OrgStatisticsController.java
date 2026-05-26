package io.github.eventify.api.organization.controller;

import io.github.eventify.api.organization.model.OrgApiKeyStatistics;
import io.github.eventify.api.organization.model.OrgSummary;
import io.github.eventify.api.organization.model.OrgTimeline;
import io.github.eventify.api.organization.model.mapper.OrgStatsMapper;
import io.github.eventify.api.organization.model.response.OrgApiKeyStatsResponse;
import io.github.eventify.api.organization.model.response.OrgSummaryResponse;
import io.github.eventify.api.organization.model.response.OrgTimelineResponse;
import io.github.eventify.api.organization.model.validator.OrgStatsValidator;
import io.github.eventify.api.organization.service.OrgStatsService;
import io.github.eventify.common.security.principal.UserTokenPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static io.github.eventify.api.Paths.ORGANIZATION_STATS_API_KEYS_PATH;
import static io.github.eventify.api.Paths.ORGANIZATION_STATS_SUMMARY_PATH;
import static io.github.eventify.api.Paths.ORGANIZATION_STATS_TIMELINE_PATH;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/** Controller for organization statistics endpoints. */
@RestController
@RequiredArgsConstructor
@Tag(
    name = "Organization Statistics",
    description = "Endpoints for organization event statistics (Owner/Admin only)"
)
public class OrgStatisticsController {

    private final OrgStatsService orgStatsService;
    private final OrgStatsMapper orgStatsMapper;
    private final OrgStatsValidator orgStatsValidator;

    @ResponseStatus(OK)
    @Operation(summary = "Get organization event timeline (Owner/Admin only)")
    @PreAuthorize("@orgSecurity.isOwnerOrAdmin(#orgId, principal.user.id)")
    @GetMapping(
        path = ORGANIZATION_STATS_TIMELINE_PATH,
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<OrgTimelineResponse> getTimeline(
        @PathVariable final Long orgId,
        @RequestParam(defaultValue = "30") final int days,
        @AuthenticationPrincipal final UserTokenPrincipal principal
    ) {
        orgStatsValidator.validateAndThrow(days);
        final OrgTimeline timeline = orgStatsService.getTimeline(orgId, days);
        return ResponseEntity.status(OK).body(orgStatsMapper.toTimelineResponse(timeline));
    }

    @ResponseStatus(OK)
    @Operation(summary = "Get organization event summary (Owner/Admin only)")
    @PreAuthorize("@orgSecurity.isOwnerOrAdmin(#orgId, principal.user.id)")
    @GetMapping(
        path = ORGANIZATION_STATS_SUMMARY_PATH,
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<OrgSummaryResponse> getSummary(
        @PathVariable final Long orgId,
        @RequestParam(defaultValue = "30") final int days,
        @AuthenticationPrincipal final UserTokenPrincipal principal
    ) {
        orgStatsValidator.validateAndThrow(days);
        final OrgSummary summary = orgStatsService.getSummary(orgId, days);
        return ResponseEntity.status(OK).body(orgStatsMapper.toSummaryResponse(summary));
    }

    @ResponseStatus(OK)
    @Operation(summary = "Get organization API key statistics (Owner/Admin only)")
    @PreAuthorize("@orgSecurity.isOwnerOrAdmin(#orgId, principal.user.id)")
    @GetMapping(
        path = ORGANIZATION_STATS_API_KEYS_PATH,
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<OrgApiKeyStatsResponse> getApiKeyStats(
        @PathVariable final Long orgId,
        @AuthenticationPrincipal final UserTokenPrincipal principal
    ) {
        final OrgApiKeyStatistics stats = orgStatsService.getApiKeyStats(orgId);
        return ResponseEntity.status(OK).body(orgStatsMapper.toApiKeyStatsResponse(stats));
    }
}
