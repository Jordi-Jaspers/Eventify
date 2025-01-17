package org.jordijaspers.eventify.api.source.repository;

import org.jordijaspers.eventify.api.source.model.Source;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


/**
 * The data access interface for {@link Source}.
 */
@Repository
@Transactional
public interface SourceRepository extends JpaRepository<Source, Long> {

}
