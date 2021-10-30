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
import tech.vtsign.documentservice.domain.Contract;
import tech.vtsign.documentservice.domain.Document;
import tech.vtsign.documentservice.domain.UserDocument;
import tech.vtsign.documentservice.domain.XFDF;
import tech.vtsign.documentservice.exception.ExceptionResponse;
import tech.vtsign.documentservice.model.DocumentStatus;
import tech.vtsign.documentservice.model.LoginServerResponseDto;
import tech.vtsign.documentservice.model.SignContractByReceiver;
import tech.vtsign.documentservice.proxy.UserServiceProxy;
import tech.vtsign.documentservice.service.ContractService;
import tech.vtsign.documentservice.service.DocumentService;
import tech.vtsign.documentservice.service.XFDFService;

import java.util.*;

@RequestMapping("/apt")
@RequiredArgsConstructor
@RestController
public class AcceptController {

    private final ContractService contractService;
    private final UserServiceProxy userServiceProxy;
    private final DocumentService documentService;
    private final XFDFService xfdfService;

    @Operation(summary = "document")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success",
                    content = @Content
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

        Map<String, Object> res = new HashMap<>();
        res.put("user", user);
        res.put("documents", documents);
        return ResponseEntity.ok(res);
    }


    @Operation(summary = "document")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success",
                    content = @Content
            )
    })
    @PostMapping("/signing")
    @Transactional
    public ResponseEntity<?> signByReceiver(@RequestBody SignContractByReceiver u) {
        UserDocument userDocument = contractService.findContractByIdAndUserId(u.getContractId(), u.getUserId());

        if (!userDocument.getStatus().equals(DocumentStatus.SIGNED)) {
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
        return ResponseEntity.ok(true);
    }

}
