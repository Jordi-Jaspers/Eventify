package org.jordijaspers.eventify.api.source.repository;

import org.jordijaspers.eventify.api.source.model.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;



/**
 * The data access interface for {@link ApiKey}.
 */
@Repository
@Transactional
public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {

}
