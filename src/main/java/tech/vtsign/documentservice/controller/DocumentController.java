package tech.vtsign.documentservice.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tech.vtsign.documentservice.domain.Contract;
import tech.vtsign.documentservice.domain.User;
import tech.vtsign.documentservice.domain.UserContract;
import tech.vtsign.documentservice.exception.MissingFieldException;
import tech.vtsign.documentservice.exception.NotFoundException;
import tech.vtsign.documentservice.model.DocumentClientRequest;
import tech.vtsign.documentservice.model.LoginServerResponseDto;
import tech.vtsign.documentservice.model.SignContractByReceiver;
import tech.vtsign.documentservice.security.UserDetailsImpl;
import tech.vtsign.documentservice.service.ContractService;
import tech.vtsign.documentservice.service.DocumentService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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
    public ResponseEntity<Boolean> signing(@Validated @RequestPart("data") DocumentClientRequest documentClientRequests,
                                           @RequestPart("files") List<MultipartFile> files,
                                           BindingResult result) {
        if (result.hasErrors()) {
            String errorMessage = result.getAllErrors()
                    .stream().map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.joining(";"));

            throw new MissingFieldException(errorMessage);
        }
        boolean success = documentService.createUserDocument(documentClientRequests, files);
        return ResponseEntity.ok(success);
    }

    @Operation(summary = "Get list contract by status", description = "DRAFT\n" +
            "SENT\n" +
            "ACTION_REQUIRE\n" +
            "WAITING\n" +
            "COMPLETED\n" +
            "DELETED\n" +
            "HIDDEN\n" +
            "SIGNED")
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
    public ResponseEntity<?> retrieveContractByStatus(UserContract usercontract,
                                                      @RequestParam(value = "page",required = false,defaultValue = "1") int page,
                                                      @RequestParam(value = "size", required = false, defaultValue = "4") int size,
                                                      @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        LoginServerResponseDto userInfo = userDetails.getLoginServerResponseDto();

        User user = new User();
        user.setId(userInfo.getId());
        usercontract.setUser(user);
        Page<UserContract> userContractPage = contractService.findContractsByUserIdAndStatus(usercontract,page - 1,size);
        List<Contract>contracts = userContractPage.stream().map(UserContract::getContract).collect(Collectors.toList());
        Map<String, Object> result = new HashMap<>();
        result.put("total_items", userContractPage.getTotalElements());
        result.put("contracts", contracts);
        result.put("total_pages",userContractPage.getTotalPages());
        result.put("current_page",page) ;
        return ResponseEntity.ok(result);
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
    @PostMapping("/sign_document")
    public ResponseEntity<?> signByUser(@RequestPart(name = "signed") SignContractByReceiver u,
                                        @RequestPart(required = false, name = "documents") List<MultipartFile> documents,
                                        @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails){
        LoginServerResponseDto userInfo = userDetails.getLoginServerResponseDto();
        u.setUserId(userInfo.getId());
        Boolean rs =  contractService.signContractByUser(u,documents);
        return ResponseEntity.ok(rs);
    }

    @Operation(summary = "Get contract by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success",
                    content = @Content
            ),
            @ApiResponse(responseCode = "404", description = "Not found contract",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = NotFoundException.class))
                    })
    })
    @GetMapping("/contract")
    public ResponseEntity<Contract> findContractByContractUUID(@RequestParam(name = "c") UUID contractUUID,
                                                               @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails){
        LoginServerResponseDto userInfo = userDetails.getLoginServerResponseDto();
        Contract contract =  contractService.findContractByContractAndReceiver(contractUUID, userInfo.getId());
        return ResponseEntity.ok(contract);
    }


}
