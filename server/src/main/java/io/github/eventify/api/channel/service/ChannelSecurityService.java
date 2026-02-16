package io.github.eventify.api.channel.service;

import io.github.eventify.api.channel.cache.ChannelCache;
import io.github.eventify.api.channel.model.Channel;
import io.github.eventify.api.channel.repository.ChannelRepository;
import io.github.eventify.api.event.model.request.BatchEventRequest;
import io.github.eventify.api.event.model.request.CreateEventRequest;
import io.github.eventify.common.security.principal.ApiKeyPrincipal;
import io.github.eventify.common.security.principal.UserTokenPrincipal;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

/**
 * Security service for channel access control. Bean name "channelSecurity" for use in SpEL expressions with @PreAuthorize.
 *
 * <p>Authorization flow for event ingestion:
 * <ol>
 * <li>Resolves channel by slug within principal's scope (userId or orgId)</li>
 * <li>If found: caches channel and grants access (ownership implicit via scoped query)</li>
 * <li>If not found: returns true to let service layer handle 404 (prevents enumeration)</li>
 * </ol>
 */
@Service("channelSecurity")
@RequiredArgsConstructor
public class ChannelSecurityService {

    private final ChannelRepository channelRepository;

    private final ChannelCache channelCache;

    /**
     * Check if an API key principal can access a channel by slug.
     * Caches the channel if found to avoid duplicate DB queries in service layer.
     * Not found or invalid slug → return true, let service/validator handle errors.
     *
     * <p>This prevents 403 for validation errors (missing slug should be 400, not 403)
     * and prevents channel enumeration (non-existent slug should be 404 from service).
     *
     * @param slug      the channel slug to access
     * @param principal the API key principal attempting access
     * @return true if authorized or passthrough for validation/service, false only if principal is invalid
     */
    public boolean canAccess(final String slug, final ApiKeyPrincipal principal) {
        if (principal == null) {
            return false;
        }
        // Let validation handle missing/blank slug (400), not security (403)
        // Otherwise, cache the channel if found
        if (slug != null && !slug.isBlank()) {
            channelRepository.findBySlugAndPrincipal(slug, principal).ifPresent(channelCache::put);
        }
        return true;
    }

    /**
     * Check if an API key principal can access all channels in a batch request.
     * Caches all resolved channels to avoid duplicate DB queries in service layer.
     *
     * <p>Returns true for empty/invalid batches to let validation handle 400 errors.
     * Security layer should only return false (403) for authentication issues.
     *
     * @param request   the batch event request containing events with channel slugs
     * @param principal the API key principal attempting access
     * @return true if authorized or passthrough for validation/service, false only if principal is invalid
     */
    public boolean canAccessBatch(final BatchEventRequest request, final ApiKeyPrincipal principal) {
        if (principal == null) {
            return false;
        }
        // Let validation handle empty/invalid batch (400), not security (403)
        // Otherwise, cache all resolved channels
        if (hasValidSlugs(request)) {
            final List<Channel> channels = extractSlugs(request).stream()
                .map(slug -> channelRepository.findBySlugAndPrincipal(slug, principal))
                .flatMap(java.util.Optional::stream)
                .toList();
            channelCache.putAll(channels);
        }
        return true;
    }

    private boolean hasValidSlugs(final BatchEventRequest request) {
        return !isEmptyBatch(request) && !extractSlugs(request).isEmpty();
    }

    /**
     * Check if a user can access a channel as a personal channel owner.
     */
    public boolean canAccessChannelAsUser(final Long channelId, final UserTokenPrincipal principal) {
        if (principal == null || channelId == null) {
            return false;
        }
        return channelRepository.findActiveChannelById(channelId)
            .filter(channel -> isPersonalChannelOwnedBy(channel, principal))
            .isPresent();
    }

    /**
     * Check if a channel belongs to an organization.
     */
    public boolean canAccessChannelInOrganization(final Long channelId, final Long orgId,
        final UserTokenPrincipal principal) {
        if (principal == null || channelId == null || orgId == null) {
            return false;
        }
        return channelRepository.findActiveChannelById(channelId)
            .filter(channel -> belongsToOrganization(channel, orgId))
            .isPresent();
    }

    private boolean isEmptyBatch(final BatchEventRequest request) {
        return request == null || request.getEvents() == null || request.getEvents().isEmpty();
    }

    private Set<String> extractSlugs(final BatchEventRequest request) {
        return request.getEvents().stream()
            .map(CreateEventRequest::getSlug)
            .filter(Objects::nonNull)
            .filter(slug -> !slug.isBlank())
            .collect(Collectors.toSet());
    }

    private boolean isPersonalChannelOwnedBy(final Channel channel, final UserTokenPrincipal principal) {
        return channel.getOrganization() == null
            && channel.getUser().getId().equals(principal.getUser().getId());
    }

    private boolean belongsToOrganization(final Channel channel, final Long orgId) {
        return channel.getOrganization() != null
            && channel.getOrganization().getId().equals(orgId);
    }
}
