package io.github.eventify.api.notification.adapter.client;

import io.github.jframe.autoconfigure.properties.ApplicationProperties;
import io.github.jframe.factory.HttpClientSSLFactory;
import io.github.jframe.tracing.HttpFilter;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import static io.github.jframe.util.constants.Constants.Headers.X_CLIENT_VERSION;
import static org.springframework.http.HttpHeaders.*;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Factory for creating {@link AdapterClient} instances per adapter type.
 * Each adapter passes its own service name for distributed tracing.
 */
@Component
@RequiredArgsConstructor
public class AdapterClientFactory {

    private final HttpFilter filter;
    private final HttpClientSSLFactory httpClientSSLFactory;
    private final ApplicationProperties applicationProperties;

    /** Creates an {@link AdapterClient} with the given service name for distributed tracing. */
    public AdapterClient create(final String serviceName) {
        final HttpHeaders headers = new HttpHeaders();
        headers.add(ACCEPT, ALL_VALUE);
        headers.add(CONNECTION, "keep-alive");
        headers.add(CONTENT_TYPE, APPLICATION_JSON_VALUE);
        headers.add(USER_AGENT, "EVENTIFY_NOTIFICATION/" + applicationProperties.getVersion());
        headers.add(X_CLIENT_VERSION, applicationProperties.getVersion());

        final RestClient restClient = RestClient.builder()
            .requestInterceptor(filter.getRequestInterceptor(serviceName))
            .defaultHeaders(consumer -> consumer.addAll(headers))
            .requestFactory(httpClientSSLFactory.createRequestFactory(false, null, null, 10, 30))
            .build();

        return new AdapterClient(restClient);
    }
}
