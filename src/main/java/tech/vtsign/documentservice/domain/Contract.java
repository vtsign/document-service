package tech.vtsign.documentservice.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Contract extends Auditable<String> implements Serializable {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "contract_uuid", unique = true, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @JsonProperty("public_message")
    private String publicMessage;
    private String title;
    @JsonProperty("sent_date")
    private LocalDateTime sentDate;
    @JsonProperty("last_modified_date")
    private LocalDateTime lastModifiedDate;
    private boolean signed;
    @JsonProperty("complete_date")
    private LocalDateTime completeDate;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "contract_uuid")
    private List<Document> documents;


    @OneToMany(mappedBy = "contract")
    @JsonProperty("user_contracts")
    private Set<UserContract> userContracts;

    @OneToMany(mappedBy = "contract")
    @JsonProperty("contract_transactions")
    private Set<ContractTransaction> contractTransaction;
}