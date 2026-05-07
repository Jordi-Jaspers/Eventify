package io.github.eventify.api.user.service;

import io.github.eventify.api.user.model.AuthProvider;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.user.model.UserAuthProvider;
import io.github.eventify.api.user.model.response.ProviderResponse;
import io.github.eventify.api.user.repository.UserAuthProviderRepository;
import io.github.eventify.api.user.repository.UserRepository;
import io.github.eventify.common.exception.LastAuthMethodException;
import io.github.eventify.common.exception.LinkOAuth2Exception;
import io.github.eventify.common.exception.LocalProviderUnlinkException;
import io.github.eventify.support.TestBuilders;
import io.github.eventify.support.UnitTest;
import io.github.jframe.exception.core.DataNotFoundException;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static io.github.eventify.common.exception.ApiErrorCode.PROVIDER_ALREADY_LINKED_ERROR;
import static io.github.eventify.common.exception.ApiErrorCode.PROVIDER_LINKED_ELSEWHERE_ERROR;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@DisplayName("Unit Test - UserAuthProvider Service")
public class UserAuthProviderServiceTest extends UnitTest {

    @Mock
    private UserAuthProviderRepository userAuthProviderRepository;

    @Mock
    private UserRepository userRepository;

    private UserAuthProviderService userAuthProviderService;

    @BeforeEach
    public void setUp() {
        userAuthProviderService = new UserAuthProviderService(userAuthProviderRepository, userRepository);
    }

    @Test
    @DisplayName("Should return all providers including unconnected when user has LOCAL and GOOGLE linked")
    public void listProvidersForUser_returnsAllProvidersIncludingUnconnected() {
        // Given: A user with LOCAL and GOOGLE linked
        final User user = aValidUser();
        user.setEmail("user@example.com");

        final UserAuthProvider localProvider = TestBuilders.aUserAuthProvider(1L, user, AuthProvider.LOCAL, user.getEmail());
        final UserAuthProvider googleProvider = TestBuilders.aUserAuthProvider(2L, user, AuthProvider.GOOGLE, "google@gmail.com");

        when(userAuthProviderRepository.findAllByUser(user))
            .thenReturn(List.of(localProvider, googleProvider));

        // When: Listing providers for the user
        final List<ProviderResponse> responses = userAuthProviderService.listProvidersForUser(user);

        // Then: Should return 3 entries (LOCAL, GOOGLE, GITHUB)
        assertThat(responses, hasSize(3));

        // And: LOCAL should be connected with user's email
        final ProviderResponse localResponse = responses.get(0);
        assertThat(localResponse.getProvider(), is(equalTo(AuthProvider.LOCAL)));
        assertThat(localResponse.isConnected(), is(true));
        assertThat(localResponse.getProviderEmail(), is(equalTo(user.getEmail())));

        // And: GOOGLE should be connected with stored email
        final ProviderResponse googleResponse = responses.get(1);
        assertThat(googleResponse.getProvider(), is(equalTo(AuthProvider.GOOGLE)));
        assertThat(googleResponse.isConnected(), is(true));
        assertThat(googleResponse.getProviderEmail(), is(equalTo("google@gmail.com")));

        // And: GITHUB should not be connected with no email
        final ProviderResponse githubResponse = responses.get(2);
        assertThat(githubResponse.getProvider(), is(equalTo(AuthProvider.GITHUB)));
        assertThat(githubResponse.isConnected(), is(false));
        assertThat(githubResponse.getProviderEmail(), is(nullValue()));
    }

    @Test
    @DisplayName("Should return all providers as not-connected when user has no linked providers")
    public void listProvidersForUser_returnsOnlyConnectedKnownProviders_whenUserHasNoLinks() {
        // Given: A user with no UserAuthProvider rows
        final User user = aValidUser();

        when(userAuthProviderRepository.findAllByUser(user))
            .thenReturn(List.of());

        // When: Listing providers for the user
        final List<ProviderResponse> responses = userAuthProviderService.listProvidersForUser(user);

        // Then: Should return 3 entries all not connected
        assertThat(responses, hasSize(3));
        assertThat(responses, everyItem(hasProperty("connected", is(false))));
        assertThat(responses, everyItem(hasProperty("providerEmail", is(nullValue()))));
    }

