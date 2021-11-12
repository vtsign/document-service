package tech.vtsign.documentservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.vtsign.documentservice.domain.Contract;
import tech.vtsign.documentservice.domain.Document;
import tech.vtsign.documentservice.domain.User;
import tech.vtsign.documentservice.domain.UserContract;
import tech.vtsign.documentservice.exception.BadRequestException;
import tech.vtsign.documentservice.exception.NotFoundException;
import tech.vtsign.documentservice.exception.UnauthorizedException;
import tech.vtsign.documentservice.model.LoginServerResponseDto;
import tech.vtsign.documentservice.repository.ContractRepository;
import tech.vtsign.documentservice.repository.UserDocumentRepository;
import tech.vtsign.documentservice.repository.UserRepository;
import tech.vtsign.documentservice.security.UserDetailsImpl;
import tech.vtsign.documentservice.service.ContractService;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ContractServiceImpl implements ContractService {
    private final ContractRepository contractRepository;
    private final UserRepository userRepository;
    private final UserDocumentRepository userDocumentRepository;
//    private final
//    private final UserDocumentRepository userDocumentRepository;


    @Override
    public Contract findContractByContractAndReceiver(UUID contractId, UUID receiverId) {
        Optional<Contract> opt = contractRepository.findById(contractId);
        if (opt.isEmpty()) {
            throw new BadRequestException("Not found contract");
        } else {
            Contract contract = opt.get();
            Optional<UserContract> userDocumentOptional = contract.getUserContracts().stream()
                    .filter(userContract -> userContract.getUser().getId().equals(receiverId))
                    .findFirst();
            UserContract userContract = userDocumentOptional.orElseThrow(() -> new UnauthorizedException("Invalid receiver"));
            userContract.setViewedDate(new Date());
            return contract;
        }
    }

    @Override
    public UserContract findContractById(UUID contractUUID) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        LoginServerResponseDto senderInfo = userDetails.getLoginServerResponseDto();
        return findUserContractByIdAndUserId(contractUUID, senderInfo.getId());
    }

    @Override
    public UserContract findUserContractByIdAndUserId(UUID contractUUID, UUID userUUID) {
//        Optional<User> opt = userRepository.findById(userUUID);
//        if (opt.isPresent()) {
//            User user = opt.get();
//            Set<UserContract> userContracts = user.getUserContracts();
//            Optional<UserContract> optContract = userContracts.stream().filter(u -> u.getContract().getId().equals(contractUUID)).findFirst();
//            UserContract userContract = optContract.orElseThrow(() -> new NotFoundException("Not found contract"));
//            return userContract;
//        }
//        throw new NotFoundException("User Not Found");
        Contract contract = new Contract();
        contract.setId(contractUUID);
        User user= new User();
        user.setId(userUUID);
        return userDocumentRepository.findUserContractByContractAndUser(contract,user);
    }

    @Override
    public List<Contract> findAllTemplateByUserId(UUID userUUID, String status) {
        Optional<User> opt = userRepository.findById(userUUID);
        List<Contract> contracts = new ArrayList<>();
        if (opt.isPresent()) {
            User user = opt.get();
            Set<UserContract> userContracts = user.getUserContracts();
            contracts = userContracts.stream().filter(u -> u.getStatus().equals(status))
                    .map(userContract -> userContract.getContract())
                    .collect(Collectors.toList());

        }
        return contracts;

    }

    @Override
    public Contract getContractById(UUID id) {
        return contractRepository.getById(id);
    }

}
