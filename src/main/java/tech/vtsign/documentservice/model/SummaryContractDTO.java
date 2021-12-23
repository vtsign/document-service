package tech.vtsign.documentservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SummaryContractDTO {
    private long deleted;
    private long waiting;
    @JsonProperty("action_require")
    private long actionRequire;
    private long completed;
}
