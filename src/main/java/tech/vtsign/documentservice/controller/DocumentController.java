package tech.vtsign.documentservice.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tech.vtsign.documentservice.domain.Document;
import tech.vtsign.documentservice.exception.ExceptionResponse;
import tech.vtsign.documentservice.model.DocumentClientRequest;
import tech.vtsign.documentservice.service.ContractService;
import tech.vtsign.documentservice.service.DocumentService;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@Tag(name="Document controller")
public class DocumentController {

    private final DocumentService documentService;
    private final ContractService contractService;

    @Operation(summary = "Signed document")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success",
                    content = @Content
            ),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    })
    })

    @GetMapping("/signed")
    public ResponseEntity<?> signByReceiver(@RequestParam("c") UUID contractId, 
                                                 @RequestParam("r") UUID receiverId ){
        List<Document> documents = contractService.getDocumentsByContractAndReceiver(contractId,receiverId);
        return ResponseEntity.ok(documents);
    }


    @PostMapping(value = "/signing")
    public ResponseEntity<Boolean> signing(@RequestPart("data") DocumentClientRequest documentClientRequests,
                                           @RequestPart List<MultipartFile> files) {
        documentService.createDigitalSignature(documentClientRequests, files);
        return ResponseEntity.ok(true);
    }
    
}
