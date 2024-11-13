package org.jordijaspers.eventify.common.security.rsa.repository;

import org.jordijaspers.eventify.common.security.rsa.model.RSAKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * The repository connection to the RSAKey table.
 */
@Repository
public interface RSAKeyRepository extends JpaRepository<RSAKey, Integer> {

}
