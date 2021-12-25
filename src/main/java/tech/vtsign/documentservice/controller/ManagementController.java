package tech.vtsign.documentservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tech.vtsign.documentservice.exception.ExceptionResponse;
import tech.vtsign.documentservice.model.CountContractDto;
import tech.vtsign.documentservice.model.SummaryContractDTO;
import tech.vtsign.documentservice.service.ContractService;
import tech.vtsign.documentservice.utils.DateUtil;

import java.time.LocalDateTime;
import java.util.UUID;


@RestController
@RequestMapping("/management")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class ManagementController {

    private final ContractService contractService;


    @Operation(summary = "Count Contracts By Id Rely On Date")
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
            @ApiResponse(responseCode = "422", description = "type not found",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),
            @ApiResponse(responseCode = "403", description = "forbidden",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),

    })

    @GetMapping("/count-contract")
    public ResponseEntity<?> countContract(@RequestParam(name = "type", defaultValue = "week") String type) {
        CountContractDto countContractDto = new CountContractDto();

        if ("all".equals(type)) {
            countContractDto.setSent(contractService.countAllContract());
            countContractDto.setCompleted(contractService.countAllContractCompleted());
        } else {
            LocalDateTime[] dates = DateUtil.getDateBetween(type);
            countContractDto.setSent(contractService.countAllContract(dates[0], dates[1]));
            countContractDto.setCompleted(contractService.countAllContractCompleted(dates[0], dates[1]));
        }

        return ResponseEntity.ok(countContractDto);
    }

    @Operation(summary = "Statistic Contracts By Id")
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
            @ApiResponse(responseCode = "422", description = "type not found",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),
            @ApiResponse(responseCode = "403", description = "forbidden",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),

    })


    @GetMapping("/statistic-contract")
    public ResponseEntity<?> statisticUser(@RequestParam(name = "type", defaultValue = "week") String type) {
        return ResponseEntity.ok(contractService.getStatistic(type));
    }

    @Operation(summary = "Count All Contracts By Id")
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
            @ApiResponse(responseCode = "403", description = "forbidden",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),

    })
    @GetMapping("/count-all-contract")
    public ResponseEntity<SummaryContractDTO> countContracts(@RequestParam(name = "id") UUID userId) {
        SummaryContractDTO dto = contractService.countAllContractWithAnyStatus(userId);
        return ResponseEntity.ok(dto);
    }
}