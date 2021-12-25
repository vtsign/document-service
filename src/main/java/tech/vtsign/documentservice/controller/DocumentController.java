package tech.vtsign.documentservice.controller;


import io.swagger.v3.oas.annotations.Hidden;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tech.vtsign.documentservice.domain.Contract;
import tech.vtsign.documentservice.domain.User;
import tech.vtsign.documentservice.domain.UserContract;
import tech.vtsign.documentservice.exception.ExceptionResponse;
import tech.vtsign.documentservice.exception.InvalidFormatException;
import tech.vtsign.documentservice.exception.MissingFieldException;
import tech.vtsign.documentservice.exception.NotFoundException;
import tech.vtsign.documentservice.model.*;
import tech.vtsign.documentservice.security.UserDetailsImpl;
import tech.vtsign.documentservice.service.ContractService;
import tech.vtsign.documentservice.service.DocumentService;

import java.util.List;
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
            ),
            @ApiResponse(responseCode = "400", description = "Missing fields or accessToken",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),
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

    //    begin NHAN
    @Hidden
    @Operation(summary = "Post client files and receivers")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success server will sent email to contact",
                    content = @Content
            ),
            @ApiResponse(responseCode = "400", description = "Missing fields or accessToken",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),
    })
    @PostMapping(value = "/signing2")
    public ResponseEntity<Boolean> signing2(@Validated @RequestPart("data") DocumentClientRequest documentClientRequests,
                                            @RequestPart("files") List<MultipartFile> files,
                                            BindingResult result) {
        if (result.hasErrors()) {
            String errorMessage = result.getAllErrors()
                    .stream().map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.joining(";"));

            throw new MissingFieldException(errorMessage);
        }
        boolean success = documentService.createUserDocument2(documentClientRequests, files);
        return ResponseEntity.ok(success);
    }
    //    end NHAN

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
                    }),
            @ApiResponse(responseCode = "400", description = "Missing fields or accessToken",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),
    })
    @GetMapping("/filter")
    public ResponseEntity<DTOList> retrieveContractByStatus(UserContract usercontract, Contract contract,
                                                            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                                                            @RequestParam(value = "size", required = false, defaultValue = "4") int size,
                                                            @RequestParam(value = "sortField", required = false) String sortField,
                                                            @RequestParam(value = "sortType", required = false, defaultValue = "desc") String sortType,
                                                            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        LoginServerResponseDto userInfo = userDetails.getLoginServerResponseDto();

        User user = new User();
        user.setId(userInfo.getId());
        usercontract.setUser(user);
        Page<UserContract> userContractPage = contractService.findContractsByUserIdAndStatus(usercontract, contract,
                page - 1, size, sortField, sortType);
        List<Contract> contracts = userContractPage.stream().map(UserContract::getContract).collect(Collectors.toList());
        DTOList<Contract> dtoList = new DTOList<>();
        dtoList.setPage(page);
        dtoList.setPageSize(size);
        dtoList.setTotalElements(userContractPage.getTotalElements());
        dtoList.setTotalPages(userContractPage.getTotalPages());
        dtoList.setList(contracts);
        return ResponseEntity.ok(dtoList);
    }

    @Operation(summary = "Signed document")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success",
                    content = @Content
            ),
            @ApiResponse(responseCode = "404", description = "Not found contract",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = NotFoundException.class))
                    }),
            @ApiResponse(responseCode = "400", description = "Missing fields or accessToken",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),
    })
    @PostMapping("/sign_document")
    public ResponseEntity<Boolean> signByUser(@RequestPart(name = "signed") SignContractByReceiver u,
                                              @RequestPart(required = false, name = "documents") List<MultipartFile> documents,
                                              @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        LoginServerResponseDto userInfo = userDetails.getLoginServerResponseDto();
        u.setUserId(userInfo.getId());
        Boolean rs = contractService.signContractByUser(u, documents);
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
                    }),
            @ApiResponse(responseCode = "400", description = "Missing fields or accessToken",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),
    })
    @GetMapping("/contract")
    public ResponseEntity<Contract> findContractByContractUUID(@RequestParam(name = "c") UUID contractUUID,
                                                               @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        LoginServerResponseDto userInfo = userDetails.getLoginServerResponseDto();
        Contract contract = contractService.findContractByContractAndReceiver(contractUUID, userInfo.getId());
        return ResponseEntity.ok(contract);
    }

    @Operation(summary = "Get contract by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = SummaryContractDTO.class))
                    }
            ),
            @ApiResponse(responseCode = "400", description = "Missing fields or accessToken",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),

    })

    @GetMapping("/count")
    public ResponseEntity<SummaryContractDTO> countContracts(@Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        LoginServerResponseDto userInfo = userDetails.getLoginServerResponseDto();

        SummaryContractDTO dto = contractService.countAllContractWithAnyStatus(userInfo.getId());
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Delete contract")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success",
                    content = @Content
            ),
            @ApiResponse(responseCode = "404", description = "Not found contract or user or user do not own this contract",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = NotFoundException.class))
                    }),
            @ApiResponse(responseCode = "422", description = "user cannot delete contract",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = InvalidFormatException.class))
                    }),
            @ApiResponse(responseCode = "400", description = "Missing fields or accessToken",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),
    })
    @DeleteMapping("/delete")
    public ResponseEntity<UserContract> deleteContract(@RequestBody UserContractRequest userContractRequest,
                                                       @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {

        LoginServerResponseDto userInfo = userDetails.getLoginServerResponseDto();
        UserContract userContract = contractService
                .deleteContractById(userInfo.getId(), userContractRequest.getContractId(), userContractRequest.getUserContractId());
        return ResponseEntity.ok(userContract);
    }

    @Operation(summary = "Restore contract")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success contract restored",
                    content = @Content
            ),
            @ApiResponse(responseCode = "404", description = "Not found contract or user or user do not own this contract",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = NotFoundException.class))
                    }),
            @ApiResponse(responseCode = "422", description = "user cannot restore contract",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = InvalidFormatException.class))
                    }),
            @ApiResponse(responseCode = "400", description = "Missing fields or accessToken",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),
    })
    @PostMapping("/restore")
    public ResponseEntity<UserContract> restoreContract(@RequestBody UserContractRequest userContractRequest,
                                                        @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {

        LoginServerResponseDto userInfo = userDetails.getLoginServerResponseDto();
        UserContract userContract = contractService
                .restoreContractById(userInfo.getId(), userContractRequest.getContractId(), userContractRequest.getUserContractId());
        return ResponseEntity.ok(userContract);
    }

    @Operation(summary = "Hidden contract")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success contract hidden",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = UserContract.class))
                    }
            ),
            @ApiResponse(responseCode = "404", description = "Not found contract or user or user do not own this contract",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = NotFoundException.class))
                    }),
            @ApiResponse(responseCode = "422", description = "user cannot hidden contract",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = InvalidFormatException.class))
                    }),
            @ApiResponse(responseCode = "", description = "Missing fields or accessToken",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),
    })
    @DeleteMapping("/hidden")
    public ResponseEntity<UserContract> hiddenContract(@RequestBody UserContractRequest userContractRequest,
                                                       @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {

        LoginServerResponseDto userInfo = userDetails.getLoginServerResponseDto();
        UserContract userContract = contractService
                .hiddenContractById(userInfo.getId(), userContractRequest.getContractId(), userContractRequest.getUserContractId());
        return ResponseEntity.ok(userContract);
    }
}