    @Test
    @DisplayName("Should remove provider when unlinking GOOGLE from user with LOCAL and GOOGLE")
    public void unlinkProvider_success_removesProvider() {
        // Given: A user with LOCAL and GOOGLE linked
        final User user = aValidUser();

        final UserAuthProvider localProvider = TestBuilders.aUserAuthProvider(1L, user, AuthProvider.LOCAL, user.getEmail());
        final UserAuthProvider googleProvider = TestBuilders.aUserAuthProvider(2L, user, AuthProvider.GOOGLE, "google@gmail.com");

        when(userAuthProviderRepository.findById(2L)).thenReturn(Optional.of(googleProvider));
        when(userAuthProviderRepository.findAllByUser(user)).thenReturn(List.of(localProvider, googleProvider));

        // When: Unlinking GOOGLE
        userAuthProviderService.unlinkProvider(user, 2L);

        // Then: The GOOGLE provider should be deleted
        verify(userAuthProviderRepository, times(1)).delete(googleProvider);
    }

    @Test
    @DisplayName("Should throw LAST_AUTH_METHOD_ERROR when unlinking only provider and user has no password")
    public void unlinkProvider_throwsLastAuthMethodError_whenUnlinkingOnlyProviderAndNoPassword() {
        // Given: A user with only GOOGLE linked and no password
        final User user = aValidUser();

        final UserAuthProvider googleProvider = TestBuilders.aUserAuthProvider(1L, user, AuthProvider.GOOGLE, "google@gmail.com");

        when(userAuthProviderRepository.findById(1L)).thenReturn(Optional.of(googleProvider));
        when(userAuthProviderRepository.findAllByUser(user)).thenReturn(List.of(googleProvider));

        // When & Then: Should throw LastAuthMethodException
        assertThrows(
            LastAuthMethodException.class,
            () -> userAuthProviderService.unlinkProvider(user, 1L)
        );

        // And: The provider should NOT be deleted
        verify(userAuthProviderRepository, never()).delete(any(UserAuthProvider.class));
    }

    @Test
    @DisplayName("Should throw LAST_AUTH_METHOD_ERROR when unlinking LOCAL and no other providers exist")
    public void unlinkProvider_throwsLastAuthMethodError_whenUnlinkingLocalAndNoOtherProviders() {
        // Given: A user with only LOCAL provider
        final User user = aValidUser();

        final UserAuthProvider localProvider = TestBuilders.aUserAuthProvider(1L, user, AuthProvider.LOCAL, user.getEmail());

        when(userAuthProviderRepository.findById(1L)).thenReturn(Optional.of(localProvider));
        when(userAuthProviderRepository.findAllByUser(user)).thenReturn(List.of(localProvider));

        // When & Then: Should throw LastAuthMethodException
        assertThrows(
            LastAuthMethodException.class,
            () -> userAuthProviderService.unlinkProvider(user, 1L)
        );

        // And: The provider should NOT be deleted
        verify(userAuthProviderRepository, never()).delete(any(UserAuthProvider.class));
    }

    @Test
    @DisplayName("Should succeed when unlinking OAuth2 provider and user has a password")
    public void unlinkProvider_succeeds_whenUnlinkingOAuth2AndUserHasPassword() {
        // Given: A user with LOCAL and GOOGLE, a password set
        final User user = aValidUser();

        final UserAuthProvider localProvider = TestBuilders.aUserAuthProvider(1L, user, AuthProvider.LOCAL, user.getEmail());
        final UserAuthProvider googleProvider = TestBuilders.aUserAuthProvider(2L, user, AuthProvider.GOOGLE, "google@gmail.com");

        when(userAuthProviderRepository.findById(2L)).thenReturn(Optional.of(googleProvider));
        when(userAuthProviderRepository.findAllByUser(user)).thenReturn(List.of(localProvider, googleProvider));

        // When: Unlinking GOOGLE
        userAuthProviderService.unlinkProvider(user, 2L);

        // Then: The GOOGLE provider should be deleted
        verify(userAuthProviderRepository, times(1)).delete(googleProvider);
    }

    @Test
    @DisplayName("Should succeed when unlinking OAuth2 and user has multiple OAuth2 providers")
    public void unlinkProvider_succeeds_whenUnlinkingOAuth2AndUserHasMultipleOAuth2Providers() {
        // Given: A user with GOOGLE and GITHUB (no LOCAL)
        final User user = aValidUser();

        final UserAuthProvider googleProvider = TestBuilders.aUserAuthProvider(1L, user, AuthProvider.GOOGLE, "google@gmail.com");
        final UserAuthProvider githubProvider = TestBuilders.aUserAuthProvider(2L, user, AuthProvider.GITHUB, "github@example.com");

        when(userAuthProviderRepository.findById(1L)).thenReturn(Optional.of(googleProvider));
        when(userAuthProviderRepository.findAllByUser(user)).thenReturn(List.of(googleProvider, githubProvider));

        // When: Unlinking GOOGLE (GITHUB remains)
        userAuthProviderService.unlinkProvider(user, 1L);

        // Then: The GOOGLE provider should be deleted
        verify(userAuthProviderRepository, times(1)).delete(googleProvider);
    }

