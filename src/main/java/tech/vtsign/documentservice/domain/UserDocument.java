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
@Table(name = "user_document")
@NoArgsConstructor
public class UserDocument {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "user_document_uuid", unique = true, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;
    private String status;
    @Column(name = "user_uuid", columnDefinition = "BINARY(16)")
    @JsonProperty("user_uuid")
    private UUID userUUID;
    @JsonProperty("viewed_date")
    private Date viewedDate;
    @JsonProperty("signed_date")
    private Date signedDate;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "contract_uuid")
    private Contract contract;

    public UserDocument(String status, UUID userUUID) {
        this.status = status;
        this.userUUID = userUUID;
    }

}