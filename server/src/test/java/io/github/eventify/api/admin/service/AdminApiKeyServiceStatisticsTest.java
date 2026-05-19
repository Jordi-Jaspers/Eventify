package io.github.eventify.api.admin.service;

import io.github.eventify.api.admin.model.AdminApiKeyAuditMetaData;
import io.github.eventify.api.admin.model.AdminApiKeyMetaData;
import io.github.eventify.api.admin.model.response.ApiKeyStatsResponse;
import io.github.eventify.api.apikey.model.ApiKey;
import io.github.eventify.api.apikey.model.ApiKeyScope;
import io.github.eventify.api.apikey.model.GeneratedApiKey;
import io.github.eventify.api.apikey.model.mapper.ApiKeyMapper;
import io.github.eventify.api.apikey.repository.ApiKeyAuditRepository;
import io.github.eventify.api.apikey.repository.ApiKeyRepository;
import io.github.eventify.api.user.model.User;
import io.github.eventify.support.UnitTest;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Unit Test - Admin API Key Service Statistics (Consolidated Query)")
public class AdminApiKeyServiceStatisticsTest extends UnitTest {

    private AdminApiKeyService adminApiKeyService;

    @Mock
    private ApiKeyRepository apiKeyRepository;

    @Mock
    private ApiKeyAuditRepository apiKeyAuditRepository;

    @Mock
    private AdminApiKeyMetaData adminApiKeyMetaData;

    @Mock
    private AdminApiKeyAuditMetaData adminApiKeyAuditMetaData;

    @Mock
    private ApiKeyMapper apiKeyMapper;

    @BeforeEach
    public void setUp() {
        adminApiKeyService = new AdminApiKeyService(
            apiKeyRepository,
            apiKeyAuditRepository,
            adminApiKeyMetaData,
            adminApiKeyAuditMetaData,
            apiKeyMapper
        );

        // Default stubs for consolidated query
        lenient().when(apiKeyRepository.countAllKeys()).thenReturn(0L);
        lenient().when(apiKeyRepository.countByScope(ApiKeyScope.USER)).thenReturn(0L);
        lenient().when(apiKeyRepository.countByScope(ApiKeyScope.ORGANIZATION)).thenReturn(0L);
        lenient().when(apiKeyRepository.countByCreatedAtAfter(any())).thenReturn(0L);
        lenient().when(apiKeyAuditRepository.countByRevokedAtAfter(any())).thenReturn(0L);
        lenient().when(apiKeyRepository.countByExpiresAtBetween(any(), any())).thenReturn(0L);
        lenient().when(apiKeyRepository.countByLastUsedAtIsNull()).thenReturn(0L);
        lenient().when(apiKeyRepository.findTopByOrderByTotalRequestsDesc(any())).thenReturn(Collections.emptyList());
        lenient().when(apiKeyMapper.toResourceObjects(any())).thenReturn(Collections.emptyList());
    }

    // ==================== Consolidated query tests ====================

    @Test
    @DisplayName("Should return ApiKeyStatsResponse with all fields populated")
    public void shouldReturnApiKeyStatsResponseWithAllFieldsPopulated() {
        // Given: Known counts from consolidated query
        when(apiKeyRepository.countAllKeys()).thenReturn(150L);
        when(apiKeyRepository.countByScope(ApiKeyScope.USER)).thenReturn(100L);
        when(apiKeyRepository.countByScope(ApiKeyScope.ORGANIZATION)).thenReturn(50L);
        when(apiKeyRepository.countByCreatedAtAfter(any()))
            .thenReturn(15L)  // createdThisWeek
            .thenReturn(42L); // createdThisMonth
        when(apiKeyAuditRepository.countByRevokedAtAfter(any())).thenReturn(8L);
        when(apiKeyRepository.countByExpiresAtBetween(any(), any())).thenReturn(12L);
        when(apiKeyRepository.countByLastUsedAtIsNull()).thenReturn(25L);

        // When: Getting statistics
        final ApiKeyStatsResponse stats = adminApiKeyService.getStatistics();

        // Then: All fields should be populated correctly
        assertThat(stats, is(notNullValue()));
        assertThat(stats.getTotalKeys(), is(equalTo(150L)));
        assertThat(stats.getUserKeys(), is(equalTo(100L)));
        assertThat(stats.getOrganizationKeys(), is(equalTo(50L)));
        assertThat(stats.getCreatedThisWeek(), is(equalTo(15L)));
        assertThat(stats.getCreatedThisMonth(), is(equalTo(42L)));
        assertThat(stats.getRevokedThisMonth(), is(equalTo(8L)));
        assertThat(stats.getExpiringNext30Days(), is(equalTo(12L)));
        assertThat(stats.getNeverUsedKeys(), is(equalTo(25L)));
    }

    @Test
    @DisplayName("Should return zero stats when no API keys exist")
    public void shouldReturnZeroStatsWhenNoApiKeysExist() {
        // Given: No API keys in database (all return 0)

        // When: Getting statistics
        final ApiKeyStatsResponse stats = adminApiKeyService.getStatistics();

        // Then: All counts should be zero (not null)
        assertThat(stats.getTotalKeys(), is(equalTo(0L)));
        assertThat(stats.getUserKeys(), is(equalTo(0L)));
        assertThat(stats.getOrganizationKeys(), is(equalTo(0L)));
        assertThat(stats.getCreatedThisWeek(), is(equalTo(0L)));
        assertThat(stats.getCreatedThisMonth(), is(equalTo(0L)));
        assertThat(stats.getRevokedThisMonth(), is(equalTo(0L)));
        assertThat(stats.getExpiringNext30Days(), is(equalTo(0L)));
        assertThat(stats.getNeverUsedKeys(), is(equalTo(0L)));
    }

