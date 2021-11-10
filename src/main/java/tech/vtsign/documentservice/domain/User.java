package tech.vtsign.documentservice.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.Set;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class User {
    @Id
    @Column(name = "user_uuid", unique = true, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;
    private String email;
    private String phone;
    @OneToMany(mappedBy = "user")
    private Set<UserContract> userContracts;
}
