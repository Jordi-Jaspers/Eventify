package io.github.eventify.api.admin.service;

import io.github.eventify.api.admin.model.AdminApiKeyAuditMetaData;
import io.github.eventify.api.admin.model.AdminApiKeyMetaData;
import io.github.eventify.api.admin.model.response.ApiKeyStatsResponse;
import io.github.eventify.api.apikey.model.ApiKey;
import io.github.eventify.api.apikey.model.ApiKeyAudit;
import io.github.eventify.api.apikey.model.ApiKeyScope;
import io.github.eventify.api.apikey.model.mapper.ApiKeyMapper;
import io.github.eventify.api.apikey.repository.ApiKeyAuditRepository;
import io.github.eventify.api.apikey.repository.ApiKeyRepository;
import io.github.eventify.api.user.model.User;
import io.github.jframe.datasource.search.JpaSearchSpecification;
import io.github.jframe.datasource.search.model.SearchCriterium;
import io.github.jframe.datasource.search.model.input.SortablePageInput;
import io.github.jframe.exception.core.DataNotFoundException;
import lombok.RequiredArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static io.github.eventify.common.exception.ApiErrorCode.API_KEY_NOT_FOUND;

/**
 * Service for admin API key management operations. Note: High coupling is expected here as this admin service coordinates between multiple
 * repositories, mappers, and metadata classes.
 */
@Service
@RequiredArgsConstructor
public class AdminApiKeyService {

    private final ApiKeyRepository apiKeyRepository;

    private final ApiKeyAuditRepository apiKeyAuditRepository;

    private final AdminApiKeyMetaData adminApiKeyMetaData;

    private final AdminApiKeyAuditMetaData adminApiKeyAuditMetaData;

    private final ApiKeyMapper apiKeyMapper;

    /**
     * Revokes an API key (user or organization) and creates an audit record.
     *
     * @param keyId   the API key ID
     * @param revoker the admin user revoking the key
     * @throws DataNotFoundException if the key does not exist
     */
    @Transactional
    public void revokeApiKey(final Long keyId, final User revoker) {
        final ApiKey apiKey = apiKeyRepository.findById(keyId)
            .orElseThrow(() -> new DataNotFoundException(API_KEY_NOT_FOUND));

        final ApiKeyAudit audit = apiKey.toAuditRecord(revoker);
        apiKeyAuditRepository.save(audit);
        apiKeyRepository.delete(apiKey);
    }

    /**
     * Get API key statistics.
     *
     * @return statistics response
     */
    @Transactional(readOnly = true)
    public ApiKeyStatsResponse getStatistics() {
        final OffsetDateTime now = OffsetDateTime.now();
        final List<ApiKey> topKeys = getTopKeysByUsage(5);
        return ApiKeyStatsResponse.builder()
            .totalKeys(apiKeyRepository.countAllKeys())
            .userKeys(apiKeyRepository.countByScope(ApiKeyScope.USER))
            .organizationKeys(apiKeyRepository.countByScope(ApiKeyScope.ORGANIZATION))
            .createdThisWeek(apiKeyRepository.countByCreatedAtAfter(now.minusDays(7)))
            .createdThisMonth(apiKeyRepository.countByCreatedAtAfter(now.minusDays(30)))
            .revokedThisMonth(apiKeyAuditRepository.countByRevokedAtAfter(now.minusDays(30)))
            .expiringNext30Days(apiKeyRepository.countByExpiresAtBetween(now, now.plusDays(30)))
            .neverUsedKeys(apiKeyRepository.countByLastUsedAtIsNull())
            .topKeysByUsage(apiKeyMapper.toResourceObjects(topKeys))
            .build();
    }

    /**
     * Search all API keys with pagination and filtering.
     *
     * @param input the search input
     * @return page of API keys
     */
    @Transactional(readOnly = true)
    public Page<ApiKey> searchApiKeys(final SortablePageInput input) {
        final Sort sort = adminApiKeyMetaData.toSort(input.getSortOrder());
        final Pageable pageable = PageRequest.of(input.getPageNumber(), input.getPageSize(), sort);
        final Specification<ApiKey> spec = adminApiKeyMetaData.toSearchSpecification(input);
        return apiKeyRepository.findAll(spec, pageable);
    }

    /**
     * Search API key audit log with pagination and enrichment.
     *
     * @param input the search input
     * @return page resource of enriched audit responses
     */
    @Transactional(readOnly = true)
    public Page<ApiKeyAudit> searchAuditLog(final SortablePageInput input) {
        final Sort sort = adminApiKeyAuditMetaData.toSort(input.getSortOrder());
        final Pageable pageable = PageRequest.of(input.getPageNumber(), input.getPageSize(), sort);

        final List<SearchCriterium> criteria = adminApiKeyAuditMetaData.toSearchCriteria(input.getSearchInputs());
        final Specification<ApiKeyAudit> spec = new JpaSearchSpecification<>(criteria);

        return apiKeyAuditRepository.findAll(spec, pageable);
    }

    /**
     * Get top API keys by usage.
     *
     * @return list of top API keys
     */
    private List<ApiKey> getTopKeysByUsage(final int limit) {
        final Pageable topFive = PageRequest.of(0, limit);
        return apiKeyRepository.findTopByOrderByTotalRequestsDesc(topFive);
    }
}
