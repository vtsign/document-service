package tech.vtsign.documentservice.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    private UUID userUUID;

    @JsonIgnore
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "document_uuid")
    private Document document;

}