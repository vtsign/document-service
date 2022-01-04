package tech.vtsign.documentservice.controller;

import io.swagger.v3.oas.annotations.Hidden;
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
import tech.vtsign.documentservice.domain.User;
import tech.vtsign.documentservice.exception.ExceptionResponse;
import tech.vtsign.documentservice.model.SignContractByReceiver;
import tech.vtsign.documentservice.model.UserContractResponse;
import tech.vtsign.documentservice.service.ContractService;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RequestMapping("/apt")
@RequiredArgsConstructor
@RestController
public class AcceptController {

    private final ContractService contractService;

    @Operation(summary = "Get Contract In Email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = UserContractResponse.class))
                    }
            ),
            @ApiResponse(responseCode = "403", description = "Forbidden you don't have permission to signing this contract",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),
            @ApiResponse(responseCode = "404", description = "Not found contract or contract being deleted",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),
            @ApiResponse(responseCode = "423", description = "Contract was signed by this user",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),
    })
    @GetMapping("/signing")
    public ResponseEntity<?> signByReceiver(@RequestParam("c") UUID contractId,
                                            @RequestParam("r") UUID receiverId,
                                            @RequestParam("uc") UUID userContractUUID,
                                            @RequestParam("s") String secretKey) {
        UserContractResponse userContractResponse = contractService.getUDRByContractIdAndUserId(contractId, receiverId, userContractUUID, secretKey);
        return ResponseEntity.ok(userContractResponse);
    }


    @Operation(summary = "Sign Contract In Email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class))
            ),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),
            @ApiResponse(responseCode = "419", description = "Missing require field see message for more details",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    })
    })
    @PostMapping("/signing")
    @Transactional
    public ResponseEntity<Boolean> signByReceiver(@RequestPart(name = "signed") SignContractByReceiver u,
                                                  @RequestPart(required = false, name = "documents") List<MultipartFile> documents) throws IOException {
        Boolean rs = contractService.signContractByUser(u, documents);
        return ResponseEntity.ok(rs);
    }

    @Hidden
    @PutMapping("/update-user")
    public User updateUser(@RequestBody User user) {
        return contractService.updateUser(user);
    }
}
