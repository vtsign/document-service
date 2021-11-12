package tech.vtsign.documentservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.vtsign.documentservice.domain.Contract;
import tech.vtsign.documentservice.domain.User;
import tech.vtsign.documentservice.domain.UserContract;

import java.util.UUID;

@Repository
public interface UserDocumentRepository extends JpaRepository<UserContract, UUID> {
    UserContract findUserContractByContractAndUser(Contract contract, User user);
//    List<UserContract> findByAndStatus(UUID userUUID, String status);

//    UserContract findByUserUUIDAndContractId(UUID userUUID, UUID contractUUID);
}
