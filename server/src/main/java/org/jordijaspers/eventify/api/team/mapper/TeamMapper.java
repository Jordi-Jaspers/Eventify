package org.jordijaspers.eventify.api.team.mapper;

import org.jordijaspers.eventify.api.team.model.Team;
import org.jordijaspers.eventify.api.team.model.request.TeamRequest;
import org.jordijaspers.eventify.api.team.model.response.TeamResponse;
import org.jordijaspers.eventify.common.mapper.DateTimeMapper;
import org.jordijaspers.eventify.common.mapper.config.SharedMapperConfig;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * The mapper for the user.
 */
@Mapper(
    config = SharedMapperConfig.class,
    uses = DateTimeMapper.class
)
public abstract class TeamMapper {

    public abstract Team toTeam(TeamRequest request);

    public abstract List<Team> toTeam(List<TeamRequest> requests);

    public abstract TeamRequest toTeamRequest(Team team);

    public abstract List<TeamRequest> toTeamRequest(List<Team> teams);

    public abstract TeamResponse toTeamResponse(Team team);

    public abstract List<TeamResponse> toTeamResponse(List<Team> teams);

}
