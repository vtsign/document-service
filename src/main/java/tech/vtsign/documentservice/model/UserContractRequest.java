package tech.vtsign.documentservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@Data
public class UserContractRequest {
    @JsonProperty("r")
    private UUID userId;
    @JsonProperty("c")
    private UUID contractId;
    @JsonProperty("uc")
    private UUID userContractId;
}
