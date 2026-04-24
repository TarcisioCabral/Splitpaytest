package com.splitpay.conciliation_service.repository;

import com.splitpay.conciliation_service.entity.Conciliation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConciliationRepository extends JpaRepository<Conciliation, Long> {
    Optional<Conciliation> findByNfeKey(String nfeKey);
}
