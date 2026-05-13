package io.github.eventify.api.changelog.service;

import io.github.eventify.api.changelog.model.ChangelogEntry;
import io.github.eventify.common.util.ResourceLoaderUtil;
import lombok.RequiredArgsConstructor;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import jakarta.annotation.PostConstruct;

import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

/** Service that loads and caches changelog entries from classpath:changelog.json. */
@Service
@RequiredArgsConstructor
public class ChangelogService {

    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;
    private List<ChangelogEntry> entries;

    /** Loads changelog entries from classpath on startup, sorted newest-first. */
    @PostConstruct
    public void init() {
        final List<ChangelogEntry> loaded = objectMapper.readValue(
            ResourceLoaderUtil.getResourceAsString("changelog.json", resourceLoader),
            new TypeReference<>() {}
        );
        this.entries = loaded.stream()
            .sorted(Comparator.<ChangelogEntry, String>comparing(ChangelogEntry::version).reversed())
            .toList();
    }

    /** Returns all changelog entries, newest-first. */
    public List<ChangelogEntry> getAll() {
        return entries;
    }

    /**
     * Returns a changelog entry by version.
     *
     * @param version the version string to look up
     * @return Optional containing the entry, or empty if not found
     */
    public Optional<ChangelogEntry> getByVersion(final String version) {
        return entries.stream()
            .filter(entry -> entry.version().equals(version))
            .findFirst();
    }
}
