package org.jordijaspers.eventify.api.team.service;

import lombok.RequiredArgsConstructor;
import org.hawaiiframework.repository.DataNotFoundException;
import org.jordijaspers.eventify.api.team.model.Team;
import org.jordijaspers.eventify.api.team.model.request.TeamRequest;
import org.jordijaspers.eventify.api.team.repository.TeamRepository;
import org.jordijaspers.eventify.api.user.model.User;
import org.jordijaspers.eventify.api.user.repository.UserRepository;
import org.jordijaspers.eventify.common.exception.TeamAlreadyExistsException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Set;
import jakarta.transaction.Transactional;

import static org.jordijaspers.eventify.common.exception.ApiErrorCode.TEAM_NOT_FOUND_ERROR;
import static org.jordijaspers.eventify.common.exception.ApiErrorCode.USER_NOT_FOUND_ERROR;

/**
 * A service to manage teams in the application. Therefore, functions are provided to create, update, delete and retrieve teams. Also, it
 * provides functions to manage the members of a team. Multiple teams can be assigned to multiple users and vice versa.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;

    private final UserRepository userRepository;

    /**
     * Retrieves a team by its id.
     *
     * @param id The id of the team.
     * @return The team.
     */
    public Team getTeam(final Long id) {
        return teamRepository.findByIdWithMembers(id)
            .orElseThrow(() -> new DataNotFoundException(TEAM_NOT_FOUND_ERROR));
    }

    /**
     * Retrieves all teams. The result is paged.
     *
     * @param pageable The paging information.
     * @return The teams.
     */
    public Page<Team> getTeams(final Pageable pageable) {
        return teamRepository.findAll(pageable);
    }

    /**
     * Creates a new team without members.
     *
     * @param request The request to create a team.
     * @return The created team.
     */
    public Team createTeam(final TeamRequest request) {
        if (teamRepository.existsByNameIgnoreCase(request.getName())) {
            throw new TeamAlreadyExistsException();
        }

        final Team team = new Team(request.getName(), request.getDescription());
        return teamRepository.save(team);
    }

    /**
     * Updates a team by its id.
     *
     * @param id      The id of the team.
     * @param request The request to update the team.
     * @return The updated team.
     */
    public Team updateTeam(final Long id, final TeamRequest request) {
        final Team team = getTeam(id);
        team.setName(request.getName());
        team.setDescription(request.getDescription());
        return teamRepository.save(team);
    }

    /**
     * Delete a team by its id.
     *
     * @param id The id of the team.
     */
    public void deleteTeam(final Long id) {
        final Team team = getTeam(id);
        teamRepository.delete(team);
    }

    /**
     * Adds the provided members to the team.
     *
     * @param teamId  The team to which the members need to be added.
     * @param userIds The users who need to be added to the team.
     * @return The updated team.
     */
    public Team addMembers(final Long teamId, final Set<Long> userIds) {
        final Team team = getTeam(teamId);
        final Set<User> users = getUsers(userIds);

        users.forEach(team::addMember);
        return teamRepository.save(team);
    }

    /**
     * Removes the members from the team.
     *
     * @param teamId  The team from which the members need to be removed.
     * @param userIds The user who needs to be removed from the team.
     * @return The updated team.
     */
    public Team removeMembers(final Long teamId, final Set<Long> userIds) {
        final Team team = getTeam(teamId);
        final Set<User> users = getUsers(userIds);

        users.forEach(team::removeMember);
        return teamRepository.save(team);
    }

    private Set<User> getUsers(final Set<Long> userIds) {
        final Set<User> users = userRepository.findAllByIdIn(userIds);
        if (users.size() != userIds.size()) {
            throw new DataNotFoundException(USER_NOT_FOUND_ERROR);
        }
        return users;
    }
}
