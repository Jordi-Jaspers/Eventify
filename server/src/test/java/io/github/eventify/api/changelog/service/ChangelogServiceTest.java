package io.github.eventify.api.changelog.service;

import io.github.eventify.api.changelog.model.ChangelogEntry;
import io.github.eventify.support.UnitTest;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Unit Test - Changelog Service")
public class ChangelogServiceTest extends UnitTest {

    private ObjectMapper objectMapper;
    private ResourceLoader resourceLoader;

    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapper();
        resourceLoader = new DefaultResourceLoader();
    }

    @Test
    @DisplayName("Should load changelog entries from classpath on startup")
    public void shouldLoadChangelogEntriesFromClasspathOnStartup() {
        // Given: A valid changelog.json on the classpath
        final ChangelogService service = new ChangelogService(objectMapper, resourceLoader);

        // When: Service initializes (PostConstruct loads entries)
        service.init();

        // Then: Entries should be loaded
        final List<ChangelogEntry> entries = service.getAll();
        assertThat(entries, is(notNullValue()));
        assertThat(entries, not(empty()));
    }

    @Test
    @DisplayName("Should return entries newest-first (sorted by version descending)")
    public void shouldReturnEntriesNewestFirst() {
        // Given: A service initialized with the test changelog.json
        final ChangelogService service = new ChangelogService(objectMapper, resourceLoader);
        service.init();

        // When: Getting all entries
        final List<ChangelogEntry> entries = service.getAll();

        // Then: Entries should be sorted newest-first
        assertThat(entries, hasSize(greaterThan(1)));
        assertThat(entries.get(0).version(), is(equalTo("1.2.0")));
        assertThat(entries.get(1).version(), is(equalTo("1.1.0")));
        assertThat(entries.get(2).version(), is(equalTo("1.0.0")));
    }

    @Test
    @DisplayName("Should return entry by version when it exists")
    public void shouldReturnEntryByVersionWhenItExists() {
        // Given: A service initialized with the test changelog.json
        final ChangelogService service = new ChangelogService(objectMapper, resourceLoader);
        service.init();

        // When: Getting entry by known version
        final Optional<ChangelogEntry> result = service.getByVersion("1.1.0");

        // Then: Entry should be present
        assertThat(result.isPresent(), is(true));
        assertThat(result.get().version(), is(equalTo("1.1.0")));
        assertThat(result.get().date(), is(equalTo("2026-04-01")));
    }

    @Test
    @DisplayName("Should return empty Optional when version does not exist")
    public void shouldReturnEmptyOptionalWhenVersionDoesNotExist() {
        // Given: A service initialized with the test changelog.json
        final ChangelogService service = new ChangelogService(objectMapper, resourceLoader);
        service.init();

        // When: Getting entry by non-existent version
        final Optional<ChangelogEntry> result = service.getByVersion("99.99.99");

        // Then: Optional should be empty
        assertThat(result.isPresent(), is(false));
    }

    @Test
    @DisplayName("Should return empty list when changelog.json is empty array")
    public void shouldReturnEmptyListWhenChangelogJsonIsEmptyArray() {
        // Given: A resource loader that returns an empty JSON array
        final ResourceLoader emptyResourceLoader = new ResourceLoader() {

            @Override
            public Resource getResource(final String location) {
                return new ByteArrayResource("[]".getBytes());
            }

            @Override
            public ClassLoader getClassLoader() {
                return getClass().getClassLoader();
            }
        };
        final ChangelogService service = new ChangelogService(objectMapper, emptyResourceLoader);

        // When: Service initializes
        service.init();

        // Then: getAll() should return empty list
        final List<ChangelogEntry> entries = service.getAll();
        assertThat(entries, is(empty()));
    }

    @Test
    @DisplayName("Should throw exception on startup when changelog.json is malformed")
    public void shouldThrowExceptionOnStartupWhenChangelogJsonIsMalformed() {
        // Given: A resource loader that returns malformed JSON
        final ResourceLoader malformedResourceLoader = new ResourceLoader() {

            @Override
            public Resource getResource(final String location) {
                return new ByteArrayResource("{ not valid json [".getBytes());
            }

            @Override
            public ClassLoader getClassLoader() {
                return getClass().getClassLoader();
            }
        };
        final ChangelogService service = new ChangelogService(objectMapper, malformedResourceLoader);

        // When & Then: init() should throw (fail-fast on bad JSON)
        assertThrows(RuntimeException.class, service::init);
    }

    @Test
    @DisplayName("Should cache entries after initial load")
    public void shouldCacheEntriesAfterInitialLoad() {
        // Given: A service initialized with the test changelog.json
        final ChangelogService service = new ChangelogService(objectMapper, resourceLoader);
        service.init();

        // When: Getting all entries twice
        final List<ChangelogEntry> firstCall = service.getAll();
        final List<ChangelogEntry> secondCall = service.getAll();

        // Then: Both calls should return the same list instance (cached)
        assertThat(firstCall, is(sameInstance(secondCall)));
    }

    @Test
    @DisplayName("Should return entry with all fields populated")
    public void shouldReturnEntryWithAllFieldsPopulated() {
        // Given: A service initialized with the test changelog.json
        final ChangelogService service = new ChangelogService(objectMapper, resourceLoader);
        service.init();

        // When: Getting entry for version 1.2.0
        final Optional<ChangelogEntry> result = service.getByVersion("1.2.0");

        // Then: Entry should have all fields
        assertThat(result.isPresent(), is(true));
        final ChangelogEntry entry = result.get();
        assertThat(entry.version(), is(equalTo("1.2.0")));
        assertThat(entry.date(), is(notNullValue()));
        assertThat(entry.features(), is(notNullValue()));
        assertThat(entry.improvements(), is(notNullValue()));
        assertThat(entry.fixes(), is(notNullValue()));
    }
}