    @Test
    @DisplayName("Should throw PROVIDER_NOT_FOUND_ERROR when provider ID does not exist")
    public void unlinkProvider_throwsNotFound_whenProviderDoesNotExist() {
        // Given: A user
        final User user = aValidUser();

        // And: No provider with the given ID
        when(userAuthProviderRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then: Should throw DataNotFoundException with PROVIDER_NOT_FOUND_ERROR
        assertThrows(
            DataNotFoundException.class,
            () -> userAuthProviderService.unlinkProvider(user, 999L)
        );
    }

    @Test
    @DisplayName("Should throw PROVIDER_NOT_FOUND_ERROR when provider belongs to a different user (no existence leak)")
    public void unlinkProvider_throwsNotFound_whenProviderBelongsToDifferentUser() {
        // Given: Two users
        final User owner = aValidUser();
        owner.setId(1L);

        final User attacker = aValidUser();
        attacker.setId(2L);
        attacker.setEmail("attacker@example.com");

        // And: A provider belonging to owner
        final UserAuthProvider ownerProvider = TestBuilders.aUserAuthProvider(5L, owner, AuthProvider.GOOGLE, "google@gmail.com");

        when(userAuthProviderRepository.findById(5L)).thenReturn(Optional.of(ownerProvider));

        // When: Attacker tries to unlink owner's provider
        // Then: Should throw DataNotFoundException (not 403, to avoid existence leak)
        assertThrows(
            DataNotFoundException.class,
            () -> userAuthProviderService.unlinkProvider(attacker, 5L)
        );

        // And: The provider should NOT be deleted
        verify(userAuthProviderRepository, never()).delete(ownerProvider);
    }

    // ========================= U4: Unlink LOCAL provider — NEVER allowed =========================

    @Test
    @DisplayName("U4: Should throw LocalProviderUnlinkException when attempting to unlink LOCAL provider")
    public void unlinkProvider_U4_throwsLocalProviderUnlinkException_whenProviderIsLocal() {
        // Given: User X has LOCAL provider record
        final User user = aValidUser();

        final UserAuthProvider localProvider = TestBuilders.aUserAuthProvider(1L, user, AuthProvider.LOCAL, user.getEmail());

        when(userAuthProviderRepository.findById(1L)).thenReturn(Optional.of(localProvider));

        // When & Then: Attempting to unlink LOCAL should throw LocalProviderUnlinkException
        assertThrows(
            LocalProviderUnlinkException.class,
            () -> userAuthProviderService.unlinkProvider(user, 1L)
        );

        // And: The LOCAL provider should NOT be deleted
        verify(userAuthProviderRepository, never()).delete(any(UserAuthProvider.class));
    }

    @Test
    @DisplayName("U4: Should throw LocalProviderUnlinkException even when user has other OAuth2 providers")
    public void unlinkProvider_U4_throwsLocalProviderUnlinkException_evenWhenOtherProvidersExist() {
        // Given: User X has LOCAL + GOOGLE (so last-method check would pass, but LOCAL guard fires first)
        final User user = aValidUser();

        final UserAuthProvider localProvider = TestBuilders.aUserAuthProvider(1L, user, AuthProvider.LOCAL, user.getEmail());
        final UserAuthProvider googleProvider = TestBuilders.aUserAuthProvider(2L, user, AuthProvider.GOOGLE, "google@gmail.com");

        when(userAuthProviderRepository.findById(1L)).thenReturn(Optional.of(localProvider));

        // When & Then: LOCAL unlink is always rejected regardless of other providers
        assertThrows(
            LocalProviderUnlinkException.class,
            () -> userAuthProviderService.unlinkProvider(user, 1L)
        );

        // And: The LOCAL provider should NOT be deleted
        verify(userAuthProviderRepository, never()).delete(any(UserAuthProvider.class));
    }

    // ========================= linkProviderForUser — K3/K4/K5 service-level enforcement =========================

    @Test
    @DisplayName("K5 (service): Should throw PROVIDER_ALREADY_LINKED_ERROR when same provider already linked to current user")
    public void linkProviderForUser_throwsPROVIDER_ALREADY_LINKED_ERROR_whenSameProviderExistsForUser() {
        // Given: User X already has GOOGLE linked
        final User userX = aValidUser();
        userX.setId(1L);

        when(userAuthProviderRepository.existsByUserAndProvider(userX, AuthProvider.GOOGLE)).thenReturn(true);

        // When & Then: Linking GOOGLE again should throw LinkOAuth2Exception with PROVIDER_ALREADY_LINKED_ERROR
        final LinkOAuth2Exception exception = assertThrows(
            LinkOAuth2Exception.class,
            () -> userAuthProviderService.linkProviderForUser(userX, AuthProvider.GOOGLE, "any@example.com")
        );

        assertThat(exception.getErrorCode(), is(equalTo(PROVIDER_ALREADY_LINKED_ERROR)));

        // And: No provider record is created
        verify(userAuthProviderRepository, never()).save(any(UserAuthProvider.class));
    }

    @Test
    @DisplayName("K4 (service): Should throw PROVIDER_LINKED_ELSEWHERE_ERROR when provider email is already linked to another user")
    public void linkProviderForUser_throwsPROVIDER_LINKED_ELSEWHERE_ERROR_whenProviderEmailLinkedToAnotherUser() {
        // Given: User X does NOT have GOOGLE linked
        final User userX = aValidUser();
        userX.setId(1L);

        // And: User Z has GOOGLE linked with providerEmail=g@example.com
        final User userZ = aValidUser();
        userZ.setId(2L);
        final UserAuthProvider zGoogleProvider = TestBuilders.aUserAuthProvider(10L, userZ, AuthProvider.GOOGLE, "g@example.com");

        when(userAuthProviderRepository.existsByUserAndProvider(userX, AuthProvider.GOOGLE)).thenReturn(false);
        when(userAuthProviderRepository.findByProviderAndProviderEmail(AuthProvider.GOOGLE, "g@example.com"))
            .thenReturn(Optional.of(zGoogleProvider));

        // When & Then: Linking GOOGLE with g@example.com should throw PROVIDER_LINKED_ELSEWHERE_ERROR
        final LinkOAuth2Exception exception = assertThrows(
            LinkOAuth2Exception.class,
            () -> userAuthProviderService.linkProviderForUser(userX, AuthProvider.GOOGLE, "g@example.com")
        );

        assertThat(exception.getErrorCode(), is(equalTo(PROVIDER_LINKED_ELSEWHERE_ERROR)));

        // And: No provider record is created
        verify(userAuthProviderRepository, never()).save(any(UserAuthProvider.class));
    }

    @Test
    @DisplayName("K1+K2 (service): Should successfully link provider when all checks pass")
    public void linkProviderForUser_succeeds_whenAllChecksPass() {
        // Given: User X does NOT have GOOGLE linked
        final User userX = aValidUser();
        userX.setId(1L);
        userX.setEmail("x@example.com");

        when(userAuthProviderRepository.existsByUserAndProvider(userX, AuthProvider.GOOGLE)).thenReturn(false);
        when(userAuthProviderRepository.findByProviderAndProviderEmail(AuthProvider.GOOGLE, "y@example.com"))
            .thenReturn(Optional.empty());
        when(userAuthProviderRepository.save(any(UserAuthProvider.class))).thenAnswer(i -> i.getArgument(0));

        // When: Linking GOOGLE with y@example.com (unknown email, no conflicts)
        userAuthProviderService.linkProviderForUser(userX, AuthProvider.GOOGLE, "y@example.com");

        // Then: Provider record is saved for user X
        verify(userAuthProviderRepository, times(1)).save(any(UserAuthProvider.class));
    }

    @Test
    @DisplayName(
        "Should succeed linking provider when provider email matches another user's primary email but provider is not linked elsewhere"
    )
    public void linkProviderForUser_succeeds_whenProviderEmailMatchesAnotherUsersPrimaryEmailButProviderNotLinkedElsewhere() {
        // Given: User X (id=1, email=x@example.com) does NOT have GITHUB linked
        final User userX = aValidUser();
        userX.setId(1L);
        userX.setEmail("x@example.com");

        // And: User Y (id=2, email=y@example.com) exists as another user
        final User userY = aValidUser();
        userY.setId(2L);
        userY.setEmail("y@example.com");

        // And: No existing UserAuthProvider for GITHUB+y@example.com (provider not linked elsewhere)
        when(userAuthProviderRepository.existsByUserAndProvider(userX, AuthProvider.GITHUB)).thenReturn(false);
        when(userAuthProviderRepository.findByProviderAndProviderEmail(AuthProvider.GITHUB, "y@example.com"))
            .thenReturn(Optional.empty());
        when(userAuthProviderRepository.save(any(UserAuthProvider.class))).thenAnswer(i -> i.getArgument(0));

        // When: Linking GITHUB with y@example.com (another user's primary email, but provider not linked)
        userAuthProviderService.linkProviderForUser(userX, AuthProvider.GITHUB, "y@example.com");

        // Then: Provider record is saved — no exception thrown
        verify(userAuthProviderRepository, times(1)).save(any(UserAuthProvider.class));
    }

    @Test
    @DisplayName("Should throw PROVIDER_LINKED_ELSEWHERE_ERROR when provider is still actively linked to another user")
    public void linkProviderForUser_throwsPROVIDER_LINKED_ELSEWHERE_ERROR_whenProviderActivelyLinkedToAnotherUser() {
        // Given: User X (id=1) does NOT have GITHUB linked
        final User userX = aValidUser();
        userX.setId(1L);

        // And: User Y (id=2) has GITHUB linked with email y@example.com
        final User userY = aValidUser();
        userY.setId(2L);
        final UserAuthProvider yGithubProvider = TestBuilders.aUserAuthProvider(10L, userY, AuthProvider.GITHUB, "y@example.com");

        when(userAuthProviderRepository.existsByUserAndProvider(userX, AuthProvider.GITHUB)).thenReturn(false);
        when(userAuthProviderRepository.findByProviderAndProviderEmail(AuthProvider.GITHUB, "y@example.com"))
            .thenReturn(Optional.of(yGithubProvider));

        // When: User X tries to link GITHUB with y@example.com
        final LinkOAuth2Exception exception = assertThrows(
            LinkOAuth2Exception.class,
            () -> userAuthProviderService.linkProviderForUser(userX, AuthProvider.GITHUB, "y@example.com")
        );

        // Then: Should throw PROVIDER_LINKED_ELSEWHERE_ERROR
        assertThat(exception.getErrorCode(), is(equalTo(PROVIDER_LINKED_ELSEWHERE_ERROR)));

        // And: No provider record is created
        verify(userAuthProviderRepository, never()).save(any(UserAuthProvider.class));
    }

    @Test
    @DisplayName("Should succeed linking provider after it was unlinked from another user (re-link scenario)")
    public void linkProviderForUser_succeeds_whenProviderWasPreviouslyUnlinkedFromAnotherUser() {
        // Given: User X (id=1, email=x@example.com) does NOT have GITHUB linked
        final User userX = aValidUser();
        userX.setId(1L);
        userX.setEmail("x@example.com");

        // And: User Y (id=2, email=y@example.com) exists as another user
        final User userY = aValidUser();
        userY.setId(2L);
        userY.setEmail("y@example.com");

        // And: No existing UserAuthProvider for GITHUB+y@example.com (was unlinked — findByProviderAndProviderEmail returns empty)
        when(userAuthProviderRepository.existsByUserAndProvider(userX, AuthProvider.GITHUB)).thenReturn(false);
        when(userAuthProviderRepository.findByProviderAndProviderEmail(AuthProvider.GITHUB, "y@example.com"))
            .thenReturn(Optional.empty());
        when(userAuthProviderRepository.save(any(UserAuthProvider.class))).thenAnswer(i -> i.getArgument(0));

        // When: Linking GITHUB with y@example.com (provider was previously unlinked from User Y)
        userAuthProviderService.linkProviderForUser(userX, AuthProvider.GITHUB, "y@example.com");

        // Then: Provider record is saved — no exception thrown (re-link is allowed)
        verify(userAuthProviderRepository, times(1)).save(any(UserAuthProvider.class));
    }

    @Test
    @DisplayName("K3 (service): Should NOT throw EMAIL_IN_USE_ERROR when provider email matches current user's own primary email")
    public void linkProviderForUser_succeeds_whenProviderEmailMatchesCurrentUserOwnEmail() {
        // Given: User X with email=x@example.com, no GOOGLE linked
        final User userX = aValidUser();
        userX.setId(1L);
        userX.setEmail("x@example.com");

        when(userAuthProviderRepository.existsByUserAndProvider(userX, AuthProvider.GOOGLE)).thenReturn(false);
        when(userAuthProviderRepository.findByProviderAndProviderEmail(AuthProvider.GOOGLE, "x@example.com"))
            .thenReturn(Optional.empty());
        when(userAuthProviderRepository.save(any(UserAuthProvider.class))).thenAnswer(i -> i.getArgument(0));

        // When: Linking GOOGLE with x@example.com (same as own primary email)
        userAuthProviderService.linkProviderForUser(userX, AuthProvider.GOOGLE, "x@example.com");

        // Then: Provider record is saved — no exception thrown
        verify(userAuthProviderRepository, times(1)).save(any(UserAuthProvider.class));
    }

}
