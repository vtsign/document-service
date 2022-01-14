package tech.vtsign.documentservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tech.vtsign.documentservice.domain.*;
import tech.vtsign.documentservice.exception.*;
import tech.vtsign.documentservice.model.*;
import tech.vtsign.documentservice.repository.ContractRepository;
import tech.vtsign.documentservice.repository.UserDocumentRepository;
import tech.vtsign.documentservice.repository.UserRepository;
import tech.vtsign.documentservice.service.*;
import tech.vtsign.documentservice.utils.DateUtil;

import javax.persistence.criteria.*;
import java.time.LocalDateTime;
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
    private final PasswordEncoder getBCryptPasswordEncoder;
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
            userContract.setViewedDate(LocalDateTime.now());
            return contract;
        }
    }


    @Override
    public Page<UserContract> findContractsByUserIdAndStatus(UserContract userContract, Contract contract,
                                                             int page, int size, String sortField, String sortType) {
        Pageable pageable = PageRequest.of(page, size);
        if (sortField == null) {
            sortField = "createdDate";
            String status = userContract.getStatus();
            if (status.equals(DocumentStatus.COMPLETED))
                sortField = "completeDate";
            else if (status.equals(DocumentStatus.DELETED))
                sortField = "lastModifiedDate";
        }

        String finalSortName = sortField;
        Page<UserContract> userContracts = userDocumentRepository.findAll(new Specification<UserContract>() {
            @Override
            public Predicate toPredicate(Root<UserContract> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {

                final Path<Contract> contractPath = root.get("contract");
                if (sortType.equalsIgnoreCase("desc")) {
                    criteriaQuery.orderBy(criteriaBuilder.desc(contractPath.get(finalSortName)));
                } else {
                    criteriaQuery.orderBy(criteriaBuilder.asc(contractPath.get(finalSortName)));
                }
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
    public UserContractResponse getUDRByContractIdAndUserId(UUID contractId, UUID userUUID, UUID userContractUUID, String secretKey) {


        UserContract userContract = this.findUserContractById(contractId, userUUID, userContractUUID);

        String status = userContract.getStatus();

        if (!status.equals(DocumentStatus.ACTION_REQUIRE)) {
            throw new SignedException("Document cannot sign by this user");
        }
        if (!getBCryptPasswordEncoder.matches(userContract.getSecretKey(), secretKey)) {
            throw new LockedException("Secret Key does not match");
        }

        Contract contract = userContract.getContract();
        UserContractResponse userContractResponse = new UserContractResponse();

        boolean lastSign = contract.getUserContracts().stream()
                .filter(ud -> (ud.getStatus().equals(DocumentStatus.SIGNED) || ud.getStatus().equals(DocumentStatus.READ)))
                .count() == contract.getUserContracts().size() - 2;
        if (userContract.getViewedDate() == null) {
            userContract.setViewedDate(LocalDateTime.now());
            //sent email receiver viewed
            CommonMessage commonMessage = new CommonMessage();
            commonMessage.setTitle(String.format("%s - Viewed", contract.getTitle()));
            User userView = userContract.getUser();
            Optional<UserContract> optionalUserContractOwner = contract.getUserContracts().stream()
                    .filter(UserContract::isOwner).findFirst();
            optionalUserContractOwner.ifPresent(ud -> {
                commonMessage.setTo(ud.getUser().getEmail());
            });
            commonMessage.setMessage(String.format("%s(%s) vừa xem tài liệu \"%s\" ",
                    userView.getFullName(), userView.getEmail(), contract.getTitle()));
            documentProducer.sendMessage(commonMessage, TOPIC_NOTIFY_COMMON);
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

        UserContract userContract = this.findUserContractById(u.getContractId(), u.getUserId(), u.getUserContractUUID());
        String status = userContract.getStatus();
        if (status.equals(DocumentStatus.DELETED) || status.equals(DocumentStatus.HIDDEN))
            throw new NotFoundException("Contract has been deleted by this User");
        User userSign = userContract.getUser();
        Contract contract = userContract.getContract();
        Optional<UserContract> userContractOwnerOpt = contract.getUserContracts().stream().filter(UserContract::isOwner).findFirst();
        User userOwner = new User();
        if (userContractOwnerOpt.isPresent()) {
            userOwner = userContractOwnerOpt.get().getUser();
        }

        if (userContract.getStatus().equals(DocumentStatus.ACTION_REQUIRE)) {
            userContract.setSignedDate(LocalDateTime.now());
            userContract.setStatus(DocumentStatus.SIGNED);

            // add xfdf for each document
            u.getDocumentXFDFS().forEach(documentXFDF -> {
                XFDF xfdf = new XFDF(documentXFDF.getXfdf());
                Document document = documentService.getById(documentXFDF.getDocumentId());
                xfdf.setDocument(document);
                xfdfService.save(xfdf);
            });

            // update contract status


            contract.setLastModifiedDate(LocalDateTime.now());
            Set<UserContract> userContracts = contract.getUserContracts();

            CommonMessage commonMessage = new CommonMessage();
            commonMessage.setTitle(String.format("%s - Signed", contract.getTitle()));

            commonMessage.setMessage(String.format("%s(%s) vừa ký tài liệu \"%s\"",
                    userSign.getFullName(), userSign.getEmail(), contract.getTitle()));
            commonMessage.setTo(userOwner.getEmail());
            documentProducer.sendMessage(commonMessage, TOPIC_NOTIFY_COMMON);


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
                    .filter(ud -> (ud.getStatus().equals(DocumentStatus.SIGNED) || ud.getStatus().equals(DocumentStatus.READ)))
                    .count() == contract.getUserContracts().size() - 1;

            if (completed) {
                contract.setSigned(true);
                contract.setCompleteDate(LocalDateTime.now());

                contract.getDocuments().forEach(document -> {
                    xfdfService.deleteAllByDocumentId(document.getId());
//                    document.getXfdfs().clear();
                });
                userContracts.forEach(uc -> {
                    uc.setStatus(DocumentStatus.COMPLETED);
                    CommonMessage commonMessageCp = new CommonMessage();
                    List<Attachment> attachments = new ArrayList<>();
                    contract.getDocuments().forEach(document -> {
                        Attachment attachment = new Attachment();
                        attachment.setUrl(document.getUrl());
                        attachment.setName(document.getOriginName());
                        attachments.add(attachment);
                    });
                    commonMessageCp.setTitle(String.format("%s - Completed", contract.getTitle()));
                    commonMessageCp.setAttachments(attachments);
                    commonMessageCp.setMessage(
                            String.format("Tài liệu \"%s\" đã hoàn thành, mời bạn tải về bên dưới file đính kèm",
                                    contract.getTitle()));
                    commonMessageCp.setTo(uc.getUser().getEmail());
                    documentProducer.sendMessage(commonMessageCp, TOPIC_NOTIFY_COMMON);

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

    @Override
    public UserContract findUserContractById(UUID contractUUID, UUID userUUID, UUID userContractUUID) {
        User user = new User();
        user.setId(userUUID);
        Contract contract = new Contract();
        contract.setId(contractUUID);
        Optional<UserContract> userContract = userDocumentRepository.findUserContractByContractAndUserAndId(contract, user, userContractUUID);

        return userContract.orElseThrow(() -> new NotFoundException("Not found contract or user or user do not own this contract"));
    }

    @Override
    public UserContract deleteContractById(UUID userUUID, UUID contractUUID, UUID userContractUUID) {
        UserContract userContract = findUserContractById(contractUUID, userUUID, userContractUUID);
        if (userContract.getStatus().equals(DocumentStatus.DELETED) || userContract.getStatus().equals(DocumentStatus.HIDDEN)) {
            throw new InvalidFormatException("Contract cannot delete by user");
        }
        try {
            userContract.setPreStatus(userContract.getStatus());
            userContract.setStatus(DocumentStatus.DELETED);

        } catch (Exception e) {
            throw new RuntimeException("Could not deleted contract, server missing an error when process your request");
        }
        return userContract;
    }

    @Override
    public UserContract hiddenContractById(UUID userUUID, UUID contractUUID, UUID userContractUUID) {
        UserContract userContract = findUserContractById(contractUUID, userUUID, userContractUUID);
        if (!userContract.getStatus().equals(DocumentStatus.DELETED)) {
            throw new InvalidFormatException("Contract cannot hidden user");
        }
        try {
            userContract.setStatus(DocumentStatus.HIDDEN);
        } catch (Exception e) {
            throw new RuntimeException("Could not hidden contract, server missing an error when process your request");
        }
        return userContract;
    }

    @Override
    public UserContract restoreContractById(UUID userUUID, UUID contractUUID, UUID userContractUUID) {
        UserContract userContract = findUserContractById(contractUUID, userUUID, userContractUUID);
        if (!userContract.getStatus().equals(DocumentStatus.DELETED)) {
            throw new InvalidFormatException("Contract cannot restore by user");
        }
        try {
            userContract.setStatus(userContract.getPreStatus());
        } catch (Exception e) {
            throw new RuntimeException("Could not restore contract, server missing an error when process your request");
        }
        return userContract;
    }

    @Override
    public User findUserById(UUID userUUID) {
        return userRepository.findById(userUUID).orElseThrow(() -> new NotFoundException("User not found"));
    }

    @Override
    public User updateUser(User user) {
        User userUpdate = userRepository.findById(user.getId()).orElse(user);
        BeanUtils.copyProperties(user, userUpdate);
        return userRepository.save(userUpdate);
    }

    @Override
    public Long countAllContract(LocalDateTime startDate, LocalDateTime endDate) {
        return contractRepository.countAllByCreatedDateBetween(startDate, endDate);
    }

    @Override
    public Long countAllContract() {
        return contractRepository.count();
    }

    @Override
    public Long countAllContractCompleted(LocalDateTime startDate, LocalDateTime endDate) {
        return contractRepository.countAllContractByCompleteDateNotNullAndCompleteDateBetween(startDate, endDate);
    }

    @Override
    public Long countAllContractCompleted() {
        return contractRepository.countAllContractByCompleteDateNotNull();
    }

    @Override
    public ContractStatisticDto getStatistic(String type) {
        try {
            ContractStatisticDto contractStatisticDto = new ContractStatisticDto();
            List<StatisticDto> sent = new ArrayList<>();
            List<StatisticDto> completed = new ArrayList<>();
            Map<String, LocalDateTime[]> dates = DateUtil.getListLocalDateTime(type);
            for (Map.Entry<String, LocalDateTime[]> entry : dates.entrySet()) {
                LocalDateTime startDate = entry.getValue()[0];
                LocalDateTime endDate = entry.getValue()[1];
                Long countAllContract = countAllContract(startDate, endDate);
                Long countAllContractCompleted = countAllContractCompleted(startDate, endDate);
                sent.add(new StatisticDto(entry.getKey(), countAllContract));
                completed.add(new StatisticDto(entry.getKey(), countAllContractCompleted));
            }
            contractStatisticDto.setSent(sent);
            contractStatisticDto.setCompleted(completed);
            return contractStatisticDto;
        } catch (IllegalArgumentException e) {
            throw new NotFoundException("Type is not valid");
        }
    }

    @Override
    public SummaryContractDTO countAllContractWithAnyStatus(UUID userId) {
        long completed = this.countAllByUserAndStatus(userId, DocumentStatus.COMPLETED);
        long waiting = countAllByUserAndStatus(userId, DocumentStatus.WAITING);
        long actionRequire = countAllByUserAndStatus(userId, DocumentStatus.ACTION_REQUIRE);
        long deleted = countAllByUserAndStatus(userId, DocumentStatus.DELETED);
        SummaryContractDTO dto = new SummaryContractDTO();
        dto.setCompleted(completed);
        dto.setDeleted(deleted);
        dto.setWaiting(waiting);
        dto.setActionRequire(actionRequire);
        return dto;
    }

}
