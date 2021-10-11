package tech.vtsign.documentservice.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Document extends Auditable<String> implements Serializable {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "user_uuid", unique = true, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;
    @JsonProperty("sent_date")
    private Date sentDate;
    @JsonProperty("signed_date")
    private Date signedDate;
    @JsonProperty("viewed_date")
    private Date viewedDate;
    private String url;

}
