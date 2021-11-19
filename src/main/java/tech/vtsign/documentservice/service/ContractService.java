package tech.vtsign.documentservice.service;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;
import tech.vtsign.documentservice.domain.Contract;
import tech.vtsign.documentservice.domain.UserContract;
import tech.vtsign.documentservice.model.SignContractByReceiver;
import tech.vtsign.documentservice.model.UserContractResponse;

import java.util.List;
import java.util.UUID;

public interface ContractService {
    Contract findContractByContractAndReceiver(UUID contractUUID, UUID receiverUUID);

    UserContract findContractById(UUID contractUUID);

    UserContract findUserContractByContractIdAndUserId(UUID contractUUID, UUID userUUID);

    Page<UserContract> findContractsByUserIdAndStatus(UserContract userContract, int page, int pageItems);


    Contract getContractById(UUID id);

    UserContractResponse getUDRByContractIdAndUserId(UUID contractUUID, UUID userUUID, String secretKey);

    Boolean signContractByUser(SignContractByReceiver u, List<MultipartFile> documents);

    long countAllByUserAndStatus(UUID userUUID, String status);
}
