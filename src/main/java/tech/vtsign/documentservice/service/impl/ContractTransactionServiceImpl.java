package tech.vtsign.documentservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tech.vtsign.documentservice.constant.ContractTransactionAction;
import tech.vtsign.documentservice.domain.Contract;
import tech.vtsign.documentservice.domain.ContractTransaction;
import tech.vtsign.documentservice.domain.User;
import tech.vtsign.documentservice.repository.ContractTransactionRepository;
import tech.vtsign.documentservice.service.ContractTransactionService;

@Service
@RequiredArgsConstructor
public class ContractTransactionServiceImpl implements ContractTransactionService {
    private final ContractTransactionRepository contractTransactionRepository;

    @Override
    public ContractTransaction createContractTransaction(String message,
                                                         ContractTransactionAction action,
                                                         Contract contract,
                                                         User user) {
        ContractTransaction contractTransaction = new ContractTransaction();
        contractTransaction.setContract(contract);
        contractTransaction.setUser(user);
        contractTransaction.setAction(action);
        contractTransaction.setMessage(message);
        contractTransactionRepository.save(contractTransaction);
        return contractTransaction;
    }
}
