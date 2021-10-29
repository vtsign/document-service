package tech.vtsign.documentservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tech.vtsign.documentservice.domain.Document;
import tech.vtsign.documentservice.exception.ExceptionResponse;
import tech.vtsign.documentservice.model.LoginServerResponseDto;
import tech.vtsign.documentservice.proxy.UserServiceProxy;
import tech.vtsign.documentservice.service.ContractService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequestMapping("/apt")
@RequiredArgsConstructor
@RestController
public class AcceptController {

    private final ContractService contractService;
    private final UserServiceProxy userServiceProxy;

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

}
