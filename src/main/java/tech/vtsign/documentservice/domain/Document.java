package tech.vtsign.documentservice.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@NoArgsConstructor
@Builder
@Data
public class Document {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "document_uuid", unique = true, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;
    private String url;
    @Column(name = "sent_date")
    private Date sentDate;
    @Column(name = "viewed_date")
    private Date viewedDate;
    @Column(name = "signed_date")
    private Date signedDate;
    @JsonIgnore
    @OneToMany(mappedBy = "document")
    private List<DigitalSignature> digitalSignatures;
}