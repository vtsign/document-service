package tech.vtsign.documentservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.vtsign.documentservice.domain.UserDocument;

import java.util.List;
import java.util.UUID;

public interface UserDocumentRepository extends JpaRepository<UserDocument, UUID> {
    List<UserDocument> findByUserUUIDAndStatus(UUID userUUID, String status);

    UserDocument findByUserUUIDAndContractId(UUID userUUID, UUID contractUUID);
}
