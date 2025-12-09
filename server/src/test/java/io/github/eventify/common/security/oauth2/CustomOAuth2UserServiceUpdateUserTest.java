package io.github.eventify.common.security.oauth2;

import io.github.eventify.api.user.model.User;
import io.github.eventify.api.user.repository.UserRepository;
import io.github.eventify.support.UnitTest;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;

import static io.github.eventify.common.security.oauth2.provider.OAuth2UserInfoFactory.GOOGLE_REGISTRATION_ID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link CustomOAuth2UserService} user update logic.
 */
@DisplayName("Unit Test - CustomOAuth2UserService - Update Existing User")
public class CustomOAuth2UserServiceUpdateUserTest extends UnitTest {

    private static final String EXISTING_FIRST_NAME = "Jane";
    private static final String EXISTING_LAST_NAME = "Smith";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private OAuth2UserRequest oAuth2UserRequest;

    @Mock
    private ClientRegistration clientRegistration;

    private CustomOAuth2UserService customOAuth2UserService;

    @BeforeEach
    public void setUp() {
        customOAuth2UserService = new CustomOAuth2UserService(userRepository, passwordEncoder);
    }

    @Test
    @DisplayName("Should update user first name when first name is blank")
    public void shouldUpdateUserFirstNameWhenFirstNameIsBlank() {
        // Given: An existing user with blank first name
        final User existingUser = aValidUser();
        existingUser.setFirstName("");
        existingUser.setLastName(EXISTING_LAST_NAME);

        // And: A valid OAuth2 user
        final OAuth2User oAuth2User = createMockOAuth2User(VALID_EMAIL, true);
        when(oAuth2UserRequest.getClientRegistration()).thenReturn(clientRegistration);
        when(clientRegistration.getRegistrationId()).thenReturn(GOOGLE_REGISTRATION_ID);
        when(userRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(existingUser));

        // When: Processing the OAuth2 user
        customOAuth2UserService.processOAuth2User(oAuth2UserRequest, oAuth2User);

        // Then: The user should be saved with updated first name
        final ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());

        final User updatedUser = userCaptor.getValue();

        // And: The first name should be updated from OAuth2
        assertThat(updatedUser.getFirstName(), is(equalTo(OAUTH2_FIRST_NAME)));

