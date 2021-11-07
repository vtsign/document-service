package tech.vtsign.documentservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.vtsign.documentservice.domain.User;

import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
//    List<UserContract> findBy(UUID userUUID, String status);
}
