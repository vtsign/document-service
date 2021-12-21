package tech.vtsign.documentservice.service;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;
import tech.vtsign.documentservice.domain.Contract;
import tech.vtsign.documentservice.domain.User;
import tech.vtsign.documentservice.domain.UserContract;
import tech.vtsign.documentservice.model.ContractStatisticDto;
import tech.vtsign.documentservice.model.SignContractByReceiver;
import tech.vtsign.documentservice.model.UserContractResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ContractService {
    Contract findContractByContractAndReceiver(UUID contractUUID, UUID receiverUUID);

    Page<UserContract> findContractsByUserIdAndStatus(UserContract userContract, Contract contract,
                                                      int page, int pageItems, String sortField, String sortType);


    UserContractResponse getUDRByContractIdAndUserId(UUID contractUUID, UUID userUUID, UUID userContractUUID, String secretKey);

    Boolean signContractByUser(SignContractByReceiver u, List<MultipartFile> documents);

    long countAllByUserAndStatus(UUID userUUID, String status);

    UserContract findUserContractById(UUID contractUUID, UUID userUUID, UUID userContractUUID);

    UserContract deleteContractById(UUID userUUID, UUID contractUUID, UUID userContractUUID);

    UserContract hiddenContractById(UUID userUUID, UUID contractUUID, UUID userContractUUID);

    UserContract restoreContractById(UUID userUUID, UUID contractUUID, UUID userContractUUID);

    User findUserById(UUID userUUID);

    User updateUser(User user);

    Long countAllContract(LocalDateTime startDate, LocalDateTime endDate);
    Long countAllContract();

    Long countAllContractCompleted(LocalDateTime startDate, LocalDateTime endDate);
    Long countAllContractCompleted();

    ContractStatisticDto getStatistic(String type);

}
