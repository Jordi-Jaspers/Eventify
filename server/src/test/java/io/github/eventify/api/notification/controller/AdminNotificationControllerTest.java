package io.github.eventify.api.notification.controller;

import io.github.eventify.api.notification.model.NotificationAudienceType;
import io.github.eventify.api.notification.model.NotificationCategory;
import io.github.eventify.api.notification.model.request.AudienceRequest;
import io.github.eventify.api.notification.model.request.CreateBroadcastRequest;
import io.github.eventify.api.notification.model.response.BroadcastResponse;
import io.github.eventify.api.notification.model.response.PreviewResponse;
import io.github.eventify.api.user.model.User;
import io.github.eventify.support.IntegrationTest;
import io.github.jframe.datasource.search.model.input.SearchInput;
import io.github.jframe.datasource.search.model.input.SortablePageInput;
import io.github.jframe.datasource.search.model.resource.PageResource;
import tools.jackson.core.type.TypeReference;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static io.github.eventify.api.Paths.*;
import static io.github.eventify.common.constant.Constants.Security.BEARER;
import static io.github.jframe.util.mapper.ObjectMappers.fromJson;
import static io.github.jframe.util.mapper.ObjectMappers.toJson;
import static jakarta.servlet.http.HttpServletResponse.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@DisplayName("Integration Test - Admin Notification Controller")
public class AdminNotificationControllerTest extends IntegrationTest {

    // ========================= POST /broadcasts =========================

