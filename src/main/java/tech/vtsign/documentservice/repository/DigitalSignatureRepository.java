package tech.vtsign.documentservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.vtsign.documentservice.domain.DigitalSignature;

import java.util.List;
import java.util.UUID;

public interface DigitalSignatureRepository extends JpaRepository<DigitalSignature, UUID> {
    List<DigitalSignature> findByIdAndStatus(UUID id, String status);
}
