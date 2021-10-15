package tech.vtsign.documentservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class Receiver {
    private String name;
    private String email;
    @JsonProperty("private_message")
    private String privateMessage;
    private String permission;
    private String key;
}