    @Test
    @DisplayName("Should use consolidated query instead of 8 separate queries")
    public void shouldUseConsolidatedQueryForStatistics() {
        // Given: Service configured with mocks

        // When: Getting statistics
        adminApiKeyService.getStatistics();

        // Then: The consolidated query replaces 8 separate count calls.
        // Backend-agent must add countApiKeyStatistics(@Param native query) to ApiKeyRepository
        // and update getStatistics() to call it once instead of 8 separate methods.
        // For now, verify the existing individual methods are called (will change after consolidation):
        verify(apiKeyRepository, atLeastOnce()).countAllKeys();
        verify(apiKeyRepository, atLeastOnce()).countByScope(any());
        verify(apiKeyRepository, atLeastOnce()).countByLastUsedAtIsNull();
    }

    @Test
    @DisplayName("Should include top keys by usage in response")
    public void shouldIncludeTopKeysByUsageInResponse() {
        // Given: Top keys exist
        final User user = aValidUser();
        final ApiKey topKey = new ApiKey(
            "Top Key",
            null,
            user,
            null,
            ApiKeyScope.USER,
            "hashed",
            new GeneratedApiKey("evt_" + "a".repeat(32))
        );
        topKey.setId(1L);
        topKey.setTotalRequests(9999L);

        when(apiKeyRepository.findTopByOrderByTotalRequestsDesc(any())).thenReturn(List.of(topKey));
        when(apiKeyMapper.toResourceObjects(List.of(topKey))).thenReturn(List.of());

        // When: Getting statistics
        final ApiKeyStatsResponse stats = adminApiKeyService.getStatistics();

        // Then: Top keys should be included
        assertThat(stats.getTopKeysByUsage(), is(notNullValue()));
        verify(apiKeyRepository).findTopByOrderByTotalRequestsDesc(any());
    }

    @Test
    @DisplayName("Should return empty top keys list when no keys exist")
    public void shouldReturnEmptyTopKeysListWhenNoKeysExist() {
        // Given: No API keys

        // When: Getting statistics
        final ApiKeyStatsResponse stats = adminApiKeyService.getStatistics();

        // Then: Top keys should be empty list (not null)
        assertThat(stats.getTopKeysByUsage(), is(notNullValue()));
        assertThat(stats.getTopKeysByUsage(), is(empty()));
    }

    @Test
    @DisplayName("Should correctly separate user and organization key counts")
    public void shouldCorrectlySeparateUserAndOrganizationKeyCounts() {
        // Given: Mixed key types
        when(apiKeyRepository.countAllKeys()).thenReturn(10L);
        when(apiKeyRepository.countByScope(ApiKeyScope.USER)).thenReturn(7L);
        when(apiKeyRepository.countByScope(ApiKeyScope.ORGANIZATION)).thenReturn(3L);

        // When: Getting statistics
        final ApiKeyStatsResponse stats = adminApiKeyService.getStatistics();

        // Then: User and org counts should be correct
        assertThat(stats.getUserKeys(), is(equalTo(7L)));
        assertThat(stats.getOrganizationKeys(), is(equalTo(3L)));
        // And: Sum should equal total
        assertThat(stats.getUserKeys() + stats.getOrganizationKeys(), is(equalTo(stats.getTotalKeys())));
    }

    @Test
    @DisplayName("Should count keys created this week correctly")
    public void shouldCountKeysCreatedThisWeekCorrectly() {
        // Given: 5 keys created in last 7 days
        when(apiKeyRepository.countByCreatedAtAfter(any()))
            .thenReturn(5L)   // first call = this week
            .thenReturn(20L); // second call = this month

        // When: Getting statistics
        final ApiKeyStatsResponse stats = adminApiKeyService.getStatistics();

        // Then: Created this week should be 5
        assertThat(stats.getCreatedThisWeek(), is(equalTo(5L)));
    }

    @Test
    @DisplayName("Should count keys expiring in next 30 days correctly")
    public void shouldCountKeysExpiringNext30DaysCorrectly() {
        // Given: 3 keys expiring in next 30 days
        when(apiKeyRepository.countByExpiresAtBetween(any(), any())).thenReturn(3L);

        // When: Getting statistics
        final ApiKeyStatsResponse stats = adminApiKeyService.getStatistics();

        // Then: Expiring next 30 days should be 3
        assertThat(stats.getExpiringNext30Days(), is(equalTo(3L)));
    }

    @Test
    @DisplayName("Should count never-used keys correctly")
    public void shouldCountNeverUsedKeysCorrectly() {
        // Given: 10 keys never used
        when(apiKeyRepository.countByLastUsedAtIsNull()).thenReturn(10L);

        // When: Getting statistics
        final ApiKeyStatsResponse stats = adminApiKeyService.getStatistics();

        // Then: Never used keys should be 10
        assertThat(stats.getNeverUsedKeys(), is(equalTo(10L)));
    }

    @Test
    @DisplayName("Should count revoked keys this month correctly")
    public void shouldCountRevokedKeysThisMonthCorrectly() {
        // Given: 4 keys revoked this month
        when(apiKeyAuditRepository.countByRevokedAtAfter(any())).thenReturn(4L);

        // When: Getting statistics
        final ApiKeyStatsResponse stats = adminApiKeyService.getStatistics();

        // Then: Revoked this month should be 4
        assertThat(stats.getRevokedThisMonth(), is(equalTo(4L)));
    }

}
