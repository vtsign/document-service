package tech.vtsign.documentservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tech.vtsign.documentservice.model.CountContractDto;
import tech.vtsign.documentservice.service.ContractService;
import tech.vtsign.documentservice.utils.DateUtil;

import java.time.LocalDateTime;


@RestController
@RequestMapping("/management")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class ManagementController {

    private final ContractService contractService;

    @GetMapping("/count-contract")
    public ResponseEntity<?> countContract(@RequestParam(value = "type") String type) {
        LocalDateTime[] dates = DateUtil.getDateBetween(type);
        CountContractDto countContractDto = new CountContractDto();
        countContractDto.setSent(contractService.countAllContractCompleted(dates[0], dates[1]));
        countContractDto.setCompleted(contractService.countAllContract(dates[0], dates[1]));
        return ResponseEntity.ok(countContractDto);
    }
}
