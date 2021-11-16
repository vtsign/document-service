package tech.vtsign.documentservice.domain;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Document extends Auditable<String> implements Serializable {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "document_uuid", unique = true, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;
    private String url;
    @JsonProperty("origin_name")
    private String originName;
    @JsonProperty("save_name")
    private String saveName;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "contract_uuid")
    @JsonIgnore
    private Contract contract;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "document")
    private List<XFDF> xfdfs;

    public Document(String url) {
        this.url = url;
    }

}
