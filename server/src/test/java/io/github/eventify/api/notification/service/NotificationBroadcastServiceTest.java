package io.github.eventify.api.notification.service;

import io.github.eventify.api.notification.model.NotificationAudience;
import io.github.eventify.api.notification.model.NotificationAudienceType;
import io.github.eventify.api.notification.model.NotificationBroadcast;
import io.github.eventify.api.notification.model.NotificationBroadcastMetaData;
import io.github.eventify.api.notification.model.NotificationCategory;
import io.github.eventify.api.notification.model.NotificationPayload;
import io.github.eventify.api.notification.model.request.AudienceRequest;
import io.github.eventify.api.notification.model.request.CreateBroadcastRequest;
import io.github.eventify.api.notification.repository.NotificationBroadcastRepository;
import io.github.eventify.api.user.model.User;
import io.github.eventify.support.UnitTest;
import io.github.jframe.datasource.search.model.input.SortablePageInput;

import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Unit Test - Notification Broadcast Service")
public class NotificationBroadcastServiceTest extends UnitTest {

    @Mock
    private AudienceResolver audienceResolver;

    @Mock
    private NotificationDispatchService notificationDispatchService;

    @Mock
    private NotificationBroadcastRepository notificationBroadcastRepository;

    @Mock
    private NotificationBroadcastMetaData broadcastMetaData;

    private NotificationBroadcastService notificationBroadcastService;

    @BeforeEach
    public void setUp() {
        lenient().when(broadcastMetaData.toSort(any())).thenReturn(Sort.unsorted());
        lenient().when(broadcastMetaData.toSearchSpecification(any())).thenReturn(
            (Specification<NotificationBroadcast>) (root, query, cb) -> null
        );
        notificationBroadcastService = new NotificationBroadcastService(
            audienceResolver,
            notificationDispatchService,
            notificationBroadcastRepository,
            broadcastMetaData
        );
    }

    // ========================= sendBroadcast =========================

    @Test
    @DisplayName("Should create broadcast row and dispatch to all resolved users")
    public void shouldSendBroadcastToAllResolvedUsers() {
        // Given: A sender and a request targeting all users
        final User sender = aValidUser();
        final CreateBroadcastRequest request = aValidBroadcastRequest(NotificationAudienceType.ALL_USERS, null, null);

        final User recipient1 = aValidUser();
        final User recipient2 = aValidUser();
        recipient2.setId(2L);

        final NotificationBroadcast savedBroadcast = aSavedBroadcast(1L, "Test Title", NotificationAudienceType.ALL_USERS, 2);

        when(audienceResolver.resolve(any(NotificationAudience.class)))
            .thenReturn(List.of(recipient1, recipient2));
        when(notificationBroadcastRepository.save(any(NotificationBroadcast.class)))
            .thenReturn(savedBroadcast);

        // When: Sending the broadcast
        final NotificationBroadcast response = notificationBroadcastService.sendBroadcast(sender, request);

        // Then: Broadcast row should be saved
        verify(notificationBroadcastRepository).save(any(NotificationBroadcast.class));

        // And: Dispatch should be called for each recipient
        verify(notificationDispatchService, times(2)).dispatch(any(NotificationAudience.class), any(NotificationPayload.class));

        // And: Response should contain correct recipient count
        assertThat(response, is(notNullValue()));
        assertThat(response.getRecipientCount(), is(2));
        assertThat(response.getId(), is(1L));
    }

    @Test
    @DisplayName("Should save broadcast with correct fields from request")
    public void shouldSaveBroadcastWithCorrectFields() {
        // Given: A sender and a detailed request
        final User sender = aValidUser();
        final CreateBroadcastRequest request = aValidBroadcastRequest(NotificationAudienceType.ALL_USERS, null, null);
        request.setTitle("Important Announcement");
        request.setMessage("This is an important message");
        request.setCategory(NotificationCategory.ANNOUNCEMENT);
        request.setActionUrl("/dashboard");
        request.setActionLabel("Go to Dashboard");

        final NotificationBroadcast savedBroadcast = aSavedBroadcast(1L, "Important Announcement", NotificationAudienceType.ALL_USERS, 1);

        when(audienceResolver.resolve(any(NotificationAudience.class))).thenReturn(List.of(aValidUser()));
        when(notificationBroadcastRepository.save(any(NotificationBroadcast.class))).thenReturn(savedBroadcast);

        // When: Sending the broadcast
        notificationBroadcastService.sendBroadcast(sender, request);

        // Then: Saved broadcast should have correct fields
        final ArgumentCaptor<NotificationBroadcast> captor = ArgumentCaptor.forClass(NotificationBroadcast.class);
        verify(notificationBroadcastRepository).save(captor.capture());

        final NotificationBroadcast captured = captor.getValue();
        assertThat(captured.getTitle(), is("Important Announcement"));
        assertThat(captured.getMessage(), is("This is an important message"));
        assertThat(captured.getCategory(), is(NotificationCategory.ANNOUNCEMENT));
        assertThat(captured.getActionUrl(), is("/dashboard"));
        assertThat(captured.getActionLabel(), is("Go to Dashboard"));
        assertThat(captured.getAudienceType(), is(NotificationAudienceType.ALL_USERS));
        assertThat(captured.getSentBy(), is(sender));
    }

