package tech.vtsign.documentservice.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.UUID;
@Data
@NoArgsConstructor
@Entity
@Table(name = "digital_signature")
public class DigitalSignature {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "digital_signature_uuid", unique = true, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;
    private String status;
    private String url;


    @ManyToOne()
    @JoinColumn(name = "user_uuid")
    private UserUUID userUUID;

    @ManyToOne()
    @JoinColumn(name = "document_uuid")
    private Document document;

}