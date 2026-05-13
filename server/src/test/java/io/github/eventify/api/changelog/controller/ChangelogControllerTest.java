package io.github.eventify.api.changelog.controller;

import io.github.eventify.api.changelog.model.ChangelogEntry;
import io.github.eventify.support.IntegrationTest;
import tools.jackson.core.type.TypeReference;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static io.github.eventify.api.Paths.PUBLIC_CHANGELOG_PATH;
import static io.github.eventify.api.Paths.PUBLIC_CHANGELOG_VERSION_PATH;
import static io.github.jframe.util.mapper.ObjectMappers.fromJson;
import static jakarta.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static jakarta.servlet.http.HttpServletResponse.SC_OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Integration Test - Changelog Controller")
public class ChangelogControllerTest extends IntegrationTest {

    @Test
    @DisplayName("Should return all changelog entries as JSON array")
    public void getAllChangelogEntriesSuccess() throws Exception {
        // Given: No authentication required (public endpoint)

        // When: Requesting all changelog entries
        final MockHttpServletRequestBuilder request = get(PUBLIC_CHANGELOG_PATH)
            .accept(APPLICATION_JSON);

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should be a non-empty JSON array
        final String content = response.andReturn().getResponse().getContentAsString();
        final List<ChangelogEntry> entries = objectMapper.readValue(content, new TypeReference<List<ChangelogEntry>>() {});

        assertThat(entries, is(notNullValue()));
        assertThat(entries, not(empty()));
    }

    @Test
    @DisplayName("Should return entries sorted newest-first")
    public void getAllChangelogEntriesNewestFirst() throws Exception {
        // Given: No authentication required (public endpoint)

        // When: Requesting all changelog entries
        final MockHttpServletRequestBuilder request = get(PUBLIC_CHANGELOG_PATH)
            .accept(APPLICATION_JSON);

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: First entry should be the newest version
        final String content = response.andReturn().getResponse().getContentAsString();
        final List<ChangelogEntry> entries = objectMapper.readValue(content, new TypeReference<List<ChangelogEntry>>() {});

        assertThat(entries, hasSize(greaterThan(1)));
        assertThat(entries.get(0).version(), is(equalTo("1.2.0")));
    }

    @Test
    @DisplayName("Should return all entries with required fields")
    public void getAllChangelogEntriesHaveRequiredFields() throws Exception {
        // Given: No authentication required (public endpoint)

        // When: Requesting all changelog entries
        final MockHttpServletRequestBuilder request = get(PUBLIC_CHANGELOG_PATH)
            .accept(APPLICATION_JSON);

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Each entry should have required fields
        final String content = response.andReturn().getResponse().getContentAsString();
        final List<ChangelogEntry> entries = objectMapper.readValue(content, new TypeReference<List<ChangelogEntry>>() {});

        for (final ChangelogEntry entry : entries) {
            assertThat(entry.version(), is(notNullValue()));
            assertThat(entry.date(), is(notNullValue()));
            assertThat(entry.features(), is(notNullValue()));
            assertThat(entry.improvements(), is(notNullValue()));
            assertThat(entry.fixes(), is(notNullValue()));
        }
    }

    @Test
    @DisplayName("Should return changelog entry by version when it exists")
    public void getChangelogByVersionSuccess() throws Exception {
        // Given: No authentication required (public endpoint)

        // When: Requesting a specific version
        final MockHttpServletRequestBuilder request = get(PUBLIC_CHANGELOG_VERSION_PATH, "1.1.0")
            .accept(APPLICATION_JSON);

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should contain the correct entry
        final String content = response.andReturn().getResponse().getContentAsString();
        final ChangelogEntry entry = fromJson(content, ChangelogEntry.class);

        assertThat(entry.version(), is(equalTo("1.1.0")));
        assertThat(entry.date(), is(equalTo("2026-04-01")));
    }

    @Test
    @DisplayName("Should return 404 when version does not exist")
    public void getChangelogByVersionNotFound() throws Exception {
        // Given: No authentication required (public endpoint)

        // When: Requesting a non-existent version
        final MockHttpServletRequestBuilder request = get(PUBLIC_CHANGELOG_VERSION_PATH, "99.99.99")
            .accept(APPLICATION_JSON);

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be 404
        response.andExpect(status().is(SC_NOT_FOUND));
    }

    @Test
    @DisplayName("Should return all entries without authentication")
    public void getAllChangelogEntriesWithoutAuthentication() throws Exception {
        // Given: No Authorization header provided

        // When: Requesting changelog without auth
        final MockHttpServletRequestBuilder request = get(PUBLIC_CHANGELOG_PATH)
            .accept(APPLICATION_JSON);

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK (no auth required for public endpoint)
        response.andExpect(status().is(SC_OK));
    }

    @Test
    @DisplayName("Should return single entry without authentication")
    public void getChangelogByVersionWithoutAuthentication() throws Exception {
        // Given: No Authorization header provided

        // When: Requesting a specific version without auth
        final MockHttpServletRequestBuilder request = get(PUBLIC_CHANGELOG_VERSION_PATH, "1.0.0")
            .accept(APPLICATION_JSON);

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK (no auth required for public endpoint)
        response.andExpect(status().is(SC_OK));

        // And: Response should contain the correct entry
        final String content = response.andReturn().getResponse().getContentAsString();
        final ChangelogEntry entry = fromJson(content, ChangelogEntry.class);

        assertThat(entry.version(), is(equalTo("1.0.0")));
    }
}
