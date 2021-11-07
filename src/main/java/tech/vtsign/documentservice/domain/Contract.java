package tech.vtsign.documentservice.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
public class Contract {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "contract_uuid", unique = true, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "sender_uuid", columnDefinition = "BINARY(16)")
    @JsonProperty("sender_uuid")
    private UUID senderUUID;
    @JsonProperty("sent_date")
    private Date sentDate;
    private boolean signed;
    @JsonProperty("sent_date")
    private Date completeDate;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "contract_uuid")
    private List<Document> documents;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "contract_uuid")
    @JsonIgnore
    private List<UserContract> userContracts;
}