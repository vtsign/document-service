package tech.vtsign.documentservice.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "digital_signature")
@NoArgsConstructor
public class DigitalSignature {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "digital_signature_uuid", unique = true, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;
    private String status;
    private String url;
    @Column(name = "user_uuid", columnDefinition = "BINARY(16)")
    @JsonProperty("user_uuid")
    private UUID userUUID;
    @JsonProperty("public_key")
    private String publicKey;
    @JsonProperty("viewed_date")
    private Date viewedDate;
    @JsonProperty("signed_date")
    private Date signedDate;

    public DigitalSignature(String status, String url, UUID userUUID, String publicKey) {
        this.status = status;
        this.url = url;
        this.userUUID = userUUID;
        this.publicKey = publicKey;
    }

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "contract_uuid")
    private Contract contract;

}