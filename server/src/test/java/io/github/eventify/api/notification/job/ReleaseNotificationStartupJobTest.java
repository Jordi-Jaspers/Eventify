package io.github.eventify.api.notification.job;

import io.github.eventify.api.changelog.model.ChangelogEntry;
import io.github.eventify.api.changelog.service.ChangelogService;
import io.github.eventify.api.notification.model.NotificationAudienceType;
import io.github.eventify.api.notification.model.NotificationCategory;
import io.github.eventify.api.notification.model.request.CreateBroadcastRequest;
import io.github.eventify.api.notification.repository.NotificationBroadcastRepository;
import io.github.eventify.api.notification.service.NotificationBroadcastService;
import io.github.eventify.support.UnitTest;
import io.github.jframe.autoconfigure.properties.ApplicationProperties;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * Unit Test - Release Notification Startup Job
 */
@DisplayName("Unit Test - Release Notification Startup Job")
public class ReleaseNotificationStartupJobTest extends UnitTest {

    private static final String RELEASE_VERSION = "1.0.0";
    private static final String SNAPSHOT_VERSION = "1.0.0-SNAPSHOT";
    private static final String EXPECTED_TITLE = "Release " + RELEASE_VERSION;

    @Mock
    private ChangelogService changelogService;

    @Mock
    private NotificationBroadcastService notificationBroadcastService;

    @Mock
    private NotificationBroadcastRepository notificationBroadcastRepository;

    @Mock
    private ApplicationProperties applicationProperties;

    private ReleaseNotificationStartupJob job;

    @BeforeEach
    public void setUp() {
        // Given: Job is initialized with mocked dependencies and a release version
        given(applicationProperties.getVersion()).willReturn(RELEASE_VERSION);
        job = new ReleaseNotificationStartupJob(
            changelogService,
            notificationBroadcastService,
            notificationBroadcastRepository,
            applicationProperties
        );
    }

    @Test
    @DisplayName("Should dispatch release notification when version has changelog entry")
    public void shouldDispatchReleaseNotification_whenVersionHasChangelog() {
        // Given: A changelog entry exists for the current version
        final ChangelogEntry entry = aChangelogEntry(RELEASE_VERSION, 2, 1, 3);
        given(changelogService.getByVersion(RELEASE_VERSION)).willReturn(Optional.of(entry));

        // And: No broadcast has been sent for this version yet
        given(
            notificationBroadcastRepository.existsByCategoryAndTitle(
                NotificationCategory.UPDATE,
                EXPECTED_TITLE
            )
        ).willReturn(false);

        // When: The job runs on application ready
        job.onApplicationReady(null);

        // Then: sendBroadcast is called with the correct request
        final ArgumentCaptor<CreateBroadcastRequest> captor = ArgumentCaptor.forClass(CreateBroadcastRequest.class);
        verify(notificationBroadcastService).sendBroadcast(eq(null), captor.capture());

        final CreateBroadcastRequest request = captor.getValue();
        assertThat(request.getCategory(), is(equalTo(NotificationCategory.UPDATE)));
        assertThat(request.getTitle(), is(equalTo(EXPECTED_TITLE)));
        assertThat(request.getActionUrl(), is(equalTo("/changelog")));
        assertThat(request.getActionLabel(), is(equalTo("View Changelog")));
        assertThat(request.getMessage(), containsString("2"));
        assertThat(request.getMessage(), containsString("1"));
        assertThat(request.getMessage(), containsString("3"));
        assertThat(request.getAudience().getType(), is(equalTo(NotificationAudienceType.ALL_USERS)));
    }

    @Test
    @DisplayName("Should skip dispatch when version is SNAPSHOT")
    public void shouldSkipDispatch_whenVersionIsSnapshot() {
        // Given: Job is configured with a SNAPSHOT version
        given(applicationProperties.getVersion()).willReturn(SNAPSHOT_VERSION);
        final ReleaseNotificationStartupJob snapshotJob = new ReleaseNotificationStartupJob(
            changelogService,
            notificationBroadcastService,
            notificationBroadcastRepository,
            applicationProperties
        );

        // When: The job runs on application ready
        snapshotJob.onApplicationReady(null);

        // Then: No interactions with any dependency
        verifyNoInteractions(changelogService);
        verifyNoInteractions(notificationBroadcastRepository);
        verifyNoInteractions(notificationBroadcastService);
    }

    @Test
    @DisplayName("Should skip dispatch when no changelog entry exists for version")
    public void shouldSkipDispatch_whenNoChangelogEntry() {
        // Given: No changelog entry exists for the current version
        given(changelogService.getByVersion(RELEASE_VERSION)).willReturn(Optional.empty());

        // When: The job runs on application ready
        job.onApplicationReady(null);

        // Then: sendBroadcast is never called
        verify(notificationBroadcastService, never()).sendBroadcast(any(), any());
        verify(notificationBroadcastRepository, never()).existsByCategoryAndTitle(any(), any());
    }

    @Test
    @DisplayName("Should skip dispatch when broadcast already exists for this version")
    public void shouldSkipDispatch_whenBroadcastAlreadyExists() {
        // Given: A changelog entry exists for the current version
        final ChangelogEntry entry = aChangelogEntry(RELEASE_VERSION, 1, 0, 0);
        given(changelogService.getByVersion(RELEASE_VERSION)).willReturn(Optional.of(entry));

        // And: A broadcast has already been sent for this version (idempotency check)
        given(
            notificationBroadcastRepository.existsByCategoryAndTitle(
                NotificationCategory.UPDATE,
                EXPECTED_TITLE
            )
        ).willReturn(true);

        // When: The job runs on application ready
        job.onApplicationReady(null);

        // Then: sendBroadcast is never called
        verify(notificationBroadcastService, never()).sendBroadcast(any(), any());
    }

    @Test
    @DisplayName("Should call sendBroadcast even when no users exist (recipient count handled by service)")
    public void shouldCreateMarkerBroadcast_whenNoUsersExist() {
        // Given: A changelog entry exists for the current version
        final ChangelogEntry entry = aChangelogEntry(RELEASE_VERSION, 0, 0, 1);
        given(changelogService.getByVersion(RELEASE_VERSION)).willReturn(Optional.of(entry));

        // And: No broadcast has been sent yet
        given(
            notificationBroadcastRepository.existsByCategoryAndTitle(
                NotificationCategory.UPDATE,
                EXPECTED_TITLE
            )
        ).willReturn(false);

        // When: The job runs on application ready (even if no users exist in the system)
        job.onApplicationReady(null);

        // Then: sendBroadcast IS called — recipient_count=0 is handled by the service, not the job
        verify(notificationBroadcastService).sendBroadcast(eq(null), any(CreateBroadcastRequest.class));
    }

    // --- Fixtures ---

    private static ChangelogEntry aChangelogEntry(
        final String version,
        final int featureCount,
        final int improvementCount,
        final int fixCount) {
        final List<String> features = buildItems("Feature", featureCount);
        final List<String> improvements = buildItems("Improvement", improvementCount);
        final List<String> fixes = buildItems("Fix", fixCount);
        return new ChangelogEntry(version, "2026-01-01", features, improvements, fixes);
    }

    private static List<String> buildItems(final String prefix, final int count) {
        return java.util.stream.IntStream.range(0, count)
            .mapToObj(i -> prefix + " " + (i + 1))
            .toList();
    }
}
