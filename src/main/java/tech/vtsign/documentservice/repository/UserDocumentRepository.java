package tech.vtsign.documentservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import tech.vtsign.documentservice.domain.Contract;
import tech.vtsign.documentservice.domain.User;
import tech.vtsign.documentservice.domain.UserContract;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserDocumentRepository extends JpaRepository<UserContract, UUID>, JpaSpecificationExecutor<UserContract> {
    UserContract findUserContractByContractAndUser(Contract contract, User user);
}
