package tech.vtsign.documentservice.domain;

import lombok.*;
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


    @ManyToOne( cascade = { CascadeType.MERGE,CascadeType.PERSIST})
    @JoinColumn(name = "user_uuid")
    private UserUUID userUUID;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "document_uuid")
    private Document document;

}