package dev.wickedenterprise.prototyping.rdbmswriter;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface LogEntryRepository extends JpaRepository<LogEntryEntity, Long> {

    @Query(value = "select count(distinct(trustee_address, truster_address)) from log_entry_entity", nativeQuery = true)
    long countDistinctByTrusteeAndTrusterAddress();

}
