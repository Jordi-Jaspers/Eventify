package io.github.eventify.api.channel.repository;

import io.github.eventify.api.channel.model.Channel;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for Channel entity.
 */
@Repository
public interface ChannelRepository extends JpaRepository<Channel, Long> {

    /**
     * Finds all channels by user ID.
     *
     * @param userId the user ID
     * @return list of channels
     */
    List<Channel> findAllByUserId(Long userId);

    /**
     * Finds all channels by organization ID.
     *
     * @param organizationId the organization ID
     * @return list of channels
     */
    List<Channel> findAllByOrganizationId(Long organizationId);
}
