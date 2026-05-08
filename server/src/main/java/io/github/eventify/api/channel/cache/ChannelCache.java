package io.github.eventify.api.channel.cache;

import io.github.eventify.api.channel.model.Channel;
import io.github.jframe.cache.RequestScopedCache;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

/**
 * Request-scoped cache for Channel entities.
 *
 * <p>Eliminates duplicate database queries within a single HTTP request.
 * Security layer populates the cache after validating access,
 * and service layer reads from cache instead of re-querying.
 */
@Component
@RequestScope
public class ChannelCache extends RequestScopedCache<Long, Channel> {

    @Override
    protected Long getId(final Channel entity) {
        return entity.getId();
    }

    /**
     * Retrieves a channel by slug from the cache.
     *
     * @param slug the channel slug to look up
     * @return Optional containing the channel if found by slug, empty otherwise
     */
    public Optional<Channel> getBySlug(final String slug) {
        return getAll().values().stream()
            .filter(channel -> channel.getSlug().equals(slug))
            .findFirst();
    }
}