    @Test
    @DisplayName("Should create broadcast row with zero recipients when org has no members")
    public void shouldCreateBroadcastWithZeroRecipientsWhenOrgEmpty() {
        // Given: A sender and a request targeting an empty organization
        final User sender = aValidUser();
        final CreateBroadcastRequest request = aValidBroadcastRequest(NotificationAudienceType.ORGANIZATION, 10L, null);

        final NotificationBroadcast savedBroadcast = aSavedBroadcast(1L, "Test Title", NotificationAudienceType.ORGANIZATION, 0);

        when(audienceResolver.resolve(any(NotificationAudience.class))).thenReturn(List.of());
        when(notificationBroadcastRepository.save(any(NotificationBroadcast.class))).thenReturn(savedBroadcast);

        // When: Sending the broadcast
        final NotificationBroadcast response = notificationBroadcastService.sendBroadcast(sender, request);

        // Then: Broadcast row should still be saved
        verify(notificationBroadcastRepository).save(any(NotificationBroadcast.class));

        // And: No notifications should be dispatched
        verifyNoInteractions(notificationDispatchService);

        // And: Response should have zero recipient count
        assertThat(response.getRecipientCount(), is(0));
    }

    @Test
    @DisplayName("Should save broadcast with audienceTargetId when audience is ORGANIZATION")
    public void shouldSaveBroadcastWithAudienceTargetIdForOrganization() {
        // Given: A request targeting a specific organization
        final User sender = aValidUser();
        final Long orgId = 42L;
        final CreateBroadcastRequest request = aValidBroadcastRequest(NotificationAudienceType.ORGANIZATION, orgId, null);

        final NotificationBroadcast savedBroadcast = aSavedBroadcast(1L, "Test Title", NotificationAudienceType.ORGANIZATION, 0);

        when(audienceResolver.resolve(any(NotificationAudience.class))).thenReturn(List.of());
        when(notificationBroadcastRepository.save(any(NotificationBroadcast.class))).thenReturn(savedBroadcast);

        // When: Sending the broadcast
        notificationBroadcastService.sendBroadcast(sender, request);

        // Then: Saved broadcast should have audienceTargetId set
        final ArgumentCaptor<NotificationBroadcast> captor = ArgumentCaptor.forClass(NotificationBroadcast.class);
        verify(notificationBroadcastRepository).save(captor.capture());

        assertThat(captor.getValue().getAudienceTargetId(), is(orgId));
    }

    @Test
    @DisplayName("Should save broadcast with audienceRole when audience is GLOBAL_ROLE")
    public void shouldSaveBroadcastWithAudienceRoleForGlobalRole() {
        // Given: A request targeting users with a specific role
        final User sender = aValidUser();
        final CreateBroadcastRequest request = aValidBroadcastRequest(NotificationAudienceType.GLOBAL_ROLE, null, "USER");

        final NotificationBroadcast savedBroadcast = aSavedBroadcast(1L, "Test Title", NotificationAudienceType.GLOBAL_ROLE, 0);

        when(audienceResolver.resolve(any(NotificationAudience.class))).thenReturn(List.of());
        when(notificationBroadcastRepository.save(any(NotificationBroadcast.class))).thenReturn(savedBroadcast);

        // When: Sending the broadcast
        notificationBroadcastService.sendBroadcast(sender, request);

        // Then: Saved broadcast should have audienceRole set
        final ArgumentCaptor<NotificationBroadcast> captor = ArgumentCaptor.forClass(NotificationBroadcast.class);
        verify(notificationBroadcastRepository).save(captor.capture());

        assertThat(captor.getValue().getAudienceRole(), is("USER"));
    }

    @Test
    @DisplayName("Should return response with sentByEmail from sender")
    public void shouldReturnResponseWithSentByEmail() {
        // Given: A sender with known email
        final User sender = aValidUser();
        final CreateBroadcastRequest request = aValidBroadcastRequest(NotificationAudienceType.ALL_USERS, null, null);

        final NotificationBroadcast savedBroadcast = aSavedBroadcast(1L, "Test Title", NotificationAudienceType.ALL_USERS, 1);
        savedBroadcast.setSentBy(sender);

        when(audienceResolver.resolve(any(NotificationAudience.class))).thenReturn(List.of(aValidUser()));
        when(notificationBroadcastRepository.save(any(NotificationBroadcast.class))).thenReturn(savedBroadcast);

        // When: Sending the broadcast
        final NotificationBroadcast result = notificationBroadcastService.sendBroadcast(sender, request);

        // Then: Response should contain sender
        assertThat(result.getSentBy(), is(sender));
    }

