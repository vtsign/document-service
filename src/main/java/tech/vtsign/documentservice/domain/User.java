package tech.vtsign.documentservice.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id

    @Column(name = "user_id", unique = true, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;
    //    @Email
    private String email;
    @JsonProperty("first_name")
    private String firstName;
    @JsonProperty("last_name")
    private String lastName;
    private String phone;
    @JsonProperty("temp_account")
    private boolean tempAccount;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_uuid")
    private List<UserContract> userContracts;
}
