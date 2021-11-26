package tech.vtsign.documentservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tech.vtsign.documentservice.domain.*;
import tech.vtsign.documentservice.exception.*;
import tech.vtsign.documentservice.model.*;
import tech.vtsign.documentservice.repository.ContractRepository;
import tech.vtsign.documentservice.repository.UserDocumentRepository;
import tech.vtsign.documentservice.repository.UserRepository;
import tech.vtsign.documentservice.security.UserDetailsImpl;
import tech.vtsign.documentservice.service.*;

import javax.persistence.criteria.*;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ContractServiceImpl implements ContractService {
    private final ContractRepository contractRepository;
    private final UserRepository userRepository;
    private final UserDocumentRepository userDocumentRepository;
    private final DocumentService documentService;
    private final XFDFService xfdfService;
    private final AzureStorageService azureStorageService;
    private final DocumentProducer documentProducer;

    @Value("${tech.vtsign.kafka.document-service.notify-common}")
    private String TOPIC_NOTIFY_COMMON;

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
        return findUserContractByContractIdAndUserId(contractUUID, senderInfo.getId());
    }

    @Override
    public UserContract findUserContractByContractIdAndUserId(UUID contractUUID, UUID userUUID) {
        Contract contract = new Contract();
        contract.setId(contractUUID);
        User user = new User();
        user.setId(userUUID);
        return userDocumentRepository.findUserContractByContractAndUser(contract, user)
                .orElseThrow(() -> new NotFoundException("contract not found"));
    }

    @Override
    public Page<UserContract> findContractsByUserIdAndStatus(UserContract userContract, Contract contract, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        String sortName = "createdDate";
        String status = userContract.getStatus();
        if (status.equals(DocumentStatus.COMPLETED))
            sortName = "completeDate";
        else if (status.equals(DocumentStatus.DELETED))
            sortName = "lastModifiedDate";


        String finalSortName = sortName;
        Page<UserContract> userContracts = userDocumentRepository.findAll(new Specification<UserContract>() {
            @Override
            public Predicate toPredicate(Root<UserContract> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {

                final Path<Contract> contractPath = root.get("contract");
                criteriaQuery.orderBy(criteriaBuilder.desc(contractPath.get(finalSortName)));
                List<Predicate> predicates = new ArrayList<>();
                if (contract.getTitle() != null) {

                    predicates.add(criteriaBuilder.and(criteriaBuilder.like(contractPath.get("title"), "%" + contract.getTitle() + "%")));
                }
                if (userContract.getStatus() != null) {
                    predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("status"), userContract.getStatus())));
                }
                if (userContract.getUser() != null) {
                    predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("user"), userContract.getUser())));
                }
                if (userContract.getViewedDate() != null) {
                    predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("viewedDate"), userContract.getViewedDate())));
                }
                if (userContract.getSignedDate() != null) {
                    predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("signedDate"), userContract.getSignedDate())));
                }

                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        }, pageable);
        return userContracts;

    }

    @Override
    public Contract getContractById(UUID id) {
        return contractRepository.getById(id);
    }

    @Override
    public UserContractResponse getUDRByContractIdAndUserId(UUID contractId, UUID userUUID, String secretKey) {

        UserContract userContract = this.findUserContractByContractIdAndUserId(contractId, userUUID);

        if (userContract.getStatus().equals(DocumentStatus.ACTION_REQUIRE) && !userContract.getSecretKey().equals(secretKey)) {
            throw new LockedException("Secret Key does not match");
        }
        if (userContract.getStatus().equals(DocumentStatus.SIGNED))
            throw new SignedException("A Contract was signed by this User");
        Contract contract = userContract.getContract();
        UserContractResponse userContractResponse = new UserContractResponse();

        boolean lastSign = contract.getUserContracts().stream()
                .filter(ud -> !ud.getStatus().equals(DocumentStatus.READ))
                .filter(ud -> ud.getStatus().equals(DocumentStatus.SIGNED))
                .count() == contract.getUserContracts().size() - 2;
        if (userContract.getViewedDate() == null) {
            userContract.setViewedDate(new Date());
            //sent email receiver viewed
            DocumentCommonMessage documentCommonMessage = new DocumentCommonMessage();
            documentCommonMessage.setTitle(String.format("%s - Viewed", contract.getTitle()));
            User userView = userContract.getUser();
            Optional<UserContract> optionalUserContractOwner = contract.getUserContracts().stream()
                    .filter(UserContract::isOwner).findFirst();
            optionalUserContractOwner.ifPresent(ud -> {
                documentCommonMessage.setTo(ud.getUser().getEmail());
            });
            documentCommonMessage.setMessage(String.format("%s(%s) vừa xem tài liệu \"%s\" ",
                    userView.getFullName(), userView.getEmail(), contract.getTitle()));
            documentProducer.sendMessage(documentCommonMessage, TOPIC_NOTIFY_COMMON);
        }

        User user = userRepository.findById(userUUID).orElseThrow(() -> new NotFoundException("user not found"));
        userContractResponse.setUser(user);
        userContractResponse.setDocuments(contract.getDocuments());
        userContractResponse.setLastSign(lastSign);
        return userContractResponse;
    }


    @SneakyThrows
    @Override
    public Boolean signContractByUser(SignContractByReceiver u, List<MultipartFile> documents) {
        UserContract userContract = this.findUserContractByContractIdAndUserId(u.getContractId(), u.getUserId());

        if (userContract.getStatus().equals(DocumentStatus.ACTION_REQUIRE)) {
            userContract.setSignedDate(new Date());
            userContract.setStatus(DocumentStatus.SIGNED);

            // add xfdf for each document
            u.getDocumentXFDFS().forEach(documentXFDF -> {
                XFDF xfdf = new XFDF(documentXFDF.getXfdf());
                Document document = documentService.getById(documentXFDF.getDocumentId());
                xfdf.setDocument(document);
                xfdfService.save(xfdf);
            });

            // update contract status


            Contract contract = userContract.getContract();
            contract.setLastModifiedDate(new Date());
            Set<UserContract> userContracts = contract.getUserContracts();
            userContracts.forEach(uc -> {
                DocumentCommonMessage documentCommonMessage = new DocumentCommonMessage();
                documentCommonMessage.setTitle(String.format("%s - Signed", contract.getTitle()));
                User user = uc.getUser();
                documentCommonMessage.setMessage(String.format("%s(%s) vừa ký tài liệu \"%s\"",
                        user.getFullName(), user.getEmail(), contract.getTitle()));
                documentCommonMessage.setTo(user.getEmail());
                documentProducer.sendMessage(documentCommonMessage, TOPIC_NOTIFY_COMMON);
            });


            if (documents != null) {
                for (MultipartFile file : documents) {
                    UUID documentId = UUID.fromString(Objects.requireNonNull(file.getOriginalFilename()));
                    Document document = documentService.getById(documentId);
                    if (document != null) {
                        azureStorageService.uploadOverride(document.getSaveName(), file.getBytes());
                    }
                }
            }
            boolean completed = contract.getUserContracts().stream()
                    .filter(ud -> ud.getStatus().equals(DocumentStatus.SIGNED))
                    .count() == contract.getUserContracts().size() - 1;

            if (completed) {
                contract.setSigned(true);
                contract.setCompleteDate(new Date());
                userContracts.forEach(uc -> {
                    uc.setStatus(DocumentStatus.COMPLETED);
                    DocumentCommonMessage documentCommonMessage = new DocumentCommonMessage();
                    List<Attachment> attachments = new ArrayList<>();
                    contract.getDocuments().forEach(document -> {
                        Attachment attachment = new Attachment();
                        attachment.setUrl(document.getUrl());
                        attachment.setName(document.getOriginName());
                        attachments.add(attachment);
                    });
                    documentCommonMessage.setTitle(String.format("%s - Completed", contract.getTitle()));
                    documentCommonMessage.setAttachments(attachments);
                    documentCommonMessage.setMessage(
                            String.format("Tài liệu \"%s\" đã hoàn thành, mời bạn tải về bên dưới file đính kèm",
                                    contract.getTitle()));
                    documentCommonMessage.setTo(uc.getUser().getEmail());
                    documentProducer.sendMessage(documentCommonMessage, TOPIC_NOTIFY_COMMON);

                });
            }

        }


        return true;
    }

    @Override
    public long countAllByUserAndStatus(UUID userUUID, String status) {
        User user = new User();
        user.setId(userUUID);
        return userDocumentRepository.countAllByUserAndStatus(user, status);
    }

}
