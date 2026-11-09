package com.echostock.audit;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AuditRepository extends JpaRepository<Audit, Long>, JpaSpecificationExecutor<Audit> {

    Optional<Audit> findByClientAuditId(String clientAuditId);
}
