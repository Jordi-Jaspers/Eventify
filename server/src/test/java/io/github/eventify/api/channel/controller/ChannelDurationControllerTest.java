package io.github.eventify.api.channel.controller;

import io.github.eventify.api.channel.model.Channel;
import io.github.eventify.api.event.model.Severity;
import io.github.eventify.api.monitor.model.DurationDirection;
import io.github.eventify.api.monitor.model.TimelineDuration;
import io.github.eventify.api.monitor.model.request.DurationDetailsRequest;
import io.github.eventify.api.monitor.model.response.DurationDetailsResponse;
import io.github.eventify.api.organization.model.Organization;
import io.github.eventify.api.organization.model.OrganizationalRole;
import io.github.eventify.api.user.model.User;
import io.github.eventify.support.IntegrationTest;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static io.github.eventify.api.Paths.*;
import static io.github.eventify.common.constant.Constants.Security.BEARER;
import static io.github.jframe.util.mapper.ObjectMappers.fromJson;
import static io.github.jframe.util.mapper.ObjectMappers.toJson;
import static jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static jakarta.servlet.http.HttpServletResponse.SC_OK;
import static java.time.ZoneOffset.UTC;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Integration Test - Channel Duration Endpoints")
public class ChannelDurationControllerTest extends IntegrationTest {

    @Test
    @DisplayName("Should return duration details for user channel")
    public void getDurationsForUserChannelSuccess() throws Exception {
        // Given: A user with a channel and events
        final User user = aValidatedUser();
        final Channel channel = aChannelForUser(user, "Test Channel");

        final OffsetDateTime t1 = OffsetDateTime.of(2026, 2, 12, 9, 50, 0, 0, UTC);
        final OffsetDateTime t2 = OffsetDateTime.of(2026, 2, 12, 10, 20, 0, 0, UTC);
        final OffsetDateTime t3 = OffsetDateTime.of(2026, 2, 12, 10, 45, 0, 0, UTC);

        anEventForChannel(channel, Severity.OK, t1);
        anEventForChannel(channel, Severity.CRITICAL, t2);
        anEventForChannel(channel, Severity.OK, t3);

        // When: Requesting durations around CRITICAL event
        final DurationDetailsRequest request = new DurationDetailsRequest();
        request.setTimestamp(t2);
        request.setDirection(DurationDirection.AROUND);

        final MockHttpServletRequestBuilder httpRequest = post(USER_CHANNEL_DURATIONS_PATH, channel.getId())
            .content(toJson(request))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Should return 3 durations with selectedIndex=1
        final String content = response.andReturn().getResponse().getContentAsString();
        final DurationDetailsResponse details = fromJson(content, DurationDetailsResponse.class);

        assertThat(details.getDurations(), hasSize(3));
        assertThat(details.getSelectedIndex(), is(1));

        // And: Selected duration should be CRITICAL
        final TimelineDuration selected = details.getDurations().get(details.getSelectedIndex());
        assertThat(selected.getSeverity(), is(Severity.CRITICAL));
        assertThat(selected.getStartTime(), is(t2));
        assertThat(selected.getEndTime(), is(t3));
    }

    @Test
    @DisplayName("Should cut off duration that extends before window")
    public void getDurationsWithCutoffSuccess() throws Exception {
        // Given: A channel with very old OK duration
        final User user = aValidatedUser();
        final Channel channel = aChannelForUser(user, "Test Channel");

        final OffsetDateTime veryOld = OffsetDateTime.of(2026, 2, 12, 6, 0, 0, 0, UTC);
        final OffsetDateTime recent = OffsetDateTime.of(2026, 2, 12, 10, 20, 0, 0, UTC);

        anEventForChannel(channel, Severity.OK, veryOld);
        anEventForChannel(channel, Severity.CRITICAL, recent);

        // When: Requesting durations around recent event
        final DurationDetailsRequest request = new DurationDetailsRequest();
        request.setTimestamp(recent);
        request.setDirection(DurationDirection.AROUND);

        final MockHttpServletRequestBuilder httpRequest = post(USER_CHANNEL_DURATIONS_PATH, channel.getId())
            .content(toJson(request))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: First duration should be cut off
        final String content = response.andReturn().getResponse().getContentAsString();
        final DurationDetailsResponse details = fromJson(content, DurationDetailsResponse.class);

        final TimelineDuration first = details.getDurations().get(0);
        assertThat(first.getSeverity(), is(Severity.OK));
        assertThat(first.getStartTime(), not(equalTo(veryOld)));
    }

