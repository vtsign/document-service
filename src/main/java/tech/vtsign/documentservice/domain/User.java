package tech.vtsign.documentservice.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class User extends Auditable<String> implements Serializable {
    @Id
    @Column(name = "user_uuid", unique = true, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;
    private String email;
    @JsonIgnore
    private String phone;
    @JsonProperty("first_name")
    private String firstName;
    @JsonProperty("last_name")
    private String lastName;
    @JsonIgnore
    @OneToMany(mappedBy = "user")
    private Set<UserContract> userContracts;

    @Transient
    @JsonProperty("full_name")
    public String getFullName() {
        return String.format("%s %s", this.firstName, this.lastName);
    }
}
