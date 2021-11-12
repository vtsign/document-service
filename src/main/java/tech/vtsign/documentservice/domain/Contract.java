package tech.vtsign.documentservice.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Contract {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "contract_uuid", unique = true, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    private String title;
    @Column(name = "sender_uuid", columnDefinition = "BINARY(16)")
    @JsonProperty("sender_uuid")
    private UUID senderUUID;
    @JsonProperty("sent_date")
    private Date sentDate;
    private boolean signed;
    @JsonProperty("complete_date")
    private Date completeDate;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "contract_uuid")
    private List<Document> documents;

    @OneToMany(mappedBy = "contract")
    @JsonProperty("user_contracts")
    private Set<UserContract> userContracts;
}