package io.github.eventify.api.channel.repository;

import io.github.eventify.api.channel.model.Channel;
import io.github.eventify.api.channel.model.ChannelStatus;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for Channel entity.
 */
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
     * Finds a personal channel by user ID and name.
     *
     * @param userId the user ID
     * @param name   the channel name
     * @return optional channel
     */
    Optional<Channel> findByUserIdAndNameAndOrganizationIdIsNull(Long userId, String name);

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
     * Finds an organization channel by organization ID and name.
     *
     * @param organizationId the organization ID
     * @param name           the channel name
     * @return optional channel
     */
    Optional<Channel> findByOrganizationIdAndName(Long organizationId, String name);

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
     * Finds all channels by status.
     *
     * @param status the channel status
     * @return list of channels
     */
    List<Channel> findByStatus(ChannelStatus status);
}
