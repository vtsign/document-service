package tech.vtsign.documentservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@Data
public class Receiver {
    @JsonIgnore
    private UUID id;
    private String name;
    private String email;
    @JsonProperty("private_message")
    private String privateMessage;
    private String permission;
    private String key;
}
