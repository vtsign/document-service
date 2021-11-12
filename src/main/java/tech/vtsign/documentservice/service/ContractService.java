package tech.vtsign.documentservice.service;

import tech.vtsign.documentservice.domain.Contract;
import tech.vtsign.documentservice.domain.Document;
import tech.vtsign.documentservice.domain.UserContract;
import tech.vtsign.documentservice.model.DocumentStatus;

import java.util.List;
import java.util.UUID;

public interface ContractService {
    Contract findContractByContractAndReceiver(UUID contractUUID, UUID receiverUUID);

    UserContract findContractById(UUID contractUUID);

    UserContract findUserContractByIdAndUserId(UUID contractUUID, UUID userUUID);

    List<Contract> findAllTemplateByUserId(UUID userUUID, String status);

    default List<Contract> findAllTemplateDraftByUserId(UUID userUUID) {
        return findAllTemplateByUserId(userUUID, DocumentStatus.DRAFT);
    }

    default List<Contract> findAllTemplateActionRequireByUserId(UUID userUUID) {
        return findAllTemplateByUserId(userUUID, DocumentStatus.ACTION_REQUIRE);
    }

    default List<Contract> findAllTemplateWaitingByUserId(UUID userUUID) {
        return findAllTemplateByUserId(userUUID, DocumentStatus.WAITING);
    }

    default List<Contract> findAllTemplateCompletedByUserId(UUID userUUID) {
        return findAllTemplateByUserId(userUUID, DocumentStatus.COMPLETED);
    }

    default List<Contract> findAllTemplateSentByUserId(UUID userUUID) {
        return findAllTemplateByUserId(userUUID, DocumentStatus.SENT);
    }

    default List<Contract> findAllTemplateDeletedByUserId(UUID userUUID) {
        return findAllTemplateByUserId(userUUID, DocumentStatus.DELETED);
    }

    default List<Contract> findAllTemplateHiddenByUserId(UUID userUUID) {
        return findAllTemplateByUserId(userUUID, DocumentStatus.HIDDEN);
    }

    Contract getContractById(UUID id);
}
