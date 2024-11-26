package org.jordijaspers.eventify.api.team.mapper;

import org.jordijaspers.eventify.api.team.model.response.TeamMemberResponse;
import org.jordijaspers.eventify.api.user.model.User;
import org.jordijaspers.eventify.common.mapper.DateTimeMapper;
import org.jordijaspers.eventify.common.mapper.config.SharedMapperConfig;
import org.mapstruct.Mapper;


/**
 * The mapper for the user.
 */
@Mapper(
    config = SharedMapperConfig.class,
    uses = DateTimeMapper.class
)
public abstract class TeamMemberMapper {

    public abstract TeamMemberResponse toTeamMember(User user);

}
