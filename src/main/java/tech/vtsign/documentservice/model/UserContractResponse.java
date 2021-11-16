package tech.vtsign.documentservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.vtsign.documentservice.domain.Document;
import tech.vtsign.documentservice.domain.User;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserContractResponse {
    @JsonProperty("documents")
    private List<Document> documents = new ArrayList<>();
    @JsonProperty("user")
    private User user;
    @JsonProperty("last_sign")
    private boolean lastSign;
}
