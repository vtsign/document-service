package tech.vtsign.documentservice.domain;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Table(name = "user_contract")
public class UserContract {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "user_contract_uuid", unique = true, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;
    private String status;
    private String permission;
    @JsonIgnore
    private String secretKey;
    @JsonProperty("viewed_date")
    private Date viewedDate;
    @JsonProperty("signed_date")
    private Date signedDate;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "contract_uuid")
    @JsonIgnore
    @ToString.Exclude
    private Contract contract;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_uuid")
    @JsonIgnore
    @ToString.Exclude
    private User user;
}