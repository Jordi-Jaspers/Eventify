package org.jordijaspers.eventify.api.team.model.mapper;

import java.util.List;

import org.jordijaspers.eventify.api.team.model.Team;
import org.jordijaspers.eventify.api.team.model.request.TeamRequest;
import org.jordijaspers.eventify.api.team.model.response.TeamResponse;
import org.jordijaspers.eventify.common.mapper.DateTimeMapper;
import org.jordijaspers.eventify.common.mapper.config.SharedMapperConfig;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * The mapper for the user.
 */
@Mapper(
    config = SharedMapperConfig.class,
    uses = {
        DateTimeMapper.class,
        TeamMemberMapper.class
    }
)
public abstract class TeamMapper {

    public abstract Team toTeam(TeamRequest request);

    public abstract List<Team> toTeam(List<TeamRequest> requests);

    public abstract TeamRequest toTeamRequest(Team team);

    public abstract List<TeamRequest> toTeamRequest(List<Team> teams);

    @Named("toTeamResponseWithMembers")
    public abstract TeamResponse toTeamResponseWithMembers(Team team);

    @Named("toTeamResponsesWithMembers")
    @IterableMapping(qualifiedByName = "toTeamResponseWithMembers")
    public abstract List<TeamResponse> toTeamResponsesWithMembers(List<Team> teams);

    @Mapping(
        target = "members",
        ignore = true
    )
    @Named("toTeamResponseWithoutMembers")
    public abstract TeamResponse toTeamResponseWithoutMembers(Team team);

    @Named("toTeamResponsesWithoutMembers")
    @IterableMapping(qualifiedByName = "toTeamResponseWithoutMembers")
    public abstract List<TeamResponse> toTeamResponsesWithoutMembers(List<Team> teams);
}
