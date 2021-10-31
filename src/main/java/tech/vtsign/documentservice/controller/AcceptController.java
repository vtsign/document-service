package tech.vtsign.documentservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tech.vtsign.documentservice.domain.Contract;
import tech.vtsign.documentservice.domain.Document;
import tech.vtsign.documentservice.domain.UserDocument;
import tech.vtsign.documentservice.domain.XFDF;
import tech.vtsign.documentservice.exception.ExceptionResponse;
import tech.vtsign.documentservice.model.DocumentStatus;
import tech.vtsign.documentservice.model.LoginServerResponseDto;
import tech.vtsign.documentservice.model.SignContractByReceiver;
import tech.vtsign.documentservice.model.UserDocumentResponse;
import tech.vtsign.documentservice.proxy.UserServiceProxy;
import tech.vtsign.documentservice.service.AzureStorageService;
import tech.vtsign.documentservice.service.ContractService;
import tech.vtsign.documentservice.service.DocumentService;
import tech.vtsign.documentservice.service.XFDFService;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RequestMapping("/apt")
@RequiredArgsConstructor
@RestController
public class AcceptController {

    private final ContractService contractService;
    private final UserServiceProxy userServiceProxy;
    private final DocumentService documentService;
    private final XFDFService xfdfService;
    private final AzureStorageService azureStorageService;

    @Operation(summary = "document")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = UserDocumentResponse.class))
                    }
            ),
            @ApiResponse(responseCode = "403", description = "Forbidden you don't have permission to signing this contract",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),
            @ApiResponse(responseCode = "404", description = "Not found contract",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),
    })
    @GetMapping("/signing")
    public ResponseEntity<?> signByReceiver(@RequestParam("c") UUID contractId,
                                            @RequestParam("r") UUID receiverId) {
        LoginServerResponseDto user = userServiceProxy.getUserById(receiverId);
        List<Document> documents = contractService.getDocumentsByContractAndReceiver(contractId, receiverId);
        UserDocumentResponse userDocumentResponse = new UserDocumentResponse();

        Contract contract = contractService.getContractById(contractId);
        boolean lastSign = contract.getUserDocuments().stream()
                .filter(ud -> ud.getStatus().equals(DocumentStatus.SIGNED))
                .count() == contract.getUserDocuments().size() - 2;

        userDocumentResponse.setUser(user);
        userDocumentResponse.setDocuments(documents);
        userDocumentResponse.setLastSign(lastSign);
        return ResponseEntity.ok(userDocumentResponse);
    }


    @Operation(summary = "document")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class))
            )
    })
    @PostMapping("/signing")
    @Transactional
    public ResponseEntity<?> signByReceiver(@RequestPart(name = "signed") SignContractByReceiver u,
                                            @RequestPart(required = false, name = "documents") List<MultipartFile> documents) throws IOException {
        UserDocument userDocument = contractService.findContractByIdAndUserId(u.getContractId(), u.getUserId());

        if (userDocument.getStatus().equals(DocumentStatus.ACTION_REQUIRE)) {
            userDocument.setSignedDate(new Date());
            userDocument.setStatus(DocumentStatus.SIGNED);

            // add xfdf for each document
            u.getDocumentXFDFS().forEach(documentXFDF -> {
                XFDF xfdf = new XFDF(documentXFDF.getXfdf());
                Document document = documentService.getById(documentXFDF.getDocumentId());
                xfdf.setDocument(document);
                xfdfService.save(xfdf);
            });

            // update contract status

            Contract contract = userDocument.getContract();
            boolean completed = contract.getUserDocuments().stream()
                    .filter(ud -> ud.getStatus().equals(DocumentStatus.SIGNED))
                    .count() == contract.getUserDocuments().size() - 1;

            if (completed) {
                contract.setSigned(true);
                contract.setCompleteDate(new Date());
                contract.getUserDocuments().forEach(ud -> {
                    ud.setStatus(DocumentStatus.COMPLETED);
                });
            }

        }

        System.out.println("documents la: ");
        System.out.println(documents);
        if (documents != null) {
            System.out.println("Da nhan duoc documents");
            for (MultipartFile file : documents) {
                UUID documentId = UUID.fromString(Objects.requireNonNull(file.getOriginalFilename()));
                Document document = documentService.getById(documentId);
                if (document != null) {
                    System.out.println("Da vao upload override");
                    azureStorageService.uploadOverride(document.getSaveName(), file.getBytes());
                }
            }
        }
        return ResponseEntity.ok(true);
    }

}
