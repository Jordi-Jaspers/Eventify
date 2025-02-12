package org.jordijaspers.eventify.api.source.service;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.hawaiiframework.repository.DataNotFoundException;
import org.jordijaspers.eventify.api.source.model.ApiKey;
import org.jordijaspers.eventify.api.source.model.Source;
import org.jordijaspers.eventify.api.source.model.request.ApiKeyRequest;
import org.jordijaspers.eventify.api.source.model.request.SourceRequest;
import org.jordijaspers.eventify.api.source.repository.ApiKeyRepository;
import org.jordijaspers.eventify.api.source.repository.SourceRepository;
import org.springframework.stereotype.Service;

import static org.jordijaspers.eventify.common.exception.ApiErrorCode.SOURCE_NOT_FOUND_ERROR;

@Service
@RequiredArgsConstructor
public class SourceService {

    private final SourceRepository sourceRepository;

    private final ApiKeyRepository apiKeyRepository;

    /**
     * Retrieves all sources.
     *
     * @return A list of all sources.
     */
    public List<Source> getSources() {
        return sourceRepository.findAll();
    }

    /**
     * Retrieves a source by its id.
     *
     * @param id The id of the source.
     * @return The source with the given id.
     */
    public Source getSource(final Long id) {
        return sourceRepository.findById(id).orElseThrow(() -> new DataNotFoundException(SOURCE_NOT_FOUND_ERROR));
    }

    /**
     * Creates a new source which comes with it's own API key. The API key is used to authenticate the source when sending events to the
     * Eventify API.
     *
     * @param request The request to create a source.
     * @return The created source.
     */
    public Source createSource(final SourceRequest request) {
        final Source source = new Source(request);
        return sourceRepository.save(source);
    }

    /**
     * Updates the details of a source.
     */
    public Source updateSource(final Long id, final SourceRequest request) {
        final Source source = getSource(id);
        source.setName(request.getName());
        source.setDescription(request.getDescription());
        return sourceRepository.save(source);
    }

    /**
     * Deletes a source by its id and all their associated checks.
     *
     * @param id The id of the source.
     */
    public void deleteSource(final Long id) {
        sourceRepository.deleteById(id);
    }

    /**
     * Regenerates the API key of a specified source.
     *
     * @param id      The id of the source.
     * @param request The request to create a new API key.
     * @return The source with the new API key.
     */
    public Source regenerateApiKey(final Long id, final ApiKeyRequest request) {
        final Source source = getSource(id);
        apiKeyRepository.delete(source.getApiKey());
        source.setApiKey(new ApiKey(request.getExpiresAt().toLocalDateTime()));
        return sourceRepository.save(source);
    }

    /**
     * Locks or unlocks the api key of a specified source.
     *
     * @param id   the id of the source
     * @param lock true to lock the source, false to unlock the source
     */
    public Source lockApiKey(final Long id, final boolean lock) {
        final Source source = getSource(id);
        source.setApiKeyEnabled(!lock);
        return sourceRepository.save(source);
    }

    /**
     * Checks if a source contains a check.
     *
     * @param sourceId The id of the source.
     * @param checkId  The id of the check.
     * @return True if the source contains the check, false otherwise.
     */
    public boolean containsCheck(final Long sourceId, final Long checkId) {
        return sourceRepository.existsBySourceAndCheck(sourceId, checkId);
    }
}
