package tech.vtsign.documentservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.util.UUID;

@NoArgsConstructor
@Data
public class Receiver {
    @JsonIgnore
    private UUID id;
    @NotBlank
    private String name;
    @Email
    private String email;
    @NotBlank
    private String phone;
    @JsonProperty("private_message")
    private String privateMessage;
    private String permission;
    private String key;
}
