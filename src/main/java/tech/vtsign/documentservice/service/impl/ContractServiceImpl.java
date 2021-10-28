package tech.vtsign.documentservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.vtsign.documentservice.domain.Contract;
import tech.vtsign.documentservice.domain.Document;
import tech.vtsign.documentservice.domain.UserDocument;
import tech.vtsign.documentservice.exception.BadRequestException;
import tech.vtsign.documentservice.exception.UnauthorizedException;
import tech.vtsign.documentservice.model.LoginServerResponseDto;
import tech.vtsign.documentservice.repository.ContractRepository;
import tech.vtsign.documentservice.repository.UserDocumentRepository;
import tech.vtsign.documentservice.security.UserDetailsImpl;
import tech.vtsign.documentservice.service.ContractService;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ContractServiceImpl implements ContractService {
    private final ContractRepository contractRepository;
    private final UserDocumentRepository userDocumentRepository;

    @Override
    public List<Document> getDocumentsByContractAndReceiver(UUID contractId, UUID receiverId) {
        Optional<Contract> opt = contractRepository.findById(contractId);
        if (opt.isEmpty()) {
            throw new BadRequestException("Not found contract");
        } else {
            Contract contract = opt.get();
            Optional<UserDocument> userDocumentOptional = contract.getUserDocuments().stream()
                    .filter(userDocument -> userDocument.getUserUUID().equals(receiverId))
                    .findFirst();
            UserDocument userDocument = userDocumentOptional.orElseThrow(() -> new UnauthorizedException("Invalid receiver"));
            userDocument.setViewedDate(new Date());
            return contract.getDocuments();
        }
    }

    @Override
    public UserDocument findContractById(UUID contractUUID) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        LoginServerResponseDto senderInfo = userDetails.getLoginServerResponseDto();
        return findContractByIdAndUserId(contractUUID, senderInfo.getId());
    }

    @Override
    public UserDocument findContractByIdAndUserId(UUID contractUUID, UUID userUUID) {
        return userDocumentRepository.findByUserUUIDAndContractId(userUUID, contractUUID);
    }

    @Override
    public List<Contract> findAllTemplateByUserId(UUID userUUID, String status) {
        List<UserDocument> userDocuments = userDocumentRepository.findByUserUUIDAndStatus(userUUID, status);
        return userDocuments.stream()
                .map(UserDocument::getContract)
                .collect(Collectors.toList());
    }

}
