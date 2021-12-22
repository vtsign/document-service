package tech.vtsign.documentservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tech.vtsign.documentservice.model.CountContractDto;
import tech.vtsign.documentservice.model.DocumentStatus;
import tech.vtsign.documentservice.service.ContractService;
import tech.vtsign.documentservice.utils.DateUtil;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@RestController
@RequestMapping("/management")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class ManagementController {

    private final ContractService contractService;

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

    @GetMapping("/statistic-contract")
    public ResponseEntity<?> statisticUser(@RequestParam(name = "type", defaultValue = "week") String type) {
        return ResponseEntity.ok(contractService.getStatistic(type));
    }

    @GetMapping("/count-all-contract")
    public ResponseEntity<?> countContracts(@RequestParam(name = "id") UUID userId) {


        Map<String, Object> result = new HashMap<>();
        result.put(DocumentStatus.COMPLETED, contractService.countAllByUserAndStatus(userId, DocumentStatus.COMPLETED));
        result.put(DocumentStatus.WAITING, contractService.countAllByUserAndStatus(userId, DocumentStatus.WAITING));
        result.put(DocumentStatus.ACTION_REQUIRE, contractService.countAllByUserAndStatus(userId, DocumentStatus.ACTION_REQUIRE));
        result.put(DocumentStatus.DELETED, contractService.countAllByUserAndStatus(userId, DocumentStatus.DELETED));
        return ResponseEntity.ok(result);
    }
}