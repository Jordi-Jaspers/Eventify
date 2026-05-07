package io.github.eventify.api.user.service;

import io.github.eventify.api.user.model.AuthProvider;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.user.model.UserAuthProvider;
import io.github.eventify.api.user.repository.UserAuthProviderRepository;
import io.github.eventify.api.user.repository.UserRepository;
import io.github.eventify.common.email.service.sender.EmailService;
import io.github.eventify.support.UnitTest;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Unit Test - UserService - Registration with Auth Provider")
public class UserServiceRegisterAuthProviderTest extends UnitTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserAuthProviderRepository userAuthProviderRepository;

    @Mock
    private EmailService emailService;

    private UserService userService;

    @BeforeEach
    public void setUp() {
        userService = new UserService(passwordEncoder, userRepository, userAuthProviderRepository, null, emailService);
        when(passwordEncoder.encode(any(String.class))).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            final User u = i.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(userRepository.findByEmail(any(String.class))).thenReturn(Optional.empty());
    }

    @Test
    @DisplayName("Should create LOCAL UserAuthProvider record after registration")
    public void register_createsLocalAuthProviderRecord() {
        // Given: A new user to register
        final User newUser = aValidUser();
        newUser.setId(null);

        // When: Registering the user
        userService.registerAndNotify(newUser, VALID_PASSWORD);

        // Then: A UserAuthProvider with LOCAL provider should be persisted
        final ArgumentCaptor<UserAuthProvider> captor = ArgumentCaptor.forClass(UserAuthProvider.class);
        verify(userAuthProviderRepository, times(1)).save(captor.capture());

        final UserAuthProvider savedProvider = captor.getValue();

        // And: The provider should be LOCAL
        assertThat(savedProvider.getProvider(), is(equalTo(AuthProvider.LOCAL)));

        // And: The providerEmail should equal the user's email
        assertThat(savedProvider.getProviderEmail(), is(equalTo(newUser.getEmail())));
    }

    @Test
    @DisplayName("Should save user after registration")
    public void register_setsHasPasswordTrue() {
        // Given: A new user to register
        final User newUser = aValidUser();
        newUser.setId(null);

        // When: Registering the user
        userService.registerAndNotify(newUser, VALID_PASSWORD);

        // Then: The user should be saved
        verify(userRepository, times(1)).save(any(User.class));
    }
}
