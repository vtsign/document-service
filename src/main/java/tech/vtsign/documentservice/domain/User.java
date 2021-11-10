package tech.vtsign.documentservice.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
public class User {
    @Id
    @Column(name = "user_uuid", unique = true, updatable = false,columnDefinition = "BINARY(16)")
    private UUID id;
    private String email;
    private String phone;
    @OneToMany(mappedBy = "user")
    private Set<UserContract> userContracts;

    public User() {
    }

    public User(UUID id, String email, String phone, Set<UserContract> userContracts) {
        this.id = id;
        this.email = email;
        this.phone = phone;
        this.userContracts = userContracts;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Set<UserContract> getUserContracts() {
        return userContracts;
    }

    public void setUserContracts(Set<UserContract> userContracts) {
        this.userContracts = userContracts;
    }
}
