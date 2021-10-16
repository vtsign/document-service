package tech.vtsign.documentservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginServerResponseDto {
    private UUID id;
    private String email;
    @JsonProperty("first_name")
    private String firstName;
    @JsonProperty("last_name")
    private String lastName;
    private String phone;
    private String organization;
    private String address;
    private boolean enabled;
    private boolean blocked;
    @JsonProperty("public_key")
    private String publicKey;
    @JsonProperty("private_key")
    private String privateKey;
    private List<Role> roles;
    private List<Permission> permissions;

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