    // ========================= previewRecipientCount =========================

    @Test
    @DisplayName("Should return recipient count without dispatching any notifications")
    public void shouldPreviewRecipientCountWithoutDispatching() {
        // Given: An audience request for all users
        final AudienceRequest audienceRequest = anAudienceRequest(NotificationAudienceType.ALL_USERS, null, null);

        when(audienceResolver.count(any(NotificationAudience.class))).thenReturn(50L);

        // When: Previewing recipient count
        final int count = notificationBroadcastService.previewRecipientCount(audienceRequest);

        // Then: Should return the count
        assertThat(count, is(50));

        // And: No broadcast should be saved
        verifyNoInteractions(notificationBroadcastRepository);

        // And: No notifications should be dispatched
        verifyNoInteractions(notificationDispatchService);
    }

    @Test
    @DisplayName("Should return zero count when audience has no recipients")
    public void shouldReturnZeroPreviewCountWhenNoRecipients() {
        // Given: An audience request for an empty organization
        final AudienceRequest audienceRequest = anAudienceRequest(NotificationAudienceType.ORGANIZATION, 99L, null);

        when(audienceResolver.count(any(NotificationAudience.class))).thenReturn(0L);

        // When: Previewing recipient count
        final int count = notificationBroadcastService.previewRecipientCount(audienceRequest);

        // Then: Should return 0
        assertThat(count, is(0));
    }

    // ========================= searchBroadcasts =========================

    @Test
    @DisplayName("Should return page of broadcasts ordered by createdAt DESC")
    public void shouldSearchBroadcastsReturnPage() {
        // Given: A page input and existing broadcasts
        final SortablePageInput input = new SortablePageInput();
        input.setPageNumber(0);
        input.setPageSize(20);

        final NotificationBroadcast broadcast1 = aSavedBroadcast(1L, "First Broadcast", NotificationAudienceType.ALL_USERS, 10);
        final NotificationBroadcast broadcast2 = aSavedBroadcast(2L, "Second Broadcast", NotificationAudienceType.ORGANIZATION, 5);
        final Page<NotificationBroadcast> page = new PageImpl<>(List.of(broadcast1, broadcast2));

        when(notificationBroadcastRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        // When: Searching broadcasts
        final Page<NotificationBroadcast> result = notificationBroadcastService.searchBroadcasts(input);

        // Then: Should return page with broadcasts
        assertThat(result, is(notNullValue()));
        assertThat(result.getContent(), hasSize(2));
    }

    @Test
    @DisplayName("Should return empty page when no broadcasts exist")
    public void shouldReturnEmptyPageWhenNoBroadcasts() {
        // Given: No broadcasts exist
        final SortablePageInput input = new SortablePageInput();
        input.setPageNumber(0);
        input.setPageSize(20);

        when(notificationBroadcastRepository.findAll(any(Specification.class), any(Pageable.class)))
            .thenReturn(Page.empty());

        // When: Searching broadcasts
        final Page<NotificationBroadcast> result = notificationBroadcastService.searchBroadcasts(input);

        // Then: Should return empty page
        assertThat(result.getContent(), is(empty()));
        assertThat(result.getTotalElements(), is(0L));
    }

    // ========================= FACTORY METHODS =========================

    private static CreateBroadcastRequest aValidBroadcastRequest(final NotificationAudienceType audienceType,
        final Long targetId, final String role) {
        final AudienceRequest audience = anAudienceRequest(audienceType, targetId, role);
        final CreateBroadcastRequest request = new CreateBroadcastRequest();
        request.setTitle("Test Title");
        request.setMessage("Test message content");
        request.setCategory(NotificationCategory.ANNOUNCEMENT);
        request.setAudience(audience);
        return request;
    }

    private static AudienceRequest anAudienceRequest(final NotificationAudienceType audienceType,
        final Long targetId, final String role) {
        final AudienceRequest audience = new AudienceRequest();
        audience.setType(audienceType);
        audience.setTargetId(targetId);
        audience.setRole(role);
        return audience;
    }

    private static NotificationBroadcast aSavedBroadcast(final Long id, final String title,
        final NotificationAudienceType audienceType, final int recipientCount) {
        final NotificationBroadcast broadcast = new NotificationBroadcast();
        broadcast.setId(id);
        broadcast.setTitle(title);
        broadcast.setMessage("Broadcast message");
        broadcast.setCategory(NotificationCategory.ANNOUNCEMENT);
        broadcast.setAudienceType(audienceType);
        broadcast.setRecipientCount(recipientCount);
        broadcast.setCreatedAt(OffsetDateTime.now());
        return broadcast;
    }
}
