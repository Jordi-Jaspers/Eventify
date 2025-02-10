package org.jordijaspers.eventify.api.source.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.jordijaspers.eventify.api.source.model.Source;
import org.jordijaspers.eventify.api.source.model.mapper.SourceMapper;
import org.jordijaspers.eventify.api.source.model.request.ApiKeyRequest;
import org.jordijaspers.eventify.api.source.model.request.SourceRequest;
import org.jordijaspers.eventify.api.source.model.response.SourceResponse;
import org.jordijaspers.eventify.api.source.model.validator.SourceValidator;
import org.jordijaspers.eventify.api.source.service.SourceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static org.jordijaspers.eventify.api.Paths.*;
import static org.springframework.http.HttpStatus.*;

// TODO: Write Integration tests + unit test for the validator.
@RestController
@RequiredArgsConstructor
public class SourceController {

    private final SourceMapper sourceMapper;

    private final SourceService sourceService;

    private final SourceValidator sourceValidator;

    @ResponseStatus(OK)
    @Operation(summary = "Retrieve a list of all sources within the application.")
    @GetMapping(path = SOURCES_PATH)
    @PreAuthorize("hasAuthority('READ_SOURCE')")
    public ResponseEntity<List<SourceResponse>> getSources() {
        final List<Source> sources = sourceService.getSources();
        return ResponseEntity.status(OK).body(sourceMapper.toSourcesResponse(sources));
    }

    @ResponseStatus(CREATED)
    @Operation(summary = "Create a new source with its own API key.")
    @GetMapping(path = SOURCES_PATH)
    @PreAuthorize("hasAuthority('WRITE_SOURCE')")
    public ResponseEntity<SourceResponse> createSource(@RequestBody final SourceRequest request) {
        sourceValidator.validateAndThrow(request);
        final Source source = sourceService.createSource(request);
        return ResponseEntity.status(CREATED).body(sourceMapper.toDetailedSourceResponse(source));
    }

    @ResponseStatus(OK)
    @Operation(summary = "Retrieve the source details with their API key.")
    @GetMapping(path = SOURCE_PATH)
    @PreAuthorize("hasAuthority('READ_SOURCE')")
    public ResponseEntity<SourceResponse> getSource(@PathVariable final Long id) {
        final Source source = sourceService.getSource(id);
        return ResponseEntity.status(OK).body(sourceMapper.toDetailedSourceResponse(source));
    }

    @ResponseStatus(NO_CONTENT)
    @Operation(summary = "Delete a source with all their associated data.")
    @DeleteMapping(path = SOURCE_PATH)
    @PreAuthorize("hasAuthority('WRITE_SOURCE')")
    public ResponseEntity<Void> deleteSource(@PathVariable final Long id) {
        sourceService.deleteSource(id);
        return ResponseEntity.status(NO_CONTENT).build();
    }

    @ResponseStatus(OK)
    @Operation(summary = "Update the details of a source.")
    @PutMapping(path = SOURCE_PATH)
    @PreAuthorize("hasAuthority('WRITE_SOURCE')")
    public ResponseEntity<SourceResponse> updateSource(@PathVariable final Long id, @RequestBody final SourceRequest request) {
        sourceValidator.validateAndThrow(request);
        final Source source = sourceService.updateSource(id, request);
        return ResponseEntity.status(OK).body(sourceMapper.toSourceResponse(source));
    }

    @ResponseStatus(OK)
    @Operation(summary = "Delete the current active API key of a source and generate a new one.")
    @PostMapping(path = REGENERATE_API_KEY_PATH)
    @PreAuthorize("hasAuthority('WRITE_SOURCE')")
    public ResponseEntity<SourceResponse> regenerateApiKey(@PathVariable final Long id, @RequestBody final ApiKeyRequest request) {
        final Source source = sourceService.regenerateApiKey(id, request);
        return ResponseEntity.status(OK).body(sourceMapper.toDetailedSourceResponse(source));
    }

    @ResponseStatus(OK)
    @Operation(summary = "Lock the API key of a source.")
    @PostMapping(path = LOCK_API_KEY_PATH)
    @PreAuthorize("hasAuthority('WRITE_SOURCE')")
    public ResponseEntity<SourceResponse> lockApiKey(@PathVariable final Long id) {
        final Source source = sourceService.lockApiKey(id, true);
        return ResponseEntity.status(OK).body(sourceMapper.toDetailedSourceResponse(source));
    }

    @ResponseStatus(OK)
    @Operation(summary = "Unlock the API key of a source.")
    @PostMapping(path = UNLOCK_API_KEY_PATH)
    @PreAuthorize("hasAuthority('WRITE_SOURCE')")
    public ResponseEntity<SourceResponse> unlockApiKey(@PathVariable final Long id) {
        final Source source = sourceService.lockApiKey(id, false);
        return ResponseEntity.status(OK).body(sourceMapper.toDetailedSourceResponse(source));
    }
}
