package tech.vtsign.documentservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.vtsign.documentservice.domain.ContractTransaction;

import java.util.UUID;

public interface ContractTransactionRepository extends JpaRepository<ContractTransaction, UUID> {
}
