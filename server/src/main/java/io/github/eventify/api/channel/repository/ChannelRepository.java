package io.github.eventify.api.channel.repository;

import io.github.eventify.api.channel.model.Channel;
import io.github.eventify.api.channel.model.ChannelStatus;
import io.github.eventify.common.security.principal.ApiKeyPrincipal;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repository for Channel entity.
 */
@SuppressWarnings("PMD.TooManyMethods")
@Repository
public interface ChannelRepository extends JpaRepository<Channel, Long>, JpaSpecificationExecutor<Channel> {

    @Query(
        """
            SELECT c FROM Channel c
            WHERE c.id = :id AND c.status != 'PENDING_DELETION'
            """
    )
    Optional<Channel> findActiveChannelById(@Param("id") Long id);

    /**
     * Finds active channels by IDs (batch query).
     *
     * @param ids the channel IDs
     * @return list of active channels
     */
    @Query(
        """
            SELECT c FROM Channel c
            WHERE c.id IN :ids AND c.status != 'PENDING_DELETION'
            """
    )
    List<Channel> findActiveChannelsByIds(@Param("ids") Collection<Long> ids);

    /**
     * Finds a personal channel by user ID and name.
     *
     * @param userId the user ID
     * @param name   the channel name
     * @return optional channel
     */
    Optional<Channel> findByUserIdAndNameAndOrganizationIdIsNull(Long userId, String name);

    /**
     * Checks if a personal channel with the given slug exists for a user.
     *
     * @param userId the user ID
     * @param slug   the channel slug
     * @return true if exists, false otherwise
     */
    boolean existsByUserIdAndSlugAndOrganizationIdIsNull(Long userId, String slug);

    /**
     * Finds a personal channel by slug and user ID.
     *
     * @param slug   the channel slug
     * @param userId the user ID
     * @return optional channel
     */
    Optional<Channel> findBySlugAndUserIdAndOrganizationIdIsNull(String slug, Long userId);

    /**
     * Finds an organization channel by slug and organization ID.
     *
     * @param slug           the channel slug
     * @param organizationId the organization ID
     * @return optional channel
     */
    Optional<Channel> findBySlugAndOrganizationId(String slug, Long organizationId);

    /**
     * Finds a channel by ID and user ID excluding deleted ones.
     *
     * @param id     the channel ID
     * @param userId the user ID
     * @param status the status to exclude
     * @return optional channel
     */
    Optional<Channel> findByIdAndUserIdAndStatusNot(Long id, Long userId, ChannelStatus status);

    /**
     * Finds all personal channels by IDs and user ID (batch query).
     *
     * @param ids    the channel IDs
     * @param userId the user ID
     * @return list of channels
     */
    @Query(
        """
            SELECT c FROM Channel c
            WHERE c.id IN :ids
            AND c.user.id = :userId
            AND c.organization IS NULL
            AND c.status != 'PENDING_DELETION'
            """
    )
    List<Channel> findAllByIdInAndUserId(@Param("ids") List<Long> ids, @Param("userId") Long userId);

    /**
     * Finds an organization channel by organization ID and name.
     *
     * @param organizationId the organization ID
     * @param name           the channel name
     * @return optional channel
     */
    Optional<Channel> findByOrganizationIdAndName(Long organizationId, String name);

    /**
     * Checks if an organization channel with the given slug exists.
     *
     * @param organizationId the organization ID
     * @param slug           the channel slug
     * @return true if exists, false otherwise
     */
    boolean existsByOrganizationIdAndSlug(Long organizationId, String slug);

    /**
     * Finds a channel by ID and organization ID excluding deleted ones.
     *
     * @param id             the channel ID
     * @param organizationId the organization ID
     * @param status         the status to exclude
     * @return optional channel
     */
    Optional<Channel> findByIdAndOrganizationIdAndStatusNot(Long id, Long organizationId, ChannelStatus status);

    /**
     * Counts channels by status.
     *
     * @param status the channel status
     * @return count of channels with the given status
     */
    Long countByStatus(ChannelStatus status);

    /**
     * Counts channels where isStale is true.
     *
     * @return count of stale channels
     */
    Long countByIsStaleTrue();

