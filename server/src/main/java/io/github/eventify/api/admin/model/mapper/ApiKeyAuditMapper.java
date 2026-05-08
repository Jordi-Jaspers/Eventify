package io.github.eventify.api.admin.model.mapper;

import io.github.eventify.api.admin.model.response.ApiKeyAuditResponse;
import io.github.eventify.api.apikey.model.ApiKeyAudit;
import io.github.eventify.api.organization.repository.OrganizationRepository;
import io.github.eventify.api.user.model.mapper.UserMapper;
import io.github.eventify.api.user.repository.UserRepository;
import io.github.jframe.datasource.search.model.mapper.PageMapper;
import io.github.jframe.util.mapper.DateTimeMapper;
import io.github.jframe.util.mapper.config.SharedMapperConfig;

import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * Mapper for admin API key audit entities to DTOs.
 */
@Mapper(
    config = SharedMapperConfig.class,
    uses = {
        DateTimeMapper.class,
        UserMapper.class
    }
)
public abstract class ApiKeyAuditMapper extends PageMapper<ApiKeyAuditResponse, ApiKeyAudit> {

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected OrganizationRepository organizationRepository;

    /**
     * Maps ApiKeyAudit to AdminApiKeyAuditResponse. Note: ownerName, ownerEmail, organizationName are set by service enrichment.
     *
     * @param audit the audit entity
     * @return the response DTO
     */
    @Override
    @Named("toResourceObject")
    @Mapping(
        target = "keyPrefix",
        expression = "java(formatAuditKeyPrefix(audit))"
    )
    @Mapping(
        target = "revokedBy",
        source = "revokedBy"
    )
    @Mapping(
        target = "totalRequestsAtRevocation",
        source = "totalRequests"
    )
    public abstract ApiKeyAuditResponse toResourceObject(ApiKeyAudit audit);

    /**
     * Format audit key prefix.
     *
     * @param audit the audit record
     * @return formatted key prefix
     */
    protected String formatAuditKeyPrefix(final ApiKeyAudit audit) {
        return audit.getScope().getPrefix() + "****" + audit.getKeySuffix();
    }

    /**
     * Enriches the response with owner and organization data.
     *
     * @param audit    the audit entity
     * @param response the response DTO to enrich
     */
    @AfterMapping
    protected void enrichOwnerData(final ApiKeyAudit audit, @MappingTarget final ApiKeyAuditResponse response) {
        if (audit.getOwnerUserId() != null) {
            userRepository.findById(audit.getOwnerUserId()).ifPresent(owner -> {
                response.setOwnerName(owner.getFirstName() + " " + owner.getLastName());
                response.setOwnerEmail(owner.getEmail());
            });
        }

        if (audit.getOrganizationId() != null) {
            organizationRepository.findById(audit.getOrganizationId())
                .ifPresent(org -> response.setOrganizationName(org.getName()));
        }
    }
}