        // And: The last name should remain unchanged
        assertThat(updatedUser.getLastName(), is(equalTo(EXISTING_LAST_NAME)));
    }

    @Test
    @DisplayName("Should update user last name when last name is blank")
    public void shouldUpdateUserLastNameWhenLastNameIsBlank() {
        // Given: An existing user with blank last name
        final User existingUser = aValidUser();
        existingUser.setFirstName(EXISTING_FIRST_NAME);
        existingUser.setLastName("");

        // And: A valid OAuth2 user
        final OAuth2User oAuth2User = createMockOAuth2User(VALID_EMAIL, true);
        when(oAuth2UserRequest.getClientRegistration()).thenReturn(clientRegistration);
        when(clientRegistration.getRegistrationId()).thenReturn(GOOGLE_REGISTRATION_ID);
        when(userRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(existingUser));

        // When: Processing the OAuth2 user
        customOAuth2UserService.processOAuth2User(oAuth2UserRequest, oAuth2User);

        // Then: The user should be saved with updated last name
        final ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());

        final User updatedUser = userCaptor.getValue();

        // And: The first name should remain unchanged
        assertThat(updatedUser.getFirstName(), is(equalTo(EXISTING_FIRST_NAME)));

        // And: The last name should be updated from OAuth2
        assertThat(updatedUser.getLastName(), is(equalTo(OAUTH2_LAST_NAME)));
    }

    @Test
    @DisplayName("Should update both first and last names when both are blank")
    public void shouldUpdateBothFirstAndLastNamesWhenBothAreBlank() {
        // Given: An existing user with both blank names
        final User existingUser = aValidUser();
        existingUser.setFirstName("");
        existingUser.setLastName("");

        // And: A valid OAuth2 user
        final OAuth2User oAuth2User = createMockOAuth2User(VALID_EMAIL, true);
        when(oAuth2UserRequest.getClientRegistration()).thenReturn(clientRegistration);
        when(clientRegistration.getRegistrationId()).thenReturn(GOOGLE_REGISTRATION_ID);
        when(userRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(existingUser));

        // When: Processing the OAuth2 user
        customOAuth2UserService.processOAuth2User(oAuth2UserRequest, oAuth2User);

        // Then: The user should be saved with both names updated
        final ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());

        final User updatedUser = userCaptor.getValue();

        // And: Both names should be updated from OAuth2
        assertThat(updatedUser.getFirstName(), is(equalTo(OAUTH2_FIRST_NAME)));
        assertThat(updatedUser.getLastName(), is(equalTo(OAUTH2_LAST_NAME)));
    }

    @Test
    @DisplayName("Should not overwrite existing first name when it is not blank")
    public void shouldNotOverwriteExistingFirstNameWhenItIsNotBlank() {
        // Given: An existing user with a non-blank first name
        final User existingUser = aValidUser();
        existingUser.setFirstName(EXISTING_FIRST_NAME);
        existingUser.setLastName("");

        // And: A valid OAuth2 user with different first name
        final OAuth2User oAuth2User = createMockOAuth2User(VALID_EMAIL, true);
        when(oAuth2UserRequest.getClientRegistration()).thenReturn(clientRegistration);
        when(clientRegistration.getRegistrationId()).thenReturn(GOOGLE_REGISTRATION_ID);
        when(userRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(existingUser));

        // When: Processing the OAuth2 user
        customOAuth2UserService.processOAuth2User(oAuth2UserRequest, oAuth2User);

        // Then: The user should be saved
        final ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());

        final User updatedUser = userCaptor.getValue();

        // And: The first name should remain unchanged
        assertThat(updatedUser.getFirstName(), is(equalTo(EXISTING_FIRST_NAME)));

        // And: The last name should be updated (because it was blank)
        assertThat(updatedUser.getLastName(), is(equalTo(OAUTH2_LAST_NAME)));
    }

    @Test
    @DisplayName("Should not overwrite existing last name when it is not blank")
    public void shouldNotOverwriteExistingLastNameWhenItIsNotBlank() {
        // Given: An existing user with a non-blank last name
        final User existingUser = aValidUser();
        existingUser.setFirstName("");
        existingUser.setLastName(EXISTING_LAST_NAME);

        // And: A valid OAuth2 user with different last name
        final OAuth2User oAuth2User = createMockOAuth2User(VALID_EMAIL, true);
        when(oAuth2UserRequest.getClientRegistration()).thenReturn(clientRegistration);
        when(clientRegistration.getRegistrationId()).thenReturn(GOOGLE_REGISTRATION_ID);
        when(userRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(existingUser));

        // When: Processing the OAuth2 user
        customOAuth2UserService.processOAuth2User(oAuth2UserRequest, oAuth2User);

        // Then: The user should be saved
        final ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());

        final User updatedUser = userCaptor.getValue();

        // And: The first name should be updated (because it was blank)
        assertThat(updatedUser.getFirstName(), is(equalTo(OAUTH2_FIRST_NAME)));

        // And: The last name should remain unchanged
        assertThat(updatedUser.getLastName(), is(equalTo(EXISTING_LAST_NAME)));
    }

    @Test
    @DisplayName("Should not overwrite either name when both exist")
    public void shouldNotOverwriteEitherNameWhenBothExist() {
        // Given: An existing user with both names populated
        final User existingUser = aValidUser();
        existingUser.setFirstName(EXISTING_FIRST_NAME);
        existingUser.setLastName(EXISTING_LAST_NAME);

        // And: A valid OAuth2 user with different names
        final OAuth2User oAuth2User = createMockOAuth2User(VALID_EMAIL, true);
        when(oAuth2UserRequest.getClientRegistration()).thenReturn(clientRegistration);
        when(clientRegistration.getRegistrationId()).thenReturn(GOOGLE_REGISTRATION_ID);
        when(userRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(existingUser));

        // When: Processing the OAuth2 user
        customOAuth2UserService.processOAuth2User(oAuth2UserRequest, oAuth2User);

        // Then: The user should still be saved (even though no changes were made)
        final ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());

        final User updatedUser = userCaptor.getValue();

        // And: Both names should remain unchanged
        assertThat(updatedUser.getFirstName(), is(equalTo(EXISTING_FIRST_NAME)));
        assertThat(updatedUser.getLastName(), is(equalTo(EXISTING_LAST_NAME)));
    }

    @Test
    @DisplayName("Should update user when first name is null")
    public void shouldUpdateUserWhenFirstNameIsNull() {
        // Given: An existing user with null first name
        final User existingUser = aValidUser();
        existingUser.setFirstName(null);
        existingUser.setLastName(EXISTING_LAST_NAME);

        // And: A valid OAuth2 user
        final OAuth2User oAuth2User = createMockOAuth2User(VALID_EMAIL, true);
        when(oAuth2UserRequest.getClientRegistration()).thenReturn(clientRegistration);
        when(clientRegistration.getRegistrationId()).thenReturn(GOOGLE_REGISTRATION_ID);
        when(userRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(existingUser));

        // When: Processing the OAuth2 user
        customOAuth2UserService.processOAuth2User(oAuth2UserRequest, oAuth2User);

        // Then: The user should be saved with updated first name
        final ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());

        final User updatedUser = userCaptor.getValue();

        // And: The first name should be updated from OAuth2
        assertThat(updatedUser.getFirstName(), is(equalTo(OAUTH2_FIRST_NAME)));

        // And: The last name should remain unchanged
        assertThat(updatedUser.getLastName(), is(equalTo(EXISTING_LAST_NAME)));
    }

    @Test
    @DisplayName("Should update user when last name is null")
    public void shouldUpdateUserWhenLastNameIsNull() {
        // Given: An existing user with null last name
        final User existingUser = aValidUser();
        existingUser.setFirstName(EXISTING_FIRST_NAME);
        existingUser.setLastName(null);

        // And: A valid OAuth2 user
        final OAuth2User oAuth2User = createMockOAuth2User(VALID_EMAIL, true);
        when(oAuth2UserRequest.getClientRegistration()).thenReturn(clientRegistration);
        when(clientRegistration.getRegistrationId()).thenReturn(GOOGLE_REGISTRATION_ID);
        when(userRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(existingUser));

        // When: Processing the OAuth2 user
        customOAuth2UserService.processOAuth2User(oAuth2UserRequest, oAuth2User);

        // Then: The user should be saved with updated last name
        final ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());

        final User updatedUser = userCaptor.getValue();

        // And: The first name should remain unchanged
        assertThat(updatedUser.getFirstName(), is(equalTo(EXISTING_FIRST_NAME)));

        // And: The last name should be updated from OAuth2
        assertThat(updatedUser.getLastName(), is(equalTo(OAUTH2_LAST_NAME)));
    }

    @Test
    @DisplayName("Should not call password encoder when updating existing user")
    public void shouldNotCallPasswordEncoderWhenUpdatingExistingUser() {
        // Given: An existing user
        final User existingUser = aValidUser();
        existingUser.setFirstName("");
        existingUser.setLastName("");

        // And: A valid OAuth2 user
        final OAuth2User oAuth2User = createMockOAuth2User(VALID_EMAIL, true);
        when(oAuth2UserRequest.getClientRegistration()).thenReturn(clientRegistration);
        when(clientRegistration.getRegistrationId()).thenReturn(GOOGLE_REGISTRATION_ID);
        when(userRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(existingUser));

        // When: Processing the OAuth2 user
        customOAuth2UserService.processOAuth2User(oAuth2UserRequest, oAuth2User);

        // Then: The password encoder should not be called
        verify(passwordEncoder, never()).encode(any(String.class));

        // And: The user should still be saved
        verify(userRepository, times(1)).save(any(User.class));
    }
}
