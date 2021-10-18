package tech.vtsign.documentservice.service;

import tech.vtsign.documentservice.domain.Document;

import java.util.List;
import java.util.UUID;

public interface ContractService {
    List<Document> getDocumentsByContractAndReceiver(UUID contractUUID, UUID receiverUUID);
}