    @Test
    @DisplayName("Should handle rapid flapping with multiple small durations")
    public void getDurationsWithFlappingSuccess() throws Exception {
        // Given: Channel with rapid severity changes
        final User user = aValidatedUser();
        final Channel channel = aChannelForUser(user, "Flapping Channel");

        final OffsetDateTime base = OffsetDateTime.of(2026, 2, 12, 10, 0, 0, 0, UTC);

        anEventForChannel(channel, Severity.OK, base);
        anEventForChannel(channel, Severity.CRITICAL, base.plusMinutes(2));
        anEventForChannel(channel, Severity.OK, base.plusMinutes(4));
        anEventForChannel(channel, Severity.WARNING, base.plusMinutes(6));
        anEventForChannel(channel, Severity.CRITICAL, base.plusMinutes(8));
        anEventForChannel(channel, Severity.OK, base.plusMinutes(10));

        // When: Requesting durations around middle of flapping
        final DurationDetailsRequest request = new DurationDetailsRequest();
        request.setTimestamp(base.plusMinutes(6));
        request.setDirection(DurationDirection.AROUND);

        final MockHttpServletRequestBuilder httpRequest = post(USER_CHANNEL_DURATIONS_PATH, channel.getId())
            .content(toJson(request))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Should return enough durations to fill window
        final String content = response.andReturn().getResponse().getContentAsString();
        final DurationDetailsResponse details = fromJson(content, DurationDetailsResponse.class);

        assertThat(details.getDurations(), hasSize(greaterThan(3)));
    }

    @Test
    @DisplayName("Should handle very long previous duration")
    public void getDurationsWithVeryLongPreviousDurationSuccess() throws Exception {
        // Given: 41-day OK period before CRITICAL
        final User user = aValidatedUser();
        final Channel channel = aChannelForUser(user, "Test Channel");

        final OffsetDateTime longAgo = OffsetDateTime.of(2026, 1, 1, 0, 0, 0, 0, UTC);
        final OffsetDateTime now = OffsetDateTime.of(2026, 2, 12, 10, 20, 0, 0, UTC);

        anEventForChannel(channel, Severity.OK, longAgo);
        anEventForChannel(channel, Severity.CRITICAL, now);

        // When: Requesting durations around recent event
        final DurationDetailsRequest request = new DurationDetailsRequest();
        request.setTimestamp(now);
        request.setDirection(DurationDirection.AROUND);

        final MockHttpServletRequestBuilder httpRequest = post(USER_CHANNEL_DURATIONS_PATH, channel.getId())
            .content(toJson(request))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Duration should be cut off appropriately
        final String content = response.andReturn().getResponse().getContentAsString();
        final DurationDetailsResponse details = fromJson(content, DurationDetailsResponse.class);

        assertThat(details.getDurations(), hasSize(2));
    }

    @Test
    @DisplayName("Should mark first duration with hasPrevious false")
    public void getFirstDurationSuccess() throws Exception {
        // Given: Channel's first event
        final User user = aValidatedUser();
        final Channel channel = aChannelForUser(user, "New Channel");

        final OffsetDateTime firstEvent = OffsetDateTime.of(2026, 2, 12, 10, 0, 0, 0, UTC);
        anEventForChannel(channel, Severity.OK, firstEvent);

        // When: Requesting durations around first event
        final DurationDetailsRequest request = new DurationDetailsRequest();
        request.setTimestamp(firstEvent);
        request.setDirection(DurationDirection.AROUND);

        final MockHttpServletRequestBuilder httpRequest = post(USER_CHANNEL_DURATIONS_PATH, channel.getId())
            .content(toJson(request))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: hasPrevious should be false
        final String content = response.andReturn().getResponse().getContentAsString();
        final DurationDetailsResponse details = fromJson(content, DurationDetailsResponse.class);

        assertThat(details.isHasPrevious(), is(false));
    }

    @Test
    @DisplayName("Should mark live duration with null end time")
    public void getLiveDurationSuccess() throws Exception {
        // Given: Channel with ongoing duration
        final User user = aValidatedUser();
        final Channel channel = aChannelForUser(user, "Live Channel");

        final OffsetDateTime t1 = OffsetDateTime.of(2026, 2, 12, 10, 0, 0, 0, UTC);
        final OffsetDateTime t2 = OffsetDateTime.of(2026, 2, 12, 10, 30, 0, 0, UTC);

        anEventForChannel(channel, Severity.OK, t1);
        anEventForChannel(channel, Severity.CRITICAL, t2);

        // When: Requesting durations around latest event
        final DurationDetailsRequest request = new DurationDetailsRequest();
        request.setTimestamp(t2);
        request.setDirection(DurationDirection.AROUND);

        final MockHttpServletRequestBuilder httpRequest = post(USER_CHANNEL_DURATIONS_PATH, channel.getId())
            .content(toJson(request))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Last duration should have null endTime
        final String content = response.andReturn().getResponse().getContentAsString();
        final DurationDetailsResponse details = fromJson(content, DurationDetailsResponse.class);

        final TimelineDuration last = details.getDurations().get(details.getDurations().size() - 1);
        assertThat(last.getEndTime(), is(nullValue()));
    }

