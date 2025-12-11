package io.github.eventify.api.bootstrap;

import io.github.eventify.api.authentication.model.Role;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.user.repository.UserRepository;
import io.github.eventify.common.config.properties.BootstrapProperties;
import io.github.eventify.common.config.properties.SecurityProperties;
import io.github.eventify.support.UnitTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Unit Test - Global Admin Bootstrap")
public class GlobalAdminBootstrapTest extends UnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SecurityProperties securityProperties;

    @Mock
    private BootstrapProperties bootstrapProperties;

    private GlobalAdminBootstrap globalAdminBootstrap;

    private static final String GLOBAL_ADMIN_EMAIL = "admin@eventify.com";
    private static final String GLOBAL_ADMIN_PASSWORD = "AdminPassword123!@#";
    private static final String GLOBAL_ADMIN_FIRST_NAME = "Global";
    private static final String GLOBAL_ADMIN_LAST_NAME = "Admin";
    private static final String ENCODED_PASSWORD = "encoded-admin-password";

    @BeforeEach
    public void setUp() {
        globalAdminBootstrap = new GlobalAdminBootstrap(userRepository, passwordEncoder, securityProperties);
        // Default behavior: securityProperties returns bootstrapProperties
        when(securityProperties.getBootstrap()).thenReturn(bootstrapProperties);
    }

    @Nested
    @DisplayName("Global Admin Creation Tests")
    public class GlobalAdminCreationTests {

        @Test
        @DisplayName("Should create global admin when no admin exists and all required properties present")
        public void shouldCreateGlobalAdminWhenNoAdminExistsAndPropertiesPresent() {
            // Given: No admin user exists
            when(userRepository.existsByRole(Role.ADMIN)).thenReturn(false);

            // And: All required properties are present
            when(bootstrapProperties.getEmail()).thenReturn(GLOBAL_ADMIN_EMAIL);
            when(bootstrapProperties.getPassword()).thenReturn(GLOBAL_ADMIN_PASSWORD);
            when(bootstrapProperties.getFirstName()).thenReturn(GLOBAL_ADMIN_FIRST_NAME);
            when(bootstrapProperties.getLastName()).thenReturn(GLOBAL_ADMIN_LAST_NAME);

            // And: Password encoder returns encoded password
            when(passwordEncoder.encode(GLOBAL_ADMIN_PASSWORD)).thenReturn(ENCODED_PASSWORD);

            // When: Application startup event is triggered
            final ApplicationStartedEvent event = mock(ApplicationStartedEvent.class);
            globalAdminBootstrap.onApplicationStarted(event);

            // Then: UserRepository.save should be called once
            final ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository, times(1)).save(userCaptor.capture());

            // And: The saved user should have correct properties
            final User savedUser = userCaptor.getValue();
            assertThat(savedUser.getEmail(), is(equalTo(GLOBAL_ADMIN_EMAIL)));
            assertThat(savedUser.getFirstName(), is(equalTo(GLOBAL_ADMIN_FIRST_NAME)));
            assertThat(savedUser.getLastName(), is(equalTo(GLOBAL_ADMIN_LAST_NAME)));
            assertThat(savedUser.getPassword(), is(equalTo(ENCODED_PASSWORD)));
            assertThat(savedUser.getRole(), is(equalTo(Role.ADMIN)));
            assertThat(savedUser.isEnabled(), is(true));
            assertThat(savedUser.isValidated(), is(true));
        }

        @Test
        @DisplayName("Should use default first name when property not overridden")
        public void shouldUseDefaultFirstNameWhenPropertyNotProvided() {
            // Given: No admin user exists
            when(userRepository.existsByRole(Role.ADMIN)).thenReturn(false);

            // And: Required properties are present with default first name
            when(bootstrapProperties.getEmail()).thenReturn(GLOBAL_ADMIN_EMAIL);
            when(bootstrapProperties.getPassword()).thenReturn(GLOBAL_ADMIN_PASSWORD);
            when(bootstrapProperties.getFirstName()).thenReturn("Global"); // default
            when(bootstrapProperties.getLastName()).thenReturn(GLOBAL_ADMIN_LAST_NAME);

            // And: Password encoder returns encoded password
            when(passwordEncoder.encode(GLOBAL_ADMIN_PASSWORD)).thenReturn(ENCODED_PASSWORD);

            // When: Application startup event is triggered
            final ApplicationStartedEvent event = mock(ApplicationStartedEvent.class);
            globalAdminBootstrap.onApplicationStarted(event);

            // Then: UserRepository.save should be called
            final ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository, times(1)).save(userCaptor.capture());

            // And: The saved user should have default first name
            final User savedUser = userCaptor.getValue();
            assertThat(savedUser.getFirstName(), is(equalTo("Global")));
        }

        @Test
        @DisplayName("Should use default last name when property not overridden")
        public void shouldUseDefaultLastNameWhenPropertyNotProvided() {
            // Given: No admin user exists
            when(userRepository.existsByRole(Role.ADMIN)).thenReturn(false);

            // And: Required properties are present with default last name
            when(bootstrapProperties.getEmail()).thenReturn(GLOBAL_ADMIN_EMAIL);
            when(bootstrapProperties.getPassword()).thenReturn(GLOBAL_ADMIN_PASSWORD);
            when(bootstrapProperties.getFirstName()).thenReturn(GLOBAL_ADMIN_FIRST_NAME);
            when(bootstrapProperties.getLastName()).thenReturn("Admin"); // default

            // And: Password encoder returns encoded password
            when(passwordEncoder.encode(GLOBAL_ADMIN_PASSWORD)).thenReturn(ENCODED_PASSWORD);

            // When: Application startup event is triggered
            final ApplicationStartedEvent event = mock(ApplicationStartedEvent.class);
            globalAdminBootstrap.onApplicationStarted(event);

            // Then: UserRepository.save should be called
            final ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository, times(1)).save(userCaptor.capture());

            // And: The saved user should have default last name
            final User savedUser = userCaptor.getValue();
            assertThat(savedUser.getLastName(), is(equalTo("Admin")));
        }

        @Test
        @DisplayName("Should hash password using PasswordEncoder before saving")
        public void shouldHashPasswordBeforeSaving() {
            // Given: No admin user exists
            when(userRepository.existsByRole(Role.ADMIN)).thenReturn(false);

            // And: All required properties are present
            when(bootstrapProperties.getEmail()).thenReturn(GLOBAL_ADMIN_EMAIL);
            when(bootstrapProperties.getPassword()).thenReturn(GLOBAL_ADMIN_PASSWORD);
            when(bootstrapProperties.getFirstName()).thenReturn(GLOBAL_ADMIN_FIRST_NAME);
            when(bootstrapProperties.getLastName()).thenReturn(GLOBAL_ADMIN_LAST_NAME);

            // And: Password encoder is configured
            when(passwordEncoder.encode(GLOBAL_ADMIN_PASSWORD)).thenReturn(ENCODED_PASSWORD);

            // When: Application startup event is triggered
            final ApplicationStartedEvent event = mock(ApplicationStartedEvent.class);
            globalAdminBootstrap.onApplicationStarted(event);

            // Then: PasswordEncoder.encode should be called with plain password
            verify(passwordEncoder, times(1)).encode(GLOBAL_ADMIN_PASSWORD);

            // And: Saved user should have encoded password, not plain text
            final ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository, times(1)).save(userCaptor.capture());
            final User savedUser = userCaptor.getValue();
            assertThat(savedUser.getPassword(), is(equalTo(ENCODED_PASSWORD)));
            assertThat(savedUser.getPassword(), is(not(equalTo(GLOBAL_ADMIN_PASSWORD))));
        }

        @Test
        @DisplayName("Should set user role to ADMIN")
        public void shouldSetUserRoleToAdmin() {
            // Given: No admin user exists
            when(userRepository.existsByRole(Role.ADMIN)).thenReturn(false);

            // And: All required properties are present
            when(bootstrapProperties.getEmail()).thenReturn(GLOBAL_ADMIN_EMAIL);
            when(bootstrapProperties.getPassword()).thenReturn(GLOBAL_ADMIN_PASSWORD);
            when(bootstrapProperties.getFirstName()).thenReturn(GLOBAL_ADMIN_FIRST_NAME);
            when(bootstrapProperties.getLastName()).thenReturn(GLOBAL_ADMIN_LAST_NAME);
            when(passwordEncoder.encode(GLOBAL_ADMIN_PASSWORD)).thenReturn(ENCODED_PASSWORD);

            // When: Application startup event is triggered
            final ApplicationStartedEvent event = mock(ApplicationStartedEvent.class);
            globalAdminBootstrap.onApplicationStarted(event);

            // Then: Saved user should have ADMIN role
            final ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository, times(1)).save(userCaptor.capture());
            final User savedUser = userCaptor.getValue();
            assertThat(savedUser.getRole(), is(equalTo(Role.ADMIN)));
        }

        @Test
        @DisplayName("Should set user enabled to true")
        public void shouldSetUserEnabledToTrue() {
            // Given: No admin user exists
            when(userRepository.existsByRole(Role.ADMIN)).thenReturn(false);

            // And: All required properties are present
            when(bootstrapProperties.getEmail()).thenReturn(GLOBAL_ADMIN_EMAIL);
            when(bootstrapProperties.getPassword()).thenReturn(GLOBAL_ADMIN_PASSWORD);
            when(bootstrapProperties.getFirstName()).thenReturn(GLOBAL_ADMIN_FIRST_NAME);
            when(bootstrapProperties.getLastName()).thenReturn(GLOBAL_ADMIN_LAST_NAME);
            when(passwordEncoder.encode(GLOBAL_ADMIN_PASSWORD)).thenReturn(ENCODED_PASSWORD);

            // When: Application startup event is triggered
            final ApplicationStartedEvent event = mock(ApplicationStartedEvent.class);
            globalAdminBootstrap.onApplicationStarted(event);

            // Then: Saved user should be enabled
            final ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository, times(1)).save(userCaptor.capture());
            final User savedUser = userCaptor.getValue();
            assertThat(savedUser.isEnabled(), is(true));
        }

        @Test
        @DisplayName("Should set user validated to true")
        public void shouldSetUserValidatedToTrue() {
            // Given: No admin user exists
            when(userRepository.existsByRole(Role.ADMIN)).thenReturn(false);

            // And: All required properties are present
            when(bootstrapProperties.getEmail()).thenReturn(GLOBAL_ADMIN_EMAIL);
            when(bootstrapProperties.getPassword()).thenReturn(GLOBAL_ADMIN_PASSWORD);
            when(bootstrapProperties.getFirstName()).thenReturn(GLOBAL_ADMIN_FIRST_NAME);
            when(bootstrapProperties.getLastName()).thenReturn(GLOBAL_ADMIN_LAST_NAME);
            when(passwordEncoder.encode(GLOBAL_ADMIN_PASSWORD)).thenReturn(ENCODED_PASSWORD);

            // When: Application startup event is triggered
            final ApplicationStartedEvent event = mock(ApplicationStartedEvent.class);
            globalAdminBootstrap.onApplicationStarted(event);

            // Then: Saved user should be validated
            final ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository, times(1)).save(userCaptor.capture());
            final User savedUser = userCaptor.getValue();
            assertThat(savedUser.isValidated(), is(true));
        }
    }


    @Nested
    @DisplayName("Idempotency Tests")
    public class IdempotencyTests {

        @Test
        @DisplayName("Should not create admin when admin already exists")
        public void shouldNotCreateAdminWhenAdminAlreadyExists() {
            // Given: An admin user already exists
            when(userRepository.existsByRole(Role.ADMIN)).thenReturn(true);

            // And: All required properties are present
            when(bootstrapProperties.getEmail()).thenReturn(GLOBAL_ADMIN_EMAIL);
            when(bootstrapProperties.getPassword()).thenReturn(GLOBAL_ADMIN_PASSWORD);

            // When: Application startup event is triggered
            final ApplicationStartedEvent event = mock(ApplicationStartedEvent.class);
            globalAdminBootstrap.onApplicationStarted(event);

            // Then: UserRepository.save should NOT be called
            verify(userRepository, times(0)).save(any());
            verify(passwordEncoder, times(0)).encode(any());
        }

        @Test
        @DisplayName("Should not create admin when email property missing")
        public void shouldNotCreateAdminWhenEmailPropertyMissing() {
            // Given: Required property is missing (email)
            when(bootstrapProperties.getEmail()).thenReturn(null);
            when(bootstrapProperties.getPassword()).thenReturn(GLOBAL_ADMIN_PASSWORD);

            // When: Application startup event is triggered
            final ApplicationStartedEvent event = mock(ApplicationStartedEvent.class);
            globalAdminBootstrap.onApplicationStarted(event);

            // Then: UserRepository.save should NOT be called
            verify(userRepository, times(0)).save(any());
        }

        @Test
        @DisplayName("Should not create admin when password property missing")
        public void shouldNotCreateAdminWhenPasswordPropertyMissing() {
            // Given: Required property is missing (password)
            when(bootstrapProperties.getEmail()).thenReturn(GLOBAL_ADMIN_EMAIL);
            when(bootstrapProperties.getPassword()).thenReturn(null);

            // When: Application startup event is triggered
            final ApplicationStartedEvent event = mock(ApplicationStartedEvent.class);
            globalAdminBootstrap.onApplicationStarted(event);

            // Then: UserRepository.save should NOT be called
            verify(userRepository, times(0)).save(any());
        }

        @Test
        @DisplayName("Should not create admin when both email and password missing")
        public void shouldNotCreateAdminWhenBothPropertiesMissing() {
            // Given: Both required properties are missing
            when(bootstrapProperties.getEmail()).thenReturn(null);
            when(bootstrapProperties.getPassword()).thenReturn(null);

            // When: Application startup event is triggered
            final ApplicationStartedEvent event = mock(ApplicationStartedEvent.class);
            globalAdminBootstrap.onApplicationStarted(event);

            // Then: UserRepository.save should NOT be called
            verify(userRepository, times(0)).save(any());
        }
    }


    @Nested
    @DisplayName("Email Property Handling Tests")
    public class EmailPropertyHandlingTests {

        @Test
        @DisplayName("Should use email from bootstrap properties")
        public void shouldUseEmailFromProperties() {
            // Given: No admin user exists
            when(userRepository.existsByRole(Role.ADMIN)).thenReturn(false);

            // And: Email property is set to specific value
            final String customEmail = "custom-admin@eventify.com";
            when(bootstrapProperties.getEmail()).thenReturn(customEmail);
            when(bootstrapProperties.getPassword()).thenReturn(GLOBAL_ADMIN_PASSWORD);
            when(bootstrapProperties.getFirstName()).thenReturn(GLOBAL_ADMIN_FIRST_NAME);
            when(bootstrapProperties.getLastName()).thenReturn(GLOBAL_ADMIN_LAST_NAME);
            when(passwordEncoder.encode(GLOBAL_ADMIN_PASSWORD)).thenReturn(ENCODED_PASSWORD);

            // When: Application startup event is triggered
            final ApplicationStartedEvent event = mock(ApplicationStartedEvent.class);
            globalAdminBootstrap.onApplicationStarted(event);

            // Then: Saved user should have custom email
            final ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository, times(1)).save(userCaptor.capture());
            final User savedUser = userCaptor.getValue();
            assertThat(savedUser.getEmail(), is(equalTo(customEmail)));
        }

        @Test
        @DisplayName("Should handle empty email string gracefully")
        public void shouldNotCreateAdminWhenEmailIsEmpty() {
            // Given: Email property is empty string
            when(bootstrapProperties.getEmail()).thenReturn("");
            when(bootstrapProperties.getPassword()).thenReturn(GLOBAL_ADMIN_PASSWORD);

            // When: Application startup event is triggered
            final ApplicationStartedEvent event = mock(ApplicationStartedEvent.class);
            globalAdminBootstrap.onApplicationStarted(event);

            // Then: UserRepository.save should NOT be called
            verify(userRepository, times(0)).save(any());
        }
    }


    @Nested
    @DisplayName("Password Property Handling Tests")
    public class PasswordPropertyHandlingTests {

        @Test
        @DisplayName("Should use password from bootstrap properties")
        public void shouldUsePasswordFromProperties() {
            // Given: No admin user exists
            when(userRepository.existsByRole(Role.ADMIN)).thenReturn(false);

            // And: Password property is set to specific value
            final String customPassword = "CustomPassword123!@#";
            final String encodedCustomPassword = "encoded-custom-password";
            when(bootstrapProperties.getEmail()).thenReturn(GLOBAL_ADMIN_EMAIL);
            when(bootstrapProperties.getPassword()).thenReturn(customPassword);
            when(bootstrapProperties.getFirstName()).thenReturn(GLOBAL_ADMIN_FIRST_NAME);
            when(bootstrapProperties.getLastName()).thenReturn(GLOBAL_ADMIN_LAST_NAME);
            when(passwordEncoder.encode(customPassword)).thenReturn(encodedCustomPassword);

            // When: Application startup event is triggered
            final ApplicationStartedEvent event = mock(ApplicationStartedEvent.class);
            globalAdminBootstrap.onApplicationStarted(event);

            // Then: PasswordEncoder should be called with custom password
            verify(passwordEncoder, times(1)).encode(customPassword);

            // And: Saved user should have encoded custom password
            final ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository, times(1)).save(userCaptor.capture());
            final User savedUser = userCaptor.getValue();
            assertThat(savedUser.getPassword(), is(equalTo(encodedCustomPassword)));
        }

        @Test
        @DisplayName("Should handle empty password string gracefully")
        public void shouldNotCreateAdminWhenPasswordIsEmpty() {
            // Given: Password property is empty string
            when(bootstrapProperties.getEmail()).thenReturn(GLOBAL_ADMIN_EMAIL);
            when(bootstrapProperties.getPassword()).thenReturn("");

            // When: Application startup event is triggered
            final ApplicationStartedEvent event = mock(ApplicationStartedEvent.class);
            globalAdminBootstrap.onApplicationStarted(event);

            // Then: UserRepository.save should NOT be called
            verify(userRepository, times(0)).save(any());
        }
    }


    @Nested
    @DisplayName("Name Property Handling Tests")
    public class NamePropertyHandlingTests {

        @Test
        @DisplayName("Should use custom first name when property provided")
        public void shouldUseCustomFirstNameWhenProvided() {
            // Given: No admin user exists
            when(userRepository.existsByRole(Role.ADMIN)).thenReturn(false);

            // And: All properties including custom first name
            final String customFirstName = "CustomFirst";
            when(bootstrapProperties.getEmail()).thenReturn(GLOBAL_ADMIN_EMAIL);
            when(bootstrapProperties.getPassword()).thenReturn(GLOBAL_ADMIN_PASSWORD);
            when(bootstrapProperties.getFirstName()).thenReturn(customFirstName);
            when(bootstrapProperties.getLastName()).thenReturn(GLOBAL_ADMIN_LAST_NAME);
            when(passwordEncoder.encode(GLOBAL_ADMIN_PASSWORD)).thenReturn(ENCODED_PASSWORD);

            // When: Application startup event is triggered
            final ApplicationStartedEvent event = mock(ApplicationStartedEvent.class);
            globalAdminBootstrap.onApplicationStarted(event);

            // Then: Saved user should have custom first name
            final ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository, times(1)).save(userCaptor.capture());
            final User savedUser = userCaptor.getValue();
            assertThat(savedUser.getFirstName(), is(equalTo(customFirstName)));
        }

        @Test
        @DisplayName("Should use custom last name when property provided")
        public void shouldUseCustomLastNameWhenProvided() {
            // Given: No admin user exists
            when(userRepository.existsByRole(Role.ADMIN)).thenReturn(false);

            // And: All properties including custom last name
            final String customLastName = "CustomLast";
            when(bootstrapProperties.getEmail()).thenReturn(GLOBAL_ADMIN_EMAIL);
            when(bootstrapProperties.getPassword()).thenReturn(GLOBAL_ADMIN_PASSWORD);
            when(bootstrapProperties.getFirstName()).thenReturn(GLOBAL_ADMIN_FIRST_NAME);
            when(bootstrapProperties.getLastName()).thenReturn(customLastName);
            when(passwordEncoder.encode(GLOBAL_ADMIN_PASSWORD)).thenReturn(ENCODED_PASSWORD);

            // When: Application startup event is triggered
            final ApplicationStartedEvent event = mock(ApplicationStartedEvent.class);
            globalAdminBootstrap.onApplicationStarted(event);

            // Then: Saved user should have custom last name
            final ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository, times(1)).save(userCaptor.capture());
            final User savedUser = userCaptor.getValue();
            assertThat(savedUser.getLastName(), is(equalTo(customLastName)));
        }
    }


    @Nested
    @DisplayName("Event Listener Tests")
    public class EventListenerTests {

        @Test
        @DisplayName("Should handle ApplicationStartedEvent successfully")
        public void shouldHandleApplicationStartedEventSuccessfully() {
            // Given: No admin user exists
            when(userRepository.existsByRole(Role.ADMIN)).thenReturn(false);

            // And: All required properties are present
            when(bootstrapProperties.getEmail()).thenReturn(GLOBAL_ADMIN_EMAIL);
            when(bootstrapProperties.getPassword()).thenReturn(GLOBAL_ADMIN_PASSWORD);
            when(bootstrapProperties.getFirstName()).thenReturn(GLOBAL_ADMIN_FIRST_NAME);
            when(bootstrapProperties.getLastName()).thenReturn(GLOBAL_ADMIN_LAST_NAME);
            when(passwordEncoder.encode(GLOBAL_ADMIN_PASSWORD)).thenReturn(ENCODED_PASSWORD);

            // When: Application startup event is triggered
            final ApplicationStartedEvent event = mock(ApplicationStartedEvent.class);

            // Then: No exception should be thrown
            globalAdminBootstrap.onApplicationStarted(event);

            // And: UserRepository.save should be called
            verify(userRepository, times(1)).save(any());
        }
    }


    @Nested
    @DisplayName("Integration Edge Case Tests")
    public class IntegrationEdgeCaseTests {

        @Test
        @DisplayName("Should handle situation where email already exists (non-admin user)")
        public void shouldAttemptToCreateEvenIfEmailExists() {
            // Given: No admin exists but email may already be in DB
            when(userRepository.existsByRole(Role.ADMIN)).thenReturn(false);

            // And: All required properties are present
            when(bootstrapProperties.getEmail()).thenReturn(GLOBAL_ADMIN_EMAIL);
            when(bootstrapProperties.getPassword()).thenReturn(GLOBAL_ADMIN_PASSWORD);
            when(bootstrapProperties.getFirstName()).thenReturn(GLOBAL_ADMIN_FIRST_NAME);
            when(bootstrapProperties.getLastName()).thenReturn(GLOBAL_ADMIN_LAST_NAME);
            when(passwordEncoder.encode(GLOBAL_ADMIN_PASSWORD)).thenReturn(ENCODED_PASSWORD);

            // When: Application startup event is triggered
            final ApplicationStartedEvent event = mock(ApplicationStartedEvent.class);
            globalAdminBootstrap.onApplicationStarted(event);

            // Then: Should attempt to save (DB constraint would prevent duplicate)
            verify(userRepository, times(1)).save(any());
        }

        @Test
        @DisplayName("Should verify email is checked for null explicitly")
        public void shouldVerifyEmailCheckIsExplicit() {
            // Given: Email is null
            when(bootstrapProperties.getEmail()).thenReturn(null);
            when(bootstrapProperties.getPassword()).thenReturn(GLOBAL_ADMIN_PASSWORD);

            // When: Application startup event is triggered
            final ApplicationStartedEvent event = mock(ApplicationStartedEvent.class);
            globalAdminBootstrap.onApplicationStarted(event);

            // Then: Should not proceed with creation
            verify(userRepository, never()).existsByRole(Role.ADMIN);
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should verify password is checked for null explicitly")
        public void shouldVerifyPasswordCheckIsExplicit() {
            // Given: Password is null
            when(bootstrapProperties.getEmail()).thenReturn(GLOBAL_ADMIN_EMAIL);
            when(bootstrapProperties.getPassword()).thenReturn(null);

            // When: Application startup event is triggered
            final ApplicationStartedEvent event = mock(ApplicationStartedEvent.class);
            globalAdminBootstrap.onApplicationStarted(event);

            // Then: Should not proceed with creation
            verify(userRepository, never()).existsByRole(Role.ADMIN);
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should check admin existence only when required properties present")
        public void shouldCheckAdminExistenceOnlyWhenPropertiesPresent() {
            // Given: Only email is provided, password is missing
            when(bootstrapProperties.getEmail()).thenReturn(GLOBAL_ADMIN_EMAIL);
            when(bootstrapProperties.getPassword()).thenReturn(null);

            // When: Application startup event is triggered
            final ApplicationStartedEvent event = mock(ApplicationStartedEvent.class);
            globalAdminBootstrap.onApplicationStarted(event);

            // Then: Should NOT check if admin exists (optimization)
            verify(userRepository, never()).existsByRole(Role.ADMIN);
        }
    }
}
