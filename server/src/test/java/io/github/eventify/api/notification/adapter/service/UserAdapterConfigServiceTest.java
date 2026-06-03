package io.github.eventify.api.notification.adapter.service;

import io.github.eventify.api.notification.adapter.model.AdapterConfig;
import io.github.eventify.api.notification.adapter.model.AdapterType;
import io.github.eventify.api.notification.adapter.model.request.CreateAdapterConfigRequest;
import io.github.eventify.api.notification.adapter.model.request.UpdateAdapterConfigRequest;
import io.github.eventify.api.notification.adapter.repository.AdapterConfigRepository;
import io.github.eventify.api.user.model.User;
import io.github.eventify.common.security.SecurityUtil;
import io.github.eventify.support.UnitTest;
import io.github.jframe.exception.core.DataNotFoundException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@DisplayName("Unit Test - User Adapter Config Service")
public class UserAdapterConfigServiceTest extends UnitTest {

    @Mock
    private AdapterConfigRepository adapterConfigRepository;

    private UserAdapterConfigService userAdapterConfigService;

    private MockedStatic<SecurityUtil> securityUtilMock;
    private User currentUser;

    @BeforeEach
    public void setUp() {
        currentUser = aValidUser();
        currentUser.setId(1L);
        securityUtilMock = mockStatic(SecurityUtil.class);
        securityUtilMock.when(SecurityUtil::getLoggedInUser).thenReturn(currentUser);
        userAdapterConfigService = new UserAdapterConfigService(adapterConfigRepository);
    }

    @AfterEach
    public void tearDown() {
        if (securityUtilMock != null) {
            securityUtilMock.close();
        }
    }

    // ========================= create =========================

    @Test
    @DisplayName("Should create personal adapter config successfully")
    public void shouldCreatePersonalAdapterConfigSuccessfully() {
        // Given: A valid create request for a personal config
        final CreateAdapterConfigRequest request = new CreateAdapterConfigRequest()
            .setAdapterType(AdapterType.IN_APP)
            .setLabel("My In-App")
            .setConfig(Map.of())
            .setEnabled(true);

        final AdapterConfig saved = anAdapterConfig(1L, currentUser, null, AdapterType.IN_APP, "My In-App");
        when(adapterConfigRepository.save(any(AdapterConfig.class))).thenReturn(saved);

        // When: Creating the config
        final AdapterConfig result = userAdapterConfigService.createPersonal(request);

        // Then: Config is saved and returned
        assertThat(result, is(notNullValue()));
        verify(adapterConfigRepository, times(1)).save(any(AdapterConfig.class));
    }

    // ========================= list =========================

    @Test
    @DisplayName("Should list personal adapter configs for current user")
    public void shouldListPersonalAdapterConfigs() {
        // Given: Two personal configs for the user
        final AdapterConfig config1 = anAdapterConfig(1L, currentUser, null, AdapterType.IN_APP, "In-App 1");
        final AdapterConfig config2 = anAdapterConfig(2L, currentUser, null, AdapterType.SLACK, "Slack 1");
        when(adapterConfigRepository.findByUserIdAndOrganizationIdIsNull(currentUser.getId()))
            .thenReturn(List.of(config1, config2));

        // When: Listing personal configs
        final List<AdapterConfig> result = userAdapterConfigService.listPersonal();

        // Then: Both configs are returned
        assertThat(result, hasSize(2));
    }

    // ========================= get =========================

    @Test
    @DisplayName("Should return config when it exists")
    public void shouldGetConfigWhenExists() {
        // Given: Config in repository
        final Long id = 1L;
        final AdapterConfig config = anAdapterConfig(id, currentUser, null, AdapterType.IN_APP, "My Config");
        when(adapterConfigRepository.findById(id)).thenReturn(Optional.of(config));

        // When: Getting the config
        final AdapterConfig result = userAdapterConfigService.get(id);

        // Then: Config is returned
        assertThat(result, is(notNullValue()));
        assertThat(result.getId(), is(id));
    }

    @Test
    @DisplayName("Should throw not found when config does not exist")
    public void shouldThrowNotFoundWhenConfigDoesNotExist() {
        // Given: No config for the given ID
        final Long id = 999L;
        when(adapterConfigRepository.findById(id)).thenReturn(Optional.empty());

        // When / Then: DataNotFoundException thrown
        assertThrows(DataNotFoundException.class, () -> userAdapterConfigService.get(id));
    }

    // ========================= update =========================

    @Test
    @DisplayName("Should update personal config successfully")
    public void shouldUpdatePersonalConfigSuccessfully() {
        // Given: Existing personal config
        final Long id = 1L;
        final AdapterConfig config = anAdapterConfig(id, currentUser, null, AdapterType.IN_APP, "Old Label");
        when(adapterConfigRepository.findById(id)).thenReturn(Optional.of(config));
        when(adapterConfigRepository.save(any(AdapterConfig.class))).thenReturn(config);

        final UpdateAdapterConfigRequest request = new UpdateAdapterConfigRequest()
            .setLabel("New Label")
            .setConfig(Map.of())
            .setEnabled(false);

        // When: Updating the config
        final AdapterConfig result = userAdapterConfigService.update(id, request);

        // Then: Config is saved with updated values
        assertThat(result, is(notNullValue()));
        verify(adapterConfigRepository, times(1)).save(config);
    }

    @Test
    @DisplayName("Should throw not found when updating non-existent config")
    public void shouldThrowNotFoundWhenUpdatingNonExistentConfig() {
        // Given: No config for the given ID
        final Long id = 999L;
        when(adapterConfigRepository.findById(id)).thenReturn(Optional.empty());

        final UpdateAdapterConfigRequest request = new UpdateAdapterConfigRequest()
            .setLabel("Label").setConfig(Map.of()).setEnabled(true);

        // When / Then: DataNotFoundException thrown
        assertThrows(DataNotFoundException.class, () -> userAdapterConfigService.update(id, request));
        verify(adapterConfigRepository, never()).save(any());
    }

    // ========================= delete =========================

    @Test
    @DisplayName("Should delete personal config successfully")
    public void shouldDeletePersonalConfigSuccessfully() {
        // Given: Existing personal config
        final Long id = 1L;
        final AdapterConfig config = anAdapterConfig(id, currentUser, null, AdapterType.IN_APP, "My Config");
        when(adapterConfigRepository.findById(id)).thenReturn(Optional.of(config));

        // When: Deleting the config
        userAdapterConfigService.delete(id);

        // Then: Config is deleted
        verify(adapterConfigRepository, times(1)).delete(config);
    }

    @Test
    @DisplayName("Should throw not found when deleting non-existent config")
    public void shouldThrowNotFoundWhenDeletingNonExistentConfig() {
        // Given: No config for the given ID
        final Long id = 999L;
        when(adapterConfigRepository.findById(id)).thenReturn(Optional.empty());

        // When / Then: DataNotFoundException thrown
        assertThrows(DataNotFoundException.class, () -> userAdapterConfigService.delete(id));
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
