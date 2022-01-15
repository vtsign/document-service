package tech.vtsign.documentservice.service;

import tech.vtsign.documentservice.constant.ContractTransactionAction;
import tech.vtsign.documentservice.domain.Contract;
import tech.vtsign.documentservice.domain.ContractTransaction;
import tech.vtsign.documentservice.domain.User;

public interface ContractTransactionService {
    ContractTransaction createContractTransaction(String message,
                                                  ContractTransactionAction action,
                                                  Contract contract,
                                                  User user);
}
