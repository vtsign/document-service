package tech.vtsign.documentservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.vtsign.documentservice.domain.Contract;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface ContractRepository extends JpaRepository<Contract, UUID> {
    Long countAllContractByCompleteDateNotNullAndCompleteDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    Long countAllByCreatedDateBetween(LocalDateTime startDate, LocalDateTime endDate);
}
