package org.jordijaspers.eventify.api.source.service;

import lombok.RequiredArgsConstructor;

import org.jordijaspers.eventify.api.source.model.Source;
import org.jordijaspers.eventify.api.source.model.request.CreateSourceRequest;
import org.jordijaspers.eventify.api.source.repository.SourceRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SourceService {

    private final SourceRepository sourceRepository;

    /**
     * Creates a new source which comes with it's own API key. The API key is used to authenticate the source when sending events to the
     * Eventify API.
     *
     * @param request The request to create a source.
     * @return The created source.
     */
    public Source createSource(final CreateSourceRequest request) {
        final Source source = new Source(request);
        return sourceRepository.save(source);
    }

}
