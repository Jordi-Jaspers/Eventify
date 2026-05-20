package io.github.eventify.api.changelog.controller;

import io.github.eventify.api.changelog.model.ChangelogEntry;
import io.github.eventify.api.changelog.service.ChangelogService;
import io.github.eventify.common.exception.ApiErrorCode;
import io.github.jframe.exception.core.DataNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static io.github.eventify.api.Paths.PUBLIC_CHANGELOG_PATH;
import static io.github.eventify.api.Paths.PUBLIC_CHANGELOG_VERSION_PATH;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/** Controller for public changelog endpoints. */
@RestController
@RequiredArgsConstructor
@Tag(
    name = "Changelog",
    description = "Public changelog endpoints"
)
public class ChangelogController {

    private final ChangelogService changelogService;

    @ResponseStatus(OK)
    @Operation(
        summary = "Get all changelog entries",
        description = "Returns all changelog entries sorted newest-first"
    )
    @GetMapping(
        path = PUBLIC_CHANGELOG_PATH,
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<ChangelogEntry>> getAllEntries() {
        return ResponseEntity.status(OK).body(changelogService.getAll());
    }

    @ResponseStatus(OK)
    @Operation(
        summary = "Get changelog entry by version",
        description = "Returns a single changelog entry for the given version"
    )
    @GetMapping(
        path = PUBLIC_CHANGELOG_VERSION_PATH,
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ChangelogEntry> getEntryByVersion(@PathVariable final String version) {
        final ChangelogEntry entry = changelogService.getByVersion(version)
            .orElseThrow(() -> new DataNotFoundException(ApiErrorCode.CHANGELOG_NOT_FOUND));
        return ResponseEntity.status(OK).body(entry);
    }
}
