package tech.vtsign.documentservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tech.vtsign.documentservice.domain.*;
import tech.vtsign.documentservice.exception.BadRequestException;
import tech.vtsign.documentservice.exception.LockedException;
import tech.vtsign.documentservice.exception.SignedException;
import tech.vtsign.documentservice.exception.UnauthorizedException;
import tech.vtsign.documentservice.model.DocumentStatus;
import tech.vtsign.documentservice.model.LoginServerResponseDto;
import tech.vtsign.documentservice.model.SignContractByReceiver;
import tech.vtsign.documentservice.model.UserContractResponse;
import tech.vtsign.documentservice.proxy.UserServiceProxy;
import tech.vtsign.documentservice.repository.ContractRepository;
import tech.vtsign.documentservice.repository.UserDocumentRepository;
import tech.vtsign.documentservice.repository.UserRepository;
import tech.vtsign.documentservice.security.UserDetailsImpl;
import tech.vtsign.documentservice.service.AzureStorageService;
import tech.vtsign.documentservice.service.ContractService;
import tech.vtsign.documentservice.service.DocumentService;
import tech.vtsign.documentservice.service.XFDFService;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
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
    private final UserServiceProxy userServiceProxy;
    private final DocumentService documentService;
    private final XFDFService xfdfService;
    private final AzureStorageService azureStorageService;



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
        Contract contract = new Contract();
        contract.setId(contractUUID);
        User user= new User();
        user.setId(userUUID);
        return userDocumentRepository.findUserContractByContractAndUser(contract,user);
    }

    @Override
    public Page<UserContract> findContractsByUserIdAndStatus(UserContract userContract, int page, int size) {
        Pageable pageable = PageRequest.of(page,size, Sort.by("status"));
        Page<UserContract> pages = userDocumentRepository.findAll(new Specification<UserContract>() {
            @Override
            public Predicate toPredicate(Root<UserContract> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                if(userContract.getStatus()!=null) {
                    predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("status"), userContract.getStatus())));
                }
                if(userContract.getUser()!=null) {
                    predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("user"), userContract.getUser())));
                }
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        },pageable);
        return pages;

    }

    @Override
    public Contract getContractById(UUID id) {
        return contractRepository.getById(id);
    }

    @Override
    public UserContractResponse getUDRByContractIdAndUserId(UUID contractId, UUID userUUID, String secretKey) {
        LoginServerResponseDto user = userServiceProxy.getUserById(userUUID);
        UserContract userContract = this.findUserContractByIdAndUserId(contractId, userUUID);

        if (userContract.getStatus().equals(DocumentStatus.ACTION_REQUIRE)&&!userContract.getSecretKey().equals(secretKey)) {
            throw new LockedException("Secret Key does not match");
        }
        Contract contract = userContract.getContract();
//        Optional<User> contractOwnerOpt = userRepository.findById(contract.getSenderUUID());
//        User contractOwner = contractOwnerOpt.orElseThrow(() -> new NotFoundException("Not found user"));
        if (userContract.getStatus().equals(DocumentStatus.SIGNED))
            throw new SignedException("A Contract was signed by this User");
        UserContractResponse userContractResponse = new UserContractResponse();

        boolean lastSign = contract.getUserContracts().stream()
                .filter(ud -> ud.getStatus().equals(DocumentStatus.SIGNED))
                .count() == contract.getUserContracts().size() - 2;
        if (userContract.getViewedDate() == null) {
            userContract.setViewedDate(new Date());
            //sent email receiver viewed

//            ReceiverContract info = new ReceiverContract();
//            info.setEmail(contractOwner.getEmail());
//            info.setMailTitle("Viewed");
//            info.setMailMessage("");
//            this.sendMail(info, TOPIC_SIGN);
        }
        userContractResponse.setUser(user);
        userContractResponse.setDocuments(contract.getDocuments());
        userContractResponse.setLastSign(lastSign);
        return userContractResponse;
    }

    @SneakyThrows
    @Override
    public Boolean signContractByUser(SignContractByReceiver u, List<MultipartFile> documents){
        UserContract userContract = this.findUserContractByIdAndUserId(u.getContractId(), u.getUserId());

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
            boolean completed = contract.getUserContracts().stream()
                    .filter(ud -> ud.getStatus().equals(DocumentStatus.SIGNED))
                    .count() == contract.getUserContracts().size() - 1;

            if (completed) {
                contract.setSigned(true);
                contract.setCompleteDate(new Date());
                contract.getUserContracts().forEach(ud -> {
                    ud.setStatus(DocumentStatus.COMPLETED);
                });
            }

        }

        if (documents != null) {
            for (MultipartFile file : documents) {
                UUID documentId = UUID.fromString(Objects.requireNonNull(file.getOriginalFilename()));
                Document document = documentService.getById(documentId);
                if (document != null) {
                    azureStorageService.uploadOverride(document.getSaveName(), file.getBytes());
                }
            }
        }
        return true;
    }

}
