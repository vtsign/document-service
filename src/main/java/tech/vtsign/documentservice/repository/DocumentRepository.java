package tech.vtsign.documentservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.vtsign.documentservice.domain.Contract;

import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<Contract, UUID> {
}
