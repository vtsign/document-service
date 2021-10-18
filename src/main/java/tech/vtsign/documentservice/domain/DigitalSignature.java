package tech.vtsign.documentservice.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@Table(name = "digital_signature")
public class DigitalSignature {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "digital_signature_uuid", unique = true, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;
    private String status;
    private String url;
    @Column(name = "user_uuid")
    private UUID userUUID;
    @JsonProperty("public_key")
    private String publicKey;

    @JsonIgnore
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "contract_uuid")
    private Contract contract;

}