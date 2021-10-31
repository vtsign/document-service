package tech.vtsign.documentservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignContractByReceiver {
    @JsonProperty("contract_uuid")
    private UUID contractId;
    @JsonProperty("user_uuid")
    private UUID userId;
    @JsonProperty("document_xfdfs")
    private List<DocumentXFDF> documentXFDFS = new ArrayList<>();
}
