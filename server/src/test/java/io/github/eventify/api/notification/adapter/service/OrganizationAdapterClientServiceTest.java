package io.github.eventify.api.notification.adapter.service;

import io.github.eventify.api.notification.adapter.model.AdapterConfig;
import io.github.eventify.api.notification.adapter.model.AdapterType;
import io.github.eventify.api.notification.adapter.model.request.CreateAdapterConfigRequest;
import io.github.eventify.api.notification.adapter.repository.AdapterConfigRepository;
import io.github.eventify.api.user.model.User;
import io.github.eventify.common.security.SecurityUtil;
import io.github.eventify.support.UnitTest;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Unit Test - Organization Adapter Config Service")
public class OrganizationAdapterClientServiceTest extends UnitTest {

    @Mock
    private AdapterConfigRepository adapterConfigRepository;

    private OrganizationAdapterConfigService organizationAdapterConfigService;

    private MockedStatic<SecurityUtil> securityUtilMock;
    private User currentUser;

    @BeforeEach
    public void setUp() {
        currentUser = aValidUser();
        currentUser.setId(1L);
        securityUtilMock = mockStatic(SecurityUtil.class);
        securityUtilMock.when(SecurityUtil::getLoggedInUser).thenReturn(currentUser);
        organizationAdapterConfigService = new OrganizationAdapterConfigService(adapterConfigRepository);
    }

    @AfterEach
    public void tearDown() {
        if (securityUtilMock != null) {
            securityUtilMock.close();
        }
    }

    // ========================= create =========================

    @Test
    @DisplayName("Should create organization adapter config successfully")
    public void shouldCreateOrgAdapterConfigSuccessfully() {
        // Given: A valid request
        final Long orgId = 100L;
        final CreateAdapterConfigRequest request = new CreateAdapterConfigRequest()
            .setAdapterType(AdapterType.SLACK)
            .setLabel("Team Slack")
            .setConfig(Map.of("webhookUrl", "https://example.com/webhook/abc"))
            .setEnabled(true);

        final AdapterConfig saved = anAdapterConfig(1L, currentUser, orgId, AdapterType.SLACK, "Team Slack");
        when(adapterConfigRepository.save(any(AdapterConfig.class))).thenReturn(saved);

        // When: Creating the org config
        final AdapterConfig result = organizationAdapterConfigService.createForOrganization(orgId, request);

        // Then: Config is saved
        assertThat(result, is(notNullValue()));
        verify(adapterConfigRepository, times(1)).save(any(AdapterConfig.class));
    }

    // ========================= list =========================

    @Test
    @DisplayName("Should list organization adapter configs")
    public void shouldListOrgAdapterConfigs() {
        // Given: Org has two configs
        final Long orgId = 100L;
        final AdapterConfig config1 = anAdapterConfig(1L, currentUser, orgId, AdapterType.SLACK, "Team Slack");
        final AdapterConfig config2 = anAdapterConfig(2L, currentUser, orgId, AdapterType.MATTERMOST, "Ops Mattermost");
        when(adapterConfigRepository.findByOrganizationId(orgId)).thenReturn(List.of(config1, config2));

        // When: Listing org configs
        final List<AdapterConfig> result = organizationAdapterConfigService.listForOrganization(orgId);

        // Then: Both configs are returned
        assertThat(result, hasSize(2));
    }

    // ========================= FACTORY METHODS =========================

    private static AdapterConfig anAdapterConfig(
        final Long id,
        final User user,
        final Long organizationId,
        final AdapterType adapterType,
        final String label
    ) {
        final AdapterConfig config = new AdapterConfig();
        config.setId(id);
        config.setUser(user);
        config.setOrganizationId(organizationId);
        config.setAdapterType(adapterType);
        config.setLabel(label);
        config.setConfig(Map.of());
        config.setEnabled(true);
        return config;
    }
}
