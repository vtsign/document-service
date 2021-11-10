package tech.vtsign.documentservice.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
public class Contract {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "contract_uuid", unique = true, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

//    @Column(name = "sender_uuid", columnDefinition = "BINARY(16)")
//    @JsonProperty("sender_uuid")
//    private UUID senderUUID;
    @JsonProperty("sent_date")
    private Date sentDate;
    private boolean signed;
    @JsonProperty("complete_date")
    private Date completeDate;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "contract_uuid")
    private List<Document> documents;

    @OneToMany(mappedBy = "contract")
    @JsonIgnore
    private Set<UserContract> userContracts;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }



    public Date getSentDate() {
        return sentDate;
    }

    public void setSentDate(Date sentDate) {
        this.sentDate = sentDate;
    }

    public boolean isSigned() {
        return signed;
    }

    public void setSigned(boolean signed) {
        this.signed = signed;
    }

    public Date getCompleteDate() {
        return completeDate;
    }

    public void setCompleteDate(Date completeDate) {
        this.completeDate = completeDate;
    }

    public List<Document> getDocuments() {
        return documents;
    }

    public void setDocuments(List<Document> documents) {
        this.documents = documents;
    }

    public Set<UserContract> getUserContracts() {
        return userContracts;
    }

    public void setUserContracts(Set<UserContract> userContracts) {
        this.userContracts = userContracts;
    }
}