    /**
     * Finds all channels by status.
     *
     * @param status the channel status
     * @return list of channels
     */
    List<Channel> findByStatus(ChannelStatus status);

    /**
     * Finds all organization channels by IDs and organization ID (batch query).
     *
     * @param ids            the channel IDs
     * @param organizationId the organization ID
     * @return list of channels
     */
    @Query(
        """
            SELECT c FROM Channel c
            WHERE c.id IN :ids
            AND c.organization.id = :organizationId
            AND c.status != 'PENDING_DELETION'
            """
    )
    List<Channel> findAllByIdInAndOrganizationId(@Param("ids") List<Long> ids, @Param("organizationId") Long organizationId);

    /**
     * Finds all organization channels by IDs and organization ID, excluding deleted ones (batch query).
     *
     * @param ids            the channel IDs
     * @param organizationId the organization ID
     * @return list of active channels
     */
    @Query(
        """
            SELECT c FROM Channel c
            WHERE c.id IN :ids
            AND c.organization.id = :organizationId
            AND c.status != 'PENDING_DELETION'
            """
    )
    List<Channel> findActiveByIdInAndOrganizationId(@Param("ids") List<Long> ids, @Param("organizationId") Long organizationId);

    /**
     * Finds personal channels by user ID and status.
     *
     * @param userId the user ID
     * @param status the channel status
     * @return list of channels
     */
    List<Channel> findByUserIdAndOrganizationIsNullAndStatus(Long userId, ChannelStatus status);

    /**
     * Finds organization channels by organization ID and status.
     *
     * @param organizationId the organization ID
     * @param status         the channel status
     * @return list of channels
     */
    List<Channel> findByOrganizationIdAndStatus(Long organizationId, ChannelStatus status);

    /**
     * Finds a channel by slug within the principal's scope (organization or personal).
     * Uses scoped query: returns only channels owned by principal's user/org.
     *
     * @param slug      the channel slug
     * @param principal the API key principal
     * @return optional channel
     */
    default Optional<Channel> findBySlugAndPrincipal(final String slug, final ApiKeyPrincipal principal) {
        if (principal.getOrganizationId() != null) {
            return findBySlugAndOrganizationId(slug, principal.getOrganizationId());
        }
        return findBySlugAndUserIdAndOrganizationIdIsNull(slug, principal.getUserId());
    }

    /**
     * Marks channels as stale based on staleness and grace period thresholds.
     * Updates isStale=true for channels where:
     * - lastEventAt is older than stalenessThreshold OR lastEventAt is NULL
     * - createdAt is older than gracePeriodThreshold (grace period protection)
     * - isStale is currently false (idempotent)
     * - status is not PENDING_DELETION
     *
     * @param stalenessThreshold   threshold for determining staleness (e.g., 7 days ago)
     * @param gracePeriodThreshold threshold for grace period (e.g., 7 days ago)
     * @return count of channels marked as stale
     */
    @Modifying(
        clearAutomatically = true,
        flushAutomatically = true
    )
    @Transactional
    @Query(
        """
            UPDATE Channel c
            SET c.isStale = true
            WHERE (c.lastEventAt < :stalenessThreshold OR c.lastEventAt IS NULL)
            AND c.createdAt < :gracePeriodThreshold
            AND c.isStale = false
            AND c.status != 'PENDING_DELETION'
            """
    )
    int markChannelsAsStale(
        @Param("stalenessThreshold") OffsetDateTime stalenessThreshold,
        @Param("gracePeriodThreshold") OffsetDateTime gracePeriodThreshold
    );

    /**
     * Clears stale flag for channels that have recent activity.
     * Safety net in case trigger was bypassed (maintenance, manual imports, etc).
     * Updates isStale=false for channels where:
     * - lastEventAt is newer than or equal to threshold
     * - isStale is currently true
     *
     * @param threshold threshold for recent activity (e.g., 7 days ago)
     * @return count of channels cleared from stale status
     */
    @Modifying(
        clearAutomatically = true,
        flushAutomatically = true
    )
    @Transactional
    @Query(
        """
            UPDATE Channel c
            SET c.isStale = false
            WHERE c.lastEventAt >= :threshold
            AND c.isStale = true
            """
    )
    int clearStaleForActiveChannels(@Param("threshold") OffsetDateTime threshold);
}
