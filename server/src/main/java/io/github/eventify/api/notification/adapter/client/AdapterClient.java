package io.github.eventify.api.notification.adapter.client;

import org.springframework.web.client.RestClient;

/**
 * Thin wrapper around {@link RestClient} for webhook calls. Created via {@link AdapterClientFactory}.
 */
public class AdapterClient {

    private final RestClient restClient;

    /** Package-private constructor — use {@link AdapterClientFactory#create(String)}. */
    AdapterClient(final RestClient restClient) {
        this.restClient = restClient;
    }

    /**
     * Sends a POST request with the given body as JSON to the specified URL.
     */
    public void post(final String url, final Object body) {
        restClient.post().uri(url).body(body).retrieve().toBodilessEntity();
    }
}
