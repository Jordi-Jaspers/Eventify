package io.github.eventify.api.admin.model.mapper;

import io.github.eventify.api.admin.model.response.AuditLogResponse;
import io.github.eventify.common.audit.model.AuditLog;
import io.github.jframe.datasource.search.model.mapper.PageMapper;
import io.github.jframe.util.mapper.DateTimeMapper;
import io.github.jframe.util.mapper.config.SharedMapperConfig;

import java.util.List;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/** MapStruct mapper for converting AuditLog entities to response objects. */
@Mapper(
    config = SharedMapperConfig.class,
    uses = DateTimeMapper.class
)
public abstract class AuditLogMapper extends PageMapper<AuditLogResponse, AuditLog> {

    @Override
    @Named("toResourceObject")
    @Mapping(
        target = "actorEmail",
        source = "actor.email"
    )
    public abstract AuditLogResponse toResourceObject(AuditLog auditLog);

    @IterableMapping(qualifiedByName = "toResourceObject")
    public abstract List<AuditLogResponse> toResourceObjects(List<AuditLog> auditLogs);
}
