package tech.vtsign.documentservice.domain;

import lombok.Builder;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Builder
@Data
public class Contract {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "contract_uuid", unique = true, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;
    @Column(name = "sent_date")
    private Date sentDate;
    @Column(name = "viewed_date")
    private Date viewedDate;
    @Column(name = "signed_date")
    private Date signedDate;
    @Column(name = "sender_uuid")
    private UUID senderUUID;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "contract_uuid")
    private List<Document> documents;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "contract_uuid")
    private List<DigitalSignature> digitalSignatures;
}