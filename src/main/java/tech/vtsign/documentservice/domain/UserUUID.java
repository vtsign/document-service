package tech.vtsign.documentservice.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Set;
import java.util.UUID;


@Data
@NoArgsConstructor
@Entity
@Table(name = "user_uuid")
public class UserUUID {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "userid_uuid", unique = true, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    private UUID user_uuid;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "userUUID")
    private Set<DigitalSignature> digitalSignatureList;
}