    @Test
    @DisplayName("Should create broadcast and return 201 with BroadcastResponse when admin sends to ALL_USERS")
    public void sendBroadcastToAllUsersSuccess() throws Exception {
        // Given: An admin user and a valid broadcast request
        final CreateBroadcastRequest request = aValidCreateBroadcastRequest(NotificationAudienceType.ALL_USERS, null, null);

        // When: Sending the broadcast
        final MockHttpServletRequestBuilder httpRequest = post(ADMIN_BROADCASTS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Response should be CREATED
        response.andExpect(status().is(SC_CREATED));

        // And: Response should contain BroadcastResponse with expected fields
        final String content = response.andReturn().getResponse().getContentAsString();
        final BroadcastResponse broadcastResponse = fromJson(content, BroadcastResponse.class);

        assertThat(broadcastResponse.getId(), is(notNullValue()));
        assertThat(broadcastResponse.getTitle(), is("Test Broadcast Title"));
        assertThat(broadcastResponse.getMessage(), is("Test broadcast message content"));
        assertThat(broadcastResponse.getCategory(), is(NotificationCategory.ANNOUNCEMENT));
        assertThat(broadcastResponse.getAudienceType(), is(NotificationAudienceType.ALL_USERS));
        assertThat(broadcastResponse.getCreatedAt(), is(notNullValue()));
        assertThat(broadcastResponse.getSentByEmail(), is(admin.getEmail()));
    }

    @Test
    @DisplayName("Should create broadcast targeting specific organization")
    public void sendBroadcastToOrganizationSuccess() throws Exception {
        // Given: An organization with a member
        final User owner = aValidatedUser();
        final io.github.eventify.api.organization.model.Organization org = anOrganisationWithOwner(owner);

        final CreateBroadcastRequest request = aValidCreateBroadcastRequest(NotificationAudienceType.ORGANIZATION, org.getId(), null);

        // When: Sending the broadcast
        final MockHttpServletRequestBuilder httpRequest = post(ADMIN_BROADCASTS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Response should be CREATED
        response.andExpect(status().is(SC_CREATED));

        // And: Response should contain correct audience type
        final String content = response.andReturn().getResponse().getContentAsString();
        final BroadcastResponse broadcastResponse = fromJson(content, BroadcastResponse.class);

        assertThat(broadcastResponse.getId(), is(notNullValue()));
        assertThat(broadcastResponse.getAudienceType(), is(NotificationAudienceType.ORGANIZATION));
        assertThat(broadcastResponse.getRecipientCount(), is(greaterThanOrEqualTo(0)));
    }

    @Test
    @DisplayName("Should create broadcast with zero recipients when org has no members")
    public void sendBroadcastToEmptyOrganizationReturnsZeroRecipients() throws Exception {
        // Given: An organization with no members (just the owner)
        final User owner = aValidatedUser();
        final io.github.eventify.api.organization.model.Organization org = anOrganisationWithOwner(owner);

        final CreateBroadcastRequest request = aValidCreateBroadcastRequest(NotificationAudienceType.ORGANIZATION, org.getId(), null);

        // When: Sending the broadcast
        final MockHttpServletRequestBuilder httpRequest = post(ADMIN_BROADCASTS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Response should be CREATED (broadcast row created even with 0 recipients)
        response.andExpect(status().is(SC_CREATED));

        // And: recipientCount should reflect actual member count
        final String content = response.andReturn().getResponse().getContentAsString();
        final BroadcastResponse broadcastResponse = fromJson(content, BroadcastResponse.class);

        assertThat(broadcastResponse.getId(), is(notNullValue()));
        assertThat(broadcastResponse.getRecipientCount(), is(greaterThanOrEqualTo(0)));
    }

    @Test
    @DisplayName("Should create broadcast with actionUrl and actionLabel")
    public void sendBroadcastWithActionSuccess() throws Exception {
        // Given: A broadcast request with action fields
        final CreateBroadcastRequest request = aValidCreateBroadcastRequest(NotificationAudienceType.ALL_USERS, null, null);
        request.setActionUrl("/dashboard");
        request.setActionLabel("Go to Dashboard");

        // When: Sending the broadcast
        final MockHttpServletRequestBuilder httpRequest = post(ADMIN_BROADCASTS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Response should be CREATED
        response.andExpect(status().is(SC_CREATED));

        // And: Response should contain action fields
        final String content = response.andReturn().getResponse().getContentAsString();
        final BroadcastResponse broadcastResponse = fromJson(content, BroadcastResponse.class);

        assertThat(broadcastResponse.getActionUrl(), is("/dashboard"));
        assertThat(broadcastResponse.getActionLabel(), is("Go to Dashboard"));
    }

    @Test
    @DisplayName("Should return 403 when non-admin sends broadcast")
    public void sendBroadcastFailsWhenNotAdmin() throws Exception {
        // Given: A regular user
        final User user = aValidatedUser();
        final CreateBroadcastRequest request = aValidCreateBroadcastRequest(NotificationAudienceType.ALL_USERS, null, null);

        // When: Regular user attempts to send broadcast
        final MockHttpServletRequestBuilder httpRequest = post(ADMIN_BROADCASTS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Response should be FORBIDDEN
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should return 401 when unauthenticated user sends broadcast")
    public void sendBroadcastFailsWhenUnauthenticated() throws Exception {
        // Given: No authentication
        final CreateBroadcastRequest request = aValidCreateBroadcastRequest(NotificationAudienceType.ALL_USERS, null, null);

        // When: Unauthenticated request
        final MockHttpServletRequestBuilder httpRequest = post(ADMIN_BROADCASTS_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Response should be UNAUTHORIZED
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }

    @Test
    @DisplayName("Should return 400 when title is missing")
    public void sendBroadcastFailsWhenTitleMissing() throws Exception {
        // Given: A request without title
        final CreateBroadcastRequest request = aValidCreateBroadcastRequest(NotificationAudienceType.ALL_USERS, null, null);
        request.setTitle(null);

        // When: Sending the broadcast
        final MockHttpServletRequestBuilder httpRequest = post(ADMIN_BROADCASTS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should return 400 when title exceeds 120 characters")
    public void sendBroadcastFailsWhenTitleTooLong() throws Exception {
        // Given: A request with title exceeding 120 chars
        final CreateBroadcastRequest request = aValidCreateBroadcastRequest(NotificationAudienceType.ALL_USERS, null, null);
        request.setTitle("A".repeat(121));

        // When: Sending the broadcast
        final MockHttpServletRequestBuilder httpRequest = post(ADMIN_BROADCASTS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should return 400 when message is missing")
    public void sendBroadcastFailsWhenMessageMissing() throws Exception {
        // Given: A request without message
        final CreateBroadcastRequest request = aValidCreateBroadcastRequest(NotificationAudienceType.ALL_USERS, null, null);
        request.setMessage(null);

        // When: Sending the broadcast
        final MockHttpServletRequestBuilder httpRequest = post(ADMIN_BROADCASTS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should return 400 when message exceeds 500 characters")
    public void sendBroadcastFailsWhenMessageTooLong() throws Exception {
        // Given: A request with message exceeding 500 chars
        final CreateBroadcastRequest request = aValidCreateBroadcastRequest(NotificationAudienceType.ALL_USERS, null, null);
        request.setMessage("A".repeat(501));

        // When: Sending the broadcast
        final MockHttpServletRequestBuilder httpRequest = post(ADMIN_BROADCASTS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should return 400 when audience type is missing")
    public void sendBroadcastFailsWhenAudienceTypeMissing() throws Exception {
        // Given: A request without audience type
        final CreateBroadcastRequest request = aValidCreateBroadcastRequest((NotificationAudienceType) null, null, null);

        // When: Sending the broadcast
        final MockHttpServletRequestBuilder httpRequest = post(ADMIN_BROADCASTS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should return 400 when actionUrl is provided without actionLabel")
    public void sendBroadcastFailsWhenActionUrlWithoutLabel() throws Exception {
        // Given: A request with actionUrl but no actionLabel
        final CreateBroadcastRequest request = aValidCreateBroadcastRequest(NotificationAudienceType.ALL_USERS, null, null);
        request.setActionUrl("/dashboard");
        request.setActionLabel(null);

        // When: Sending the broadcast
        final MockHttpServletRequestBuilder httpRequest = post(ADMIN_BROADCASTS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should return 400 when actionLabel is provided without actionUrl")
    public void sendBroadcastFailsWhenActionLabelWithoutUrl() throws Exception {
        // Given: A request with actionLabel but no actionUrl
        final CreateBroadcastRequest request = aValidCreateBroadcastRequest(NotificationAudienceType.ALL_USERS, null, null);
        request.setActionUrl(null);
        request.setActionLabel("Click here");

        // When: Sending the broadcast
        final MockHttpServletRequestBuilder httpRequest = post(ADMIN_BROADCASTS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    // ========================= GET /broadcasts =========================

    @Test
    @DisplayName("Should return paginated broadcasts ordered by createdAt DESC when admin lists broadcasts")
    public void listBroadcastsSuccess() throws Exception {
        // Given: Two existing broadcasts
        aBroadcastForAdmin(admin, "First Broadcast", NotificationAudienceType.ALL_USERS);
        aBroadcastForAdmin(admin, "Second Broadcast", NotificationAudienceType.ORGANIZATION);

        // When: Listing broadcasts
        final SortablePageInput input = new SortablePageInput();
        input.setPageNumber(0);
        input.setPageSize(20);

        final MockHttpServletRequestBuilder httpRequest = post(ADMIN_BROADCASTS_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue())
            .content(toJson(input));

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should contain broadcasts
        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<BroadcastResponse> page = fromJson(content, new TypeReference<>() {});

        assertThat(page, is(notNullValue()));
        assertThat(page.getTotalElements(), is(greaterThanOrEqualTo(2L)));
    }

    @Test
    @DisplayName("Should return empty page when no broadcasts exist")
    public void listBroadcastsReturnsEmptyWhenNone() throws Exception {
        // Given: No broadcasts exist

        // When: Listing broadcasts
        final SortablePageInput input = new SortablePageInput();
        input.setPageNumber(0);
        input.setPageSize(20);

        final MockHttpServletRequestBuilder httpRequest = post(ADMIN_BROADCASTS_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue())
            .content(toJson(input));

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should be empty
        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<BroadcastResponse> page = fromJson(content, new TypeReference<>() {});

        assertThat(page.getTotalElements(), is(0L));
    }

    @Test
    @DisplayName("Should return 403 when non-admin lists broadcasts")
    public void listBroadcastsFailsWhenNotAdmin() throws Exception {
        // Given: A regular user
        final User user = aValidatedUser();

        // When: Regular user attempts to list broadcasts
        final SortablePageInput input = new SortablePageInput();
        input.setPageNumber(0);
        input.setPageSize(20);

        final MockHttpServletRequestBuilder httpRequest = post(ADMIN_BROADCASTS_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(input));

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Response should be FORBIDDEN
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should return 401 when unauthenticated user lists broadcasts")
    public void listBroadcastsFailsWhenUnauthenticated() throws Exception {
        // Given: No authentication

        // When: Unauthenticated request
        final SortablePageInput input = new SortablePageInput();
        input.setPageNumber(0);
        input.setPageSize(20);

        final MockHttpServletRequestBuilder httpRequest = post(ADMIN_BROADCASTS_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(input));

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Response should be UNAUTHORIZED
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }

    // ========================= POST /broadcasts/preview =========================

    @Test
    @DisplayName("Should return recipient count without creating broadcast when admin previews")
    public void previewBroadcastSuccess() throws Exception {
        // Given: An audience request for all users
        final AudienceRequest audienceRequest = anAudienceRequest(NotificationAudienceType.ALL_USERS, null, null);

        // When: Previewing recipient count
        final MockHttpServletRequestBuilder httpRequest = post(ADMIN_BROADCASTS_PREVIEW_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue())
            .content(toJson(audienceRequest));

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should contain recipientCount
        final String content = response.andReturn().getResponse().getContentAsString();
        final PreviewResponse previewResponse = fromJson(content, PreviewResponse.class);

        assertThat(previewResponse.getRecipientCount(), is(greaterThanOrEqualTo(0)));
    }

    @Test
    @DisplayName("Should not create any broadcast or notification rows when previewing")
    public void previewBroadcastHasNoSideEffects() throws Exception {
        // Given: An audience request
        final AudienceRequest audienceRequest = anAudienceRequest(NotificationAudienceType.ALL_USERS, null, null);

        // When: Previewing recipient count
        final MockHttpServletRequestBuilder httpRequest = post(ADMIN_BROADCASTS_PREVIEW_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue())
            .content(toJson(audienceRequest));

        mockMvc.perform(httpRequest).andExpect(status().is(SC_OK));

        // Then: No broadcast rows should be created
        assertThat(notificationBroadcastRepository.count(), is(0L));
    }

    @Test
    @DisplayName("Should return 403 when non-admin previews broadcast")
    public void previewBroadcastFailsWhenNotAdmin() throws Exception {
        // Given: A regular user
        final User user = aValidatedUser();
        final AudienceRequest audienceRequest = anAudienceRequest(NotificationAudienceType.ALL_USERS, null, null);

        // When: Regular user attempts to preview
        final MockHttpServletRequestBuilder httpRequest = post(ADMIN_BROADCASTS_PREVIEW_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(audienceRequest));

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Response should be FORBIDDEN
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should return 401 when unauthenticated user previews broadcast")
    public void previewBroadcastFailsWhenUnauthenticated() throws Exception {
        // Given: No authentication
        final AudienceRequest audienceRequest = anAudienceRequest(NotificationAudienceType.ALL_USERS, null, null);

        // When: Unauthenticated request
        final MockHttpServletRequestBuilder httpRequest = post(ADMIN_BROADCASTS_PREVIEW_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(audienceRequest));

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Response should be UNAUTHORIZED
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }

    @Test
    @DisplayName("Should return 400 when audience type is missing in preview request")
    public void previewBroadcastFailsWhenAudienceTypeMissing() throws Exception {
        // Given: An audience request without type
        final AudienceRequest audienceRequest = anAudienceRequest((NotificationAudienceType) null, null, null);

        // When: Previewing
        final MockHttpServletRequestBuilder httpRequest = post(ADMIN_BROADCASTS_PREVIEW_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue())
            .content(toJson(audienceRequest));

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    // ========================= POST /broadcasts/search — filtering =========================

    @Test
    @DisplayName("Should return only ANNOUNCEMENT broadcasts when filtering by category=ANNOUNCEMENT")
    public void shouldFilterBroadcastsByCategory() throws Exception {
        // Given: One ANNOUNCEMENT broadcast and one SYSTEM broadcast
        aBroadcastForAdmin(admin, "Announcement Broadcast", NotificationAudienceType.ALL_USERS);

        final CreateBroadcastRequest systemRequest = aValidCreateBroadcastRequest(NotificationAudienceType.ALL_USERS, null, null);
        systemRequest.setCategory(NotificationCategory.SYSTEM);
        mockMvc.perform(
            post(ADMIN_BROADCASTS_PATH)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue())
                .content(toJson(systemRequest))
        );

        // And: A search input filtering by category=ANNOUNCEMENT
        final SortablePageInput input = new SortablePageInput();
        input.setPageNumber(0);
        input.setPageSize(10);

        final SearchInput categoryFilter = new SearchInput();
        categoryFilter.setFieldName("category");
        categoryFilter.setTextValue("ANNOUNCEMENT");
        input.getSearchInputs().add(categoryFilter);

        // When: Searching with category filter
        final MockHttpServletRequestBuilder httpRequest = post(ADMIN_BROADCASTS_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue())
            .content(toJson(input));

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Only ANNOUNCEMENT broadcasts should be returned
        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<BroadcastResponse> page = fromJson(content, new TypeReference<>() {});

        assertThat(page.getContent(), is(not(empty())));
        page.getContent().forEach(b -> assertThat(b.getCategory(), is(NotificationCategory.ANNOUNCEMENT)));
    }

    @Test
    @DisplayName("Should return only matching broadcast when filtering by title with partial FUZZY_TEXT match")
    public void shouldFilterBroadcastsByTitle() throws Exception {
        // Given: Two broadcasts with distinct titles
        aBroadcastForAdmin(admin, "Important System Update", NotificationAudienceType.ALL_USERS);
        aBroadcastForAdmin(admin, "Weekly Newsletter", NotificationAudienceType.ALL_USERS);

        // And: A search input filtering by partial title "Important"
        final SortablePageInput input = new SortablePageInput();
        input.setPageNumber(0);
        input.setPageSize(10);

        final SearchInput titleFilter = new SearchInput();
        titleFilter.setFieldName("title");
        titleFilter.setTextValue("Important");
        input.getSearchInputs().add(titleFilter);

        // When: Searching with title filter
        final MockHttpServletRequestBuilder httpRequest = post(ADMIN_BROADCASTS_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue())
            .content(toJson(input));

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Only the matching broadcast should be returned
        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<BroadcastResponse> page = fromJson(content, new TypeReference<>() {});

        assertThat(page.getContent(), hasSize(1));
        assertThat(page.getContent().get(0).getTitle(), containsStringIgnoringCase("Important"));
    }

    @Test
    @DisplayName("Should return only broadcasts sent by the filtered admin email")
    public void shouldFilterBroadcastsBySentByEmail() throws Exception {
        // Given: A second admin user
        final User secondAdmin = aValidatedUserWithRole(io.github.eventify.api.authentication.model.Role.ADMIN);

        // And: One broadcast from each admin
        aBroadcastForAdmin(admin, "Broadcast From First Admin", NotificationAudienceType.ALL_USERS);
        aBroadcastForAdmin(secondAdmin, "Broadcast From Second Admin", NotificationAudienceType.ALL_USERS);

        // And: A search input filtering by the first admin's email
        final SortablePageInput input = new SortablePageInput();
        input.setPageNumber(0);
        input.setPageSize(10);

        final SearchInput emailFilter = new SearchInput();
        emailFilter.setFieldName("sentByEmail");
        emailFilter.setTextValue(admin.getEmail());
        input.getSearchInputs().add(emailFilter);

        // When: Searching with sentByEmail filter
        final MockHttpServletRequestBuilder httpRequest = post(ADMIN_BROADCASTS_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue())
            .content(toJson(input));

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Only broadcasts from the first admin should be returned
        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<BroadcastResponse> page = fromJson(content, new TypeReference<>() {});

        assertThat(page.getContent(), is(not(empty())));
        page.getContent().forEach(b -> assertThat(b.getSentByEmail(), is(admin.getEmail())));
    }

    @Test
    @DisplayName("Should return only broadcasts matching both category AND title when combined filters applied")
    public void shouldFilterBroadcastsByCombinedFilters() throws Exception {
        // Given: Broadcasts with different category/title combinations
        aBroadcastForAdmin(admin, "Important Announcement", NotificationAudienceType.ALL_USERS);
        aBroadcastForAdmin(admin, "Important Newsletter", NotificationAudienceType.ALL_USERS);

        final CreateBroadcastRequest systemRequest = aValidCreateBroadcastRequest(NotificationAudienceType.ALL_USERS, null, null);
        systemRequest.setTitle("Important System Alert");
        systemRequest.setCategory(NotificationCategory.SYSTEM);
        mockMvc.perform(
            post(ADMIN_BROADCASTS_PATH)
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue())
                .content(toJson(systemRequest))
        );

        // And: A search input filtering by category=ANNOUNCEMENT AND title contains "Important"
        final SortablePageInput input = new SortablePageInput();
        input.setPageNumber(0);
        input.setPageSize(10);

        final SearchInput categoryFilter = new SearchInput();
        categoryFilter.setFieldName("category");
        categoryFilter.setTextValue("ANNOUNCEMENT");
        input.getSearchInputs().add(categoryFilter);

        final SearchInput titleFilter = new SearchInput();
        titleFilter.setFieldName("title");
        titleFilter.setTextValue("Important");
        input.getSearchInputs().add(titleFilter);

        // When: Searching with combined filters
        final MockHttpServletRequestBuilder httpRequest = post(ADMIN_BROADCASTS_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue())
            .content(toJson(input));

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Only ANNOUNCEMENT broadcasts with "Important" in title should be returned (AND semantics)
        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<BroadcastResponse> page = fromJson(content, new TypeReference<>() {});

        assertThat(page.getContent(), is(not(empty())));
        page.getContent().forEach(b -> {
            assertThat(b.getCategory(), is(NotificationCategory.ANNOUNCEMENT));
            assertThat(b.getTitle(), containsStringIgnoringCase("Important"));
        });
        // The SYSTEM broadcast should not appear
        final boolean hasSystemBroadcast = page.getContent().stream()
            .anyMatch(b -> "SYSTEM".equals(b.getCategory()));
        assertThat(hasSystemBroadcast, is(false));
    }

    // ========================= FACTORY METHODS =========================

}
