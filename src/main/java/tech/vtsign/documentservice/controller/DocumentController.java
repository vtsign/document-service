package tech.vtsign.documentservice.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tech.vtsign.documentservice.domain.Contract;
import tech.vtsign.documentservice.domain.DigitalSignature;
import tech.vtsign.documentservice.exception.ExceptionResponse;
import tech.vtsign.documentservice.exception.NotFoundException;
import tech.vtsign.documentservice.model.DocumentClientRequest;
import tech.vtsign.documentservice.model.LoginServerResponseDto;
import tech.vtsign.documentservice.security.UserDetailsImpl;
import tech.vtsign.documentservice.service.ContractService;
import tech.vtsign.documentservice.service.DocumentService;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@Tag(name = "Document controller")
public class DocumentController {

    private final DocumentService documentService;
    private final ContractService contractService;

    @Operation(summary = "Post client files and receivers")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success server will sent email to contact",
                    content = @Content
            )
    })
    @PostMapping(value = "/signing")
    public ResponseEntity<Boolean> signing(@RequestPart("data") DocumentClientRequest documentClientRequests,
                                           @RequestPart List<MultipartFile> files) {
        documentService.createDigitalSignature(documentClientRequests, files);
        return ResponseEntity.ok(true);
    }

    @Operation(summary = "Signed document")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success",
                    content = @Content
            ),
            @ApiResponse(responseCode = "4", description = "",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    })
    })

    @GetMapping("/contract")
    public ResponseEntity<?> findContractById(@RequestParam("id") UUID contractUUID) {
        DigitalSignature signature = contractService.findContractById(contractUUID);
        return ResponseEntity.ok(signature);
    }

    @Operation(summary = "Signed document")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success",
                    content = @Content
            ),
            @ApiResponse(responseCode = "404", description = "Not found contract",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = NotFoundException.class))
                    })
    })
    @GetMapping("/filter")
    public ResponseEntity<?> retrieveContractByStatus(@RequestParam("status") String status) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        LoginServerResponseDto userInfo = userDetails.getLoginServerResponseDto();
        List<Contract> contracts = contractService.findAllTemplateByUserId(userInfo.getId(), status);
        return ResponseEntity.ok(contracts);
    }


}
