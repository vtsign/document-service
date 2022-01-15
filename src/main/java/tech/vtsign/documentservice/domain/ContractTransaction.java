package tech.vtsign.documentservice.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import tech.vtsign.documentservice.constant.ContractTransactionAction;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "contract_transaction")
public class ContractTransaction {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "contract_transaction_uuid", unique = true, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;


    private LocalDateTime time = LocalDateTime.now();
    @Enumerated(EnumType.STRING)
    private ContractTransactionAction action;
    private String message;

    @JsonIgnore
    @ManyToOne()
    @JoinColumn(name = "user_uuid")
    private User user;

    @JsonIgnore
    @ManyToOne()
    @JoinColumn(name = "contract_uuid")
    private Contract contract;
}
