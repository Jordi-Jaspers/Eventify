package org.jordijaspers.eventify.api.team.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.jordijaspers.eventify.api.team.mapper.TeamMapper;
import org.jordijaspers.eventify.api.team.model.Team;
import org.jordijaspers.eventify.api.team.model.request.TeamMemberRequest;
import org.jordijaspers.eventify.api.team.model.request.TeamRequest;
import org.jordijaspers.eventify.api.team.model.response.TeamResponse;
import org.jordijaspers.eventify.api.team.service.TeamService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static org.jordijaspers.eventify.api.Paths.*;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    private final TeamMapper teamMapper;

    @ResponseStatus(OK)
    @Operation(summary = "Get a paginated response of all the teams.")
    @GetMapping(
        path = TEAMS_PATH,
        produces = APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyAuthority('MANAGER, ADMIN')")
    public ResponseEntity<Page<TeamResponse>> getTeams(@RequestParam(defaultValue = "0") int pageNo,
        @RequestParam(defaultValue = "10") int pageSize) {
        final Page<Team> teamPage = teamService.getTeams(PageRequest.of(pageNo, pageSize));
        return ResponseEntity.status(OK).body(teamPage.map(teamMapper::toTeamResponse));
    }

    @ResponseStatus(CREATED)
    @Operation(summary = "Creates a new team without members.")
    @PostMapping(
        path = TEAMS_PATH,
        produces = APPLICATION_JSON_VALUE,
        consumes = APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyAuthority('MANAGER, ADMIN')")
    public ResponseEntity<TeamResponse> createTeam(@RequestBody final TeamRequest request) {
        final Team team = teamService.createTeam(request);
        return ResponseEntity.status(CREATED).body(teamMapper.toTeamResponse(team));
    }

    @ResponseStatus(OK)
    @Operation(summary = "Updates name and/or description of a team.")
    @PutMapping(
        path = TEAM_PATH,
        produces = APPLICATION_JSON_VALUE,
        consumes = APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyAuthority('MANAGER, ADMIN')")
    public ResponseEntity<TeamResponse> updateTeam(@PathVariable final Long id,
        @RequestBody final TeamRequest request) {
        final Team team = teamService.updateTeam(id, request);
        return ResponseEntity.status(OK).body(teamMapper.toTeamResponse(team));
    }

    @ResponseStatus(NO_CONTENT)
    @Operation(summary = "Remove all members from a team and delete the team.")
    @DeleteMapping(
        path = TEAM_PATH,
        produces = APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyAuthority('MANAGER, ADMIN')")
    public ResponseEntity<Void> deleteTeam(@PathVariable final Long id) {
        teamService.deleteTeam(id);
        return ResponseEntity.status(NO_CONTENT).build();
    }

    @ResponseStatus(OK)
    @Operation(summary = "Add members to a team.")
    @PutMapping(
        path = TEAM_MEMBERS_PATH,
        produces = APPLICATION_JSON_VALUE,
        consumes = APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyAuthority('MANAGER, ADMIN')")
    public ResponseEntity<TeamResponse> addMembers(@PathVariable final Long id,
        @RequestBody final TeamMemberRequest request) {
        final Team team = teamService.addMembers(id, request.getUserIds());
        return ResponseEntity.status(OK).body(teamMapper.toTeamResponse(team));
    }

    @ResponseStatus(OK)
    @Operation(summary = "Remove members from a team.")
    @DeleteMapping(
        path = TEAM_MEMBERS_PATH,
        produces = APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyAuthority('MANAGER, ADMIN')")
    public ResponseEntity<TeamResponse> removeMembers(@PathVariable final Long id,
        @RequestBody final TeamMemberRequest request) {
        final Team team = teamService.removeMembers(id, request.getUserIds());
        return ResponseEntity.status(OK).body(teamMapper.toTeamResponse(team));
    }
}
