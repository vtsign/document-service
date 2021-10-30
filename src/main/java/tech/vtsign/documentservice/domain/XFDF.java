package tech.vtsign.documentservice.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
public class XFDF {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "xfdf_uuid", unique = true, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;
    @Column(columnDefinition = "TEXT")
    private String xfdf;

    public XFDF(String xfdf) {
        this.xfdf = xfdf;
    }


    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "document_uuid")
    @JsonIgnore
    private Document document;
}
