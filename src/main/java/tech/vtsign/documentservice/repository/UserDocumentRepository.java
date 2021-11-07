package tech.vtsign.documentservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.vtsign.documentservice.domain.UserContract;

import java.util.UUID;

@Repository
public interface UserDocumentRepository extends JpaRepository<UserContract, UUID> {
//    List<UserContract> findByAndStatus(UUID userUUID, String status);

//    UserContract findByUserUUIDAndContractId(UUID userUUID, UUID contractUUID);
}