    @Test
    @DisplayName("Should fetch durations before timestamp")
    public void getDurationsBeforeSuccess() throws Exception {
        // Given: Channel with multiple durations
        final User user = aValidatedUser();
        final Channel channel = aChannelForUser(user, "Test Channel");

        final OffsetDateTime base = OffsetDateTime.of(2026, 2, 12, 10, 0, 0, 0, UTC);

        anEventForChannel(channel, Severity.OK, base.minusHours(2));
        anEventForChannel(channel, Severity.WARNING, base.minusHours(1));
        anEventForChannel(channel, Severity.CRITICAL, base);

        // When: Requesting durations before timestamp
        final DurationDetailsRequest request = new DurationDetailsRequest();
        request.setTimestamp(base);
        request.setDirection(DurationDirection.BEFORE);

        final MockHttpServletRequestBuilder httpRequest = post(USER_CHANNEL_DURATIONS_PATH, channel.getId())
            .content(toJson(request))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Should only return durations before timestamp
        final String content = response.andReturn().getResponse().getContentAsString();
        final DurationDetailsResponse details = fromJson(content, DurationDetailsResponse.class);

        assertThat(details.getDurations(), hasSize(2));
    }

    @Test
    @DisplayName("Should fetch durations after timestamp")
    public void getDurationsAfterSuccess() throws Exception {
        // Given: Channel with multiple durations
        final User user = aValidatedUser();
        final Channel channel = aChannelForUser(user, "Test Channel");

        final OffsetDateTime base = OffsetDateTime.of(2026, 2, 12, 10, 0, 0, 0, UTC);

        anEventForChannel(channel, Severity.CRITICAL, base);
        anEventForChannel(channel, Severity.WARNING, base.plusHours(1));
        anEventForChannel(channel, Severity.OK, base.plusHours(2));

        // When: Requesting durations after timestamp
        final DurationDetailsRequest request = new DurationDetailsRequest();
        request.setTimestamp(base);
        request.setDirection(DurationDirection.AFTER);

        final MockHttpServletRequestBuilder httpRequest = post(USER_CHANNEL_DURATIONS_PATH, channel.getId())
            .content(toJson(request))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Should only return durations after timestamp
        final String content = response.andReturn().getResponse().getContentAsString();
        final DurationDetailsResponse details = fromJson(content, DurationDetailsResponse.class);

        assertThat(details.getDurations(), hasSize(2));
    }

    @Test
    @DisplayName("Should reject request when user does not own channel")
    public void getDurationsUnauthorizedUserFails() throws Exception {
        // Given: Two users, channel owned by user1
        final User user1 = aValidatedUser();
        final User user2 = aValidatedUser();
        final Channel channel = aChannelForUser(user1, "User1 Channel");

        final OffsetDateTime timestamp = OffsetDateTime.of(2026, 2, 12, 10, 0, 0, 0, UTC);
        anEventForChannel(channel, Severity.OK, timestamp);

        // When: User2 attempts to access user1's channel
        final DurationDetailsRequest request = new DurationDetailsRequest();
        request.setTimestamp(timestamp);
        request.setDirection(DurationDirection.AROUND);

        final MockHttpServletRequestBuilder httpRequest = post(USER_CHANNEL_DURATIONS_PATH, channel.getId())
            .content(toJson(request))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user2.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Response should be FORBIDDEN
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should allow org member to access org channel")
    public void getDurationsForOrgChannelAsOrgMemberSuccess() throws Exception {
        // Given: Organization with owner and member
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);

        final User member = aValidatedUser();
        addMemberToOrganization(org, member, OrganizationalRole.MEMBER);

        final Channel channel = aChannelForOrganisation(owner, org, "Org Channel");

        final OffsetDateTime timestamp = OffsetDateTime.of(2026, 2, 12, 10, 0, 0, 0, UTC);
        anEventForChannel(channel, Severity.OK, timestamp);

        // When: Member accesses org channel
        final DurationDetailsRequest request = new DurationDetailsRequest();
        request.setTimestamp(timestamp);
        request.setDirection(DurationDirection.AROUND);

        final MockHttpServletRequestBuilder httpRequest = post(ORGANIZATION_CHANNEL_DURATIONS_PATH, org.getId(), channel.getId())
            .content(toJson(request))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + member.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Should return durations
        final String content = response.andReturn().getResponse().getContentAsString();
        final DurationDetailsResponse details = fromJson(content, DurationDetailsResponse.class);

        assertThat(details.getDurations(), not(empty()));
    }

    @Test
    @DisplayName("Should reject request when user not in organization")
    public void getDurationsForOrgChannelAsNonMemberFails() throws Exception {
        // Given: Organization with channel
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final Channel channel = aChannelForOrganisation(owner, org, "Org Channel");

        // And: Different user not in organization
        final User outsider = aValidatedUser();

        final OffsetDateTime timestamp = OffsetDateTime.of(2026, 2, 12, 10, 0, 0, 0, UTC);
        anEventForChannel(channel, Severity.OK, timestamp);

        // When: Outsider attempts to access org channel
        final DurationDetailsRequest request = new DurationDetailsRequest();
        request.setTimestamp(timestamp);
        request.setDirection(DurationDirection.AROUND);

        final MockHttpServletRequestBuilder httpRequest = post(ORGANIZATION_CHANNEL_DURATIONS_PATH, org.getId(), channel.getId())
            .content(toJson(request))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + outsider.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(httpRequest);

        // Then: Response should be FORBIDDEN
        response.andExpect(status().is(SC_FORBIDDEN));
    }
}
