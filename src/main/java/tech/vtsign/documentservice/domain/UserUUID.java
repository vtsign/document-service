package tech.vtsign.documentservice.domain;

import lombok.*;

import javax.persistence.*;
import java.util.Set;
import java.util.UUID;


@Builder
@Entity
@Getter
@Setter
@Table(name = "user_uuid")
public class UserUUID {
    @Id
    private UUID user_uuid;
//    @GeneratedValue(generator = "uuid2")
//    @GenericGenerator(name = "uuid2", strategy = "uuid2")
//    @Column(name = "userid_uuid", unique = true, updatable = false, columnDefinition = "BINARY(16)")
//    private UUID id;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "userUUID")
    private Set<DigitalSignature> digitalSignatureList;
}
