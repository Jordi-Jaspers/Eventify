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
 * <p>Design principles:
 * <ul>
 * <li>User channels: non-existent or not-owned → false (403). Hides existence to prevent enumeration.</li>
 * <li>Org channels: existence/deletion checks are the service layer's job (404). Security only checks org binding.</li>
 * <li>Empty/null lists → true (let validator return 400, not security 403).</li>
 * <li>Single-channel methods delegate to batch variants to avoid duplicate logic.</li>
 * </ul>
 */
@Service("channelSecurity")
@RequiredArgsConstructor
public class ChannelSecurityService {

    private final ChannelRepository channelRepository;

    private final ChannelCache channelCache;

    // ========================= API KEY METHODS =========================

    /**
     * Check if an API key principal can access a channel by slug.
     * Caches the channel if found to avoid duplicate DB queries in service layer.
     * Not found or invalid slug → return true, let service/validator handle errors.
     */
    public boolean canAccess(final String slug, final ApiKeyPrincipal principal) {
        if (principal == null) {
            return false;
        }
        if (slug != null && !slug.isBlank()) {
            channelRepository.findBySlugAndPrincipal(slug, principal).ifPresent(channelCache::put);
        }
        return true;
    }

    /**
     * Check if an API key principal can access all channels in a batch request.
     * Caches all resolved channels to avoid duplicate DB queries in service layer.
     * Returns true for empty/invalid batches to let validation handle 400 errors.
     */
    public boolean canAccessBatch(final BatchEventRequest request, final ApiKeyPrincipal principal) {
        if (principal == null) {
            return false;
        }
        if (hasValidSlugs(request)) {
            final List<Channel> channels = extractSlugs(request).stream()
                .map(slug -> channelRepository.findBySlugAndPrincipal(slug, principal))
                .flatMap(java.util.Optional::stream)
                .toList();
            channelCache.putAll(channels);
        }
        return true;
    }

    // ========================= USER CHANNEL METHODS =========================

    /**
     * Check if a user can access a single personal channel.
     * Delegates to the batch variant for consistent logic.
     */
    public boolean canAccessChannelAsUser(final Long channelId, final UserTokenPrincipal principal) {
        return channelId != null && canAccessChannelsAsUser(List.of(channelId), principal);
    }

    /**
     * Check if a user can access all channels in a batch as personal channel owner.
     *
     * <p>Returns false if ANY channel is missing, belongs to another user, belongs to an org, or is already deleted.
     * This intentionally hides existence to prevent enumeration (non-existent → 403, not 404).
     * Empty/null list → true (let validator return 400).
     */
    public boolean canAccessChannelsAsUser(final List<Long> channelIds, final UserTokenPrincipal principal) {
        if (principal == null) {
            return false;
        }
        final boolean emptyBatch = channelIds == null || channelIds.isEmpty();
        return emptyBatch || channelRepository.findAllByIdInAndUserId(channelIds, principal.getUser().getId()).size() == channelIds.size();
    }

    // ========================= ORG CHANNEL METHODS =========================

    /**
     * Check if a channel belongs to an organization (for single-channel access, e.g. durations endpoint).
     * Delegates to the batch variant for consistent logic.
     */
    public boolean canAccessChannelInOrganization(final Long channelId, final Long orgId,
        final UserTokenPrincipal principal) {
        return channelId != null && orgId != null && canAccessChannelsInOrganization(List.of(channelId), orgId, principal);
    }

    /**
     * Check if all channels in a batch belong to the given organization.
     *
     * <p>Only checks org binding for channels that actually exist and are active.
     * Non-existent or deleted channels are NOT a security concern — the service layer handles those (404).
     * If any found channel belongs to a DIFFERENT org → false (cross-org access is a security violation).
     * Empty/null list → true (let validator return 400).
     */
    public boolean canAccessChannelsInOrganization(final List<Long> channelIds, final Long orgId,
        final UserTokenPrincipal principal) {
        if (principal == null || orgId == null) {
            return false;
        }
        final boolean emptyBatch = channelIds == null || channelIds.isEmpty();
        return emptyBatch || channelRepository.findActiveChannelsByIds(channelIds).stream()
            .allMatch(channel -> belongsToOrganization(channel, orgId));
    }

    // ========================= PRIVATE HELPERS =========================

    private boolean hasValidSlugs(final BatchEventRequest request) {
        return !isEmptyBatch(request) && !extractSlugs(request).isEmpty();
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

    private boolean belongsToOrganization(final Channel channel, final Long orgId) {
        return channel.getOrganization() != null
            && channel.getOrganization().getId().equals(orgId);
    }
}
