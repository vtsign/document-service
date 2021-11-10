package tech.vtsign.documentservice.domain;


import com.fasterxml.jackson.annotation.JsonProperty;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "user_contract")
public class UserContract {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "user_contract_uuid", unique = true, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;
    private String status;
    private String permission;
    private String secretKey;
    @JsonProperty("viewed_date")
    private Date viewedDate;
    @JsonProperty("signed_date")
    private Date signedDate;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "contract_uuid")
    private Contract contract;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_uuid")
    private User user;

    public UserContract() {
    }



    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public Date getViewedDate() {
        return viewedDate;
    }

    public void setViewedDate(Date viewedDate) {
        this.viewedDate = viewedDate;
    }

    public Date getSignedDate() {
        return signedDate;
    }

    public void setSignedDate(Date signedDate) {
        this.signedDate = signedDate;
    }

    public Contract getContract() {
        return contract;
    }

    public void setContract(Contract contract) {
        this.contract = contract;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}