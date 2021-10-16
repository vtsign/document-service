package tech.vtsign.documentservice.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tech.vtsign.documentservice.exception.ExceptionResponse;
import tech.vtsign.documentservice.model.DocumentClientRequest;
import tech.vtsign.documentservice.service.DocumentService;

import java.util.List;

@RequiredArgsConstructor
@RestController
@Tag(name="Document controller")
public class DocumentController {

    private final DocumentService documentService;

    @PreAuthorize("hasRole('admin')")
    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }


    @Operation(summary = "Signing document")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success",
                    content = @Content
            ),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
            })
    })

    @GetMapping("/sign")
    public ResponseEntity<String> signByReceiver(@RequestParam("document") String urlDocument,
                                           @RequestParam("public-key") String urlPublickey,
                                           @RequestParam("signature") String urlSignature
                                           )
    {
        return ResponseEntity.ok("Successfull");
    }


    @PostMapping(value = "/signing")
    public ResponseEntity<Boolean> signing(@RequestPart("data") DocumentClientRequest documentClientRequests,
                                     @RequestPart List<MultipartFile> files) {
       documentService.createDigitalSignature(documentClientRequests ,files);

        return ResponseEntity.ok(true);
    }
}
